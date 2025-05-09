package com.example.personalizedlearningexperienceapp.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.personalizedlearningexperienceapp.R;
import com.example.personalizedlearningexperienceapp.adapters.QuizQuestionAdapter;
import com.example.personalizedlearningexperienceapp.api.ApiClient;
import com.example.personalizedlearningexperienceapp.models.QuizQuestion;
import com.example.personalizedlearningexperienceapp.models.QuizResponse;
import com.example.personalizedlearningexperienceapp.fragments.ResultFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QuizFragment extends Fragment {

    private static final String TAG = "QuizFragment";
    public static final String ARG_TOPIC_NAME = "topicName";
    public static final String ARG_USER_ID = "userId"; // If needed later

    private RecyclerView recyclerViewQuizQuestions;
    private Button buttonSubmitQuiz;
    private QuizQuestionAdapter adapter;
    private List<QuizQuestion> questionsList = new ArrayList<>();
    private NavController navController;

    private String topicName;
    private int userId; // If you need to use it

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_quiz, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);

        recyclerViewQuizQuestions = view.findViewById(R.id.recyclerViewQuizQuestions);
        buttonSubmitQuiz = view.findViewById(R.id.buttonSubmitQuiz);

        setupRecyclerView();

        if (topicName != null && !topicName.isEmpty()) {
            fetchQuizQuestions(topicName);
        } else {
            Toast.makeText(getContext(), "Topic not provided!", Toast.LENGTH_SHORT).show();
            navController.popBackStack();
        }

        buttonSubmitQuiz.setOnClickListener(v -> handleSubmitQuiz());
    }

    private void setupRecyclerView() {
        adapter = new QuizQuestionAdapter(getContext(), questionsList);
        recyclerViewQuizQuestions.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewQuizQuestions.setAdapter(adapter);
    }

    private void fetchQuizQuestions(String topic) {
        // Show a progress bar or loading indicator here if you have one
        executorService.execute(() -> {
            ApiClient.getQuizApi().getQuiz(topic).enqueue(new Callback<QuizResponse>() {
                @Override
                public void onResponse(@NonNull Call<QuizResponse> call, @NonNull Response<QuizResponse> response) {
                    mainThreadHandler.post(() -> {
                        // Hide progress bar
                        if (response.isSuccessful() && response.body() != null && response.body().getQuiz() != null && !response.body().getQuiz().isEmpty()) {
                            questionsList.clear();
                            questionsList.addAll(response.body().getQuiz());
                            adapter.notifyDataSetChanged(); // Or more specific notify methods
                        } else {
                            Log.e(TAG, "Failed to fetch quiz or quiz is empty. Code: " + response.code());
                            Toast.makeText(getContext(), "Failed to load quiz for " + topic, Toast.LENGTH_LONG).show();
                            navController.popBackStack();
                        }
                    });
                }

                @Override
                public void onFailure(@NonNull Call<QuizResponse> call, @NonNull Throwable t) {
                    mainThreadHandler.post(() -> {
                        // Hide progress bar
                        Log.e(TAG, "API call failed: " + t.getMessage(), t);
                        Toast.makeText(getContext(), "Error fetching quiz: " + t.getMessage(), Toast.LENGTH_LONG).show();
                        navController.popBackStack();
                    });
                }
            });
        });
    }

    private void handleSubmitQuiz() {
        Map<Integer, String> userAnswers = adapter.getSelectedAnswers();
        if (userAnswers.size() != questionsList.size()) {
            Toast.makeText(getContext(), "Please answer all questions.", Toast.LENGTH_SHORT).show();
            return;
        }

        int score = 0;
        for (int i = 0; i < questionsList.size(); i++) {
            QuizQuestion question = questionsList.get(i);
            String selectedAnswer = userAnswers.get(i);
            if (selectedAnswer != null && selectedAnswer.equals(question.getCorrectAnswer())) {
                score++;
            }
        }

        Log.d(TAG, "Quiz submitted. Score: " + score + "/" + questionsList.size());

        // Navigate to ResultFragment
        Bundle bundle = new Bundle();
        bundle.putInt(ResultFragment.ARG_SCORE, score);
        bundle.putInt(ResultFragment.ARG_TOTAL_QUESTIONS, questionsList.size());
        bundle.putString(ResultFragment.ARG_TOPIC_NAME, topicName);

        try {
            // Ensure R.id.action_quizFragment_to_resultFragment is defined in your dashboard_nav_graph.xml
            navController.navigate(R.id.action_quizFragment_to_resultFragment, bundle);
        } catch (Exception e) {
            Log.e(TAG, "Navigation to ResultFragment failed. Is action defined in nav graph?", e);
            Toast.makeText(getContext(), "Error navigating to results.", Toast.LENGTH_LONG).show();
             if (navController.getCurrentDestination() != null && navController.getCurrentDestination().getId() == R.id.quizFragment) { // Check current destination before popping
                navController.popBackStack(R.id.dashboardFragment, false); // Fallback to dashboard
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // executorService.shutdownNow(); // Consider if you need to explicitly shut down
    }
}