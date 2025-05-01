package com.example.itubeapp.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.itubeapp.R;
import com.example.itubeapp.data.DatabaseClient;
import com.example.itubeapp.data.User;

public class SignUpFragment extends Fragment {

    // Required empty constructor for class SignUpFragment
    public SignUpFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sign_up, container, false);

        // Mapping JavaScript variables with their respected .xml elements
        EditText signUpFullName = view.findViewById(R.id.signUpFullName);
        EditText signUpUsername = view.findViewById(R.id.signUpUsername);
        EditText signUpPassword = view.findViewById(R.id.signUpPassword);
        EditText signUpConfirmPassword = view.findViewById(R.id.signUpConfirmPassword);
        Button createAccountButton = view.findViewById(R.id.createAccountButton);

        // Add event-handler to "Create account" button
        createAccountButton.setOnClickListener(v -> {
            String fullName = signUpFullName.getText().toString().trim();
            String username = signUpUsername.getText().toString().trim();
            String password = signUpPassword.getText().toString().trim();
            String confirmPassword = signUpConfirmPassword.getText().toString().trim();

            // Check if all fields are filled
            if (fullName.isEmpty() || username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(getContext(), "Please fill all fields!!!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check if the input "password" is matching with "confirm password"
            if (!password.equals(confirmPassword)) {
                Toast.makeText(getContext(), "Password and Confirm Password is not the same", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check if the input username is existed in the database
            new Thread(() -> {
                User existingUser = DatabaseClient.getInstance(getContext()).getAppDatabase().userDao().getUserByUserName(username);
                getActivity().runOnUiThread(() -> {
                    if (existingUser != null) {
                        Toast.makeText(getContext(), "This username is already taken", Toast.LENGTH_SHORT).show();
                    } else {

                        // Insert new user to Table "users"
                        User user = new User(fullName, username, password);
                        new Thread(() -> {
                            try {
                                DatabaseClient.getInstance(getContext()).getAppDatabase().userDao().insertUser(user);
                                getActivity().runOnUiThread(() -> {
                                    Toast.makeText(getContext(), "Account is created! Please login.", Toast.LENGTH_SHORT).show();
                                    // Return to LoginFragment
                                    getActivity().getSupportFragmentManager().popBackStack();
                                });
                            } catch (Exception e) {
                                getActivity().runOnUiThread(() -> {
                                    Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                            }
                        }).start();
                    }
                });
            }).start();
        });

        return view;
    }
}