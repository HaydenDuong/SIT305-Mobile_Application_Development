package com.example.personalizedlearningexperienceapp.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.personalizedlearningexperienceapp.R;
import com.example.personalizedlearningexperienceapp.adapters.DashboardTopicAdapter;
import com.example.personalizedlearningexperienceapp.data.DatabaseClient;
import com.example.personalizedlearningexperienceapp.data.User;
import com.example.personalizedlearningexperienceapp.data.UserDao;
import com.example.personalizedlearningexperienceapp.data.UserTopic;
import com.example.personalizedlearningexperienceapp.data.UserTopicDao;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DashboardFragment extends Fragment implements DashboardTopicAdapter.OnTopicClickListener {

    private TextView tvWelcomeMessage, tvTaskCount;
    private Button btnEditInterests;
    private RecyclerView rvDashboardTopics;
    private ProgressBar pbLoading;
    private DashboardTopicAdapter adapter;

    private UserDao userDao;
    private UserTopicDao userTopicDao;
    private ExecutorService executorService;
    private SharedPreferences prefs;
    private int currentUserId = SignUpFragment.DEFAULT_USER_ID;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getContext() != null) {
            Context appContext = getContext().getApplicationContext();
            userDao = DatabaseClient.getInstance(appContext).getAppDatabase().userDao();
            userTopicDao = DatabaseClient.getInstance(appContext).getAppDatabase().userTopicDao();
            prefs = appContext.getSharedPreferences(SignUpFragment.PREFS_NAME, Context.MODE_PRIVATE);
            currentUserId = prefs.getInt(SignUpFragment.KEY_USER_ID, SignUpFragment.DEFAULT_USER_ID);
        }
        executorService = Executors.newSingleThreadExecutor();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        tvWelcomeMessage = view.findViewById(R.id.tv_welcome_message);
        tvTaskCount = view.findViewById(R.id.tv_task_count);
        btnEditInterests = view.findViewById(R.id.btn_edit_interests);
        rvDashboardTopics = view.findViewById(R.id.rv_dashboard_topics);
        pbLoading = view.findViewById(R.id.pb_dashboard_loading);

        setupRecyclerView();

        btnEditInterests.setOnClickListener(v -> {
            if (getView() != null) {
                try {
                    Bundle args = new Bundle();
                    args.putBoolean("isEditingMode", true);

                    Navigation.findNavController(getView()).navigate(R.id.action_dashboardFragment_to_editInterests);
                } catch (IllegalArgumentException e) {
                    Toast.makeText(getContext(), "Navigation Error: Cannot edit interests now.", Toast.LENGTH_SHORT).show();
                    Log.e("DashboardFragment", "Navigation action to YourInterestsFragment not found", e);
                }
            }
        });

        loadDashboardData();

        return view;
    }

    private void setupRecyclerView() {
        adapter = new DashboardTopicAdapter(this);
        rvDashboardTopics.setLayoutManager(new LinearLayoutManager(getContext()));
        rvDashboardTopics.setAdapter(adapter);
    }

    private void loadDashboardData() {
        pbLoading.setVisibility(View.VISIBLE);

        if (currentUserId == SignUpFragment.DEFAULT_USER_ID) {
            pbLoading.setVisibility(View.GONE);
            Toast.makeText(getContext(), "Error: Could not identify user.", Toast.LENGTH_LONG).show();
            return;
        }

        if (userDao == null || userTopicDao == null) {
            if (getContext() != null) {
                Context appContext = getContext().getApplicationContext();
                userDao = DatabaseClient.getInstance(appContext).getAppDatabase().userDao();
                userTopicDao = DatabaseClient.getInstance(appContext).getAppDatabase().userTopicDao();
            }
            if (userDao == null || userTopicDao == null) {
                pbLoading.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Database error.", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        executorService.execute(() -> {
            User user = userDao.getUserById(currentUserId);
            List<UserTopic> topics = userTopicDao.getUserTopics(currentUserId);

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    pbLoading.setVisibility(View.GONE);

                    if (user != null) {
                        tvWelcomeMessage.setText("Hello, " + user.getUsername() + "!");
                    } else {
                        tvWelcomeMessage.setText("Hello!");
                    }

                    int topicCount = (topics != null) ? topics.size() : 0;
                    if(isAdded()) {
                       tvTaskCount.setText(getResources().getQuantityString(R.plurals.topics_to_explore_count, topicCount, topicCount));
                    } else {
                       tvTaskCount.setText("Topics available: " + topicCount);
                    }

                    if (topics != null) {
                        adapter.setTopics(topics);
                    }
                });
            }
        });
    }

    @Override
    public void onTopicClick(UserTopic topic) {
        if (getView() != null && topic != null) {
            try {
                Bundle args = new Bundle();
                args.putString(QuizFragment.ARG_TOPIC_NAME, topic.getTopic());

                Navigation.findNavController(getView()).navigate(R.id.action_dashboardFragment_to_quizFragment, args);

            } catch(IllegalArgumentException e) {
                 Toast.makeText(getContext(), "Navigation Error: Cannot start quiz now.", Toast.LENGTH_SHORT).show();
                 Log.e("DashboardFragment", "Navigation action/argument issue for QuizFragment", e);
            }
        }
    }
        
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        tvWelcomeMessage = null; tvTaskCount = null; btnEditInterests = null;
        rvDashboardTopics = null; pbLoading = null; adapter = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}