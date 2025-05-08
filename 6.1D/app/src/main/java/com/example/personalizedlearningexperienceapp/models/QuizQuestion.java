package com.example.personalizedlearningexperienceapp.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class QuizQuestion {
    @SerializedName("question")
    private String question;

    @SerializedName("options")
    private List<String> options;

    @SerializedName("correct_answer")
    private String correctAnswer;

    // Getters
    public String getQuestion() {
        return question;
    }

    public List<String> getOptions() {
        return options;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }
}