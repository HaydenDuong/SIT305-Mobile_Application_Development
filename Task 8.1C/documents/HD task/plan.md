# ChatConnect AI - Development Plan (Task 8.2 HD)

**Overall Goal:** Develop a high-distinction quality, functioning prototype of the "ChatConnect AI" application by May 30th, demonstrating advanced use of Llama 2, robust backend integration, and a user-friendly interface, as outlined in the project proposal.

**Timeline:** End of Day, May 30th.

**Core Technologies:**
*   **Frontend:** Android Studio (Java)
*   **AI Core:** Llama 2 API (Python backend)
*   **Authentication:** Firebase Authentication
*   **Graph Database:** Neo4j
*   **Backend Framework:** Flask (Python, for Llama 2 API and potentially Neo4j interactions)

---

## Phased Development Plan

### Phase 1: Core Setup & Enhanced AI (Building on Task 8.1C Base)
*(Focus: Establish user identity, enhance Llama 2 for core functionality, prepare data for graph)*

**Objective:** Integrate secure authentication and fundamentally upgrade the Llama 2 backend to extract user interests from conversations.

**High HD Focus:** Demonstrating advanced Llama 2 integration beyond simple chat by performing interest extraction. Well-structured and secure authentication.

1.  **Firebase Authentication Integration (Android & Backend)**
    *   **Task:** Implement user registration and login using Firebase Authentication (e.g., Email/Password, Google Sign-In).
    *   **Android:**
        *   Update `LoginActivity` to use Firebase UI or custom Firebase calls.
        *   Handle user sessions (sign in, sign out).
        *   Securely pass Firebase User UID or ID Token to the backend upon successful login for authenticated API calls.
    *   **Backend (Python):**
        *   (If needed for protected endpoints) Add middleware to verify Firebase ID Tokens for authenticated requests.
    *   **Deliverable:** Users can create accounts and log in securely. The Android app has access to a unique User UID.

2.  **Llama 2 Backend - Interest Extraction & Enhanced Chat Response**
    *   **Task:** Modify the Python Flask backend (`main-directModel.py` or a new version).
    *   **Prompt Engineering (Key for HD):**
        *   Design prompts for Llama 2 that instruct it to:
            1.  Engage in natural conversation.
            2.  Simultaneously identify and list key interests, hobbies, or topics mentioned by the user *within that conversation*.
        *   Consider a two-step process: first get a conversational reply, then make a second call to Llama 2 with the conversation history to extract interests, or try to get both in one structured output.
    *   **Backend Logic:**
        *   The `/chat` endpoint should now aim to return:
            *   The natural chat response (as before).
            *   A structured list of identified interests/keywords (e.g., JSON array: `{"reply": "That sounds fun!", "interests": ["hiking", "photography"]}`).
    *   **Testing:** Thoroughly test different conversation snippets to see how well interests are extracted.
    *   **Deliverable:** Backend API can provide both a chat reply and a list of potential user interests based on the conversation.

3.  **Basic User Profile & Interest Storage (Initial - Pre-Neo4j)**
    *   **Task:** Create a mechanism to temporarily store or log extracted interests associated with a User UID.
    *   **Backend:** When the Llama 2 backend extracts interests for an authenticated user, log these interests alongside the User UID.
    *   **(Optional Initial Storage):** If Neo4j setup is delayed, consider a simple temporary store (e.g., a dictionary in memory on the backend for testing, or a simple file log) to validate the interest extraction flow. This is just a stepping stone to Neo4j.
    *   **Deliverable:** Confirmation that interests are being extracted and associated with users.

4.  **Android UI for Interest Input & Display (Leverage Existing Chat)**
    *   **Task:** The existing `ChatActivity` will serve as the primary interface for users to interact with Llama 2.
    *   **Android:**
        *   Modify `ChatActivity` to handle the new structured response from the backend (chat reply + interests).
        *   Initially, the app can just display the chat reply as it does now. The extracted interests might be logged in Android's Logcat for verification or temporarily displayed in a Toast/debug TextView.
    *   **Deliverable:** Chat UI functions with the updated backend response format.

---

### Phase 2: Neo4j Integration & First Recommendations
*(Focus: Building the knowledge graph and implementing the first layer of social matching)*

**Objective:** Set up Neo4j, populate it with users and their extracted interests, and implement a basic user-to-user recommendation feature.

**High HD Focus:** Exceptional use of a graph database (Neo4j) for modeling complex social relationships and interests. Advanced coding techniques for backend-database interaction.

1.  **Neo4j Setup & Configuration**
    *   **Task:** Install and configure a Neo4j instance.
        *   Options: Local Neo4j Desktop, Docker container, or a cloud-hosted Neo4j AuraDB free tier.
    *   **Model Definition:** Define initial node labels (`User`, `Interest`) and relationship types (`HAS_INTEREST`).
    *   **Deliverable:** A running Neo4j instance accessible by the backend.

2.  **Backend Service to Update Neo4j**
    *   **Task:** Implement Python backend logic to interact with Neo4j.
    *   **Libraries:** Use the official Neo4j Python driver (`neo4j`).
    *   **Functionality:**
        *   When interests are extracted for a User UID:
            *   Create/update the `User` node (e.g., `MERGE (u:User {userId: $uid})`).
            *   For each extracted interest:
                *   Create/update the `Interest` node (e.g., `MERGE (i:Interest {name: $interestName})`).
                *   Create a `HAS_INTEREST` relationship between the user and the interest (e.g., `MERGE (u)-[:HAS_INTEREST]->(i)`).
    *   **Integration:** This logic should be triggered after Llama 2 extracts interests.
    *   **Deliverable:** User profiles and their interests are being populated in the Neo4j graph.

