package com.example.newsapp.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.newsapp.R;
import com.example.newsapp.adapter.RelatedNewsAdapter;
import com.example.newsapp.data.News;

public class NewsDetailFragment extends Fragment {

    // Define key to obtain data from Bundle
    private static final String ARG_NEWS = "news";

    // Empty public constructor for this Fragment (Required)
    public NewsDetailFragment() {

    }

    // Static method for creating Fragment and transmit data
    public static NewsDetailFragment newInstance(News news) {
        NewsDetailFragment fragment = new NewsDetailFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_NEWS, news);
        fragment.setArguments(args);
        return fragment;
    }

    // Method for creating the layout for Fragment
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Applying "fragment_news_detail.xml" for NewsDetail Fragment
        View view = inflater.inflate(R.layout.fragment_news_detail, container, false);

        // Obtain the data from Bundle
        News news = getArguments().getParcelable(ARG_NEWS);

        // Setting up the details for the Fragment
        ImageView newsImage = view.findViewById(R.id.detailNewsImage);
        TextView newsTitle = view.findViewById(R.id.detailNewsTitle);
        TextView newsInfo = view.findViewById(R.id.detailNewsInfo);

        if (news != null) {
            newsImage.setImageResource(news.getImageResId());
            newsTitle.setText(news.getTitle());
            newsTitle.setText(news.getDescription());
        }

        // Setup the RecyclerView for Related News
        RecyclerView relatedNewsRecyclerView = view.findViewById(R.id.relatedNewsRecycleView);
        relatedNewsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        relatedNewsRecyclerView.setAdapter(new RelatedNewsAdapter(news != null ? news.getRelatedNews() : null));

        // Inflate the setup
        return view;
    }
}