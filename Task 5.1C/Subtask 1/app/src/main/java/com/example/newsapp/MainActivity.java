package com.example.newsapp;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newsapp.adapter.NewsAdapter;
import com.example.newsapp.adapter.TopStoriesAdapter;
import com.example.newsapp.data.DummyData;


public class MainActivity extends AppCompatActivity {

    private RecyclerView topStoriesRecycleView;
    private RecyclerView newsRecycleView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        topStoriesRecycleView = findViewById(R.id.topStoriesRecyclerView);
        newsRecycleView = findViewById(R.id.newsRecyclerView);

        // Setup for Top Stories RecyclerView
        topStoriesRecycleView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        topStoriesRecycleView.setAdapter(new TopStoriesAdapter(DummyData.TOP_STORIES));

        // Setup for News RecyclerView
        // newsRecycleView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        // Set to 2 columns for displaying news in News section
        newsRecycleView.setLayoutManager(new GridLayoutManager(this, 2));
        newsRecycleView.setAdapter(new NewsAdapter(DummyData.NEWS_LIST));
    }
}