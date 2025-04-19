package com.example.quizapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {

    // Declare private Java variables
    private EditText enterNameEditText;
    private Button startButton, calculatorButton;

    // Function for handling transition to Quiz Activity
    public void jumpClickToQuizActivity(View view) {

        // Get username from enterNameEditText field
        String userName = enterNameEditText.getText().toString();

        // Check if the user had entered his/her name
        // If not, then a warning-toast will display to tell the user input his/her name
        // If yes, then allow transition to Quiz Activity through using Intent
        if (userName.isEmpty()) {
            Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show();
            enterNameEditText.requestFocus();
        } else {
            Intent intentToQuizActivity = new Intent(this, QuizActivity.class);
            intentToQuizActivity.putExtra("USER_NAME", userName);
            startActivity(intentToQuizActivity);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Mapping declared private Java variables with their corresponding View (.xml)
        enterNameEditText = findViewById(R.id.enterNameEditText);
        startButton = findViewById(R.id.jumpToQuizActivityButton);
        calculatorButton = findViewById(R.id.jumpToCalculatorButton);

        // Event-handling after "Start" button is clicked
        // It will call 'jumpClickToQuizActivity()' function with current view is passed in
        startButton.setOnClickListener(v -> jumpClickToQuizActivity(v));

        // Event-handling after "Calculator" button is clicked
        calculatorButton.setOnClickListener(v -> {
            Intent intentToCalculatorActivity = new Intent(MainActivity.this, CalculatorActivity.class);
            startActivity(intentToCalculatorActivity);
            finish();
        });
    }
}