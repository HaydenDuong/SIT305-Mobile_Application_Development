# 5-Day Implementation Plan: Personalized Learning App Features (Revised Flow)

This plan outlines tasks to implement Profile, History, Sharing, and Purchasing features over 5 days, reflecting the Profile Page as the central hub.

**Assumptions:**
*   A mechanism to retrieve the current logged-in user's `userId` is available in the Android app.
*   Basic project setup for Android (Kotlin/Java, Gradle, existing navigation) is in place.
*   The `DashboardFragment` dynamically displays quiz options based on user-selected interests and allows editing of these interests.
*   Login/Signup and Interest Selection fragments have logic to handle incomplete setups, eventually directing users to the main app content.

---

## Day 1: Foundation - Data Storage (Room) & Basic History Display

**Goal:** Set up local data persistence for quiz results and display a basic history view accessible from the Profile.

1.  **Room Database Setup:**
    *   Define Room Entities:
        *   `UserEntity` (if not already existing, to store `userId`, `username`, `email`, `currentTier` - initially `currentTier` can be a default).
        *   `QuizAttemptEntity` (`id`, `userId` (foreign key to `UserEntity`), `topicName`, `timestamp`, `totalQuestions`, `correctAnswers`).
        *   `QuestionResponseEntity` (`id`, `quizAttemptId` (foreign key to `QuizAttemptEntity`), `questionText`, `options` (as String or separate table), `userAnswer`, `correctAnswer`, `wasCorrect`).
    *   Create DAOs (Data Access Objects) for each entity with necessary insert, query (e.g., get attempts by `userId`, get questions by `attemptId`, get stats by `userId`).
    *   Create the Room `AppDatabase` class.
    *   Implement Repository pattern to abstract data operations.
2.  **Integrate Quiz Saving:**
    *   Modify `ResultFragment` (or wherever quiz completion is handled) to:
        *   Gather data for `QuizAttemptEntity` and `QuestionResponseEntity`.
        *   Save this data to Room via the Repository, associated with the current `userId`.
3.  **Basic `HistoryFragment` UI & ViewModel:**
    *   Create `HistoryFragment.java` and its XML layout (`fragment_history.xml`).
    *   Create `HistoryViewModel.java`.
    *   In `HistoryViewModel`, implement a function to fetch `QuizAttemptEntity` objects for the current `userId` from Room.
    *   In `HistoryFragment`, use a RecyclerView to display a list of quiz attempts (e.g., Topic and Date).
    *   Include a button in `fragment_history.xml` placeholder: "Start New Quiz" / "Go to Dashboard".
4.  **Initial Navigation Stubs (for `ProfileFragment` access):**
    *   Mentally note: `ProfileFragment` will be the new destination after `LoginFragment` / `YourInterestsFragment`. `ProfileFragment` will then link to `HistoryFragment`.

---

## Day 2: Profile Page - UI, Stats, QR Sharing & Core Navigation

**Goal:** Create the Profile screen as the central hub, display user statistics, implement QR sharing, and set up core navigation.

1.  **`ProfileFragment` UI & ViewModel:**
    *   Create `ProfileFragment.java` and its XML layout (`fragment_profile.xml`). This is the **new main landing screen after login/initial setup.** Design should include:
        *   Top box for Username & Email.
        *   Area to display current tier.
        *   "Upgrade Account" button.
        *   Stats boxes: "Total Questions," "Correctly Answered," "Incorrect Answers."
        *   Button/clickable area for "View Quiz History" (navigating to `HistoryFragment`).
        *   "Share Profile" button.
    *   Create `ProfileViewModel.java`.
2.  **Display User Stats:**
    *   In `ProfileViewModel`, implement functions to query Room to calculate stats based on `userId`.
    *   Populate UI in `ProfileFragment`.
3.  **Implement QR Code Sharing:**
    *   Add QR code generation library (e.g., ZXing Android Embedded).
    *   On "Share Profile" button click: Gather data (Username, Email, stats), generate QR, use `Intent` to share.
