package com.example.chatbotapp.data;

public class UserGroup {
    private String groupName;
    // You could add more fields later, e.g., number of members, last message, etc.

    public UserGroup(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
} 