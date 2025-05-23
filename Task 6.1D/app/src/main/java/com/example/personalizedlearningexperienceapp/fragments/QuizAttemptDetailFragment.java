package com.example.personalizedlearningexperienceapp.fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.example.personalizedlearningexperienceapp.R;
import com.example.personalizedlearningexperienceapp.adapters.QuizAttemptDetailAdapter;
import com.example.personalizedlearningexperienceapp.viewmodels.QuizAttemptDetailViewModel;
import com.google.android.material.appbar.MaterialToolbar;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class QuizAttemptDetailFragment extends Fragment {

    private QuizAttemptDetailViewModel viewModel;
    private QuizAttemptDetailAdapter adapter;
    private RecyclerView recyclerViewDetails;
    private TextView textViewTopicName, textViewDate, textViewScore;
    private MaterialToolbar toolbar;
    private NavController navController;
    public static final String ARG_QUIZ_ATTEMPT_ID = "quiz_attempt_id";
    private int quizAttemptId = -1;

    public QuizAttemptDetailFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            quizAttemptId = getArguments().getInt(ARG_QUIZ_ATTEMPT_ID, -1);
        }
        viewModel = new ViewModelProvider(this).get(QuizAttemptDetailViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_quiz_attempt_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);

        toolbar = view.findViewById(R.id.toolbarQuizDetail);
        textViewTopicName = view.findViewById(R.id.textViewDetailTopicName);
        textViewDate = view.findViewById(R.id.textViewDetailDate);
        textViewScore = view.findViewById(R.id.textViewDetailScore);
        recyclerViewDetails = view.findViewById(R.id.recyclerViewQuizAttemptDetails);

        setupToolbar();
        setupRecyclerView();

        if (quizAttemptId != -1) {
            viewModel.loadQuizAttemptDetails(quizAttemptId);
        } else {
            textViewTopicName.setText("Error: Quiz ID not found.");
        }

        observeViewModel();
    }

    private void setupToolbar() {
        toolbar.setNavigationOnClickListener(v -> navController.navigateUp());
    }

    private void setupRecyclerView() {
        adapter = new QuizAttemptDetailAdapter(requireContext(), new ArrayList<>());
        recyclerViewDetails.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewDetails.setAdapter(adapter);
        recyclerViewDetails.setNestedScrollingEnabled(false);
    }

    private void observeViewModel() {
        viewModel.getQuizAttempt().observe(getViewLifecycleOwner(), attempt -> {
            if (attempt != null) {
                textViewTopicName.setText(getString(R.string.detail_topic_format, attempt.topicName));
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                textViewDate.setText(getString(R.string.detail_date_format, sdf.format(new Date(attempt.timestamp))));
                textViewScore.setText(getString(R.string.detail_score_format, attempt.correctAnswers, attempt.totalQuestions));
                toolbar.setTitle(getString(R.string.detail_toolbar_title_format, attempt.topicName));
            } else if (quizAttemptId != -1) {
                textViewTopicName.setText(R.string.quiz_attempt_not_found);
            }
        });

        viewModel.getQuestionResponses().observe(getViewLifecycleOwner(), responses -> {
            if (responses != null) {
                adapter.updateData(responses);
            }
        });
    }
}
