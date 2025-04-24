// TaskManagerCombined.java

///*************************
/// 1. Entity (Model Class) *
package com.example.taskmanager.models;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Dao;
import androidx.room.Database;
import androidx.room.Delete;
import androidx.room.Entity;
import androidx.room.Insert;
import androidx.room.PrimaryKey;
import androidx.room.Query;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverter;
import androidx.room.TypeConverters;
import androidx.room.Update;

import com.example.taskmanager.R;
import com.example.taskmanager.TaskManager;
import com.example.taskmanager.activities.AddEditTaskActivity;
import com.example.taskmanager.activities.TaskDetailActivity;
import com.example.taskmanager.database.Converters;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@Entity(tableName = "tasks")
public class Task {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private String title;
    private String description;
    private Date dueDate;

    // Constructor for class Task
    public Task(String title, String description, Date dueDate) {
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
    }

    // Getters
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public Date getDueDate() { return dueDate; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) {this.description = description; }
    public void setDueDate(Date dueDate) {this.dueDate = dueDate; }
}

///*******************
/// 2. DAO Interface  *
package com.example.taskmanager.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.taskmanager.models.Task;
import java.util.List;

@Dao
public interface TaskDao {
    @Insert
    void insertTask(Task task);

    @Update
    void updateTask(Task task);

    @Delete
    void deleteTask(Task task);

    @Query("SELECT * FROM tasks ORDER BY dueDATE ASC")
    List<Task> getAllTasks();

    @Query("SELECT * FROM tasks WHERE id = :taskId")
    Task getTaskById(int taskId);
}

///**********************
/// 3. Database Class    *
package com.example.taskmanager.database;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.taskmanager.models.Task;
import com.example.taskmanager.database.TaskDao;

@Database(entities = {Task.class}, version = 1)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {
    public abstract com.example.taskmanager.database.TaskDao taskDao();
}

///**********************
/// 4. Converters Class    *
package com.example.taskmanager.database;

import androidx.room.TypeConverter;
import java.util.Date;

public class Converters {
    @TypeConverter
    public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }

    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }
}

///**********************
/// 4. Adapter Class    *
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

public class TaskAdapter extends RecyclerView.Adapter<com.example.taskmanager.adapters.TaskAdapter.TaskViewHolder> {
    private List<Task> taskList;
    private Context context;

    public TaskAdapter(List<Task> taskList, Context context) {
        this.taskList = taskList;
        this.context = context;
    }

    @NonNull
    @Override
    public com.example.taskmanager.adapters.TaskAdapter.TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new com.example.taskmanager.adapters.TaskAdapter.TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull com.example.taskmanager.adapters.TaskAdapter.TaskViewHolder holder, int position) {
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

///**********************
/// 4. Activities    *

// MainActivity.java
package com.example.taskmanager.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.taskmanager.R;
import com.example.taskmanager.TaskManager;
import com.example.taskmanager.adapters.TaskAdapter;
import com.example.taskmanager.database.AppDatabase;
import com.example.taskmanager.models.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private com.example.taskmanager.adapters.TaskAdapter adapter;
    private List<Task> taskList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize database
        com.example.taskmanager.database.AppDatabase db = TaskManager.getDatabase();

        // Get all tasks
        taskList = db.taskDao().getAllTasks();

        // Setup RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new com.example.taskmanager.adapters.TaskAdapter(taskList, this);
        recyclerView.setAdapter(adapter);

        // FAB for adding new task
        FloatingActionButton fab = findViewById(R.id.fabAddTask);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(com.example.taskmanager.activities.MainActivity.this, AddEditTaskActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh task list
        taskList.clear();
        taskList.addAll(TaskManager.getDatabase().taskDao().getAllTasks());
        adapter.notifyDataSetChanged();
    }
}

// AddEditTaskActivity.java
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
            Intent intentDoneEditAndReturnHome = new Intent(com.example.taskmanager.activities.AddEditTaskActivity.this, com.example.taskmanager.activities.MainActivity.class);
            startActivity(intentDoneEditAndReturnHome);
        });

        btnCancelAndReturn.setOnClickListener(v -> {
            Intent intentCancelAndReturn = new Intent(com.example.taskmanager.activities.AddEditTaskActivity.this, com.example.taskmanager.activities.MainActivity.class);
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

// TaskDetailActivity.java
package com.example.taskmanager.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.taskmanager.R;
import com.example.taskmanager.TaskManager;
import com.example.taskmanager.database.AppDatabase;
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
                    Intent intent = new Intent(com.example.taskmanager.activities.TaskDetailActivity.this, com.example.taskmanager.activities.AddEditTaskActivity.class);
                    intent.putExtra("taskId", task.getId());
                    startActivity(intent);
                });

                btnDelete.setOnClickListener(v -> {
                    TaskManager.getDatabase().taskDao().deleteTask(task);
                    Toast.makeText(this, "Task deleted", Toast.LENGTH_SHORT).show();
                    finish();
                });

                btnReturnHome.setOnClickListener(v -> {
                    Intent intentReturnHome = new Intent(com.example.taskmanager.activities.TaskDetailActivity.this, com.example.taskmanager.activities.MainActivity.class);
                    startActivity(intentReturnHome);
                });
            }
        }
    }
}

