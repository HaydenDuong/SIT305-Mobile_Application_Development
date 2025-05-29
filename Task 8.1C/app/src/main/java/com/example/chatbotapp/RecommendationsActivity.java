package com.example.chatbotapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;

public class RecommendationsActivity extends AppCompatActivity implements UserGroupAdapter.OnGroupClickListener {

    private static final String TAG = "RecommendationsActivity";
    private RecyclerView recyclerViewRecommendedUsers;
    private RecyclerView recyclerViewUserGroups;
    private RecommendedUserAdapter recommendedUserAdapter;
    private UserGroupAdapter userGroupAdapter;
    private List<RecommendedUser> recommendedUserList = new ArrayList<>();
    private List<UserGroup> userGroupList = new ArrayList<>();
    private RequestQueue requestQueue;

    private String currentUserId;
    private String BASE_URL = "http://10.0.2.2:5000"; // For Android emulator to reach localhost

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommendations);

        // Retrieve the currentUserId from the Intent
        currentUserId = getIntent().getStringExtra("currentUserId");
        if (currentUserId == null || currentUserId.isEmpty()) {
            Log.e(TAG, "Current User ID not passed in Intent or is empty!");
            Toast.makeText(this, "Error: User ID not provided.", Toast.LENGTH_LONG).show();
            // Depending on your app's logic, you might want to finish the activity
            // or prevent data fetching if the ID is crucial and missing.
            // For now, let data fetching proceed which will likely fail or use a bad URL.
            // Consider finishing the activity: finish(); return;
        }

        requestQueue = Volley.newRequestQueue(this);

        // Setup Recommended Users RecyclerView
        recyclerViewRecommendedUsers = findViewById(R.id.recyclerViewRecommendedUsers);
        recyclerViewRecommendedUsers.setLayoutManager(new LinearLayoutManager(this));
        recommendedUserAdapter = new RecommendedUserAdapter(recommendedUserList);
        recyclerViewRecommendedUsers.setAdapter(recommendedUserAdapter);

        // Setup User Groups RecyclerView
        recyclerViewUserGroups = findViewById(R.id.recyclerViewUserGroups);
        recyclerViewUserGroups.setLayoutManager(new GridLayoutManager(this, 2));
        userGroupAdapter = new UserGroupAdapter(userGroupList, this);
        recyclerViewUserGroups.setAdapter(userGroupAdapter);

        // Fetch data
        fetchRecommendedUsers();
        fetchUserInterestGroups();
    }

    private void fetchRecommendedUsers() {
        String url = BASE_URL + "/recommendations/users?user_id=" + currentUserId;
        Log.d(TAG, "Fetching recommended users from: " + url);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET, url, null,
                response -> {
                    Log.d(TAG, "Recommended users response: " + response.toString());
                    try {
                        JSONArray recommendationsArray = response.getJSONArray("recommendations");
                        List<RecommendedUser> newRecUsers = new ArrayList<>();
                        for (int i = 0; i < recommendationsArray.length(); i++) {
                            JSONObject recObject = recommendationsArray.getJSONObject(i);
                            String userId = recObject.getString("userId");
                            int commonInterests = recObject.getInt("commonInterests");
                            
                            List<String> interestNames = new ArrayList<>();
                            if (recObject.has("commonInterestNames")) { // Check if backend sends this field
                                JSONArray namesArray = recObject.getJSONArray("commonInterestNames");
                                for (int j = 0; j < namesArray.length(); j++) {
                                    interestNames.add(namesArray.getString(j));
                                }
                            }
                            newRecUsers.add(new RecommendedUser(userId, commonInterests, interestNames));
                        }
                        recommendedUserAdapter.updateData(newRecUsers);
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing recommended users JSON: " + e.getMessage());
                        Toast.makeText(RecommendationsActivity.this, "Error parsing recommendations", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e(TAG, "Volley error fetching recommended users: " + error.toString());
                    Toast.makeText(RecommendationsActivity.this, "Error fetching recommendations", Toast.LENGTH_SHORT).show();
                }
        );
        requestQueue.add(jsonObjectRequest);
    }

    private void fetchUserInterestGroups() {
        String url = BASE_URL + "/user_interests?uid=" + currentUserId;
        Log.d(TAG, "Fetching user interests from: " + url);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET, url, null,
                response -> {
                    Log.d(TAG, "User interests response: " + response.toString());
                    try {
                        JSONArray interestsArray = response.getJSONArray("interests");
                        List<UserGroup> newGroups = new ArrayList<>();
                        for (int i = 0; i < interestsArray.length(); i++) {
                            String interestName = interestsArray.getString(i);
                            newGroups.add(new UserGroup(interestName)); // Group name is the interest name
                        }
                        userGroupAdapter.updateData(newGroups);
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing user interests JSON: " + e.getMessage());
                        Toast.makeText(RecommendationsActivity.this, "Error parsing interest groups", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e(TAG, "Volley error fetching user interests: " + error.toString());
                    Toast.makeText(RecommendationsActivity.this, "Error fetching interest groups", Toast.LENGTH_SHORT).show();
                }
        );
        requestQueue.add(jsonObjectRequest);
    }

    @Override
    public void onGroupClick(UserGroup group) {
        Toast.makeText(this, "Clicked on group: " + group.getGroupName(), Toast.LENGTH_SHORT).show();
        checkGroupMembershipAndProceed(group.getGroupName());
    }

    private void checkGroupMembershipAndProceed(String groupName) {
        String url = BASE_URL + "/groups/ismember?user_id=" + currentUserId + "&group_name=" + groupName;
        Log.d(TAG, "Checking membership for group: " + groupName + " from URL: " + url);

        JsonObjectRequest checkMembershipRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        boolean isMember = response.getBoolean("isMember");
                        if (isMember) {
                            navigateToGroupChat(groupName);
                        } else {
                            showJoinGroupDialog(groupName);
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing isMember JSON: " + e.getMessage());
                        Toast.makeText(this, "Error checking group membership.", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e(TAG, "Volley error checking membership: " + error.toString());
                    // Handle case where /ismember endpoint might return 404 if group doesn't exist (though it should from user_interests)
                    if (error.networkResponse != null && error.networkResponse.statusCode == 404) {
                         // This case should ideally not happen if groupName comes from user's own interests which means groups exist.
                         // But if it can, prompt to join directly or handle as an error.
                        Log.w(TAG, "Group not found via /ismember, attempting to show join dialog anyway for: " + groupName);
                        showJoinGroupDialog(groupName); 
                    } else {
                        Toast.makeText(this, "Error checking group membership.", Toast.LENGTH_SHORT).show();
                    }
                }
        );
        requestQueue.add(checkMembershipRequest);
    }

    private void showJoinGroupDialog(String groupName) {
        new AlertDialog.Builder(this)
                .setTitle("Join Group")
                .setMessage("Do you want to join the '" + groupName + "' group?")
                .setPositiveButton("Yes", (dialog, which) -> joinGroupAndProceed(groupName))
                .setNegativeButton("No", null)
                .show();
    }

    private void joinGroupAndProceed(String groupName) {
        String url = BASE_URL + "/groups/join";
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("user_id", currentUserId);
            requestBody.put("group_name", groupName);
        } catch (JSONException e) {
            Log.e(TAG, "Error creating joinGroup request body: " + e.getMessage());
            return;
        }

        JsonObjectRequest joinGroupRequest = new JsonObjectRequest(Request.Method.POST, url, requestBody,
                response -> {
                    try {
                        String status = response.getString("status");
                        if ("success".equals(status)) {
                            Toast.makeText(this, "Successfully joined group: " + groupName, Toast.LENGTH_SHORT).show();
                            navigateToGroupChat(groupName);
                        } else {
                            String message = response.optString("message", "Failed to join group.");
                            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing joinGroup response: " + e.getMessage());
                        Toast.makeText(this, "Error after joining group.", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e(TAG, "Volley error joining group: " + error.toString());
                    Toast.makeText(this, "Error joining group.", Toast.LENGTH_SHORT).show();
                }
        );
        requestQueue.add(joinGroupRequest);
    }

    private void navigateToGroupChat(String groupName) {
        // TODO: Create and navigate to GroupChatActivity
        // Intent intent = new Intent(this, GroupChatActivity.class);
        // intent.putExtra("GROUP_NAME", groupName);
        // intent.putExtra("CURRENT_USER_ID", currentUserId);
        // startActivity(intent);
        Toast.makeText(this, "Navigating to group: " + groupName + " (Not implemented yet)", Toast.LENGTH_LONG).show();
    }
} 