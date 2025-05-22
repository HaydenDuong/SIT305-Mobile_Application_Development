import os
os.environ['TORCHDYNAMO_DISABLE'] = '1'

import torch
torch.set_float32_matmul_precision('high')

from flask import Flask, request, jsonify
import re
from transformers import AutoTokenizer, AutoModelForCausalLM
import traceback # Import traceback module

app = Flask(__name__)

# Load the model and tokenizer
# MODEL = "meta-llama/Llama-3.2-1B"
MODEL = "google/gemma-3-1b-it"



tokenizer = None
model = None
device = None

try:
    print(f"Loading tokenizer for {MODEL}...")
    tokenizer = AutoTokenizer.from_pretrained(MODEL)
    print(f"Loading model for {MODEL}...")
    model = AutoModelForCausalLM.from_pretrained(MODEL)
    device = torch.device("cuda:0" if torch.cuda.is_available() else "cpu")
    print(f"Moving model to device: {device}")
    model.to(device)
    print(f"Successfully loaded model and tokenizer on device: {device}")
except Exception as e:
    print(f"CRITICAL ERROR during model loading: {str(e)}")
    print(traceback.format_exc()) # Print full traceback for loading errors
    # Optionally, exit if model loading fails, as the app can't function
    # exit()

def fetchQuizFromLlama(student_topic):
    if not model or not tokenizer or not device:
        print("ERROR: Model, tokenizer, or device not initialized properly!")
        raise Exception("Model, tokenizer, or device not initialized.")

    print(f"Generating quiz for topic: {student_topic} using {MODEL}")
    prompt = (
        f"Generate a quiz with 3 questions to test students on the provided topic. "
        f"For each question, generate 4 options where only one of the options is correct. "
        f"Format your response as follows:\n"
        f"**QUESTION 1:** [Your question here]?\n"
        f"**OPTION A:** [First option]\n"
        f"**OPTION B:** [Second option]\n"
        f"**OPTION C:** [Third option]\n"
        f"**OPTION D:** [Fourth option]\n"
        f"**ANS:** [Correct answer letter]\n\n"
        f"**QUESTION 2:** [Your question here]?\n"
        f"**OPTION A:** [First option]\n"
        f"**OPTION B:** [Second option]\n"
        f"**OPTION C:** [Third option]\n"
        f"**OPTION D:** [Fourth option]\n"
        f"**ANS:** [Correct answer letter]\n\n"
        f"**QUESTION 3:** [Your question here]?\n"
        f"**OPTION A:** [First option]\n"
        f"**OPTION B:** [Second option]\n"
        f"**OPTION C:** [Third option]\n"
        f"**OPTION D:** [Fourth option]\n"
        f"**ANS:** [ONLY THE SINGLE CORRECT LETTER: A, B, C, or D. For example: B]\n\n"
        f"Ensure text is properly formatted. It needs to start with a question, then the options, and finally the correct answer. "
        f"Follow this pattern for all questions. "
        f"Do not include any additional information, questions, or prompts beyond the 3 questions specified above. "
        f"Here is the student topic:\n{student_topic}"
    )

    try:
        print("Tokenizing prompt...")
        inputs = tokenizer(prompt, return_tensors="pt").to(device)
        print("Generating text with model...")
        outputs = model.generate(
            **inputs,
            max_new_tokens=500,
            temperature=0.7,
            top_p=0.9,
            do_sample=True,
            pad_token_id=tokenizer.eos_token_id
        )
        print("Decoding generated tokens...")
        generated_text = tokenizer.decode(outputs[0], skip_special_tokens=True)
        print(f"Raw generated text (first 200 chars): {generated_text[:200]}...")

        quiz_start = generated_text.find("**QUESTION 1:**")
        if quiz_start == -1:
            print(f"ERROR: '**QUESTION 1:**' not found in generated text.")
            print(f"Full generated text was: {generated_text}") # Log the full text if marker is missing
            raise Exception("Failed to generate a properly formatted quiz (start marker not found)")
        quiz_text = generated_text[quiz_start:]
        return quiz_text
    except Exception as e:
        print(f"ERROR in fetchQuizFromLlama: {str(e)}")
        print(traceback.format_exc()) # Print full traceback
        raise # Re-raise the exception to be caught by the route handler

