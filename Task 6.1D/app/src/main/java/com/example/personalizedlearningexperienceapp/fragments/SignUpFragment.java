package com.example.personalizedlearningexperienceapp.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.example.personalizedlearningexperienceapp.R;
import com.example.personalizedlearningexperienceapp.data.DatabaseClient;
import com.example.personalizedlearningexperienceapp.data.User;
import com.example.personalizedlearningexperienceapp.data.UserDao;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SignUpFragment extends Fragment {

    private EditText etUsername, etEmail, etConfirmEmail, etPassword, etConfirmPassword, etPhoneNumber;
    private Button btnSignUp;
    private UserDao userDao;
    private ExecutorService executorService;

    public static final String PREFS_NAME = "UserPrefs";
    public static final String KEY_USER_ID = "userId";
    public static final int DEFAULT_USER_ID = -1; // Represents no user logged in

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getContext() != null) {
            userDao = DatabaseClient.getInstance(getContext().getApplicationContext()).getAppDatabase().userDao();
        }
        executorService = Executors.newSingleThreadExecutor();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sign_up, container, false);

        etUsername = view.findViewById(R.id.et_username);
        etEmail = view.findViewById(R.id.et_email);
        etConfirmEmail = view.findViewById(R.id.et_confirm_email);
        etPassword = view.findViewById(R.id.et_password);
        etConfirmPassword = view.findViewById(R.id.et_confirm_password);
        etPhoneNumber = view.findViewById(R.id.et_phone_number);
        btnSignUp = view.findViewById(R.id.btn_sign_up);
        btnSignUp.setOnClickListener(v -> registerUser());

        return view;
    }

    private void registerUser() {
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String confirmEmail = etConfirmEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        String phoneNumber = etPhoneNumber.getText().toString().trim();

        if (TextUtils.isEmpty(username)) { etUsername.setError("Username is required"); etUsername.requestFocus(); return; }
        if (TextUtils.isEmpty(email)) { etEmail.setError("Email is required"); etEmail.requestFocus(); return; }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) { etEmail.setError("Enter a valid email"); etEmail.requestFocus(); return; }
        if (TextUtils.isEmpty(confirmEmail)) { etConfirmEmail.setError("Confirm email is required"); etConfirmEmail.requestFocus(); return; }
        if (!email.equals(confirmEmail)) { etConfirmEmail.setError("Emails do not match"); etConfirmEmail.requestFocus(); return; }
        if (TextUtils.isEmpty(password)) { etPassword.setError("Password is required"); etPassword.requestFocus(); return; }
        if (TextUtils.isEmpty(confirmPassword)) { etConfirmPassword.setError("Confirm password is required"); etConfirmPassword.requestFocus(); return; }
        if (!password.equals(confirmPassword)) { etConfirmPassword.setError("Passwords do not match"); etConfirmPassword.requestFocus(); return; }
        if (TextUtils.isEmpty(phoneNumber)) { etPhoneNumber.setError("Phone number is required"); etPhoneNumber.requestFocus(); return; }

        if (userDao == null && getContext() != null) {
             userDao = DatabaseClient.getInstance(getContext().getApplicationContext()).getAppDatabase().userDao();
        }
        if (userDao == null) {
            Toast.makeText(getContext(), "Database error.", Toast.LENGTH_SHORT).show();
            return;
        }

        executorService.execute(() -> {
            User existingUser = userDao.getUserByEmail(email);
            if (existingUser != null) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        etEmail.setError("Email already registered");
                        etEmail.requestFocus();
                        Toast.makeText(getContext(), "This email is already registered.", Toast.LENGTH_SHORT).show();
                    });
                }
                return;
            }

            User newUser = new User(username, email, password, phoneNumber);
            long newUserIdLong = userDao.insertUser(newUser);

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (newUserIdLong > 0) {
                        int newUserId = (int) newUserIdLong;
                        saveUserIdToPrefs(newUserId);
                        Toast.makeText(getContext(), "Registration successful!", Toast.LENGTH_SHORT).show();
                        if (getView() != null) {
                            Navigation.findNavController(getView()).navigate(R.id.action_signUpFragment_to_yourInterestsFragment);
                        }
                    } else {
                        Toast.makeText(getContext(), "Registration failed.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void saveUserIdToPrefs(int userId) {
        if (getContext() == null) return;
        SharedPreferences prefs = getContext().getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_USER_ID, userId);
        editor.apply();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        etUsername = null; etEmail = null; etConfirmEmail = null; etPassword = null;
        etConfirmPassword = null; etPhoneNumber = null; btnSignUp = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
