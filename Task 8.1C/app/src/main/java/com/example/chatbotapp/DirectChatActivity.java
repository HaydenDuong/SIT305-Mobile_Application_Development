package com.example.chatbotapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.chatbotapp.adapters.GroupChatAdapter;
import com.example.chatbotapp.data.GroupMessage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DirectChatActivity extends AppCompatActivity {

    private static final String TAG = "DirectChatActivity";
    private static final String BASE_URL = "http://10.0.2.2:5000";

    private RecyclerView recyclerViewDirectMessages;
    private GroupChatAdapter directChatAdapter; // Reusing GroupChatAdapter
    private List<GroupMessage> messageList = new ArrayList<>(); // Reusing GroupMessage model
    private EditText editTextDirectMessage;
    private ImageButton buttonSendDirectMessage;
    private Toolbar toolbarDirectChat;

    private RequestQueue requestQueue;
    private String currentUserId;
    private String otherUserId;
    private String otherUserDisplayName;
    private String chatRoomId; // Generated from currentUserId and otherUserId

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_direct_chat);

        Intent intent = getIntent();
        currentUserId = intent.getStringExtra("CURRENT_USER_ID");
        otherUserId = intent.getStringExtra("OTHER_USER_ID");
        otherUserDisplayName = intent.getStringExtra("OTHER_USER_DISPLAY_NAME");

        if (currentUserId == null || currentUserId.isEmpty() || 
            otherUserId == null || otherUserId.isEmpty()) {
            Log.e(TAG, "Current User ID or Other User ID is missing!");
            Toast.makeText(this, "Error: User information missing for chat.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        
        // Generate Chat Room ID
        chatRoomId = generateLocalChatRoomId(currentUserId, otherUserId);

        toolbarDirectChat = findViewById(R.id.toolbarDirectChat);
        setSupportActionBar(toolbarDirectChat);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(otherUserDisplayName != null ? otherUserDisplayName : otherUserId);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        requestQueue = Volley.newRequestQueue(this);

        recyclerViewDirectMessages = findViewById(R.id.recyclerViewDirectMessages);
        editTextDirectMessage = findViewById(R.id.editTextDirectMessage);
        buttonSendDirectMessage = findViewById(R.id.buttonSendDirectMessage);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerViewDirectMessages.setLayoutManager(layoutManager);
        directChatAdapter = new GroupChatAdapter(messageList, currentUserId); // Pass currentUserId
        recyclerViewDirectMessages.setAdapter(directChatAdapter);

        buttonSendDirectMessage.setOnClickListener(v -> sendDirectMessage());

        fetchDirectMessages();
    }
    
    private String generateLocalChatRoomId(String uid1, String uid2) {
        List<String> uids = new ArrayList<>();
        uids.add(uid1);
        uids.add(uid2);
        Collections.sort(uids);
        return uids.get(0) + "_" + uids.get(1);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void fetchDirectMessages() {
        // Use chatRoomId directly if preferred, or pass uid1 and uid2
        String url = BASE_URL + "/direct_chat/messages?uid1=" + currentUserId + "&uid2=" + otherUserId + "&limit=100";
        Log.d(TAG, "Fetching direct messages from: " + url);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
            response -> {
                try {
                    Log.d(TAG, "Direct messages response: " + response.toString());
                    JSONArray messagesArray = response.getJSONArray("messages");
                    List<GroupMessage> fetchedMessages = new ArrayList<>();
                    for (int i = 0; i < messagesArray.length(); i++) {
                        JSONObject msgObject = messagesArray.getJSONObject(i);
                        fetchedMessages.add(new GroupMessage(
                                msgObject.getString("text"),
                                msgObject.getString("senderId"),
                                msgObject.getString("timestamp")
                        ));
                    }
                    // Backend already sends them in ASC order for direct messages
                    directChatAdapter.updateMessages(fetchedMessages);
                    if (directChatAdapter.getItemCount() > 0) {
                        recyclerViewDirectMessages.scrollToPosition(directChatAdapter.getItemCount() - 1);
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Error parsing direct messages JSON: " + e.getMessage());
                    Toast.makeText(DirectChatActivity.this, "Error loading messages.", Toast.LENGTH_SHORT).show();
                }
            },
            error -> {
                Log.e(TAG, "Volley error fetching direct messages: " + error.toString());
                Toast.makeText(DirectChatActivity.this, "Failed to load messages.", Toast.LENGTH_SHORT).show();
            }
        );
        requestQueue.add(jsonObjectRequest);
    }

    private void sendDirectMessage() {
        String messageText = editTextDirectMessage.getText().toString().trim();
        if (TextUtils.isEmpty(messageText)) {
            return;
        }

        String url = BASE_URL + "/direct_chat/send";
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("sender_uid", currentUserId);
            requestBody.put("receiver_uid", otherUserId);
            requestBody.put("message_text", messageText);
        } catch (JSONException e) {
            Log.e(TAG, "Error creating send direct message request body: " + e.getMessage());
            return;
        }

        JsonObjectRequest sendMessageRequest = new JsonObjectRequest(Request.Method.POST, url, requestBody,
            response -> {
                try {
                    Log.d(TAG, "Send direct message response: " + response.toString());
                    String status = response.getString("status");
                    if ("success".equals(status)) {
                        String timestamp = response.getString("timestamp");                        
                        GroupMessage sentMessage = new GroupMessage(messageText, currentUserId, timestamp);
                        directChatAdapter.addMessage(sentMessage);
                        recyclerViewDirectMessages.scrollToPosition(directChatAdapter.getItemCount() - 1);
                        editTextDirectMessage.setText("");
                    } else {
                        String msg = response.optString("message", "Failed to send message.");
                        Toast.makeText(DirectChatActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Error parsing send direct message response: " + e.getMessage());
                    Toast.makeText(DirectChatActivity.this, "Error after sending message.", Toast.LENGTH_SHORT).show();
                }
            },
            error -> {
                Log.e(TAG, "Volley error sending direct message: " + error.toString());
                Toast.makeText(DirectChatActivity.this, "Failed to send message.", Toast.LENGTH_LONG).show();
            }
        );
        requestQueue.add(sendMessageRequest);
    }
} 