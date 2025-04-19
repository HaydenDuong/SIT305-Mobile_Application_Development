package com.example.quizapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;


import androidx.appcompat.app.AppCompatActivity;


public class ResultActivity extends AppCompatActivity {

    // Declare private Java variables
    private TextView congratUserText, scoreText;
    private Button takeNewQuizButton, finishButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        // Mapping the declared private Java variables with their .xml elements
        congratUserText = findViewById(R.id.congratUserText);
        scoreText = findViewById(R.id.scoreText);
        takeNewQuizButton = findViewById(R.id.takeNewQuizButton);
        finishButton = findViewById(R.id.finishButton);

        // Retrieve stored information from the intentToResultActivity
        String userName = getIntent().getStringExtra("USER_NAME");
        int score = getIntent().getIntExtra("SCORE", 0);
        int totalQuestions = getIntent().getIntExtra("TOTAL_QUESTIONS", 0);

        // Setting congratUserText with retrieved userName
        congratUserText.setText("Congratulations " + userName);
        scoreText.setText(score + "/" + totalQuestions);

        // Event-handling for selecting 'Take New Quiz' button
        takeNewQuizButton.setOnClickListener(v -> {
            Intent intentToMainActivity = new Intent(ResultActivity.this, MainActivity.class);
            startActivity(intentToMainActivity);
            finish();
        });

        // Event-handling for selecting 'Finish' button
        // finishAffinity() will close all activities present in the current Stack of Quiz App
        finishButton.setOnClickListener(v -> finishAffinity());
    }
}