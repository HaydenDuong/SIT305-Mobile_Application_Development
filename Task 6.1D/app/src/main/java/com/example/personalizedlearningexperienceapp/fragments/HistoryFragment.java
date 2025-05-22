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

// Import SignUpFragment to access its public constants for SharedPreferences



import java.util.ArrayList;

public class HistoryFragment extends Fragment {

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);

        recyclerViewHistory = view.findViewById(R.id.recyclerViewHistory);
        buttonGoToDashboard = view.findViewById(R.id.buttonGoToDashboard);
        // Assuming textViewNoHistory ID exists in fragment_history.xml, otherwise this will be null
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


        if (currentUserIdForRepo != null) { // Only observe if we have a valid user to fetch for
            historyViewModel.getQuizAttemptsLiveData().observe(getViewLifecycleOwner(), attempts -> {
                if (attempts != null && !attempts.isEmpty()) {
                    historyAdapter.updateData(attempts);
                    recyclerViewHistory.setVisibility(View.VISIBLE);
                    if (textViewNoHistory != null) textViewNoHistory.setVisibility(View.GONE);
                } else {
                    Log.d("HistoryFragment", "No quiz attempts found or list is empty for user: " + currentUserIdForRepo);
                    recyclerViewHistory.setVisibility(View.GONE);
                    if (textViewNoHistory != null) {
                        textViewNoHistory.setText(getString(R.string.history_no_quizzes_prompt)); // Using string resource
                        textViewNoHistory.setVisibility(View.VISIBLE);
                    }
                }
            });
        }


        buttonGoToDashboard.setOnClickListener(v -> {
            if (navController.getCurrentDestination() != null && navController.getCurrentDestination().getId() == R.id.historyFragment) {
                navController.navigate(R.id.action_historyFragment_to_dashboardFragment);
            }
        });
    }

    private void setupRecyclerView() {
        recyclerViewHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        historyAdapter = new HistoryAdapter(new ArrayList<>());
        recyclerViewHistory.setAdapter(historyAdapter);
    }
}