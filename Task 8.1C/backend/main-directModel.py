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
            "Instructions for Interest Extraction:\n"
            "1. Analyze the 'User Text' below to identify distinct interests, hobbies, or topics.\n"
            "2. Output ONLY a comma-separated list of these interests (e.g., reading, travel, cooking).\n"
            "3. If NO specific interests are found in the 'User Text', output ONLY the special token: __NONE__\n"
            "4. Do NOT add any explanations, numbering, or conversational filler.\n"
            "5. Do NOT include the token __NONE__ if actual interests are identified.\n\n"
            "User Text:\n'''\n{text}\n'''\n\n"
            "Extracted Interests (comma-separated or __NONE__):\n"
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

        # Take only the first meaningful line for interest extraction
        first_line_llm = extracted_interests_text.split('\n')[0].strip()
        parsed_interests = []

        # Handle the special "no interest" marker (new and old, just in case)
        # Convert to uppercase for case-insensitive comparison
        first_line_upper = first_line_llm.upper()
        if first_line_upper == "__NONE__" or first_line_upper == "NONE" or first_line_upper == "NONE.":
            parsed_interests = [] # Explicitly no interests
        else:
            # Remove potential surrounding single/double quotes from the whole line
            if (first_line_llm.startswith("'") and first_line_llm.endswith("'")) or \
               (first_line_llm.startswith("\"") and first_line_llm.endswith("\"")):
                first_line_llm = first_line_llm[1:-1]

            potential_interests_raw = first_line_llm.split(',')
            
            for item_raw in potential_interests_raw:
                item = item_raw.strip() # General strip
                if not item:  # Skip if item is empty after stripping
                    continue

                # Attempt to remove " NONE" or ".NONE" suffix if present with other text
                # This specifically targets cases like "music NONE"
                item_lower_check = item.lower()
                if item_lower_check.endswith(" none"):
                    item = item[:-5].strip() # Remove " none" (5 chars) and re-strip
                elif item_lower_check.endswith(".none"): # Llama sometimes adds a period
                    item = item[:-5].strip() # Remove ".none" (5 chars) and re-strip
                
                # Remove trailing periods from the cleaned item
                if item.endswith('.'):
                    item = item[:-1].strip()

                # Final check: if item is not empty and not the "NONE" marker itself
                # Store interests in a consistent case, e.g., lowercase
                item_final_lower = item.lower()
                if item_final_lower and item_final_lower not in ["none", "none.", "__none__"]:
                    parsed_interests.append(item_final_lower)
            
            # Remove duplicates by converting to set and back to list, then sort
            if parsed_interests:
                parsed_interests = sorted(list(set(parsed_interests)))

        print(f"DEBUG - Parsed Interests after refinement: {parsed_interests}")

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
        # Ensure User node exists
        session.run("MERGE (u:User {id: $uid})", uid=uid)
        
        for interest_name in interests: # Renamed 'interest' to 'interest_name' for clarity
            # Ensure Interest node exists
            session.run("MERGE (i:Interest {name: $interest_name})", interest_name=interest_name)
            
            # Ensure User has HAS_INTEREST relationship to Interest
            session.run(
                """
                MATCH (u:User {id: $uid})
                MATCH (i:Interest {name: $interest_name})
                MERGE (u)-[:HAS_INTEREST]->(i)
                """,
                uid=uid,
                interest_name=interest_name
            )
            
# Endpoint to get user interests
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

# Endpoint to delete a specific interest for a user
@app.route('/add_interest', methods=['POST'])
def add_interest():
    uid = request.form.get('user_id') or (request.json.get('user_id') if request.is_json else None)
    interest = request.form.get('interest') or (request.json.get('interest') if request.is_json else None)
    if not uid or not interest:
        return Response("Error: user_id and interest are required", status=400, mimetype='text/plain')
    driver = get_neo4j_driver()
    with driver.session(database="neo4j") as session:
        session.run(
            """
            MERGE (u:User {id: $uid})
            MERGE (i:Interest {name: $interest})
            MERGE (u)-[:HAS_INTEREST]->(i)
            """,
            uid=uid,
            interest=interest
        )
    return jsonify({"status": "success", "added_interest": interest})

