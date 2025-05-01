package com.example.itubeapp.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

// Defining methods for interacting with Table "users"
@Dao
public interface UserDAO {

    // Insert a newly created user to Table 'users"
    @Insert
    void insertUser(User user);

    // Retrieve a created user credentials from Table "users"
    @Query("SELECT * FROM users WHERE username = :username AND password = :password")
    User getUser(String username, String password);

    @Query("SELECT * FROM users WHERE username = :username")
    User getUserByUserName(String username);
}
