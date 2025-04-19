package com.example.taskmanager.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskmanager.R;
import com.example.taskmanager.activities.TaskDetailActivity;
import com.example.taskmanager.models.Task;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
    private List<Task> taskList;
    private Context context;

    public TaskAdapter(List<Task> taskList, Context context) {
        this.taskList = taskList;
        this.context = context;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);
        holder.tvTitle.setText(task.getTitle());

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        String dateStr = sdf.format(task.getDueDate());

        // Check if the task is expired
        if (task.getDueDate().before(new Date())) {
            holder.tvDueDate.setTextColor(ContextCompat.getColor(context, R.color.red));
            dateStr += " (Expired!!!)";
        } else {
            holder.tvDueDate.setTextColor(ContextCompat.getColor(context, R.color.black));
        }

        holder.tvDueDate.setText(dateStr);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, TaskDetailActivity.class);
            intent.putExtra("taskId", task.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDueDate;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTaskTitle);
            tvDueDate = itemView.findViewById(R.id.tvTaskDueDate);
        }
    }
}
