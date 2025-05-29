package com.example.chatbotapp.data;

public class GroupMessage {
    private String text;
    private String senderId;
    private String timestamp; // Storing as String as received from backend, can be parsed if needed

    // Constructor
    public GroupMessage(String text, String senderId, String timestamp) {
        this.text = text;
        this.senderId = senderId;
        this.timestamp = timestamp;
    }

    // Getters
    public String getText() {
        return text;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    // Optional: Setters if you ever need to modify a message object after creation
    // public void setText(String text) { this.text = text; }
    // public void setSenderId(String senderId) { this.senderId = senderId; }
    // public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
} 