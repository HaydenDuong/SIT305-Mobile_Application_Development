package com.example.newsapp.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.newsapp.R;
import com.example.newsapp.adapter.NewsAdapter;
import com.example.newsapp.adapter.TopStoriesAdapter;
import com.example.newsapp.data.DummyData;
import com.example.newsapp.data.News;

public class MainFragment extends Fragment {

    // Required empty constructor for class MainFragment
    public MainFragment() {

    }

    private void navigateToNewsDetailFragment(News news) {
        NewsDetailFragment fragment = NewsDetailFragment.newInstance(news);
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        // Top Stories RecyclerView
        RecyclerView topStoriesRecycleView = view.findViewById(R.id.topStoriesRecyclerView);
        topStoriesRecycleView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        TopStoriesAdapter topStoriesAdapter = new TopStoriesAdapter(DummyData.TOP_STORIES);

        topStoriesAdapter.setOnTopStoriesClickListener(this::navigateToNewsDetailFragment);
        topStoriesRecycleView.setAdapter(topStoriesAdapter);

        // News RecyclerView
        RecyclerView newsRecycleView = view.findViewById(R.id.newsRecyclerView);
        newsRecycleView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        NewsAdapter newsAdapter = new NewsAdapter(DummyData.NEWS_LIST);

        newsAdapter.setOnNewsClickListener(this::navigateToNewsDetailFragment);
        newsRecycleView.setAdapter(newsAdapter);

        return view;
    }
}