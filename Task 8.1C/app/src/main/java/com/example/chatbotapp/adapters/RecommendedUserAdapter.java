package com.example.chatbotapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatbotapp.R;
import com.example.chatbotapp.data.RecommendedUser;

import java.util.List;
import java.util.Locale;

public class RecommendedUserAdapter extends RecyclerView.Adapter<RecommendedUserAdapter.ViewHolder> {

    private List<RecommendedUser> recommendedUserList;
    private OnChatButtonClickListener chatButtonClickListener;

    public interface OnChatButtonClickListener {
        void onChatButtonClick(RecommendedUser user);
    }

    public RecommendedUserAdapter(List<RecommendedUser> recommendedUserList, OnChatButtonClickListener listener) {
        this.recommendedUserList = recommendedUserList;
        this.chatButtonClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recommended_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RecommendedUser user = recommendedUserList.get(position);
        holder.userIdTextView.setText(String.format(Locale.getDefault(), "User ID: %s", user.getUserId()));
        holder.commonInterestsCountTextView.setText(String.format(Locale.getDefault(), "Common Interests: %d", user.getCommonInterests()));

        if (user.getCommonInterestNames() != null && !user.getCommonInterestNames().isEmpty()) {
            holder.commonInterestNamesTextView.setText("Shared: " + String.join(", ", user.getCommonInterestNames()));
            holder.commonInterestNamesTextView.setVisibility(View.VISIBLE);
        } else {
            holder.commonInterestNamesTextView.setVisibility(View.GONE);
        }

        holder.chatButton.setOnClickListener(v -> {
            if (chatButtonClickListener != null) {
                chatButtonClickListener.onChatButtonClick(user);
            }
        });
    }

    @Override
    public int getItemCount() {
        return recommendedUserList.size();
    }

    public void updateData(List<RecommendedUser> newList) {
        this.recommendedUserList.clear();
        this.recommendedUserList.addAll(newList);
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView userIdTextView;
        TextView commonInterestsCountTextView;
        TextView commonInterestNamesTextView;
        Button chatButton;

        ViewHolder(View itemView) {
            super(itemView);
            userIdTextView = itemView.findViewById(R.id.textViewRecommendedUserId);
            commonInterestsCountTextView = itemView.findViewById(R.id.textViewCommonInterestsCount);
            commonInterestNamesTextView = itemView.findViewById(R.id.textViewCommonInterestNames);
            chatButton = itemView.findViewById(R.id.buttonStartDirectChat);
        }
    }
} 