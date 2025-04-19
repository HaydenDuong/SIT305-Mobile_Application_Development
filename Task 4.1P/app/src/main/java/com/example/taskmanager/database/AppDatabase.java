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
    public abstract TaskDao taskDao();
}
