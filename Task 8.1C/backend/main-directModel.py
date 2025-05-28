from flask import Flask, request, Response, jsonify
from transformers import AutoTokenizer, AutoModelForCausalLM, BitsAndBytesConfig
import torch
import argparse
from neo4j import GraphDatabase # Import Neo4j driver
import os # For potentially using environment variables later

app = Flask(__name__)

# --- LLM Model and Tokenizer ---
llm_model = None # Renamed from model to avoid conflict
tokenizer = None
MODEL_NAME = "meta-llama/Llama-2-7b-chat-hf" # Changed back for stability

# --- Neo4j Connection Details ---
# IMPORTANT: Replace with your actual AuraDB credentials
NEO4J_URI = "neo4j+ssc://26c68983.databases.neo4j.io"
NEO4J_USER = "neo4j"
NEO4J_PASSWORD = "B0_uJk_ec2ISsyystlRcGpfqJlIHq3MgIs3OtCq1Tq8"
neo4j_driver = None

def get_neo4j_driver():
    global neo4j_driver
    if neo4j_driver is None:
        try:
            neo4j_driver = GraphDatabase.driver(NEO4J_URI, auth=(NEO4J_USER, NEO4J_PASSWORD))
            # Check connectivity
            with neo4j_driver.session(database="neo4j") as session: # AuraDB default database is neo4j
                session.run("RETURN 1 AS test")
            print("Successfully connected to Neo4j AuraDB!")
        except Exception as e:
            print(f"Error connecting to Neo4j: {e}")
            neo4j_driver = None # Ensure driver is None if connection failed
    return neo4j_driver

def close_neo4j_driver():
    global neo4j_driver
    if neo4j_driver is not None:
        neo4j_driver.close()
        neo4j_driver = None
        print("Neo4j connection closed.")


def prepareLlamaBot():
    global llm_model, tokenizer # Use renamed llm_model
    print(f"Loading {MODEL_NAME} model... This may take a while.")

    # Configure 4-bit quantization
    quantization_config = BitsAndBytesConfig(
        load_in_4bit=True,
        bnb_4bit_compute_dtype=torch.float16,
        bnb_4bit_quant_type="nf4",
        bnb_4bit_use_double_quant=False
    )

    # Load tokenizer
    tokenizer = AutoTokenizer.from_pretrained(MODEL_NAME)
    tokenizer.pad_token = tokenizer.eos_token if tokenizer.pad_token is None else tokenizer.pad_token

    # Load model with quantization
    llm_model = AutoModelForCausalLM.from_pretrained(
        MODEL_NAME,
        device_map="auto",
        quantization_config=quantization_config,
    )
    # Print the device the model is loaded on.
    # If 'cuda' or 'cuda:X', input tensors need to be moved to this device.
    print(f"Model loaded. Primary device: {llm_model.device}")
    print("Model and tokenizer loaded successfully.")


@app.route('/')
def index():
    return "Welcome to the Llama Chatbot API with Neo4j Integration!"


