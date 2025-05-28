package com.example.chatbotapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log; // For logging
import android.view.View;
import android.widget.Button;
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
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;

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
    private Button buttonSignOut;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatMessagesList;
    private String currentUserUid; // To store Firebase User UID
    private String currentUserDisplayName; // To store display name (email for now)
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Retrieve User UID and Display Name from LoginActivity
        currentUserUid = getIntent().getStringExtra("USER_UID");
        currentUserDisplayName = getIntent().getStringExtra("USER_DISPLAY_NAME");

        if (currentUserDisplayName == null || currentUserDisplayName.trim().isEmpty()) {
            currentUserDisplayName = "User"; // Fallback display name
        }

        if (currentUserUid == null) {
            Log.e(TAG, "User UID is null. Navigating back to Login.");
            Toast.makeText(this, "Error: User session not found.", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return; 
        }

        recyclerViewChat = findViewById(R.id.recyclerViewChat);
        editTextMessage = findViewById(R.id.editTextMessage);
        buttonSend = findViewById(R.id.buttonSend);
        buttonSignOut = findViewById(R.id.buttonSignOut);

        chatMessagesList = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatMessagesList, currentUserDisplayName);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerViewChat.setLayoutManager(layoutManager);
        recyclerViewChat.setAdapter(chatAdapter);

        apiService = RetrofitClient.getApiService();

        addInitialBotMessage();

        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessageToServer();
            }
        });

        buttonSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                // Also sign out from Google if needed
                GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(
                        ChatActivity.this,
                        new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                .requestIdToken(getString(R.string.default_web_client_id))
                                .requestEmail()
                                .build()
                );
                googleSignInClient.signOut();
                // Go back to LoginActivity
                Intent intent = new Intent(ChatActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });
    }

    private void addInitialBotMessage() {
        String welcomeText = "Welcome, " + currentUserDisplayName + "!";
        ChatMessage botMessage = new ChatMessage(welcomeText, ChatMessage.SenderType.BOT);
        chatAdapter.addMessage(botMessage);
    }

    private void sendMessageToServer() {
        String messageText = editTextMessage.getText().toString().trim();
        if (TextUtils.isEmpty(messageText)) {
            Toast.makeText(this, "Message cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        ChatMessage userMessage = new ChatMessage(messageText, ChatMessage.SenderType.USER);
        chatAdapter.addMessage(userMessage);
        editTextMessage.setText("");
        scrollToBottom();

        Call<String> call = apiService.sendMessage(currentUserUid, messageText);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String botReply = response.body();
                    ChatMessage botMessage = new ChatMessage(botReply, ChatMessage.SenderType.BOT);
                    chatAdapter.addMessage(botMessage);
                    scrollToBottom();
                } else {
                    String errorBody = "Error: ";
                    try {
                        if (response.errorBody() != null) {
                            errorBody += response.errorBody().string();
                        } else {
                             errorBody += "Response not successful and error body is null. Code: " + response.code();
                        }
                    } catch (Exception e) {
                        errorBody += "Error parsing error body. Code: " + response.code();
                    }
                    Log.e(TAG, "API Error: " + errorBody);
                    ChatMessage errorMessage = new ChatMessage("Sorry, bot error. (" + response.code() + ")", ChatMessage.SenderType.BOT);
                    chatAdapter.addMessage(errorMessage);
                    scrollToBottom();
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                Log.e(TAG, "Network Failure: " + t.getMessage(), t);
                ChatMessage errorMessage = new ChatMessage("Network error. Please check connection.", ChatMessage.SenderType.BOT);
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