def process_quiz(quiz_text):
    print("Processing quiz text...")
    questions = []
    # Updated regex to be more robust at the end of the ANS line
    pattern = re.compile(
        r'\*\*QUESTION \d+:\*\* (.+?)\n'
        r'\*\*OPTION A:\*\* (.+?)\n'
        r'\*\*OPTION B:\*\* (.+?)\n'
        r'\*\*OPTION C:\*\* (.+?)\n'
        r'\*\*OPTION D:\*\* (.+?)\n'
        r'\*\*ANS:\*\* (.+?)(?=\n\n\*\*QUESTION|\n*$)', # Match until next question or end of string (with optional newlines)
        re.DOTALL
    )
    matches = pattern.findall(quiz_text)
    print(f"Found {len(matches)} matches with regex.")

    for i, match in enumerate(matches):
        print(f"Processing match {i+1}: {match}")
        question = match[0].strip()
        options = [match[1].strip(), match[2].strip(), match[3].strip(), match[4].strip()]
        correct_ans_letter = match[5].strip().upper()

        if correct_ans_letter not in ["A", "B", "C", "D"]:
            print(f"WARNING: Invalid correct answer letter '{correct_ans_letter}' for question: {question}")
            continue 

        if "[Your question here]" in question or any("[First option]" in opt for opt in options) or "[Option" in correct_ans_letter:
            print(f"Skipping placeholder question: {question}")
            continue

        question_data = {
            "question": question,
            "options": options,
            "correct_answer": correct_ans_letter
        }
        questions.append(question_data)
    print(f"Successfully processed {len(questions)} questions.")
    return questions

@app.route('/getQuiz', methods=['GET'])
def get_quiz():
    print("Request received for /getQuiz")
    student_topic = request.args.get('topic')
    if not student_topic:
        print("ERROR: Missing topic parameter")
        return jsonify({'error': 'Missing topic parameter'}), 400
    try:
        print(f"Attempting to generate quiz for topic: {student_topic}")
        quiz_text_from_llama = fetchQuizFromLlama(student_topic)
        # Log more of the response if it's short, or a fixed amount if long
        log_length = min(len(quiz_text_from_llama), 500)
        print(f"Raw quiz text from Llama (up to {log_length} chars for /getQuiz): {quiz_text_from_llama[:log_length]}...")
        
        processed_quiz_data = process_quiz(quiz_text_from_llama)
        
        if not processed_quiz_data:
            print("ERROR: Failed to parse quiz data or no questions processed.")
            # Return a snippet of the raw response to help diagnose formatting issues from LLM
            return jsonify({'error': 'Failed to parse quiz data from LLM response', 
                            'raw_response_snippet': quiz_text_from_llama[:min(len(quiz_text_from_llama),1000)]}), 500
        
        print(f"Successfully processed quiz, returning {len(processed_quiz_data)} questions.")
        return jsonify({'quiz': processed_quiz_data}), 200
    except Exception as e:
        # This is the critical block for catching and logging any error
        print(f"---------- UNHANDLED EXCEPTION IN /getQuiz ROUTE ----------")
        print(f"Error type: {type(e).__name__}")
        print(f"Error message: {str(e)}")
        print(f"Traceback:")
        print(traceback.format_exc()) # This will print the detailed stack trace
        return jsonify({
            'error': f'An unexpected error occurred on the server: {str(e)}',
            'error_type': type(e).__name__ 
        }), 500

@app.route('/test', methods=['GET'])
def run_test():
    return jsonify({'quiz': "test"}), 200

if __name__ == '__main__':
    if not model or not tokenizer: # Check if model loading failed
        print("CRITICAL: Model or Tokenizer not loaded. Exiting Flask app.")
    else:
        port_num = 5000
        print(f"App running on port {port_num}")
        app.run(port=port_num, host="0.0.0.0")