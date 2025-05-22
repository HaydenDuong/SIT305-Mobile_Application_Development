package com.example.personalizedlearningexperienceapp.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import java.util.List;

@Dao
public interface QuizAttemptDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertQuizAttempt(QuizAttemptEntity quizAttempt); // Returns the row ID

    @Query("SELECT * FROM quiz_attempts WHERE userId = :userId ORDER BY timestamp DESC")
    List<QuizAttemptEntity> getQuizAttemptsForUser(int userId);

    // Add other queries as needed
} 