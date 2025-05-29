package com.example.chatbotapp;

import androidx.appcompat.app.AppCompatActivity;
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

public class RecommendationsActivity extends AppCompatActivity {

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

        requestQueue = Volley.newRequestQueue(this);

        currentUserId = getIntent().getStringExtra("currentUserId");
        if (currentUserId == null || currentUserId.isEmpty()) {
            Log.e(TAG, "Current User ID not passed in Intent!");
        }

        // Setup Recommended Users RecyclerView
        recyclerViewRecommendedUsers = findViewById(R.id.recyclerViewRecommendedUsers);
        recyclerViewRecommendedUsers.setLayoutManager(new LinearLayoutManager(this));
        recommendedUserAdapter = new RecommendedUserAdapter(recommendedUserList);
        recyclerViewRecommendedUsers.setAdapter(recommendedUserAdapter);

        // Setup User Groups RecyclerView
        recyclerViewUserGroups = findViewById(R.id.recyclerViewUserGroups);
        recyclerViewUserGroups.setLayoutManager(new LinearLayoutManager(this));
        userGroupAdapter = new UserGroupAdapter(userGroupList);
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
                            newRecUsers.add(new RecommendedUser(userId, commonInterests));
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
} 