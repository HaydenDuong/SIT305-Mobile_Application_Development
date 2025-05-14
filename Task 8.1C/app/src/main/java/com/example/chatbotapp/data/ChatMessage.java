// Create new file: app/src/main/java/com/example/chatbotapp/ChatMessage.java
package com.example.chatbotapp.data;

public class ChatMessage {

    public enum SenderType {
        USER,
        BOT
    }

    private String messageText;
    private SenderType senderType;
    private long timestamp;

    public ChatMessage(String messageText, SenderType senderType) {
        this.messageText = messageText;
        this.senderType = senderType;
        this.timestamp = System.currentTimeMillis();
    }

    // Setters
    public String getMessageText() { return messageText; }
    public SenderType getSenderType() { return senderType; }
    public long getTimestamp() { return timestamp; }
}
