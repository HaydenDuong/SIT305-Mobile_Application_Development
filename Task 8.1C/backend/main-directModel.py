from flask import Flask, request, Response
from transformers import AutoTokenizer, AutoModelForCausalLM, BitsAndBytesConfig
import torch
import argparse

app = Flask(__name__)
model = None
tokenizer = None


# MODEL = "meta-llama/Llama-3.2-1B"
# MODEL = "google/gemma-3-1b-it"
MODEL = "meta-llama/Llama-2-7b-chat-hf"

def prepareLlamaBot():
    global model, tokenizer
    print(f"Loading {MODEL} model... This may take a while.")

    # Configure 4-bit quantization
    quantization_config = BitsAndBytesConfig(
        load_in_4bit=True,
        bnb_4bit_compute_dtype=torch.float16,
        bnb_4bit_quant_type="nf4",
        bnb_4bit_use_double_quant=False
    )

    # Load tokenizer
    tokenizer = AutoTokenizer.from_pretrained(MODEL)
    tokenizer.pad_token = tokenizer.eos_token if tokenizer.pad_token is None else tokenizer.pad_token

    # Load model with quantization
    model = AutoModelForCausalLM.from_pretrained(
        MODEL,
        device_map="auto",
        quantization_config=quantization_config,
    )
    # Print the device the model is loaded on.
    # If 'cuda' or 'cuda:X', input tensors need to be moved to this device.
    print(f"Model loaded. Primary device: {model.device}")
    print("Model and tokenizer loaded successfully.")


@app.route('/')
def index():
    return "Welcome to the Llama Chatbot API!"


@app.route('/chat', methods=['POST'])
def chat():
    global model, tokenizer

    user_message = request.form.get('userMessage') or request.get_data(as_text=True).strip()

    if not user_message:
        return Response("Error: userMessage cannot be empty", status=400, mimetype='text/plain')

    print(f"\\nReceived User Message: {user_message}")

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
        
        # Ensure inputs are on the same device as the model
        interest_inputs_on_device = {k: v.to(model.device) for k, v in interest_inputs.items()}

        with torch.no_grad():
            interest_outputs_generate = model.generate(
                input_ids=interest_inputs_on_device['input_ids'],
                attention_mask=interest_inputs_on_device['attention_mask'],
                max_new_tokens=60,
                min_new_tokens=1,
                do_sample=False,
                temperature=None, # Explicitly set to None if do_sample is False
                top_p=None,       # Explicitly set to None if do_sample is False
                pad_token_id=tokenizer.pad_token_id,
                eos_token_id=tokenizer.eos_token_id
            )
        
        # Decode only the newly generated tokens
        num_input_tokens_interest = interest_inputs_on_device['input_ids'].shape[1]
        extracted_interests_text = tokenizer.decode(interest_outputs_generate[0][num_input_tokens_interest:], skip_special_tokens=True).strip()
        
        print(f"DEBUG - Raw Extracted Interests from LLM: '{extracted_interests_text}'")

        # Handle potential instruction regurgitation if output starts with NONE
        processed_interests_text = extracted_interests_text
        if extracted_interests_text.upper().startswith("NONE"):
            # Take only the first line if it starts with NONE, to discard regurgitated instructions
            processed_interests_text = extracted_interests_text.split('\n')[0].strip()

        if processed_interests_text.upper() == "NONE" or processed_interests_text.upper() == "NONE.":
            parsed_interests = [] 
        else:
            if processed_interests_text.startswith("'") and processed_interests_text.endswith("'"):
                processed_interests_text = processed_interests_text[1:-1]
            if processed_interests_text.startswith("\\\"") and processed_interests_text.endswith("\\\""):
                processed_interests_text = processed_interests_text[1:-1]
            
            parsed_interests = [
                interest.strip() for interest in processed_interests_text.split(',') 
                if interest.strip() and interest.strip().upper() not in ["NONE", "NONE."]
            ]
        
        print(f"DEBUG - Parsed Interests: {parsed_interests}")

    except Exception as e:
        print(f"Error during interest extraction: {str(e)}")
        # parsed_interests remains []
    # --- End of Interest Extraction ---

    # --- Section 2: Chat Response Generation ---
    # Use the original user_message as the prompt for a conversational response
    chat_prompt_text = user_message 
    
    chat_inputs = tokenizer(chat_prompt_text, return_tensors="pt", truncation=True, max_length=512, padding=True)
    chat_inputs_on_device = {k: v.to(model.device) for k, v in chat_inputs.items()}

    generated_chat_response = ""
    try:
        with torch.no_grad():
            chat_outputs_generate = model.generate(
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


    print(f"Final App Response: {final_app_response}\\n")
    return Response(final_app_response, mimetype='text/plain')


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--port', type=int, default=5000, help='Specify the port number')
    args = parser.parse_args()

    port_num = args.port
    prepareLlamaBot()
    print(f"App running on port {port_num}")
    app.run(host='0.0.0.0', port=port_num)
