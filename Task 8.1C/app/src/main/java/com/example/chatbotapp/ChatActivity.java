package com.example.chatbotapp;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log; // For logging
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast; // For error messages

import androidx.annotation.NonNull; // For @NonNull
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatbotapp.adapters.ChatAdapter;
import com.example.chatbotapp.data.ChatMessage;
import com.example.chatbotapp.network.ApiService;     // Import ApiService
import com.example.chatbotapp.network.RetrofitClient; // Import RetrofitClient

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;        // Import Call
import retrofit2.Callback;    // Import Callback
import retrofit2.Response;    // Import Response

public class ChatActivity extends AppCompatActivity {

    private static final String TAG = "ChatActivity"; // For Logcat

    private RecyclerView recyclerViewChat;
    private EditText editTextMessage;
    private ImageButton buttonSend;

    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatMessagesList;
    private String currentUsername;
    private ApiService apiService; // Declare ApiService

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Retrieve the username from LoginActivity
        currentUsername = getIntent().getStringExtra("USERNAME");
        if (currentUsername == null || currentUsername.trim().isEmpty()) {
            currentUsername = "User"; // Fallback username
        }

        // Initialize views
        recyclerViewChat = findViewById(R.id.recyclerViewChat);
        editTextMessage = findViewById(R.id.editTextMessage);
        buttonSend = findViewById(R.id.buttonSend);

        // Initialize message list and adapter
        chatMessagesList = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatMessagesList, currentUsername);

        // Setup RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        // Allow first item to place at the bottom, the latest one will be placed on top
        layoutManager.setStackFromEnd(true);
        recyclerViewChat.setLayoutManager(layoutManager);
        recyclerViewChat.setAdapter(chatAdapter);

        // Initialize ApiService
        apiService = RetrofitClient.getApiService();

        // Add initial welcome message from the bot
        addInitialBotMessage();

        // Setup send button listener
        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessageToServer(); // Changed method name for clarity
            }
        });
    }

    private void addInitialBotMessage() {
        String welcomeText = "Welcome, " + currentUsername + "!";
        if (currentUsername.equalsIgnoreCase("User") && getIntent().getStringExtra("USERNAME") == null) {
            // If username was not passed and defaulted to "User", use generic welcome from wireframe
            welcomeText = "Welcome User!";
        }
        ChatMessage botMessage = new ChatMessage(welcomeText, ChatMessage.SenderType.BOT);
        chatAdapter.addMessage(botMessage);
        // No need to scroll here as it's the first message
    }

    private void sendMessageToServer() {
        String messageText = editTextMessage.getText().toString().trim();
        if (TextUtils.isEmpty(messageText)) {
            Toast.makeText(this, "Message cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        // Add user message to UI
        ChatMessage userMessage = new ChatMessage(messageText, ChatMessage.SenderType.USER);
        chatAdapter.addMessage(userMessage);
        editTextMessage.setText("");
        scrollToBottom();

        // Send message to server
        Call<String> call = apiService.sendMessage(messageText);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String botReply = response.body();
                    ChatMessage botMessage = new ChatMessage(botReply, ChatMessage.SenderType.BOT);
                    chatAdapter.addMessage(botMessage);
                    scrollToBottom();
                } else {
                    // Handle API error (e.g., server error 500, or if response.body() is null)
                    String errorBody = "Error: ";
                    try {
                        if (response.errorBody() != null) {
                            errorBody += response.errorBody().string();
                        } else {
                            errorBody += "Response not successful and error body is null.";
                        }
                    } catch (Exception e) {
                        errorBody += "Error parsing error body.";
                    }
                    Log.e(TAG, "API Error: " + response.code() + " - " + errorBody);
                    ChatMessage errorMessage = new ChatMessage("Sorry, bot error: " + response.code(), ChatMessage.SenderType.BOT);
                    chatAdapter.addMessage(errorMessage);
                    scrollToBottom();
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                // Handle network failure (e.g., no internet, server down)
                Log.e(TAG, "Network Failure: " + t.getMessage(), t);
                ChatMessage errorMessage = new ChatMessage("Network error: " + t.getMessage(), ChatMessage.SenderType.BOT);
                chatAdapter.addMessage(errorMessage);
                scrollToBottom();
            }
        });
    }

    private void scrollToBottom() {
        if (chatAdapter.getItemCount() > 0) {
            recyclerViewChat.smoothScrollToPosition(chatAdapter.getItemCount() - 1);
        }
    }

    // Optional: Method to simulate a bot response (for testing UI)
    /*
    private void simulateBotResponse(String userQuery) {
        new android.os.Handler().postDelayed(
            new Runnable() {
                public void run() {
                    ChatMessage botResponse = new ChatMessage("I received: " + userQuery, ChatMessage.SenderType.BOT);
                    chatAdapter.addMessage(botResponse);
                    if (chatAdapter.getItemCount() > 0) {
                        recyclerViewChat.smoothScrollToPosition(chatAdapter.getItemCount() - 1);
                    }
                }
            }, 1000); // 1 second delay
    }
    */
}