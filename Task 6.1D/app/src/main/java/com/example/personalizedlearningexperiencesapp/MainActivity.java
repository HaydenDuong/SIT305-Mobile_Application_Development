package com.example.personalizedlearningexperiencesapp;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.example.personalizedlearningexperiencesapp.api.ApiClient;
import com.example.personalizedlearningexperiencesapp.api.QuizApi;
import com.example.personalizedlearningexperiencesapp.models.QuizQuestion;
import com.example.personalizedlearningexperiencesapp.models.QuizResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        QuizApi quizApi = ApiClient.getClient().create(QuizApi.class);
        Call<QuizResponse> call = quizApi.getQuiz("movies");

        call.enqueue(new Callback<QuizResponse>() {
            @Override
            public void onResponse(Call<QuizResponse> call, Response<QuizResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    QuizResponse quizResponse = response.body();
                    Log.d(TAG, "Quiz received: " + quizResponse.getQuiz().size() + " questions");
                    for (QuizQuestion question : quizResponse.getQuiz()) {
                        Log.d(TAG, "Question: " + question.getQuestion());
                        Log.d(TAG, "Options: " + question.getOptions());
                        Log.d(TAG, "Correct Answer: " + question.getCorrectAnswer());
                    }
                } else {
                    Log.e(TAG, "Failed to get quiz: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<QuizResponse> call, Throwable t) {
                Log.e(TAG, "Error: " + t.getMessage());
            }
        });
    }
}