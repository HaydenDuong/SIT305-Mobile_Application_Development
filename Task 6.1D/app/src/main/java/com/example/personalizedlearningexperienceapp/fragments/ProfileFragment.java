package com.example.personalizedlearningexperienceapp.fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.example.personalizedlearningexperienceapp.R;
import com.example.personalizedlearningexperienceapp.viewmodels.ProfileViewModel;


public class ProfileFragment extends Fragment {

    private ProfileViewModel profileViewModel;
    private NavController navController;
    private TextView textViewUsername, textViewEmail, textViewAccountTier;
    private TextView textViewTotalQuestions, textViewCorrectAnswers, textViewIncorrectAnswers;
    private TextView textViewTotalQuestionsLabel;
    private Button buttonUpgradeAccount, buttonShareProfile;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);

        // Initialize UI elements
        textViewUsername = view.findViewById(R.id.textViewUsername);
        textViewEmail = view.findViewById(R.id.textViewEmail);
        textViewAccountTier = view.findViewById(R.id.textViewAccountTier);
        textViewTotalQuestionsLabel = view.findViewById(R.id.textViewTotalQuestionsLabel);
        textViewTotalQuestions = view.findViewById(R.id.textViewTotalQuestions);
        textViewCorrectAnswers = view.findViewById(R.id.textViewCorrectAnswers);
        textViewIncorrectAnswers = view.findViewById(R.id.textViewIncorrectAnswers);
        buttonUpgradeAccount = view.findViewById(R.id.buttonUpgradeAccount);
        buttonShareProfile = view.findViewById(R.id.buttonShareProfile);

        // Set up click listeners
        textViewTotalQuestionsLabel.setOnClickListener(v -> {
            if (navController.getCurrentDestination() != null && navController.getCurrentDestination().getId() == R.id.profileFragment) {
                navController.navigate(R.id.action_profileFragment_to_historyFragment);
            }
        });

        buttonUpgradeAccount.setOnClickListener(v -> {
            if (navController.getCurrentDestination() != null && navController.getCurrentDestination().getId() == R.id.profileFragment) {
                navController.navigate(R.id.action_profileFragment_to_upgradeAccountFragment);
            }
        });

        buttonShareProfile.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Share Profile (QR Code) clicked - To be implemented", Toast.LENGTH_SHORT).show();
        });

        // Call loadProfileData() from ViewModel to fetch data
        profileViewModel.loadProfileData(); // <<< --- ADD THIS LINE

        observeViewModel();
    }

    private void observeViewModel() {
        // Observe LiveData from ProfileViewModel
        profileViewModel.getUserLiveData().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                textViewUsername.setText(user.getUsername());
                textViewEmail.setText(user.getEmail());
            } else {
                textViewUsername.setText("N/A");
                textViewEmail.setText("N/A");
            }
        });

        profileViewModel.getAccountTierLiveData().observe(getViewLifecycleOwner(), tier -> {
            textViewAccountTier.setText(tier != null ? tier : "N/A");
        });

        profileViewModel.getTotalQuestionsAnsweredLiveData().observe(getViewLifecycleOwner(), total -> {
            textViewTotalQuestions.setText(total != null ? String.valueOf(total) : "0");
        });

        profileViewModel.getCorrectAnswersLiveData().observe(getViewLifecycleOwner(), correct -> {
            textViewCorrectAnswers.setText(correct != null ? String.valueOf(correct) : "0");
        });

        profileViewModel.getIncorrectAnswersLiveData().observe(getViewLifecycleOwner(), incorrect -> {
            textViewIncorrectAnswers.setText(incorrect != null ? String.valueOf(incorrect) : "0");
        });
    }
}