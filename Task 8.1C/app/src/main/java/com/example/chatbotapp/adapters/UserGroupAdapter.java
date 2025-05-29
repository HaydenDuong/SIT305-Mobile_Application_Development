package com.example.chatbotapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatbotapp.R;
import com.example.chatbotapp.data.UserGroup;

import java.util.List;

public class UserGroupAdapter extends RecyclerView.Adapter<UserGroupAdapter.ViewHolder> {

    private List<UserGroup> groupList;
    private OnGroupClickListener onGroupClickListener;

    // Interface for click events
    public interface OnGroupClickListener {
        void onGroupClick(UserGroup group);
    }

    public UserGroupAdapter(List<UserGroup> groupList, OnGroupClickListener listener) {
        this.groupList = groupList;
        this.onGroupClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_group, parent, false);
        return new ViewHolder(view, onGroupClickListener, groupList);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserGroup group = groupList.get(position);
        holder.groupNameTextView.setText(group.getGroupName());
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

        ViewHolder(View itemView, OnGroupClickListener listener, List<UserGroup> currentGroupList) {
            super(itemView);
            groupNameTextView = itemView.findViewById(R.id.textViewGroupName);
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onGroupClick(currentGroupList.get(position));
                }
            });
        }
    }
} 