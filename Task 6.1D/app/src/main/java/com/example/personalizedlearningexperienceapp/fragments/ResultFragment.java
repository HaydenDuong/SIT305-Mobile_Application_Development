package com.example.personalizedlearningexperienceapp.fragments;

import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.personalizedlearningexperienceapp.R;
import com.example.personalizedlearningexperienceapp.adapters.ResultAdapter;
import com.example.personalizedlearningexperienceapp.models.QuizQuestion; // Your model from the network/LLM

// Room Database imports
import com.example.personalizedlearningexperienceapp.data.QuizAttemptEntity;
import com.example.personalizedlearningexperienceapp.data.QuestionResponseEntity;
import com.example.personalizedlearningexperienceapp.data.QuizRepository;
// Placeholder for your session/user ID management
// import com.example.personalizedlearningexperienceapp.utils.SessionManager;

import com.google.gson.Gson; // For serializing options list

import java.util.ArrayList;
import java.util.List;

// Import SignUpFragment to access its public constants for SharedPreferences
import com.example.personalizedlearningexperienceapp.fragments.SignUpFragment;
import android.content.Context; // For SharedPreferences
import android.content.SharedPreferences; // For SharedPreferences

public class ResultFragment extends Fragment {

    public static final String ARG_SCORE = "score";
    public static final String ARG_TOTAL_QUESTIONS = "totalQuestions";
    public static final String ARG_TOPIC_NAME = "topicName";
    public static final String ARG_QUESTIONS_LIST = "questionsList";
    // It seems user's answers are not directly passed. We'll infer them if QuizQuestion holds selected answer.
    // Or, this data needs to be passed to ResultFragment if it's not part of QuizQuestion model.
    // For now, I'll assume QuizQuestion might have a field like `userSelectedAnswer` or similar that was set during QuizFragment.
    // If not, this logic needs adjustment based on how QuizFragment passes answers.

    private TextView textViewResultPageTitle;
    private TextView textViewResultTopicName;
    private TextView textViewResultFinalScore;
    private Button buttonResultAction;
    private RecyclerView recyclerViewResults;
    private ResultAdapter resultAdapter;
    private List<QuizQuestion> questionsListFromBundle = new ArrayList<>(); // Renamed to avoid confusion

    private NavController navController;
    private QuizRepository quizRepository; // Added
    private Gson gson = new Gson(); // For serializing options list to JSON string

    public ResultFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        quizRepository = new QuizRepository(requireActivity().getApplication()); // Initialize repository

        if (getArguments() != null) {
            if (getArguments().containsKey(ARG_QUESTIONS_LIST)) {
                ArrayList<Parcelable> parcelableList = getArguments().getParcelableArrayList(ARG_QUESTIONS_LIST);
                if (parcelableList != null) {
                    this.questionsListFromBundle.clear();
                    for (Parcelable p : parcelableList) {
                        if (p instanceof QuizQuestion) {
                            this.questionsListFromBundle.add((QuizQuestion) p);
                        }
                    }
                } else {
                    Log.e("ResultFragment", "Retrieved questions list is null from bundle.");
                }
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_result, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);

        textViewResultPageTitle = view.findViewById(R.id.textViewResultPageTitle);
        textViewResultTopicName = view.findViewById(R.id.textViewResultTopicName);
        textViewResultFinalScore = view.findViewById(R.id.textViewResultFinalScore);
        buttonResultAction = view.findViewById(R.id.buttonResultAction);
        recyclerViewResults = view.findViewById(R.id.recyclerViewResults);

        int score = 0;
        int totalQuestions = 0;
        String topicName = "N/A";

        if (getArguments() != null) {
            score = getArguments().getInt(ARG_SCORE, 0);
            totalQuestions = getArguments().getInt(ARG_TOTAL_QUESTIONS, 0);
            topicName = getArguments().getString(ARG_TOPIC_NAME, "N/A");
        }

        textViewResultPageTitle.setText(getString(R.string.quiz_completed_title));
        textViewResultTopicName.setText(getString(R.string.quiz_results_for_topic_format, topicName));
        textViewResultFinalScore.setText(getString(R.string.your_score_format, score, totalQuestions));

        setupRecyclerView();
        saveQuizResultsToDatabase(score, totalQuestions, topicName);

        buttonResultAction.setOnClickListener(v -> {
            if (navController.getCurrentDestination() != null && navController.getCurrentDestination().getId() == R.id.resultFragment) {
                navController.popBackStack(R.id.dashboardFragment, false);
            }
        });
    }

