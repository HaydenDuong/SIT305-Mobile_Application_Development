package com.example.personalizedlearningexperienceapp.api;

import com.example.personalizedlearningexperienceapp.models.QuizResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface QuizApi {
    @GET("getQuiz")
    Call<QuizResponse> getQuiz(@Query("topic") String topic);
}
