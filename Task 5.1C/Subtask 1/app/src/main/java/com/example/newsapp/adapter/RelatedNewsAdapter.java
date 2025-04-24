package com.example.newsapp.adapter;

import com.example.newsapp.R;
import com.example.newsapp.data.News;

import android.media.Image;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RelatedNewsAdapter extends RecyclerView.Adapter<RelatedNewsAdapter.RelatedNewsViewHolder> {

    private List<News> relatedNewsList;

    public RelatedNewsAdapter(List<News> relatedNewsList) {
        this.relatedNewsList = relatedNewsList;
    }

    @NonNull
    @Override
    public RelatedNewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.items_relatednews, parent, false);
        return new RelatedNewsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RelatedNewsViewHolder holder, int position) {
        News relatedNews = relatedNewsList.get(position);
        holder.relatedNewsImage.setImageResource(relatedNews.getImageResId());
        holder.relatedNewsTitle.setText(relatedNews.getTitle());
        holder.relatedNewsInfo.setText(relatedNews.getDescription());
    }

    @Override
    public int getItemCount() {
        if (relatedNewsList != null) {
            return relatedNewsList.size();
        }
        return 0;
    }

    static class RelatedNewsViewHolder extends RecyclerView.ViewHolder {

        private ImageView relatedNewsImage;
        private TextView relatedNewsTitle, relatedNewsInfo;

        public RelatedNewsViewHolder(@NonNull View itemView) {
            super(itemView);

           relatedNewsImage = itemView.findViewById(R.id.relatedNewsImage);
           relatedNewsTitle = itemView.findViewById(R.id.relatedNewsTitle);
           relatedNewsInfo = itemView.findViewById(R.id.relatedNewsInfo);
        }
    }
}
