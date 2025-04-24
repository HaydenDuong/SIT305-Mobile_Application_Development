package com.example.newsapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.newsapp.R;
import com.example.newsapp.data.News;

import java.util.List;

public class TopStoriesAdapter extends RecyclerView.Adapter<TopStoriesAdapter.TopStoriesViewHolder> {

    private List<News> topStoriesList;
    private OnTopStoriesClickListener onTopStoriesClickListener;

    public TopStoriesAdapter(List<News> topStoriesList) {
        this.topStoriesList = topStoriesList;
    }

    public interface OnTopStoriesClickListener {
        void onTopStoriesClick(News news);
    }

    public void setOnTopStoriesClickListener(OnTopStoriesClickListener listener) {
        this.onTopStoriesClickListener = listener;
    }

    @NonNull
    @Override
    public TopStoriesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_topstories, parent, false);
        return new TopStoriesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TopStoriesViewHolder holder, int position) {
        News topStory = topStoriesList.get(position);

        if (topStory == null) {
            return;
        }

        holder.topStoriesImage.setImageResource(topStory.getImageResId());
        holder.topStoriesTitle.setText(topStory.getTitle());
        holder.itemView.setOnClickListener(v -> {
            if (onTopStoriesClickListener != null) {
                onTopStoriesClickListener.onTopStoriesClick(topStory);
            }
        });
    }

    @Override
    public int getItemCount() {
        if (topStoriesList != null) {
            return topStoriesList.size();
        }
        return 0;
    }

    class TopStoriesViewHolder extends RecyclerView.ViewHolder {
        private ImageView topStoriesImage;
        private TextView topStoriesTitle;

        public TopStoriesViewHolder(@NonNull View itemView) {
            super(itemView);

            topStoriesImage = itemView.findViewById(R.id.topStoriesImage);
            topStoriesTitle = itemView.findViewById(R.id.topStoriesTitle);
        }
    }
}
