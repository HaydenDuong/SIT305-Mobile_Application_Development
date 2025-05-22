package com.example.personalizedlearningexperienceapp.data;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;

@Entity(tableName = "question_responses",
        foreignKeys = @ForeignKey(entity = QuizAttemptEntity.class,
                                   parentColumns = "id",
                                   childColumns = "quizAttemptId",
                                   onDelete = ForeignKey.CASCADE))
public class QuestionResponseEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(index = true)
    public int quizAttemptId; // Foreign key to QuizAttemptEntity

    public String questionText;
    public String options; // Storing as JSON string for simplicity
    public String userAnswer;
    public String correctAnswer;
    public boolean wasCorrect;

    public QuestionResponseEntity(int quizAttemptId, String questionText, String options, String userAnswer, String correctAnswer, boolean wasCorrect) {
        this.quizAttemptId = quizAttemptId;
        this.questionText = questionText;
        this.options = options;
        this.userAnswer = userAnswer;
        this.correctAnswer = correctAnswer;
        this.wasCorrect = wasCorrect;
    }
} 