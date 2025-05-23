package com.example.personalizedlearningexperienceapp.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ToggleButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.personalizedlearningexperienceapp.R;
import com.example.personalizedlearningexperienceapp.data.DatabaseClient;
import com.example.personalizedlearningexperienceapp.data.UserTopic;
import com.example.personalizedlearningexperienceapp.data.UserTopicDao;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class YourInterestsFragment extends Fragment {

    private RecyclerView rvTopics;
    private Button btnNext;
    private List<String> allTopics = new ArrayList<>(Arrays.asList(
            "Mathematics", "Literature", "History", "Science", "Algorithms",
            "Data Structures", "Web Development", "Mobile Development", "Testing", "AI & ML"
    )); // Example topics
    private List<String> selectedTopicsList = new ArrayList<>();
    private TopicAdapter topicAdapter;
    private UserTopicDao userTopicDao;
    private ExecutorService executorService;
    private int currentUserId = SignUpFragment.DEFAULT_USER_ID;
    private boolean isEditingMode = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            isEditingMode = getArguments().getBoolean("isEditingMode", false);
        }

        if (getContext() != null) {
            userTopicDao = DatabaseClient.getInstance(getContext().getApplicationContext()).getAppDatabase().userTopicDao();
            SharedPreferences prefs = getContext().getApplicationContext().getSharedPreferences(SignUpFragment.PREFS_NAME, Context.MODE_PRIVATE);
            currentUserId = prefs.getInt(SignUpFragment.KEY_USER_ID, SignUpFragment.DEFAULT_USER_ID);
        }
        executorService = Executors.newSingleThreadExecutor();
        selectedTopicsList = new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_your_interests, container, false);

        rvTopics = view.findViewById(R.id.rv_topics);
        btnNext = view.findViewById(R.id.btn_next);

        if (currentUserId == SignUpFragment.DEFAULT_USER_ID) {
            Toast.makeText(getContext(), "Error: User not identified. Please sign up again.", Toast.LENGTH_LONG).show();
            btnNext.setEnabled(false);
            return view;
        }

        rvTopics.setLayoutManager(new GridLayoutManager(getContext(), 2));
        topicAdapter = new TopicAdapter(allTopics, selectedTopicsList);
        rvTopics.setAdapter(topicAdapter);

        loadUserInterests();

        btnNext.setOnClickListener(v -> saveInterestsAndProceed());

        return view;
    }

    private void loadUserInterests() {
        if (currentUserId == SignUpFragment.DEFAULT_USER_ID || userTopicDao == null) {
            return; // Cannot load if no user or DAO
        }
        executorService.execute(() -> {
            List<UserTopic> previouslySelected = userTopicDao.getUserTopics(currentUserId);
            List<String> previouslySelectedNames = new ArrayList<>();
            for (UserTopic ut : previouslySelected) {
                previouslySelectedNames.add(ut.getTopic());
            }

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    selectedTopicsList.clear();
                    selectedTopicsList.addAll(previouslySelectedNames);
                    if (topicAdapter != null) {
                        topicAdapter.notifyDataSetChanged();
                    }
                });
            }
        });
    }
        
    
    private void saveInterestsAndProceed() {
        if (selectedTopicsList.isEmpty()) {
            Toast.makeText(getContext(), "Please select at least one interest.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (currentUserId == SignUpFragment.DEFAULT_USER_ID) {
             Toast.makeText(getContext(), "Cannot save interests: User not identified.", Toast.LENGTH_LONG).show();
             return;
        }
        if (userTopicDao == null && getContext() != null) {
             userTopicDao = DatabaseClient.getInstance(getContext().getApplicationContext()).getAppDatabase().userTopicDao();
        }
        if (userTopicDao == null) {
            Toast.makeText(getContext(), "Database error.", Toast.LENGTH_SHORT).show();
            return;
        }

        executorService.execute(() -> {
            userTopicDao.deleteUserTopics(currentUserId);

            for (String topic : selectedTopicsList) {
                UserTopic userTopic = new UserTopic(currentUserId, topic);
                userTopicDao.insertUserTopic(userTopic);
            }

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), isEditingMode ? "Interests updated!" : "Interests saved!", Toast.LENGTH_SHORT).show();
                    if (getView() != null) {
                        NavController navController = Navigation.findNavController(getView());
                        
                        if (isEditingMode) {
                            navController.popBackStack();
                        } else {
                            navController.navigate(R.id.action_yourInterestsFragment_to_dashboardActivity);
                        }
                    }
                });
            }
        });
    }

    // Adapter for RecyclerView using ToggleButton
    private static class TopicAdapter extends RecyclerView.Adapter<TopicAdapter.TopicViewHolder> {
        private List<String> topics;
        private List<String> selectedTopicsInternal;

        public TopicAdapter(List<String> topics, List<String> selectedTopics) {
            this.topics = topics;
            this.selectedTopicsInternal = selectedTopics;
        }

        @NonNull
        @Override
        public TopicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_topic, parent, false);
            return new TopicViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull TopicViewHolder holder, int position) {
            String topic = topics.get(position);
            holder.topicToggleButton.setText(topic);
            holder.topicToggleButton.setTextOn(topic); // Ensures text is shown when checked
            holder.topicToggleButton.setTextOff(topic); // Ensures text is shown when unchecked
            holder.topicToggleButton.setOnCheckedChangeListener(null); // Clear previous listener
            holder.topicToggleButton.setChecked(selectedTopicsInternal.contains(topic)); // Re-set state before adding listener

            holder.topicToggleButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    if (!selectedTopicsInternal.contains(topic)) {
                        selectedTopicsInternal.add(topic);
                    }
                } else {
                    selectedTopicsInternal.remove(topic);
                }
            });
        }

        @Override
        public int getItemCount() {
            return topics.size();
        }

        static class TopicViewHolder extends RecyclerView.ViewHolder {
            ToggleButton topicToggleButton;

            public TopicViewHolder(@NonNull View itemView) {
                super(itemView);
                topicToggleButton = itemView.findViewById(R.id.toggle_topic);
            }
        }
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        rvTopics = null;
        btnNext = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
