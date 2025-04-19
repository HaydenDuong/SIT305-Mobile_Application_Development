package com.example.taskmanager.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.util.Date;

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
