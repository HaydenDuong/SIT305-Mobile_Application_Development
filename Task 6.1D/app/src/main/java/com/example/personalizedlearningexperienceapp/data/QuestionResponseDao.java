package com.example.personalizedlearningexperienceapp.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import java.util.List;

@Dao
public interface QuestionResponseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertQuestionResponse(QuestionResponseEntity questionResponse);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAllQuestionResponses(List<QuestionResponseEntity> questionResponses);

    @Query("SELECT * FROM question_responses WHERE quizAttemptId = :quizAttemptId")
    List<QuestionResponseEntity> getResponsesForQuizAttempt(int quizAttemptId);

    // Queries for Stats (to be used in ProfileViewModel)
    @Query("SELECT COUNT(qr.id) FROM question_responses qr " +
           "INNER JOIN quiz_attempts qa ON qr.quizAttemptId = qa.id " +
           "WHERE qa.userId = :userId")
    int getTotalQuestionsAnsweredByUser(int userId);

    @Query("SELECT COUNT(qr.id) FROM question_responses qr " +
           "INNER JOIN quiz_attempts qa ON qr.quizAttemptId = qa.id " +
           "WHERE qa.userId = :userId AND qr.wasCorrect = 1")
    int getTotalCorrectAnswersByUser(int userId);
} 