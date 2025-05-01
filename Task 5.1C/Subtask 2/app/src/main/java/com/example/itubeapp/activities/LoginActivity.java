package com.example.itubeapp.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.itubeapp.R;
import com.example.itubeapp.fragment.LoginFragment;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Display LoginFragment on default
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new LoginFragment())
                    .commit();
        };
    }
}