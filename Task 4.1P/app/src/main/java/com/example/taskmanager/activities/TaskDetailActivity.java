package com.example.taskmanager.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.taskmanager.R;
import com.example.taskmanager.TaskManager;
import com.example.taskmanager.models.Task;

import java.text.SimpleDateFormat;

public class TaskDetailActivity extends AppCompatActivity {
    private TextView tvTitle, tvDescription, tvDueDate;
    private Button btnEdit, btnDelete, btnReturnHome;
    private Task task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);

        tvTitle = findViewById(R.id.tvTitle);
        tvDescription = findViewById(R.id.tvDescription);
        tvDueDate = findViewById(R.id.tvDueDate);
        btnEdit = findViewById(R.id.btnEdit);
        btnDelete = findViewById(R.id.btnDelete);
        btnReturnHome = findViewById(R.id.btnReturnHome);

        int taskId = getIntent().getIntExtra("taskId", -1);
        if (taskId != -1) {
            task = TaskManager.getDatabase().taskDao().getTaskById(taskId);

            if (task != null) {
                tvTitle.setText(task.getTitle());
                tvDescription.setText(task.getDescription());

                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                tvDueDate.setText(sdf.format(task.getDueDate()));

                btnEdit.setOnClickListener(v -> {
                    Intent intent = new Intent(TaskDetailActivity.this, AddEditTaskActivity.class);
                    intent.putExtra("taskId", task.getId());
                    startActivity(intent);
                });

                btnDelete.setOnClickListener(v -> {
                    TaskManager.getDatabase().taskDao().deleteTask(task);
                    Toast.makeText(this, "Task deleted", Toast.LENGTH_SHORT).show();
                    finish();
                });

                btnReturnHome.setOnClickListener(v -> {
                    Intent intentReturnHome = new Intent(TaskDetailActivity.this, MainActivity.class);
                    startActivity(intentReturnHome);
                });
            }
        }
    }
}