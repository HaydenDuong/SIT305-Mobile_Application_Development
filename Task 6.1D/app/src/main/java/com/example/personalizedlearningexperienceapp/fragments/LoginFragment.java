package com.example.personalizedlearningexperienceapp.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.personalizedlearningexperienceapp.R;
import com.example.personalizedlearningexperienceapp.data.DatabaseClient;
import com.example.personalizedlearningexperienceapp.data.User;
import com.example.personalizedlearningexperienceapp.data.UserDao;
import com.example.personalizedlearningexperienceapp.data.UserTopic;
import com.example.personalizedlearningexperienceapp.data.UserTopicDao;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginFragment extends Fragment {

    private EditText etUsernameLogin, etPasswordLogin;
    private Button btnLogin;
    private TextView tvSignUpPrompt;
    private UserDao userDao;
    private UserTopicDao userTopicDao;
    private ExecutorService executorService;
    private SharedPreferences prefs;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getContext() != null) {
            Context appContext = getContext().getApplicationContext();
            userDao = DatabaseClient.getInstance(appContext).getAppDatabase().userDao();
            userTopicDao = DatabaseClient.getInstance(appContext).getAppDatabase().userTopicDao();
            prefs = appContext.getSharedPreferences(SignUpFragment.PREFS_NAME, Context.MODE_PRIVATE);
        }
        executorService = Executors.newSingleThreadExecutor();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        etUsernameLogin = view.findViewById(R.id.et_username);
        etPasswordLogin = view.findViewById(R.id.et_password);
        btnLogin = view.findViewById(R.id.btn_login);
        tvSignUpPrompt = view.findViewById(R.id.tv_sign_up);

        btnLogin.setOnClickListener(v -> attemptLogin());
        tvSignUpPrompt.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_loginFragment_to_signUpFragment);
        });

        return view;
    }

    private void attemptLogin() {
        String username = etUsernameLogin.getText().toString().trim(); // Using username as email
        String password = etPasswordLogin.getText().toString().trim();

        if (TextUtils.isEmpty(username)) { etUsernameLogin.setError("Email is required"); etUsernameLogin.requestFocus(); return; }
        if (TextUtils.isEmpty(password)) { etPasswordLogin.setError("Password is required"); etPasswordLogin.requestFocus(); return; }

        if (userDao == null && getContext() != null) {
             userDao = DatabaseClient.getInstance(getContext().getApplicationContext()).getAppDatabase().userDao();
        }
        if (userDao == null) {
            Toast.makeText(getContext(), "Database error.", Toast.LENGTH_SHORT).show();
            return;
        }

        executorService.execute(() -> {
            User user = userDao.getUserByEmail(username);

            final boolean loginSuccess;
            final int userIdToProceedWith;

            if (user != null && user.getPassword().equals(password)) {
                loginSuccess = true;
                userIdToProceedWith = user.getId();
            } else {
                loginSuccess = false;
                userIdToProceedWith = SignUpFragment.DEFAULT_USER_ID;
            }

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (loginSuccess) {
                        saveUserIdToPrefs(userIdToProceedWith);
                        Toast.makeText(getContext(), "Login Successful!", Toast.LENGTH_SHORT).show();
                        navigateToNextScreen(userIdToProceedWith);
                    } else {
                        Toast.makeText(getContext(), "Invalid email or password.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void navigateToNextScreen(int userId) {
         if (userTopicDao == null && getContext() != null) {
             userTopicDao = DatabaseClient.getInstance(getContext().getApplicationContext()).getAppDatabase().userTopicDao();
        }
        if (userTopicDao == null) {
            Toast.makeText(getContext(), "Database error checking interests.", Toast.LENGTH_SHORT).show();
            return;
        }

        executorService.execute(() -> {
            List<UserTopic> userTopics = userTopicDao.getUserTopics(userId);
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (getView() == null) return;

                    if (userTopics != null && !userTopics.isEmpty()) {
                        Navigation.findNavController(getView()).navigate(R.id.action_loginFragment_to_dashboardActivity);
                    } else {
                        Navigation.findNavController(getView()).navigate(R.id.action_loginFragment_to_yourInterestsFragment);
                    }
                });
            }
        });
    }

    private void saveUserIdToPrefs(int userId) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(SignUpFragment.KEY_USER_ID, userId);
        editor.apply();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        etUsernameLogin = null;
        etPasswordLogin = null;
        btnLogin = null;
        tvSignUpPrompt = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
