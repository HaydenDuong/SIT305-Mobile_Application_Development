package com.example.chatbotapp.data;

import java.util.List;

public class RecommendedUser {
    private String userId;
    private String displayName;
    private int commonInterests;
    private List<String> commonInterestNames;

    public RecommendedUser(String userId, String displayName, int commonInterests, List<String> commonInterestNames) {
        this.userId = userId;
        this.displayName = displayName;
        this.commonInterests = commonInterests;
        this.commonInterestNames = commonInterestNames;
    }

    public String getUserId() {
        return userId;
    }

    public String getDisplayName() {
        return displayName;
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