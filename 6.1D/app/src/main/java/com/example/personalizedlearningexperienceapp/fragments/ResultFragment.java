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
import androidx.recyclerview.widget.LinearLayoutManager; // Import
import androidx.recyclerview.widget.RecyclerView;       // Import

import com.example.personalizedlearningexperienceapp.R;
import com.example.personalizedlearningexperienceapp.adapters.ResultAdapter; // Import
import com.example.personalizedlearningexperienceapp.models.QuizQuestion;    // Import

import java.util.ArrayList; // Import
import java.util.List;      // Import

public class ResultFragment extends Fragment {

    public static final String ARG_SCORE = "score";
    public static final String ARG_TOTAL_QUESTIONS = "totalQuestions";
    public static final String ARG_TOPIC_NAME = "topicName";
    public static final String ARG_QUESTIONS_LIST = "questionsList";

    private TextView textViewResultPageTitle;
    private TextView textViewResultTopicName;
    private TextView textViewResultFinalScore;
    private Button buttonResultAction;
    private RecyclerView recyclerViewResults; // NEW
    private ResultAdapter resultAdapter;      // NEW
    private List<QuizQuestion> questionsList = new ArrayList<>(); // Initialize

    private NavController navController;

    public ResultFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) { 
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            if (getArguments().containsKey(ARG_QUESTIONS_LIST)) {
                // Retrieve as ArrayList<Parcelable> first
                ArrayList<Parcelable> parcelableList = getArguments().getParcelableArrayList(ARG_QUESTIONS_LIST);
                if (parcelableList != null) {
                    this.questionsList.clear();
                    // Iterate and cast each element
                    for (Parcelable p : parcelableList) {
                        if (p instanceof QuizQuestion) {
                            this.questionsList.add((QuizQuestion) p);
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
        recyclerViewResults = view.findViewById(R.id.recyclerViewResults); // Initialize RecyclerView

        // Retrieve other arguments for header
        int score = 0;
        int totalQuestions = 0;
        String topicName = "N/A";

        if (getArguments() != null) {
            score = getArguments().getInt(ARG_SCORE, 0);
            totalQuestions = getArguments().getInt(ARG_TOTAL_QUESTIONS, 0);
            topicName = getArguments().getString(ARG_TOPIC_NAME, "N/A");
        }

        textViewResultPageTitle.setText(getString(R.string.quiz_completed_title)); // You might already have this string
        textViewResultTopicName.setText(getString(R.string.quiz_results_for_topic_format, topicName)); // You might already have this string
        textViewResultFinalScore.setText(getString(R.string.your_score_format, score, totalQuestions)); // You might already have this string

        setupRecyclerView();

        buttonResultAction.setOnClickListener(v -> {
            if (navController.getCurrentDestination() != null && navController.getCurrentDestination().getId() == R.id.resultFragment) {
                navController.popBackStack(R.id.dashboardFragment, false);
            }
        });
    }

    private void setupRecyclerView() {
        if (getContext() == null) return;
        resultAdapter = new ResultAdapter(getContext(), questionsList);
        recyclerViewResults.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewResults.setAdapter(resultAdapter);
    }
}