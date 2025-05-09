package com.example.personalizedlearningexperienceapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment; // Make sure this import is androidx.fragment.app.Fragment
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.personalizedlearningexperienceapp.R;

public class ResultFragment extends Fragment { // Extend Fragment

    // These constants are needed by QuizFragment
    public static final String ARG_SCORE = "score";
    public static final String ARG_TOTAL_QUESTIONS = "totalQuestions";
    public static final String ARG_TOPIC_NAME = "topicName";

    private TextView textViewResultTitle;
    private TextView textViewResultTopic;
    private TextView textViewResultScore;
    private Button buttonBackToDashboard;

    private NavController navController;

    public ResultFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_result, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);

        textViewResultTitle = view.findViewById(R.id.textViewResultTitle);
        textViewResultTopic = view.findViewById(R.id.textViewResultTopic);
        textViewResultScore = view.findViewById(R.id.textViewResultScore);
        buttonBackToDashboard = view.findViewById(R.id.buttonBackToDashboard);

        if (getArguments() != null) {
            int score = getArguments().getInt(ARG_SCORE, 0);
            int totalQuestions = getArguments().getInt(ARG_TOTAL_QUESTIONS, 0);
            String topicName = getArguments().getString(ARG_TOPIC_NAME, "N/A");

            // Ensure you have these strings in strings.xml
            textViewResultTitle.setText(getString(R.string.quiz_completed_title));
            textViewResultTopic.setText(String.format(getString(R.string.quiz_results_for_topic_format), topicName));
            textViewResultScore.setText(String.format(getString(R.string.your_score_format), score, totalQuestions));
        }

        buttonBackToDashboard.setOnClickListener(v -> {
            if (navController.getCurrentDestination() != null && navController.getCurrentDestination().getId() == R.id.resultFragment) {
                 navController.popBackStack(R.id.dashboardFragment, false); // Pop back to Dashboard
            }
        });
    }
}
