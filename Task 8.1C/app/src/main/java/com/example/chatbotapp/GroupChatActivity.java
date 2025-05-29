package com.example.chatbotapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class GroupChatActivity extends AppCompatActivity {

    private static final String TAG = "GroupChatActivity";
    private static final String BASE_URL = "http://10.0.2.2:5000"; // For Android emulator

    private RecyclerView recyclerViewGroupMessages;
    private GroupChatAdapter groupChatAdapter;
    private List<GroupMessage> messageList = new ArrayList<>();
    private EditText editTextGroupMessage;
    private ImageButton buttonSendGroupMessage;
    private Toolbar toolbarGroupChat;

    private RequestQueue requestQueue;
    private String currentUserId;
    private String groupName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        Intent intent = getIntent();
        currentUserId = intent.getStringExtra("CURRENT_USER_ID");
        groupName = intent.getStringExtra("GROUP_NAME");

        if (currentUserId == null || currentUserId.isEmpty() || groupName == null || groupName.isEmpty()) {
            Log.e(TAG, "User ID or Group Name is missing!");
            Toast.makeText(this, "Error: User ID or Group Name missing.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        toolbarGroupChat = findViewById(R.id.toolbarGroupChat);
        setSupportActionBar(toolbarGroupChat);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(groupName);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Show back button
        }

        requestQueue = Volley.newRequestQueue(this);

        recyclerViewGroupMessages = findViewById(R.id.recyclerViewGroupMessages);
        editTextGroupMessage = findViewById(R.id.editTextGroupMessage);
        buttonSendGroupMessage = findViewById(R.id.buttonSendGroupMessage);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        // layoutManager.setStackFromEnd(true); // New messages appear at bottom and view focuses there
        recyclerViewGroupMessages.setLayoutManager(layoutManager);
        groupChatAdapter = new GroupChatAdapter(messageList, currentUserId);
        recyclerViewGroupMessages.setAdapter(groupChatAdapter);

        buttonSendGroupMessage.setOnClickListener(v -> sendMessage());

        fetchGroupMessages();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); // Handle toolbar back button press
        return true;
    }

    private void fetchGroupMessages() {
        String url = BASE_URL + "/groups/messages?group_name=" + groupName + "&limit=100"; // Fetch more messages if needed
        Log.d(TAG, "Fetching messages from: " + url);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
            response -> {
                try {
                    Log.d(TAG, "Messages response: " + response.toString());
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
                    // Backend sends newest first (DESC), adapter expects oldest first for typical chat display
                    // So, reverse the list before updating the adapter.
                    Collections.reverse(fetchedMessages);
                    groupChatAdapter.updateMessages(fetchedMessages);
                    recyclerViewGroupMessages.scrollToPosition(groupChatAdapter.getItemCount() - 1);
                } catch (JSONException e) {
                    Log.e(TAG, "Error parsing messages JSON: " + e.getMessage());
                    Toast.makeText(GroupChatActivity.this, "Error loading messages.", Toast.LENGTH_SHORT).show();
                }
            },
            error -> {
                Log.e(TAG, "Volley error fetching messages: " + error.toString());
                Toast.makeText(GroupChatActivity.this, "Failed to load messages.", Toast.LENGTH_SHORT).show();
            }
        );
        requestQueue.add(jsonObjectRequest);
    }

    private void sendMessage() {
        String messageText = editTextGroupMessage.getText().toString().trim();
        if (TextUtils.isEmpty(messageText)) {
            return; // Don't send empty messages
        }

        String url = BASE_URL + "/groups/message";
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("user_id", currentUserId);
            requestBody.put("group_name", groupName);
            requestBody.put("message_text", messageText);
        } catch (JSONException e) {
            Log.e(TAG, "Error creating send message request body: " + e.getMessage());
            return;
        }

        JsonObjectRequest sendMessageRequest = new JsonObjectRequest(Request.Method.POST, url, requestBody,
            response -> {
                try {
                    Log.d(TAG, "Send message response: " + response.toString());
                    String status = response.getString("status");
                    if ("success".equals(status)) {
                        // Optimistically add message to UI - or fetch again for robustness
                        // String messageId = response.getString("messageId"); // if needed
                        String timestamp = response.getString("timestamp"); // Use timestamp from server
                        
                        GroupMessage sentMessage = new GroupMessage(messageText, currentUserId, timestamp);
                        groupChatAdapter.addMessage(sentMessage);
                        recyclerViewGroupMessages.scrollToPosition(groupChatAdapter.getItemCount() - 1);
                        editTextGroupMessage.setText(""); // Clear input field
                    } else {
                        String msg = response.optString("message", "Failed to send message.");
                        Toast.makeText(GroupChatActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Error parsing send message response: " + e.getMessage());
                    Toast.makeText(GroupChatActivity.this, "Error after sending message.", Toast.LENGTH_SHORT).show();
                }
            },
            error -> {
                Log.e(TAG, "Volley error sending message: " + error.toString() + (error.networkResponse != null ? " SC: " + error.networkResponse.statusCode : ""));
                String errorMsg = "Failed to send message.";
                if (error.networkResponse != null && error.networkResponse.statusCode == 403) {
                    try {
                        String responseBody = new String(error.networkResponse.data, "utf-8");
                        JSONObject errorJson = new JSONObject(responseBody);
                        errorMsg = errorJson.optString("error", "Cannot send message (e.g., not a member).");
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing 403 error for send message: " + e.getMessage());
                    }
                }
                Toast.makeText(GroupChatActivity.this, errorMsg, Toast.LENGTH_LONG).show();
            }
        );
        requestQueue.add(sendMessageRequest);
    }
    
    // --- Leave Group Logic ---
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_group_chat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_leave_group) {
            confirmLeaveGroup();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void confirmLeaveGroup() {
        new AlertDialog.Builder(this)
                .setTitle("Leave Group")
                .setMessage("Are you sure you want to leave the group '" + groupName + "'?")
                .setPositiveButton("Leave", (dialog, which) -> performLeaveGroup())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void performLeaveGroup() {
        String url = BASE_URL + "/groups/leave";
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("user_id", currentUserId);
            requestBody.put("group_name", groupName);
        } catch (JSONException e) {
            Log.e(TAG, "Error creating leave group request body: " + e.getMessage());
            Toast.makeText(this, "Error preparing to leave group.", Toast.LENGTH_SHORT).show();
            return;
        }

        JsonObjectRequest leaveGroupRequest = new JsonObjectRequest(Request.Method.POST, url, requestBody,
            response -> {
                try {
                    Log.d(TAG, "Leave group response: " + response.toString());
                    String status = response.getString("status");
                    if ("success".equals(status)) {
                        Toast.makeText(GroupChatActivity.this, "You have left the group: " + groupName, Toast.LENGTH_LONG).show();
                        // Optionally, set a result for RecommendationsActivity to refresh or update its state
                        // Intent resultIntent = new Intent();
                        // resultIntent.putExtra("left_group_name", groupName);
                        // setResult(RESULT_OK, resultIntent);
                        finish(); // Close chat activity
                    } else {
                        String message = response.optString("message", "Failed to leave group.");
                        Toast.makeText(GroupChatActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Error parsing leave group response: " + e.getMessage());
                    Toast.makeText(GroupChatActivity.this, "Error after trying to leave group.", Toast.LENGTH_SHORT).show();
                }
            },
            error -> {
                Log.e(TAG, "Volley error leaving group: " + error.toString() + (error.networkResponse != null ? " SC: " + error.networkResponse.statusCode : ""));
                String errorMsg = "Failed to leave group.";
                 if (error.networkResponse != null && error.networkResponse.data != null) {
                    try {
                        String responseBody = new String(error.networkResponse.data, "utf-8");
                        JSONObject errorJson = new JSONObject(responseBody);
                        errorMsg = errorJson.optString("error", errorMsg); // Use backend error if available
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing error for leave group: " + e.getMessage());
                    }
                }
                Toast.makeText(GroupChatActivity.this, errorMsg, Toast.LENGTH_LONG).show();
            }
        );
        requestQueue.add(leaveGroupRequest);
    }
} 