    private void setupRecyclerView() {
        if (getContext() == null || questionsListFromBundle.isEmpty()) return;
        resultAdapter = new ResultAdapter(getContext(), questionsListFromBundle);
        recyclerViewResults.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewResults.setAdapter(resultAdapter);
    }

    private void saveQuizResultsToDatabase(int score, int totalQuestionsBundle, String topicName) {
        // Get user ID from SharedPreferences
        SharedPreferences prefs = requireActivity().getSharedPreferences(SignUpFragment.PREFS_NAME, Context.MODE_PRIVATE);
        int currentUserIdInt = prefs.getInt(SignUpFragment.KEY_USER_ID, SignUpFragment.DEFAULT_USER_ID);

        if (currentUserIdInt == SignUpFragment.DEFAULT_USER_ID) { // Check if actual user ID is valid
             Log.e("ResultFragment", "Invalid User ID from SharedPreferences. Cannot save results.");
             return;
        }

        if (questionsListFromBundle.isEmpty()) {
            Log.e("ResultFragment", "Questions list is empty. Cannot save results.");
            return;
        }

        QuizAttemptEntity attempt = new QuizAttemptEntity(
                currentUserIdInt, // Use the retrieved int ID
                topicName,
                System.currentTimeMillis(),
                questionsListFromBundle.size(),
                score
        );

        List<QuestionResponseEntity> responseEntities = new ArrayList<>();
        for (QuizQuestion question : questionsListFromBundle) {
            String userAnswer = question.getUserSelectedAnswer(); // What user selected
            if (userAnswer == null) userAnswer = ""; // Ensure userAnswer is not null

            String optionsJson = gson.toJson(question.getOptions()); // Serialize options list

            // Debug logging to understand the values
            String correctAnswer = question.getCorrectAnswer();
            Log.d("ResultFragment", "Q: " + question.getQuestion());
            Log.d("ResultFragment", "User Answer: '" + userAnswer + "'");
            Log.d("ResultFragment", "Correct Answer: '" + correctAnswer + "'");

            // Determine if correct - Improved comparison
            boolean isCorrect = false;

            // First, try direct comparison (after trimming and ignoring case)
            if (userAnswer.trim().equalsIgnoreCase(correctAnswer.trim())) {
                isCorrect = true;
            }

            // If that fails, check if correctAnswer is a letter (A, B, C, D) and compare with options
            else if (correctAnswer.trim().matches("[A-Da-d]")) {
                // Get the index (0 for A, 1 for B, etc.)
                int index = Character.toUpperCase(correctAnswer.trim().charAt(0)) - 'A';

                // If valid index and we have options
                List<String> options = question.getOptions();
                if (index >= 0 && index < options.size()) {
                    // Compare user's answer with the text of the correct option
                    String correctOptionText = options.get(index);
                    isCorrect = userAnswer.trim().equalsIgnoreCase(correctOptionText.trim());
                    Log.d("ResultFragment", "Letter comparison - Correct option text: '" + correctOptionText + "', isCorrect: " + isCorrect);
                }
            }

            Log.d("ResultFragment", "Is answer correct? " + isCorrect);

            responseEntities.add(new QuestionResponseEntity(
                    0, // quizAttemptId will be set by Repository after attempt is inserted
                    question.getQuestion(),
                    optionsJson, // Save options as JSON string
                    userAnswer,
                    correctAnswer,
                    isCorrect // Using our improved comparison
            ));
        }

        quizRepository.insertQuizAttemptWithResponses(attempt, responseEntities);
        Log.d("ResultFragment", "Quiz results saved to database for user: " + currentUserIdInt);
    }
}