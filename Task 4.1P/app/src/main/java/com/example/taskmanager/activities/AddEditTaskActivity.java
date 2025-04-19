package com.example.taskmanager.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.example.taskmanager.R;
import com.example.taskmanager.TaskManager;
import com.example.taskmanager.database.AppDatabase;
import com.example.taskmanager.models.Task;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddEditTaskActivity extends AppCompatActivity {
    private EditText etTitle, etDescription, etDueDate;
    private Button btnSave, btnCancelAndReturn;
    private Date selectedDate;
    private int taskId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_task);

        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        etDueDate = findViewById(R.id.etDueDate);
        btnSave = findViewById(R.id.btnSave);
        btnCancelAndReturn = findViewById(R.id.btnCancelAndReturn);

        // Retrieve taskId from Intent (If applicable)
        taskId = getIntent().getIntExtra("taskId", -1);

        // Check if this activity was called from TaskDetailActivity
        if (taskId != -1) {
            loadTaskData(taskId);
        }

        etDueDate.setOnClickListener(v -> showDatePicker());

        btnSave.setOnClickListener(v -> {
            saveTask();
            Intent intentDoneEditAndReturnHome = new Intent(AddEditTaskActivity.this, MainActivity.class);
            startActivity(intentDoneEditAndReturnHome);
        });

        btnCancelAndReturn.setOnClickListener(v -> {
            Intent intentCancelAndReturn = new Intent(AddEditTaskActivity.this, MainActivity.class);
            startActivity(intentCancelAndReturn);
        });
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year1, month1, dayOfMonth) -> {
                    Calendar selectedCalendar = Calendar.getInstance();
                    selectedCalendar.set(year1, month1, dayOfMonth);
                    selectedDate = selectedCalendar.getTime();
                    etDueDate.setText(dayOfMonth + "/" + (month1 + 1) + "/" + year1);
                },
                year, month, day
        );
        datePickerDialog.show();
    }

    private void loadTaskData(int taskId) {
        Task task = TaskManager.getDatabase().taskDao().getTaskById(taskId);
        if (task != null) {
            etTitle.setText(task.getTitle());
            etDescription.setText(task.getDescription());

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String formattedDate = sdf.format(task.getDueDate());

            etDueDate.setText(formattedDate);
            selectedDate = task.getDueDate();
        }
    }

    private void saveTask() {
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        if (title.isEmpty()) {
            etTitle.setError("Title is required");
            return;
        }

        if (selectedDate == null) {
            Toast.makeText(this, "Please select a due date", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a Task-class object
        Task task = new Task(title, description, selectedDate);

        task.setTitle(title);
        task.setDescription(description);
        task.setDueDate(selectedDate);

        // If this function is called from Editing a current Task, check through taskId
        if (taskId != -1) {
            task.setId(taskId);
            TaskManager.getDatabase().taskDao().updateTask(task);
            Toast.makeText(this, "Task is updated", Toast.LENGTH_SHORT).show();
        } else {
            TaskManager.getDatabase().taskDao().insertTask(task);
            Toast.makeText(this, "A new task is added", Toast.LENGTH_SHORT).show();
        }

        finish();
    }
}