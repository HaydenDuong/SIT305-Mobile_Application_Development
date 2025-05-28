package com.example.chatbotapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatbotapp.adapters.InterestsAdapter;
import com.example.chatbotapp.network.ApiService;
import com.example.chatbotapp.network.RetrofitClient;
import com.example.chatbotapp.responses.InterestsResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InterestsFragment extends Fragment {
    private RecyclerView recyclerView;
    private InterestsAdapter adapter;
    private List<String> interests = new ArrayList<>();
    private String userId;
    private EditText editTextAddInterest;
    private Button buttonAddInterest;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_interests, container, false);
        recyclerView = view.findViewById(R.id.recyclerViewInterests);
        editTextAddInterest = view.findViewById(R.id.editTextAddInterest);
        buttonAddInterest = view.findViewById(R.id.buttonAddInterest);

        userId = getArguments().getString("USER_UID");
        adapter = new InterestsAdapter(interests, userId, this::onDeleteInterest);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        fetchInterests();

        buttonAddInterest.setOnClickListener(v -> {
            String newInterest = editTextAddInterest.getText().toString().trim();
            if (!newInterest.isEmpty()) {
                addInterest(newInterest);
            }
        });

        return view;
    }

    private void fetchInterests() {
        ApiService apiService = RetrofitClient.getApiService();
        apiService.getUserInterests(userId).enqueue(new Callback<InterestsResponse>() {
            @Override
            public void onResponse(Call<InterestsResponse> call, Response<InterestsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    interests.clear();
                    interests.addAll(response.body().getInterests());
                    adapter.notifyDataSetChanged();
                }
            }
            @Override
            public void onFailure(Call<InterestsResponse> call, Throwable t) { }
        });
    }

    private void onDeleteInterest(String interest) {
        ApiService apiService = RetrofitClient.getApiService();
        apiService.deleteInterest(userId, interest).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                interests.remove(interest);
                adapter.notifyDataSetChanged();
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) { }
        });
    }

    private void addInterest(String interest) {
        ApiService apiService = RetrofitClient.getApiService();
        apiService.addInterest(userId, interest).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                interests.add(interest);
                adapter.notifyDataSetChanged();
                editTextAddInterest.setText("");
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) { }
        });
    }
}
