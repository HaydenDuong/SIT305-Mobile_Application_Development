package com.example.chatbotapp.data;

import java.util.List;

public class RecommendedUser {
    private String userId;
    private int commonInterests;
    private List<String> commonInterestNames;

    public RecommendedUser(String userId, int commonInterests, List<String> commonInterestNames) {
        this.userId = userId;
        this.commonInterests = commonInterests;
        this.commonInterestNames = commonInterestNames;
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

    public List<String> getCommonInterestNames() {
        return commonInterestNames;
    }

    public void setCommonInterestNames(List<String> commonInterestNames) {
        this.commonInterestNames = commonInterestNames;
    }
} 