3.  **Basic User Recommendation Logic (Backend)**
    *   **Task:** Create a new backend API endpoint (e.g., `GET /recommendations/users`).
    *   **Input:** User UID.
    *   **Logic:**
        *   Query Neo4j using Cypher to find other users with shared interests.
        *   Example Cypher (conceptual):
            ```cypher
            MATCH (currentUser:User {userId: $currentUserUID})-[:HAS_INTEREST]->(interest:Interest)<-[:HAS_INTEREST]-(recommendedUser:User)
            WHERE currentUser <> recommendedUser
            WITH recommendedUser, COUNT(interest) AS commonInterests
            ORDER BY commonInterests DESC
            RETURN recommendedUser.userId, commonInterests
            LIMIT 10 // Or some other limit
            ```
    *   **Output:** A list of recommended User UIDs (and perhaps their common interests).
    *   **Deliverable:** Backend API that provides user recommendations based on shared interests in Neo4j.

4.  **Android UI for Displaying User Recommendations**
    *   **Task:** Create a new `Activity` or `Fragment` (e.g., `RecommendationsActivity`).
    *   **Functionality:**
        *   Fetch recommendations from the backend API endpoint.
        *   Use a `RecyclerView` to display recommended users (e.g., show username/UID and maybe the number of common interests).
        *   (Future enhancement): Clicking on a recommended user could lead to their profile or initiate a chat.
    *   **Deliverable:** Android app can display a list of recommended users.

---

### Phase 3: Refinements & Advanced Features (Time Permitting for High HD)
*(Focus: Expanding recommendation types, incorporating user feedback, and polishing the app)*

**Objective:** Enhance the recommendation system, introduce group/community suggestions, and implement a feedback loop for AI improvement.

**High HD Focus:** Innovative features, thoughtful consideration of future work and scalability, compelling presentation of complex information flow.

1.  **Group/Community Recommendations**
    *   **Task:** Extend the system to recommend groups or communities.
    *   **Neo4j Model:** Add `Group` node label and `INTERESTED_IN_GROUP` or `MEMBER_OF` relationships.
    *   **Llama 2 / Backend:**
        *   Can Llama 2 also suggest relevant existing groups based on interests?
        *   Or, initially, manually populate some groups related to common interests in Neo4j.
    *   **Recommendation Logic:** Update backend to query for relevant groups based on user interests.
    *   **Android UI:** Add a section/tab in `RecommendationsActivity` to show suggested groups.
    *   **Deliverable:** App can suggest relevant groups/communities.

2.  **User Feedback on Recommendations**
    *   **Task:** Allow users to provide feedback on the quality of recommendations.
    *   **Android UI:** Add simple "useful" / "not useful" or a rating system to recommended items.
    *   **Backend:** Create an endpoint to receive this feedback and store it (e.g., in Neo4j as properties on relationships, or in a separate database).
    *   **AI Learning (Conceptual for HD):**
        *   Discuss how this feedback *could* be used to refine Llama 2's interest extraction (e.g., by providing examples of good/bad interest associations) or to adjust weights in Neo4j queries. Full implementation might be out of scope, but demonstrating understanding is key.
    *   **Deliverable:** Users can provide feedback; system stores it. Discussion of how it aids AI.

3.  **(Optional) Manual Profile Input / Interest Tagging**
    *   **Task:** Allow users to explicitly add interests to their profile.
    *   **Android UI:** A simple profile editing screen.
    *   **Backend & Neo4j:** Update user's interests in Neo4j based on manual input. This complements AI-driven extraction.
    *   **Deliverable:** Users can augment their AI-generated interest profile.

---

## High HD Criteria Focus Summary:

*   **Functionality:** Seamless Llama 2 integration for both chat and *advanced interest extraction*. Flawless operation of all features.
*   **User Interface:** Exceptionally designed, intuitive, enhances engagement (especially the recommendation display).
*   **Technical Proficiency:** Exceptional use of Android/Java, Python. Advanced coding for Llama 2 (prompt engineering, structured output), Neo4j (complex queries, graph modeling), and their integration. Well-structured, readable, optimized code.
*   **Innovation and Creativity:** The core concept of ChatConnect AI, using Llama 2 for social matchmaking. The way interests are derived and connections are mapped in Neo4j.
*   **Future Work and Scalability:** Articulating how the feedback loop improves the AI, how new recommendation types can be added, and how the system can scale.

---

## Key Challenges:

1.  **Llama 2 for Structured Output:** Reliably getting Llama 2 to extract interests in a usable, structured format while maintaining natural conversation.
2.  **Neo4j Learning Curve & Integration:** Efficiently modeling data and writing performant Cypher queries if new to Neo4j.
3.  **Scope Management:** The proposal is ambitious. Prioritize core features that demonstrate the main concept for the deadline. Focus on a "vertical slice" that shows the end-to-end flow (chat -> interest extraction -> Neo4j storage -> recommendation).

---

## Presentation Preparation:

*   Continuously document development progress: code snippets, architectural decisions, challenges faced and overcome.
*   Take screenshots/short video clips of working features at each stage.
*   Clearly explain the data flow: User Input -> Android App -> Python Backend (Llama 2) -> Neo4j -> Recommendation Engine -> Android App.
*   Emphasize the innovative aspects and how advanced technologies (Llama 2, Neo4j) are used.

---