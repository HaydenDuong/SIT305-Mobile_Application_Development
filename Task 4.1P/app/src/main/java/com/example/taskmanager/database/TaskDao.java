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
