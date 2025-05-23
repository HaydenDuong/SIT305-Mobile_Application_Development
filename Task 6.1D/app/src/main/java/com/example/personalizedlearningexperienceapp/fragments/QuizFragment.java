package com.example.personalizedlearningexperienceapp.fragments;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import com.example.personalizedlearningexperienceapp.R;
import com.example.personalizedlearningexperienceapp.api.ApiClient;
import com.example.personalizedlearningexperienceapp.models.QuizQuestion;
import com.example.personalizedlearningexperienceapp.models.QuizResponse;
import java.util.ArrayList;
import java.util.Arrays; 
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QuizFragment extends Fragment {

    private static final String TAG = "QuizFragment";
    public static final String ARG_TOPIC_NAME = "topicName";
    public static final String ARG_USER_ID = "userId";

    // UI Elements
    private TextView textViewQuestionNumber;
    private ProgressBar progressBarQuiz;
    private TextView textViewQuestionTitle;
    private TextView textViewQuestionText;
    private List<Button> answerButtons;
    private Button buttonAnswer1, buttonAnswer2, buttonAnswer3, buttonAnswer4;
    private Button buttonQuizAction;
    private ProgressBar progressBarQuizInitialLoading;

    private NavController navController;
    private String topicName;
    private int userId;

    private List<QuizQuestion> allQuestionsList = new ArrayList<>();
    private int currentQuestionIndex = 0;
    private int score = 0;
    private int selectedAnswerIndex = -1; // -1 means no answer selected, 0-3 for button index
    private boolean isAnswerSubmitted = false; // To track if current question's answer is submitted

    // Default button tint (to reset to)
    private ColorStateList defaultButtonTintColor;
    // Highlighting tint for selected (but not yet submitted) answer
    private ColorStateList selectedButtonTintColor;


    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    public QuizFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            topicName = getArguments().getString(ARG_TOPIC_NAME);
            userId = getArguments().getInt(ARG_USER_ID, -1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_quiz, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);

        // Initialize UI elements
        textViewQuestionNumber = view.findViewById(R.id.textViewQuestionNumber);
        progressBarQuiz = view.findViewById(R.id.progressBarQuiz);
        textViewQuestionTitle = view.findViewById(R.id.textViewQuestionTitle); 
        textViewQuestionText = view.findViewById(R.id.textViewQuestionText);
        buttonQuizAction = view.findViewById(R.id.buttonQuizAction);
        progressBarQuizInitialLoading = view.findViewById(R.id.progressBarQuizInitialLoading);

        buttonAnswer1 = view.findViewById(R.id.buttonAnswer1);
        buttonAnswer2 = view.findViewById(R.id.buttonAnswer2);
        buttonAnswer3 = view.findViewById(R.id.buttonAnswer3);
        buttonAnswer4 = view.findViewById(R.id.buttonAnswer4);

        answerButtons = new ArrayList<>(Arrays.asList(buttonAnswer1, buttonAnswer2, buttonAnswer3, buttonAnswer4));

        defaultButtonTintColor = buttonAnswer1.getBackgroundTintList();
        if (getContext() != null) {
            selectedButtonTintColor = ColorStateList.valueOf(ContextCompat.getColor(getContext(), android.R.color.holo_orange_light));
        }

        // Set click listeners for answer buttons
        for (int i = 0; i < answerButtons.size(); i++) {
            final int buttonIndex = i;
            answerButtons.get(i).setOnClickListener(v -> handleAnswerButtonClick(buttonIndex));
        }

        // Click listener for the main action button (Submit/Next/Done)
        buttonQuizAction.setOnClickListener(v -> handleQuizActionButtonClick());

        // Hide all quiz content initially until questions are loaded
        setQuizElementsVisibility(View.GONE);

        if (topicName != null && !topicName.isEmpty()) {

            // Set the topic name in the question title
            if (textViewQuestionTitle != null) {
                textViewQuestionTitle.setText(topicName);
                textViewQuestionTitle.setVisibility(View.VISIBLE); // Ensure it's visible
            }

            fetchQuizQuestions(topicName);
        } else {
            Toast.makeText(getContext(), "Error: Topic not provided!", Toast.LENGTH_LONG).show();
            if (textViewQuestionTitle != null) { // Hide title if no topic
                textViewQuestionTitle.setVisibility(View.GONE);
            }
            navController.popBackStack();
        }
    }
    
    private void setQuizElementsVisibility(int visibility) {
        if (textViewQuestionNumber != null) textViewQuestionNumber.setVisibility(visibility);
        if (progressBarQuiz != null) progressBarQuiz.setVisibility(visibility);
        if (textViewQuestionText != null) textViewQuestionText.setVisibility(visibility);
        for (Button btn : answerButtons) {
            if (btn != null) btn.setVisibility(visibility);
        }
    }


    private void handleAnswerButtonClick(int buttonIndex) {
        if (isAnswerSubmitted) {
            return;
        }

        selectedAnswerIndex = buttonIndex;

        if (currentQuestionIndex < allQuestionsList.size()) {
            QuizQuestion currentQuestion = allQuestionsList.get(currentQuestionIndex);
            if (buttonIndex < currentQuestion.getOptions().size()) {
                currentQuestion.setUserSelectedAnswer(currentQuestion.getOptions().get(buttonIndex));
            }
        }

        // Reset all button tints to default
        for (Button btn : answerButtons) {
            btn.setBackgroundTintList(defaultButtonTintColor);
        }
        // Highlight the newly selected button
        if (selectedButtonTintColor != null) {
            answerButtons.get(buttonIndex).setBackgroundTintList(selectedButtonTintColor);
        } else {
            answerButtons.get(buttonIndex).setBackgroundColor(Color.LTGRAY);
        }
    }

    private void resetButtonBackgrounds() {
        for (Button btn : answerButtons) {
            btn.setBackgroundTintList(defaultButtonTintColor);
            btn.setEnabled(true);
        }
    }


    private void fetchQuizQuestions(String topic) {
        if (progressBarQuizInitialLoading != null) {
            progressBarQuizInitialLoading.setVisibility(View.VISIBLE);
        }
        setQuizElementsVisibility(View.GONE);
        if (buttonQuizAction != null) buttonQuizAction.setEnabled(false);


        executorService.execute(() -> {
            ApiClient.getQuizApi().getQuiz(topic).enqueue(new Callback<QuizResponse>() {
                @Override
                public void onResponse(@NonNull Call<QuizResponse> call, @NonNull Response<QuizResponse> response) {
                    mainThreadHandler.post(() -> {
                        if (progressBarQuizInitialLoading != null) {
                            progressBarQuizInitialLoading.setVisibility(View.GONE);
                        }
                        if (buttonQuizAction != null) buttonQuizAction.setEnabled(true);

                        if (response.isSuccessful() && response.body() != null && response.body().getQuiz() != null && !response.body().getQuiz().isEmpty()) {
                            allQuestionsList.clear();
                            allQuestionsList.addAll(response.body().getQuiz());
                            currentQuestionIndex = 0;
                            score = 0;
                            isAnswerSubmitted = false;
                            if (!allQuestionsList.isEmpty()) {
                                displayCurrentQuestion();
                                setQuizElementsVisibility(View.VISIBLE); // Show quiz content now
                            } else {
                                Toast.makeText(getContext(), "Quiz contains no questions.", Toast.LENGTH_LONG).show();
                                navController.popBackStack();
                            }
                        } else {
                            Log.e(TAG, "Failed to fetch quiz or quiz is empty. Code: " + response.code() + " Body: " + response.errorBody());
                            Toast.makeText(getContext(), "Failed to load quiz for " + topic, Toast.LENGTH_LONG).show();
                            if (navController.getCurrentDestination() != null && navController.getCurrentDestination().getId() == R.id.quizFragment) {
                                navController.popBackStack();
                            }
                        }
                    });
                }

                @Override
                public void onFailure(@NonNull Call<QuizResponse> call, @NonNull Throwable t) {
                    mainThreadHandler.post(() -> {
                        if (progressBarQuizInitialLoading != null) {
                            progressBarQuizInitialLoading.setVisibility(View.GONE);
                        }
                         if (buttonQuizAction != null) buttonQuizAction.setEnabled(true);
                        Log.e(TAG, "API call failed: " + t.getMessage(), t);
                        Toast.makeText(getContext(), "Error fetching quiz: " + t.getMessage(), Toast.LENGTH_LONG).show();
                        if (navController.getCurrentDestination() != null && navController.getCurrentDestination().getId() == R.id.quizFragment) {
                            navController.popBackStack();
                        }
                    });
                }
            });
        });
    }

    private void displayCurrentQuestion() {
        if (allQuestionsList == null || allQuestionsList.isEmpty() || currentQuestionIndex >= allQuestionsList.size()) {
            Log.e(TAG, "Attempted to display question out of bounds or with no questions.");
            if(allQuestionsList != null && !allQuestionsList.isEmpty()) { 
                navigateToResults();
            }
            return;
        }

        QuizQuestion currentQuestion = allQuestionsList.get(currentQuestionIndex);

        if (getContext() == null) return;

        textViewQuestionNumber.setText(getString(R.string.question_number_format, currentQuestionIndex + 1, allQuestionsList.size()));
        progressBarQuiz.setMax(allQuestionsList.size());
        progressBarQuiz.setProgress(currentQuestionIndex + 1);

        textViewQuestionText.setText(currentQuestion.getQuestion());

        List<String> options = currentQuestion.getOptions();
        for (int i = 0; i < answerButtons.size(); i++) {
            if (i < options.size()) {
                answerButtons.get(i).setText(options.get(i));
                answerButtons.get(i).setVisibility(View.VISIBLE);
            } else {
                answerButtons.get(i).setVisibility(View.GONE);
            }
        }

        resetButtonBackgrounds(); // Reset colors and enable buttons
        selectedAnswerIndex = -1; // Reset selection for the new question
        isAnswerSubmitted = false; // Reset submission state
        buttonQuizAction.setText(getString(R.string.submit_button_text));
        buttonQuizAction.setEnabled(true); // Ensure submit button is enabled
    }


    private void handleQuizActionButtonClick() {
        if (!isAnswerSubmitted) {
            if (selectedAnswerIndex == -1) {
                Toast.makeText(getContext(), "Please select an answer.", Toast.LENGTH_SHORT).show();
                return;
            }

            QuizQuestion currentQuestion = allQuestionsList.get(currentQuestionIndex);
            String correctAnswerLetter = currentQuestion.getCorrectAnswer().trim().toUpperCase();

            int correctAnswerActualIndex = -1; 
            switch (correctAnswerLetter) {
                case "A": correctAnswerActualIndex = 0; break;
                case "B": correctAnswerActualIndex = 1; break;
                case "C": correctAnswerActualIndex = 2; break;
                case "D": correctAnswerActualIndex = 3; break;
                default:
                    Log.e(TAG, "Invalid correct answer letter from API: " + correctAnswerLetter);
                    break;
            }

            if (getContext() == null) return;

            if (selectedAnswerIndex == correctAnswerActualIndex) {
                score++;
                answerButtons.get(selectedAnswerIndex).setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), android.R.color.holo_green_light)));
            } else {
                // User was wrong
                answerButtons.get(selectedAnswerIndex).setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), android.R.color.holo_red_light)));
                // Highlight the actual correct answer button if the letter was valid and index is in bounds
                if (correctAnswerActualIndex != -1 && correctAnswerActualIndex < answerButtons.size()) {
                    answerButtons.get(correctAnswerActualIndex).setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), android.R.color.holo_green_light)));
                } else {
                     Log.e(TAG, "Could not highlight correct answer button because correct index was invalid. Index: " + correctAnswerActualIndex + ", Letter: " + correctAnswerLetter);
                }
            }

            // All the following logic belongs to the 'Submit' action, after evaluating the answer
            isAnswerSubmitted = true;
            for (Button btn : answerButtons) {
                btn.setEnabled(false); // Disable answer buttons after submission
            }

            if (currentQuestionIndex < allQuestionsList.size() - 1) {
                buttonQuizAction.setText(getString(R.string.next_button_text));
            } else {
                buttonQuizAction.setText(getString(R.string.done_button_text));
            }

        } else {
            currentQuestionIndex++;
            if (currentQuestionIndex < allQuestionsList.size()) {
                displayCurrentQuestion();
            } else {
                navigateToResults();
            }
        }
    }
    
    private void navigateToResults() {
        if (getContext() == null || navController == null) return;

        Bundle bundle = new Bundle();
        bundle.putInt(ResultFragment.ARG_SCORE, score);
        bundle.putInt(ResultFragment.ARG_TOTAL_QUESTIONS, allQuestionsList.size());
        bundle.putString(ResultFragment.ARG_TOPIC_NAME, topicName);

        if (allQuestionsList instanceof ArrayList) {
            bundle.putParcelableArrayList(ResultFragment.ARG_QUESTIONS_LIST, (ArrayList<? extends Parcelable>) allQuestionsList);
        } else {
            bundle.putParcelableArrayList(ResultFragment.ARG_QUESTIONS_LIST, new ArrayList<Parcelable>(allQuestionsList));
        }

        try {
            if (navController.getCurrentDestination() != null && navController.getCurrentDestination().getId() == R.id.quizFragment) {
                 navController.navigate(R.id.action_quizFragment_to_resultFragment, bundle);
            }
        } catch (Exception e) {
            Log.e(TAG, "Navigation to ResultFragment failed.", e);
            Toast.makeText(getContext(), "Error navigating to results.", Toast.LENGTH_LONG).show();
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();

        textViewQuestionNumber = null;
        progressBarQuiz = null;
        textViewQuestionTitle = null;
        textViewQuestionText = null;
        buttonAnswer1 = null; buttonAnswer2 = null; buttonAnswer3 = null; buttonAnswer4 = null;
        if (answerButtons != null) answerButtons.clear();
        buttonQuizAction = null;
        progressBarQuizInitialLoading = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdownNow();
        }
    }
}