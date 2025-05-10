package com.example.personalizedlearningexperienceapp.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class QuizResponse {
    @SerializedName("quiz")
    private List<QuizQuestion> quiz;

    public List<QuizQuestion> getQuiz() {
        return quiz;
    }
}