# Endpoint to delete a specific interest for a user
@app.route('/delete_interest', methods=['POST'])
def delete_interest():
    uid = request.form.get('user_id') or (request.json.get('user_id') if request.is_json else None)
    interest = request.form.get('interest') or (request.json.get('interest') if request.is_json else None)
    if not uid or not interest:
        return Response("Error: user_id and interest are required", status=400, mimetype='text/plain')
    driver = get_neo4j_driver()
    with driver.session(database="neo4j") as session:
        # Delete the HAS_INTEREST relationship
        session.run(
            """
            MATCH (u:User {id: $uid})-[r:HAS_INTEREST]->(i:Interest {name: $interest})
            DELETE r
            """,
            uid=uid,
            interest=interest
        )
        # Optionally, delete orphaned interests
        session.run(
            """
            MATCH (i:Interest)
            WHERE NOT (i)<-[:HAS_INTEREST]-(:User)
            DELETE i
            """
        )
    return jsonify({"status": "success", "deleted_interest": interest})

@app.route('/recommendations/users', methods=['GET'])
def get_user_recommendations():
    user_id = request.args.get('user_id') # Or 'uid' depending on how you call it from client
    if not user_id:
        return jsonify({"error": "user_id parameter is required"}), 400

    driver = get_neo4j_driver()
    recommendations = []
    with driver.session(database="neo4j") as session:
        result = session.run(
            """
            MATCH (currentUser:User {id: $user_id})-[:HAS_INTEREST]->(interest:Interest)<-[:HAS_INTEREST]-(recommendedUser:User)
            WHERE currentUser <> recommendedUser
            WITH recommendedUser, COUNT(interest) AS commonInterests, COLLECT(interest.name) AS commonInterestNames
            ORDER BY commonInterests DESC
            LIMIT 10 // You can make the limit configurable if needed
            RETURN recommendedUser.id AS recommendedUserId, commonInterests, commonInterestNames
            """,
            user_id=user_id
        )
        for record in result:
            recommendations.append({
                "userId": record["recommendedUserId"],
                "commonInterests": record["commonInterests"],
                "commonInterestNames": record["commonInterestNames"]
            })
    
    return jsonify({"recommendations": recommendations})

@app.route('/groups/join', methods=['POST'])
def join_group():
    data = request.get_json()
    user_id = data.get('user_id')
    group_name = data.get('group_name')

    if not user_id or not group_name:
        return jsonify({"error": "user_id and group_name are required"}), 400

    driver = get_neo4j_driver()
    with driver.session(database="neo4j") as session:
        # Ensure user exists
        user_exists_result = session.run("MATCH (u:User {id: $user_id}) RETURN u", user_id=user_id).single()
        if not user_exists_result:
            return jsonify({"error": f"User with id '{user_id}' not found"}), 404

        # Check if the interest (group_name) is shared by at least 2 users
        shared_check_result = session.run(
            """MATCH (i:Interest {name: $interest_name})<-[:HAS_INTEREST]-(u:User)
               WITH i, COUNT(DISTINCT u) AS userCount
               RETURN userCount >= 2 AS isSharableGroup, i.name AS interestName""",
            interest_name=group_name
        )
        shared_record = shared_check_result.single()
        
        if not (shared_record and shared_record["isSharableGroup"]):
            return jsonify({"error": f"Cannot join group. Interest '{group_name}' is not shared by enough users yet."}), 409 # 409 Conflict or 400

        # If sharable, ensure the Group node and its ABOUT relationship exist
        # The interestName from the query confirms the interest node exists.
        session.run(
            '''MERGE (i:Interest {name: $interest_name})
               MERGE (g:Group {name: $interest_name}) 
               MERGE (g)-[:ABOUT]->(i)''',
            interest_name=shared_record["interestName"]
        )

        # Now, create the MEMBER_OF relationship
        session.run(
            """
            MATCH (u:User {id: $user_id}), (g:Group {name: $group_name})
            MERGE (u)-[:MEMBER_OF]->(g)
            """,
            user_id=user_id,
            group_name=group_name
        )
    return jsonify({"status": "success", "message": f"User '{user_id}' joined group '{group_name}'"}), 200


@app.route('/groups/leave', methods=['POST'])
def leave_group():
    data = request.get_json()
    user_id = data.get('user_id')
    group_name = data.get('group_name')

    if not user_id or not group_name:
        return jsonify({"error": "user_id and group_name are required"}), 400

    driver = get_neo4j_driver()
    with driver.session(database="neo4j") as session:
        # Check if the relationship exists before trying to delete
        result = session.run(
            """
            MATCH (u:User {id: $user_id})-[r:MEMBER_OF]->(g:Group {name: $group_name})
            DELETE r
            RETURN COUNT(r) AS deleted_count
            """,
            user_id=user_id,
            group_name=group_name
        )
        deleted_count = result.single()["deleted_count"]
        
        if deleted_count > 0:
            return jsonify({"status": "success", "message": f"User '{user_id}' left group '{group_name}'"}), 200
        else:
            return jsonify({"error": f"User '{user_id}' is not a member of group '{group_name}', or group/user does not exist."}), 404


