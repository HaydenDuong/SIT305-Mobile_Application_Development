package com.example.personalizedlearningexperienceapp.adapters; // Adjust package if needed

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.personalizedlearningexperienceapp.R;
import com.example.personalizedlearningexperienceapp.data.UserTopic;
import java.util.ArrayList;
import java.util.List;

public class DashboardTopicAdapter extends RecyclerView.Adapter<DashboardTopicAdapter.TopicViewHolder> {

    private List<UserTopic> topics = new ArrayList<>();
    private OnTopicClickListener listener;

    // Interface for click events
    public interface OnTopicClickListener {
        void onTopicClick(UserTopic topic);
    }

    public DashboardTopicAdapter(OnTopicClickListener listener) {
        this.listener = listener;
    }

    public void setTopics(List<UserTopic> topics) {
        this.topics = topics;
        notifyDataSetChanged(); // Or use DiffUtil for better performance
    }

    @NonNull
    @Override
    public TopicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_dashboard_topic, parent, false);
        return new TopicViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TopicViewHolder holder, int position) {
        UserTopic currentTopic = topics.get(position);
        holder.bind(currentTopic, listener);
    }

    @Override
    public int getItemCount() {
        return topics.size();
    }

    // ViewHolder Class
    static class TopicViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTopicTitle;
        private TextView tvTopicDescription;
        private ImageView ivArrow; // Optional if needed

        public TopicViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTopicTitle = itemView.findViewById(R.id.tv_topic_title);
            tvTopicDescription = itemView.findViewById(R.id.tv_topic_description);
            ivArrow = itemView.findViewById(R.id.imageView_arrow);
        }

        public void bind(final UserTopic topic, final OnTopicClickListener listener) {
            tvTopicTitle.setText("Quiz on " + topic.getTopic());
            // You can add more description if needed

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTopicClick(topic);
                }
            });
        }
    }
}