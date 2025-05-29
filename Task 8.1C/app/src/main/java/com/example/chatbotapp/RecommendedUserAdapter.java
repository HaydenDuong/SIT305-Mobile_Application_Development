package com.example.chatbotapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class RecommendedUserAdapter extends RecyclerView.Adapter<RecommendedUserAdapter.ViewHolder> {

    private List<RecommendedUser> recommendedUserList;
    // TODO: Add a click listener interface if needed

    public RecommendedUserAdapter(List<RecommendedUser> recommendedUserList) {
        this.recommendedUserList = recommendedUserList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recommended_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RecommendedUser recommendedUser = recommendedUserList.get(position);
        holder.userIdTextView.setText("User ID: " + recommendedUser.getUserId());
        holder.commonInterestsTextView.setText("Common Interests: " + recommendedUser.getCommonInterests());
        // TODO: Set click listener for the item if needed
    }

    @Override
    public int getItemCount() {
        return recommendedUserList.size();
    }

    public void updateData(List<RecommendedUser> newRecommendedUsers) {
        this.recommendedUserList.clear();
        this.recommendedUserList.addAll(newRecommendedUsers);
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView userIdTextView;
        TextView commonInterestsTextView;

        ViewHolder(View itemView) {
            super(itemView);
            userIdTextView = itemView.findViewById(R.id.textViewRecommendedUserId);
            commonInterestsTextView = itemView.findViewById(R.id.textViewCommonInterests);
        }
    }
} 