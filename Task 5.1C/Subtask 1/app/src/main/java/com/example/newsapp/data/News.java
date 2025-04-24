package com.example.newsapp.data;

import java.util.ArrayList;
import java.util.List;

public class News {
    private int id;
    private int imageResId;
    private String title;
    private String description;
    private List<News> relatedNews;

    // There will 2 Constructors for class News
    // 1: Without relatedNews
    public News(int id, int imageResId, String title, String description) {
        this.id = id;
        this.imageResId = imageResId;
        this.title = title;
        this.description = description;
        this.relatedNews = new ArrayList<>();   // Prevent NullPointException
    }

    // 2: With relateNews
    public News(int id, int imageResId, String title, String description, List<News> relatedNews) {
        this.id = id;
        this.imageResId = imageResId;
        this.title = title;
        this.description = description;
        this.relatedNews = relatedNews;
    }

    // Getters
    public int getId() {
        return id;
    }

    public int getImageResId() {
        return imageResId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public List<News> getRelatedNews() {
        return relatedNews;
    }
}
