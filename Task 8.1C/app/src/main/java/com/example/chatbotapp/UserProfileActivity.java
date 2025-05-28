package com.example.chatbotapp;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class UserProfileActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        // Pass userId to fragment
        String userId = getIntent().getStringExtra("USER_UID");
        Bundle bundle = new Bundle();
        bundle.putString("USER_UID", userId);

        InterestsFragment fragment = new InterestsFragment();
        fragment.setArguments(bundle);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}