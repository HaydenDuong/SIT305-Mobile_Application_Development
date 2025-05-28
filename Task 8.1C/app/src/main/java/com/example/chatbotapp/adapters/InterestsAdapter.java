package com.example.chatbotapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatbotapp.R;

import java.util.List;

public class InterestsAdapter extends RecyclerView.Adapter<InterestsAdapter.ViewHolder> {
    private List<String> interests;
    private String userId;
    private OnDeleteInterestListener deleteListener;

    public interface OnDeleteInterestListener {
        void onDelete(String interest);
    }

    public InterestsAdapter(List<String> interests, String userId, OnDeleteInterestListener listener) {
        this.interests = interests;
        this.userId = userId;
        this.deleteListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_interest, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String interest = interests.get(position);
        holder.textViewInterest.setText(interest);
        holder.buttonDelete.setOnClickListener(v -> deleteListener.onDelete(interest));
    }

    @Override
    public int getItemCount() {
        return interests.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewInterest;
        Button buttonDelete;
        ViewHolder(View itemView) {
            super(itemView);
            textViewInterest = itemView.findViewById(R.id.textViewInterest);
            buttonDelete = itemView.findViewById(R.id.buttonDeleteInterest);
        }
    }
}
