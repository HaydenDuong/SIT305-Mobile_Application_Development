package com.example.personalizedlearningexperienceapp.data;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;

@Entity(tableName = "quiz_attempts", foreignKeys = @ForeignKey(entity = User.class, parentColumns = "id", childColumns = "userId", onDelete = ForeignKey.CASCADE))
public class QuizAttemptEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(index = true)
    public int userId;

    public String topicName;
    public long timestamp;
    public int totalQuestions;
    public int correctAnswers;

    public QuizAttemptEntity(int userId, String topicName, long timestamp, int totalQuestions, int correctAnswers) {
        this.userId = userId;
        this.topicName = topicName;
        this.timestamp = timestamp;
        this.totalQuestions = totalQuestions;
        this.correctAnswers = correctAnswers;
    }
} 