@app.route('/chat', methods=['POST'])
def chat():
    global llm_model, tokenizer

    # Get user_id from form or JSON
    user_id = request.form.get('user_id') or (request.json.get('user_id') if request.is_json else None)
    user_message = request.form.get('userMessage') or (request.json.get('userMessage') if request.is_json else request.get_data(as_text=True).strip())

    if not user_id or not user_message:
        return Response("Error: user_id and userMessage are required", status=400, mimetype='text/plain')

    print(f"\nReceived User Message: {user_message} (from user: {user_id})")

    # --- Section 1: Interest Extraction ---
    extracted_interests_text = "NONE" # Default
    parsed_interests = []
    try:
        interest_prompt_template = (
            "Instruction: Analyze ONLY the following 'User text' to extract distinct interests, hobbies, or topics. "
            "Do NOT invent or list example interests if none are found in the 'User text'. "
            "Output ONLY a comma-separated list (e.g., books, hiking, programming). "
            "If no specific interests are evident IN THE PROVIDED 'User text', output the single word: NONE. "
            "Do not add any explanation, numbering, or conversational filler. "
            "User text: '{text}'"
            "Interests:"
        )
        interest_extraction_prompt = interest_prompt_template.format(text=user_message)
        print(f"DEBUG - Interest Extraction Prompt: {interest_extraction_prompt}")

        interest_inputs = tokenizer(interest_extraction_prompt, return_tensors="pt", truncation=True, max_length=512, padding=True)
        interest_inputs_on_device = {k: v.to(llm_model.device) for k, v in interest_inputs.items()}

        with torch.no_grad():
            interest_outputs_generate = llm_model.generate(
                input_ids=interest_inputs_on_device['input_ids'],
                attention_mask=interest_inputs_on_device['attention_mask'],
                max_new_tokens=60,
                min_new_tokens=1,
                do_sample=False,
                temperature=None,
                top_p=None,
                pad_token_id=tokenizer.pad_token_id,
                eos_token_id=tokenizer.eos_token_id
            )
        num_input_tokens_interest = interest_inputs_on_device['input_ids'].shape[1]
        extracted_interests_text = tokenizer.decode(interest_outputs_generate[0][num_input_tokens_interest:], skip_special_tokens=True).strip()
        print(f"DEBUG - Raw Extracted Interests from LLM: '{extracted_interests_text}'")

        first_line = extracted_interests_text.split('\n')[0].strip()
        if first_line.upper() == "NONE" or first_line.upper() == "NONE.":
            parsed_interests = []
        else:
            # Remove quotes if present
            if first_line.startswith("'") and first_line.endswith("'"):
                first_line = first_line[1:-1]
            if first_line.startswith("\"") and first_line.endswith("\""):
                first_line = first_line[1:-1]
            parsed_interests = [
                interest.strip() for interest in first_line.split(',')
                if interest.strip() and interest.strip().upper() not in ["NONE", "NONE."]
            ]
        print(f"DEBUG - Parsed Interests: {parsed_interests}")

    except Exception as e:
        print(f"Error during interest extraction: {str(e)}")
        # parsed_interests remains []

    # --- Neo4j Integration: Store user and interests ---
    if parsed_interests:
        update_user_interests_in_neo4j(user_id, parsed_interests)
        print(f"Stored/updated interests for user {user_id}: {parsed_interests}")

    # --- Section 2: Chat Response Generation ---
    # Use the original user_message as the prompt for a conversational response
    chat_prompt_text = user_message 
    
    chat_inputs = tokenizer(chat_prompt_text, return_tensors="pt", truncation=True, max_length=512, padding=True)
    chat_inputs_on_device = {k: v.to(llm_model.device) for k, v in chat_inputs.items()}

    generated_chat_response = ""
    try:
        with torch.no_grad():
            chat_outputs_generate = llm_model.generate(
                input_ids=chat_inputs_on_device['input_ids'],
                attention_mask=chat_inputs_on_device['attention_mask'],
                max_new_tokens=100,
                min_new_tokens=1,
                do_sample=True,
                top_p=0.85,
                temperature=0.6,
                pad_token_id=tokenizer.pad_token_id,
                eos_token_id=tokenizer.eos_token_id,
                no_repeat_ngram_size=2
            )
        # Decode only the newly generated tokens
        num_input_tokens_chat = chat_inputs_on_device['input_ids'].shape[1]
        generated_chat_response = tokenizer.decode(chat_outputs_generate[0][num_input_tokens_chat:], skip_special_tokens=True).strip()
        
        # More robust cleaning for chat response, especially for leading non-alphanumeric characters
        # This will remove leading periods, spaces, and the replacement characters ()
        cleaned_chat_response = ""
        # First, specifically remove all Unicode Replacement Characters (U+FFFD)
        temp_response = generated_chat_response.replace('\ufffd', '')

        for char_index, char_code in enumerate(temp_response):
            if char_code.isalnum(): # Find the first alphanumeric character
                cleaned_chat_response = temp_response[char_index:]
                break
        else: # If no alphanumeric characters found (e.g., all are or spaces)
            cleaned_chat_response = "" # Set to empty to trigger fallback

        generated_chat_response = cleaned_chat_response

    except Exception as e:
        print(f"Error during chat response generation: {str(e)}")
        # Fallback response will be handled below

    print(f"DEBUG - Raw Chat Response from LLM: '{generated_chat_response}'")

    # Use the generated chat response, with fallback
    final_app_response = generated_chat_response

    # Fallback for empty, short, or irrelevant responses (applied to the chat response)
    if not final_app_response or final_app_response.isspace() or len(final_app_response.split()) < 2 :
        final_app_response = f"I received your message: '{user_message}'. Could you please elaborate or ask something else?"
    elif len(set(final_app_response.split())) < len(final_app_response.split()) * 0.5 and len(final_app_response.split()) > 5 : # crude repetitiveness check
        final_app_response = f"I'm finding it a bit tricky to respond to '{user_message}'. Can you try rephrasing?"

    print(f"Final App Response: {final_app_response}\n")

    return Response(final_app_response, mimetype='text/plain')


def update_user_interests_in_neo4j(uid, interests):
    driver = get_neo4j_driver()
    with driver.session(database="neo4j") as session:
        session.run("MERGE (u:User {id: $uid})", uid=uid)
        for interest in interests:
            session.run(
                """
                MERGE (i:Interest {name: $interest})
                MERGE (u:User {id: $uid})
                MERGE (u)-[:HAS_INTEREST]->(i)
                """,
                uid=uid,
                interest=interest
            )


# (Optional) Endpoint to get user interests
@app.route('/user_interests')
def user_interests():
    uid = request.args.get('uid')
    driver = get_neo4j_driver()
    with driver.session(database="neo4j") as session:
        result = session.run(
            "MATCH (u:User {id: $uid})-[:HAS_INTEREST]->(i:Interest) RETURN i.name",
            uid=uid
        )
        interests = [record["i.name"] for record in result]
    return jsonify({"uid": uid, "interests": interests})


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--port', type=int, default=5000, help='Specify the port number')
    args = parser.parse_args()

    port_num = args.port
    prepareLlamaBot()
    get_neo4j_driver() # Initialize driver on startup
    print(f"App running on port {port_num}")
    # Ensure Neo4j driver is closed when app shuts down (won't work perfectly with Flask dev server reloads)
    # For robust cleanup, a proper application context or atexit might be needed in a production app.
    try:
        app.run(host='0.0.0.0', port=port_num)
    finally:
        close_neo4j_driver()