@app.route('/groups/message', methods=['POST'])
def send_group_message():
    data = request.get_json()
    user_id = data.get('user_id')
    group_name = data.get('group_name')
    message_text = data.get('message_text')

    if not all([user_id, group_name, message_text]):
        return jsonify({"error": "user_id, group_name, and message_text are required"}), 400

    driver = get_neo4j_driver()
    with driver.session(database="neo4j") as session:
        # Ensure user and group exist
        user_node = session.run("MATCH (u:User {id: $user_id}) RETURN u", user_id=user_id).single()
        group_node = session.run("MATCH (g:Group {name: $group_name}) RETURN g", group_name=group_name).single()

        if not user_node:
            return jsonify({"error": f"User '{user_id}' not found"}), 404
        if not group_node:
            return jsonify({"error": f"Group '{group_name}' not found"}), 404
        
        # Enforce that only members can post
        is_member_result = session.run(
            """MATCH (:User {id: $user_id})-[:MEMBER_OF]->(:Group {name: $group_name}) 
               RETURN COUNT(*) > 0 AS isMember""",
            user_id=user_id, group_name=group_name
        )
        is_member_check = is_member_result.single()

        if not (is_member_check and is_member_check["isMember"]):
            return jsonify({"error": f"User '{user_id}' is not a member of group '{group_name}' and cannot post messages."}), 403

        # Create message and relationships
        result = session.run(
            """
            MATCH (u:User {id: $user_id})
            MATCH (g:Group {name: $group_name})
            CREATE (msg:Message {text: $message_text, timestamp: datetime(), senderId: $user_id})
            CREATE (msg)-[:IN_GROUP]->(g) // Changed from IN to IN_GROUP for clarity with Cypher's IN keyword
            CREATE (u)-[:SENT_MESSAGE]->(msg)
            RETURN msg.timestamp AS timestamp, id(msg) AS messageId
            """,
            user_id=user_id,
            group_name=group_name,
            message_text=message_text
        )
        created_message_info = result.single()
        
    return jsonify({
        "status": "success", 
        "message": "Message sent to group", 
        "messageId": created_message_info["messageId"],
        "timestamp": str(created_message_info["timestamp"])
    }), 201


@app.route('/groups/messages', methods=['GET'])
def get_group_messages():
    group_name = request.args.get('group_name')
    if not group_name:
        return jsonify({"error": "group_name parameter is required"}), 400

    limit = request.args.get('limit', default=50, type=int) # Default to 50 messages, allow client to specify

    driver = get_neo4j_driver()
    messages = []
    with driver.session(database="neo4j") as session:
        # Ensure group exists
        group_node = session.run("MATCH (g:Group {name: $group_name}) RETURN g", group_name=group_name).single()
        if not group_node:
            return jsonify({"error": f"Group '{group_name}' not found"}), 404

        result = session.run(
            """
            MATCH (msg:Message)-[:IN_GROUP]->(g:Group {name: $group_name})
            // OPTIONAL: To get sender's username if you store it on User node
            // MATCH (sender:User {id: msg.senderId})
            RETURN msg.text AS text, msg.senderId AS senderId, msg.timestamp AS timestamp //, sender.username AS senderUsername
            ORDER BY msg.timestamp DESC // Show newest messages first, or ASC for oldest first
            LIMIT $limit
            """,
            group_name=group_name,
            limit=limit
        )
        for record in result:
            messages.append({
                "text": record["text"],
                "senderId": record["senderId"],
                "timestamp": str(record["timestamp"])
            })
        
        # Messages are fetched in DESC order (newest first), so reverse for chronological display if needed by client
        # Or client can handle ordering. For now, returning newest first.
    return jsonify({"group_name": group_name, "messages": messages}), 200


