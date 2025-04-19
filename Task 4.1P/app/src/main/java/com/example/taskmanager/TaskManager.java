package com.example.taskmanager;

import android.app.Application;

import androidx.room.Room;

import com.example.taskmanager.database.AppDatabase;

public class TaskManager extends Application {
    private static AppDatabase database;

    @Override
    public void onCreate() {
        super.onCreate();
        database = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "task-database")
                .allowMainThreadQueries() // For simplicity, in production use background threads
                .build();
    }

    public static AppDatabase getDatabase() {
        return database;
    }
}
