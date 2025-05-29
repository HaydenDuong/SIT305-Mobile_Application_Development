package com.example.chatbotapp;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatbotapp.adapters.ChatAdapter; // Assuming this is your adapter
import com.example.chatbotapp.data.ChatMessage;   // Assuming this is your message model
import com.example.chatbotapp.network.ApiService;
import com.example.chatbotapp.network.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatFragment extends Fragment {

    private static final String TAG = "ChatFragment";

    private RecyclerView recyclerViewChat;
    private EditText editTextMessage;
    private ImageButton buttonSend;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatMessagesList;
    private String currentUserUid;
    private String currentUserDisplayName;
    private ApiService apiService;

    public ChatFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            currentUserUid = getArguments().getString("USER_UID");
            currentUserDisplayName = getArguments().getString("USER_DISPLAY_NAME");
        }

        if (currentUserDisplayName == null || currentUserDisplayName.trim().isEmpty()) {
            currentUserDisplayName = "User"; // Fallback display name
        }

        if (currentUserUid == null) {
            Log.e(TAG, "User UID is null in ChatFragment.");
            // Consider how to handle this - perhaps navigate back or show error
            Toast.makeText(getContext(), "Error: User session not found for chat.", Toast.LENGTH_LONG).show();
        }
        apiService = RetrofitClient.getApiService();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        recyclerViewChat = view.findViewById(R.id.recyclerViewChat);
        editTextMessage = view.findViewById(R.id.editTextMessage);
        buttonSend = view.findViewById(R.id.buttonSend);

        chatMessagesList = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatMessagesList, currentUserDisplayName != null ? currentUserDisplayName : "User");

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true);
        recyclerViewChat.setLayoutManager(layoutManager);
        recyclerViewChat.setAdapter(chatAdapter);

        addInitialBotMessage();

        buttonSend.setOnClickListener(v -> {
            if (currentUserUid != null) {
                sendMessageToServer();
            } else {
                Toast.makeText(getContext(), "Cannot send message: User ID missing.", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void addInitialBotMessage() {
        String welcomeText = "Welcome, " + (currentUserDisplayName != null ? currentUserDisplayName : "User") + "! How can I help you today?";
        ChatMessage botMessage = new ChatMessage(welcomeText, ChatMessage.SenderType.BOT);
        if (chatAdapter != null) { // Ensure adapter is initialized
             chatAdapter.addMessage(botMessage);
        }
    }

    private void sendMessageToServer() {
        String messageText = editTextMessage.getText().toString().trim();
        if (TextUtils.isEmpty(messageText)) {
            Toast.makeText(getContext(), "Message cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        ChatMessage userMessage = new ChatMessage(messageText, ChatMessage.SenderType.USER);
        chatAdapter.addMessage(userMessage);
        editTextMessage.setText("");
        scrollToBottom();

        if (currentUserUid == null) {
             Log.e(TAG, "Cannot send message to server: currentUserUid is null.");
             ChatMessage errorMessage = new ChatMessage("Error: Cannot send message. User session issue.", ChatMessage.SenderType.BOT);
             chatAdapter.addMessage(errorMessage);
             scrollToBottom();
             return;
        }

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
                             errorBody += "Response not successful. Code: " + response.code();
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
        if (chatAdapter != null && chatAdapter.getItemCount() > 0) {
            recyclerViewChat.smoothScrollToPosition(chatAdapter.getItemCount() - 1);
        }
    }
} 