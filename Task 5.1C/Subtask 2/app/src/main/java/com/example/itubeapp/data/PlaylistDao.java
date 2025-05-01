package com.example.itubeapp.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface PlaylistDao {

    @Insert
    void insertPlaylist(Playlist playlist);

    @Query("SELECT * FROM playlists WHERE userId = :userId")
    List<Playlist> getPlaylistsForUser(int userId);
}
