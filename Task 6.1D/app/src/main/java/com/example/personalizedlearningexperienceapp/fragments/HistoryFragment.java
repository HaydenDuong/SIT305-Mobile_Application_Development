package com.example.personalizedlearningexperienceapp.fragments;

import android.content.Context; // For SharedPreferences
import android.content.SharedPreferences; // For SharedPreferences
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.personalizedlearningexperienceapp.R;
import com.example.personalizedlearningexperienceapp.adapters.HistoryAdapter;
import com.example.personalizedlearningexperienceapp.viewmodels.HistoryViewModel;
import com.example.personalizedlearningexperienceapp.data.QuizAttemptEntity;


import java.util.ArrayList;

public class HistoryFragment extends Fragment implements HistoryAdapter.OnDeleteInteractionListener {

    private HistoryViewModel historyViewModel;
    private RecyclerView recyclerViewHistory;
    private HistoryAdapter historyAdapter;
    private NavController navController;
    private Button buttonGoToDashboard;
    private TextView textViewNoHistory;

    public HistoryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        historyViewModel = new ViewModelProvider(this).get(HistoryViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);

        recyclerViewHistory = view.findViewById(R.id.recyclerViewHistory);
        buttonGoToDashboard = view.findViewById(R.id.buttonGoToDashboard);
        textViewNoHistory = view.findViewById(R.id.textViewNoHistory);


        setupRecyclerView();

        // Retrieve User ID from SharedPreferences
        SharedPreferences prefs = requireActivity().getSharedPreferences(SignUpFragment.PREFS_NAME, Context.MODE_PRIVATE);
        int userIdInt = prefs.getInt(SignUpFragment.KEY_USER_ID, SignUpFragment.DEFAULT_USER_ID);
        String currentUserIdForRepo;

        if (userIdInt != SignUpFragment.DEFAULT_USER_ID) {
            currentUserIdForRepo = String.valueOf(userIdInt);
            historyViewModel.fetchQuizAttemptsForUser(currentUserIdForRepo);
            if (textViewNoHistory != null) { // If using a dedicated "loading" text
                textViewNoHistory.setText("Loading history..."); // Or use a string resource
                textViewNoHistory.setVisibility(View.VISIBLE);
            }
            recyclerViewHistory.setVisibility(View.GONE); // Hide until data is loaded
        } else {
            Log.e("HistoryFragment", "No valid user ID found from SharedPreferences. Cannot load history.");
            if (textViewNoHistory != null) {
                textViewNoHistory.setText(getString(R.string.history_login_prompt)); // Using string resource
                textViewNoHistory.setVisibility(View.VISIBLE);
            }
            recyclerViewHistory.setVisibility(View.GONE);
            currentUserIdForRepo = null; // Or handle as an error state
        }

        historyViewModel.getQuizAttemptsLiveData().observe(getViewLifecycleOwner(), attempts -> {
            if (attempts != null && !attempts.isEmpty()) {
                historyAdapter.updateData(attempts);
                recyclerViewHistory.setVisibility(View.VISIBLE);
                if (textViewNoHistory != null) textViewNoHistory.setVisibility(View.GONE);
            } else {
                // If attempts is null or empty, show appropriate message
                recyclerViewHistory.setVisibility(View.GONE);
                if (textViewNoHistory != null) {
                    // Re-check current user status to display correct message
                    SharedPreferences recheckPrefs = requireActivity().getSharedPreferences(SignUpFragment.PREFS_NAME, Context.MODE_PRIVATE);
                    int recheckUserIdInt = recheckPrefs.getInt(SignUpFragment.KEY_USER_ID, SignUpFragment.DEFAULT_USER_ID);
                    if (recheckUserIdInt == SignUpFragment.DEFAULT_USER_ID) {
                        textViewNoHistory.setText(getString(R.string.history_login_prompt));
                    } else {
                        textViewNoHistory.setText(getString(R.string.history_no_quizzes_prompt));
                    }
                    textViewNoHistory.setVisibility(View.VISIBLE);
                }
            }
        });

        buttonGoToDashboard.setOnClickListener(v -> {
            if (navController.getCurrentDestination() != null && navController.getCurrentDestination().getId() == R.id.historyFragment) {
                navController.navigate(R.id.action_historyFragment_to_dashboardFragment);
            }
        });
    }

    private void setupRecyclerView() {
        recyclerViewHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        historyAdapter = new HistoryAdapter(requireContext(), new ArrayList<>(), this);
        recyclerViewHistory.setAdapter(historyAdapter);
    }

    @Override
    public void onDeleteAttemptClicked(QuizAttemptEntity attempt) {
        if (historyViewModel != null && attempt != null) {
            historyViewModel.deleteQuizAttempt(attempt);
        }
    }
}