@app.route('/groups/ismember', methods=['GET'])
def is_group_member():
    user_id = request.args.get('user_id')
    group_name = request.args.get('group_name') # This is an interest name

    if not user_id or not group_name:
        return jsonify({"error": "user_id and group_name parameters are required"}), 400

    driver = get_neo4j_driver()
    with driver.session(database="neo4j") as session:
        # Check if the interest (group_name) is shared by at least 2 users
        shared_check_result = session.run(
            """MATCH (i:Interest {name: $interest_name})<-[:HAS_INTEREST]-(u:User)
               WITH i, COUNT(DISTINCT u) AS userCount
               RETURN userCount >= 2 AS isSharableGroup""",
            interest_name=group_name
        )
        shared_record = shared_check_result.single()
        is_sharable_group = shared_record["isSharableGroup"] if shared_record else False

        if not is_sharable_group:
            return jsonify({"isMember": False, "groupExistsAndQualifies": False, "message": "Interest not shared by enough users to form a group."}), 200

        # If sharable, ensure the Group node and its ABOUT relationship exist
        session.run(
            """MERGE (i:Interest {name: $interest_name})
               MERGE (g:Group {name: $interest_name})
               MERGE (g)-[:ABOUT]->(i)""",
            interest_name=group_name
        )

        # Now check actual membership
        membership_result = session.run(
            """MATCH (u:User {id: $user_id})-[:MEMBER_OF]->(g:Group {name: $group_name})
               RETURN COUNT(u) > 0 AS isMember""",
            user_id=user_id,
            group_name=group_name
        )
        member_record = membership_result.single()
        is_member = member_record["isMember"] if member_record else False
        
    return jsonify({"isMember": is_member, "groupExistsAndQualifies": True}), 200


def generate_chat_room_id(uid1, uid2):
    uids = sorted([str(uid1), str(uid2)])
    return f"{uids[0]}_{uids[1]}"

@app.route('/direct_chat/send', methods=['POST'])
def send_direct_message():
    data = request.get_json()
    sender_uid = data.get('sender_uid')
    receiver_uid = data.get('receiver_uid')
    message_text = data.get('message_text')

    if not all([sender_uid, receiver_uid, message_text]):
        return jsonify({"error": "sender_uid, receiver_uid, and message_text are required"}), 400

    # Basic check if users exist (optional, but good practice)
    driver = get_neo4j_driver()
    with driver.session(database="neo4j") as session:
        sender_node = session.run("MATCH (u:User {id: $uid}) RETURN u", uid=sender_uid).single()
        receiver_node = session.run("MATCH (u:User {id: $uid}) RETURN u", uid=receiver_uid).single()
        if not sender_node:
            return jsonify({"error": f"Sender with id '{sender_uid}' not found"}), 404
        if not receiver_node:
            return jsonify({"error": f"Receiver with id '{receiver_uid}' not found"}), 404

        chat_room_id = generate_chat_room_id(sender_uid, receiver_uid)

        result = session.run(
            """
            MATCH (sender:User {id: $sender_uid})
            CREATE (msg:UserMessage {
                text: $message_text,
                senderId: $sender_uid,
                receiverId: $receiver_uid, 
                chatRoomId: $chat_room_id,
                timestamp: datetime()
            })
            CREATE (sender)-[:SENT_MESSAGE]->(msg)
            RETURN id(msg) AS messageId, msg.timestamp AS timestamp, msg.chatRoomId AS chatRoomId
            """,
            sender_uid=sender_uid,
            receiver_uid=receiver_uid, # Not directly used in MATCH for message creation but good for context
            message_text=message_text,
            chat_room_id=chat_room_id
        )
        created_message_info = result.single()
        if created_message_info:
            return jsonify({
                "status": "success",
                "messageId": created_message_info["messageId"],
                "timestamp": str(created_message_info["timestamp"]),
                "chatRoomId": created_message_info["chatRoomId"]
            }), 201
        else:
            return jsonify({"error": "Failed to create message in database"}), 500

@app.route('/direct_chat/messages', methods=['GET'])
def get_direct_messages():
    uid1 = request.args.get('uid1')
    uid2 = request.args.get('uid2')
    limit = request.args.get('limit', default=50, type=int)

    if not uid1 or not uid2:
        return jsonify({"error": "uid1 and uid2 parameters are required"}), 400

    chat_room_id = generate_chat_room_id(uid1, uid2)
    messages = []
    driver = get_neo4j_driver()
    with driver.session(database="neo4j") as session:
        result = session.run(
            """
            MATCH (msg:UserMessage {chatRoomId: $chat_room_id})
            RETURN msg.text AS text, msg.senderId AS senderId, msg.timestamp AS timestamp
            ORDER BY msg.timestamp ASC 
            LIMIT $limit
            """,
            chat_room_id=chat_room_id,
            limit=limit
        )
        for record in result:
            messages.append({
                "text": record["text"],
                "senderId": record["senderId"],
                "timestamp": str(record["timestamp"])
            })
    return jsonify({"chatRoomId": chat_room_id, "messages": messages}), 200


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
