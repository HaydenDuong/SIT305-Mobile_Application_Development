package com.example.chatbotapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class UserProfileActivity extends AppCompatActivity {

    private CardView userInfoCard;
    private TextView textViewUsername;
    private TextView textViewEmail;
    private Button buttonChatWithAI;
    private Button buttonChatWithGroup;
    private Button buttonSignOut;
    private FrameLayout fragmentContainer;

    private String userUid;
    private String userDisplayName;
    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        userInfoCard = findViewById(R.id.userInfoCard);
        textViewUsername = findViewById(R.id.textViewUsername);
        textViewEmail = findViewById(R.id.textViewEmail);
        buttonChatWithAI = findViewById(R.id.buttonChatWithAI);
        buttonChatWithGroup = findViewById(R.id.buttonChatWithGroup);
        buttonSignOut = findViewById(R.id.buttonSignOut);
        fragmentContainer = findViewById(R.id.fragment_container); // Make sure this ID exists in your layout

        // Get user details from Intent
        userUid = getIntent().getStringExtra("USER_UID");
        userDisplayName = getIntent().getStringExtra("USER_DISPLAY_NAME");
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userEmail = currentUser.getEmail();
        }

        // Set user details
        if (userDisplayName != null && !userDisplayName.isEmpty()) {
            textViewUsername.setText("Username: " + userDisplayName);
        } else if (userEmail != null && !userEmail.isEmpty()) {
            textViewUsername.setText("Username: " + userEmail.split("@")[0]); // Fallback to part of email
        } else {
            textViewUsername.setText("Username: Not available");
        }
        textViewEmail.setText("Email: " + (userEmail != null ? userEmail : "Not available"));

        // Initially hide the fragment container and show buttons
        fragmentContainer.setVisibility(View.GONE);
        setButtonsVisibility(View.VISIBLE);

        userInfoCard.setOnClickListener(v -> {
            loadInterestsFragment();
            // Hide buttons and show fragment container
            setButtonsVisibility(View.GONE);
            fragmentContainer.setVisibility(View.VISIBLE);
        });

        buttonChatWithAI.setOnClickListener(v -> {
            Intent intent = new Intent(UserProfileActivity.this, ChatActivity.class);
            intent.putExtra("USER_UID", userUid);
            intent.putExtra("USER_DISPLAY_NAME", userDisplayName);
            startActivity(intent);
        });

        buttonChatWithGroup.setOnClickListener(v -> {
            Toast.makeText(UserProfileActivity.this, "Group chat feature coming soon!", Toast.LENGTH_SHORT).show();
        });

        buttonSignOut.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(UserProfileActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void loadInterestsFragment() {
        InterestsFragment fragment = new InterestsFragment();
        Bundle bundle = new Bundle();
        bundle.putString("USER_UID", userUid);
        fragment.setArguments(bundle);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null); // So user can press back to go to profile view
        transaction.commit();
    }

    private void setButtonsVisibility(int visibility) {
        buttonChatWithAI.setVisibility(visibility);
        buttonChatWithGroup.setVisibility(visibility);
        buttonSignOut.setVisibility(visibility);
    }

    @Override
    public void onBackPressed() {
        if (fragmentContainer.getVisibility() == View.VISIBLE) {
            // If InterestsFragment is visible, hide it and show buttons
            getSupportFragmentManager().popBackStackImmediate(); // Clear the fragment
            fragmentContainer.setVisibility(View.GONE);
            setButtonsVisibility(View.VISIBLE);
        } else {
            super.onBackPressed();
        }
    }
}