package com.example.itubeapp.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "playlists")
public class Playlist {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private int userId;
    private String videoURL;

    // Default constructor for Room
    public Playlist() {

    }

    // Public constructor for class Playlist
    public Playlist(int userId, String videoURL) {
        this.userId = userId;
        this.videoURL = videoURL;
    }

    // Getters
    public int getId() { return id; }
    public int getUserId() { return userId; }
    public String getVideoURL() { return videoURL; }

    // Setters
    public void setId(int Id) { this.id = id; }
    public void setUserId(int userId) { this.userId = userId; }
    public void setVideoURL(String videoURL) { this.videoURL = videoURL; }
}
