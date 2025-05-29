package com.example.chatbotapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class UserGroupAdapter extends RecyclerView.Adapter<UserGroupAdapter.ViewHolder> {

    private List<UserGroup> groupList;
    // TODO: Add a click listener interface if needed

    public UserGroupAdapter(List<UserGroup> groupList) {
        this.groupList = groupList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_group, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserGroup group = groupList.get(position);
        holder.groupNameTextView.setText("Group: " + group.getGroupName());
        // TODO: Set click listener for the item if needed
    }

    @Override
    public int getItemCount() {
        return groupList.size();
    }

    public void updateData(List<UserGroup> newGroups) {
        this.groupList.clear();
        this.groupList.addAll(newGroups);
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView groupNameTextView;

        ViewHolder(View itemView) {
            super(itemView);
            groupNameTextView = itemView.findViewById(R.id.textViewGroupName);
        }
    }
} 