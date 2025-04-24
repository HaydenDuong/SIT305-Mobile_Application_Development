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

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {

    private List<News> newsList;
    private OnNewsClickListener onNewsClickListener;

    public NewsAdapter(List<News> newsList) {
        this.newsList = newsList;
    }

    public interface OnNewsClickListener {
        void onNewsClick(News news);
    }

    public void setOnNewsClickListener(OnNewsClickListener listener) {
        this.onNewsClickListener = listener;
    }

    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_news, parent, false);
        return new NewsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder holder, int position) {
        News news = newsList.get(position);

        if (news == null) {
            return;
        }

        holder.newsImage.setImageResource(news.getImageResId());
        holder.newsTitle.setText(news.getTitle());
        holder.newsInfo.setText(news.getDescription());
        holder.itemView.setOnClickListener(v -> {
            if (onNewsClickListener != null) {
                onNewsClickListener.onNewsClick(news);
            }
        });
    }

    @Override
    public int getItemCount() {
        if (newsList != null) {
            return newsList.size();
        }
        return 0;
    }

    class NewsViewHolder extends RecyclerView.ViewHolder {

        private ImageView newsImage;
        private TextView newsTitle, newsInfo;

        public NewsViewHolder(@NonNull View itemView) {
            super(itemView);

            newsImage = itemView.findViewById(R.id.newsImage);
            newsTitle = itemView.findViewById(R.id.newsTitle);
            newsInfo = itemView.findViewById(R.id.newsInfo);

        }
    }
}