4.  **Implement Core Navigation Flow:**
    *   Adjust existing navigation graphs (`nav_graph.xml` or similar):
        *   `LoginFragment` on success should navigate to `ProfileFragment`.
        *   `YourInterestsFragment` on completion (for new users or users completing setup) should navigate to `ProfileFragment`.
    *   In `ProfileFragment`, implement navigation from "View Quiz History" to `HistoryFragment`.
    *   In `HistoryFragment`, implement navigation from its "Start New Quiz" / "Go to Dashboard" button to `DashboardFragment`.
    *   Ensure `DashboardFragment`'s existing "EDIT INTERESTS" flow (to `YourInterestsFragment` and back, refreshing quizzes) remains functional.
    *   Ensure `ResultFragment` navigates back to `DashboardFragment`.

---

## Day 3: Upgrade Account Screen & Tiered Quiz Backend Logic

**Goal:** Build the UI for account upgrades and modify the backend to support tiered quiz generation based on user's tier.

1.  **`UpgradeAccountFragment` UI & ViewModel:**
    *   Create `UpgradeAccountFragment.java` and its XML layout (`fragment_upgrade_account.xml`).
    *   Design layout for tiers (Starter, Intermediate, Advanced) with descriptions and "Purchase" buttons.
    *   Create `UpgradeAccountViewModel.java`.
2.  **Navigation:**
    *   Implement navigation from `ProfileFragment`'s "Upgrade Account" button to `UpgradeAccountFragment`.
3.  **Backend Modifications (Python Flask App for `main-directModel.py`):**
    *   Modify `/getQuiz` endpoint to accept a `tier` parameter.
    *   In `fetchQuizFromLlama`, adjust quiz generation (number of questions, prompt complexity) based on the `tier`.
4.  **Android App API Call Update:**
    *   Update `QuizApi.java`, `ApiClient.java`, and the calling site (likely in `DashboardViewModel` or `QuizViewModel` when a quiz topic is selected from `DashboardFragment`).
    *   When calling `/getQuiz`, send the current user's tier (fetched from Room/`UserEntity` via `ProfileViewModel` or a shared user state).
    *   Mock/hardcode tier on client for initial backend testing if needed.

---

## Day 4: Payment Integration

**Goal:** Integrate a payment system to allow users to purchase account tiers.

1.  **Choose Payment Gateway & Setup:** (e.g., Google Play Billing)
    *   Set up product IDs (SKUs) for each tier in the payment provider's console.
2.  **Integrate Payment Library:**
    *   Add dependencies, initialize billing client.
    *   Query for SKUs.
3.  **Implement Purchase Flow in `UpgradeAccountFragment`:**
    *   Launch purchase flow on "Purchase" button click.
    *   Handle purchase results.
4.  **Verify Purchases & Grant Entitlements:**
    *   Implement secure purchase verification.
    *   Upon success, update the user's tier in Room (`UserEntity`) and refresh relevant UI in `ProfileFragment` (e.g., tier display).

---

## Day 5: Refinements, Testing & Report Preparation

**Goal:** Polish features, conduct thorough testing of the new flow, and begin compiling the report.

1.  **UI/UX Refinements:**
    *   **History Page (`HistoryFragment`):**
        *   Finalize RecyclerView layout. If showing individual questions for an attempt, consider how this is presented (e.g., tap an attempt to see details).
    *   **Profile Page (`ProfileFragment`):**
        *   Implement the clickable Username/Email box to show current tier details more explicitly if needed.
    *   Ensure consistent design, smooth transitions across all screens, respecting the new navigation flow.
    *   Implement loading states and error handling for network calls and data loading.
2.  **Thorough Testing (Focus on New Flow):**
    *   **Onboarding & Landing:** Test new user (Signup -> Interests -> Profile) and existing user (Login -> Profile) flows. Test incomplete interest setup (Login -> Interests -> Profile).
    *   **Core Navigation:** `Profile` -> `History` -> `Dashboard`. `Dashboard` -> `Quiz` -> `Result` -> `Dashboard`. `Dashboard` -> `Edit Interests` -> `Dashboard`.
    *   **Data Persistence & Segregation:** Test with multiple user accounts (if possible on a test device) to ensure history/stats are separate.
    *   **QR Code Sharing, Tiered Quizzes, Payment Flow:** As per previous plan.
3.  **Report Preparation:**
    *   Document features, adherence to modern Android practices (emphasizing Room, ViewModel, Jetpack Navigation for the new flow), and LLM improvements.

---

**Important Note:** This remains an ambitious plan. The key change is the central role of the `ProfileFragment` and the adjusted navigation path to the `DashboardFragment`. Ensure this flow is robustly implemented. Good luck!
