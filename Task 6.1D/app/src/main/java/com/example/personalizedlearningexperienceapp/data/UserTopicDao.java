package com.example.personalizedlearningexperienceapp.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface UserTopicDao {
    @Insert
    void insertUserTopic(UserTopic userTopic);

    @Query("SELECT * FROM user_topics WHERE userId = :userId")
    List<UserTopic> getUserTopics(int userId);

    @Query("DELETE FROM user_topics WHERE userId = :userId")
    void deleteUserTopics(int userId);
}
