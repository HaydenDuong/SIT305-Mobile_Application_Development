package com.example.personalizedlearningexperienceapp.data;

import androidx.room.Insert;
import androidx.room.Dao;
import androidx.room.Query;

@Dao
public interface UserDao {
    @Insert
    long insertUser(User user);

    @Query("SELECT * FROM users WHERE email = :email AND password = :password LIMIT 1")
    User getUserByEmailAndPassword(String email, String password);

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    User getUserByEmail(String email);

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    User getUserById(int userId);

    @Query("UPDATE users SET currentTier = :newTier WHERE id = :userId")
    void updateUserTier(int userId, String newTier);

    @Query("SELECT currentTier FROM users WHERE id = :userId")
    String getCurrentTierByUserId(int userId);
}
