package com.example.chatbotapp;

public class RecommendedUser {
    private String userId;
    private int commonInterests;

    public RecommendedUser(String userId, int commonInterests) {
        this.userId = userId;
        this.commonInterests = commonInterests;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getCommonInterests() {
        return commonInterests;
    }

    public void setCommonInterests(int commonInterests) {
        this.commonInterests = commonInterests;
    }
} 