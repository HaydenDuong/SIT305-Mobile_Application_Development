package com.example.itubeapp.data;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {User.class, Playlist.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract UserDAO userDao();
    public abstract PlaylistDao playlistDao();
}
