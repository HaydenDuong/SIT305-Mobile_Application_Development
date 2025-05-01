package com.example.itubeapp.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.itubeapp.R;
import com.example.itubeapp.activities.HomeActivity;
import com.example.itubeapp.data.DatabaseClient;
import com.example.itubeapp.data.User;

import org.apache.commons.codec.digest.DigestUtils;

public class LoginFragment extends Fragment {

    // Required empty public constructor for class LoginFragment
    public LoginFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        // Mapping JavaScript variables with their respected .xml element
        EditText inputUsername = view.findViewById(R.id.inputUsername);
        EditText inputPassword = view.findViewById(R.id.inputPassword);
        Button loginButton = view.findViewById(R.id.loginButton);
        Button signUpButton = view.findViewById(R.id.signUpButton);

        // Add event-handler for "Login" button
        loginButton.setOnClickListener(v -> {

            // Retrieve input information from two EditText fields and store in Java variables
            String username = inputUsername.getText().toString().trim();
            String password = inputPassword.getText().toString().trim();

            // Check if both "username" and "password" fields are filled
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Hashing the input "password" for comparison with existing store data
            String hashedPassword = DigestUtils.sha256Hex(password);

            // Checking user's info
            new Thread(() -> {
                try {
                    User user = DatabaseClient.getInstance(getContext()).getAppDatabase().userDao().getUser(username, hashedPassword);
                    getActivity().runOnUiThread(() -> {
                        if (user != null) {
                            Intent intent = new Intent(getActivity(), HomeActivity.class);
                            intent.putExtra("userId", user.getId());
                            startActivity(intent);
                            getActivity().finish();
                        } else {
                            Toast.makeText(getContext(), "Invalid username or password", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (Exception e) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }
            }).start();
        });

        // Add event-handler for "Sign Up" button
        signUpButton.setOnClickListener(v -> {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new SignUpFragment())
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }
}