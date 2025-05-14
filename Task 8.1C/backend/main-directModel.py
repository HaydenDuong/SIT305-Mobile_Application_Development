from flask import Flask, request, Response
from transformers import AutoTokenizer, AutoModelForCausalLM, BitsAndBytesConfig
import torch
import argparse

app = Flask(__name__)
model = None
tokenizer = None

MODEL = "meta-llama/Llama-3.2-1B"
MODEL = "google/gemma-3-1b-it"


def prepareLlamaBot():
    global model, tokenizer
    print(f"Loading {MODEL} model... This may take a while.")

    # print("Loading Llama-3.2-1B model with 4-bit quantization... This may take a while.")

    # Configure 4-bit quantization
    # quantization_config = BitsAndBytesConfig(
    #     load_in_4bit=True,
    #     bnb_4bit_compute_dtype=torch.float16,
    #     bnb_4bit_quant_type="nf4",
    #     bnb_4bit_use_double_quant=False
    # )

    # Load tokenizer
    tokenizer = AutoTokenizer.from_pretrained(MODEL)
    tokenizer.pad_token = tokenizer.eos_token if tokenizer.pad_token is None else tokenizer.pad_token

    # Load model with quantization
    model = AutoModelForCausalLM.from_pretrained(
        MODEL,
        # device_map="auto",
        # # quantization_config=quantization_config,
        # torch_dtype=torch.float16 if torch.cuda.is_available() else torch.float32
    )

    print("Model and tokenizer loaded successfully.")


@app.route('/')
def index():
    return "Welcome to the Llama Chatbot API!"


@app.route('/chat', methods=['POST'])
def chat():
    global model, tokenizer

    # Get userMessage from form data or raw body
    user_message = request.form.get('userMessage') or request.get_data(as_text=True).strip()

    # Validate userMessage
    if not user_message:
        return Response("Error: userMessage cannot be empty", status=400, mimetype='text/plain')

    # Print received request
    print("\nReceived Request:")
    print(f"userMessage: {user_message}")

    # Use raw userMessage as the input
    prompt = user_message

    # Tokenize input
    inputs = tokenizer(prompt, return_tensors="pt", truncation=True, max_length=512, padding=True)
    # if torch.cuda.is_available():
    #    inputs = {k: v.cuda() for k, v in inputs.items()}

    # Generate response
    try:
        with torch.no_grad():
            outputs = model.generate(
                input_ids=inputs['input_ids'],
                attention_mask=inputs['attention_mask'],
                max_new_tokens=100,  # Limit to short responses
                min_new_tokens=1,
                do_sample=True,
                top_p=0.85,  # Focused sampling
                temperature=0.6,  # Low randomness
                pad_token_id=tokenizer.pad_token_id,
                no_repeat_ngram_size=2  # Prevent repetition
            )
        raw_output = tokenizer.decode(outputs[0], skip_special_tokens=True).strip()
        # Remove the prompt from the output
        if raw_output.startswith(prompt):
            raw_output = raw_output[len(prompt):].strip()
    except Exception as e:
        print(f"Error during generation: {str(e)}")
        raw_output = ""

    # Print raw output
    print(f"Raw Model Output: {raw_output}")

    # Use raw output as response
    response = raw_output

    # Fallback for empty, short, or irrelevant responses
    if not response or response.isspace() or len(response.split()) < 3 or len(set(response.split())) < len(
            response.split()) * 0.7:
        response = f"Sorry, I couldn't provide a relevant answer to: '{user_message}'. Please rephrase."

    # Truncate to one sentence if too long
    # response = response.split('.')[0] + '.' if '.' in response else response

    # Print generated response
    print(f"Generated Response: {response}\n")

    # Return plain text response
    return Response(response, mimetype='text/plain')


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--port', type=int, default=5000, help='Specify the port number')
    args = parser.parse_args()

    port_num = args.port
    prepareLlamaBot()
    print(f"App running on port {port_num}")
    app.run(host='0.0.0.0', port=port_num)
