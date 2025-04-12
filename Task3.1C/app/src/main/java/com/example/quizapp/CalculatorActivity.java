package com.example.quizapp;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;

public class CalculatorActivity extends AppCompatActivity {

    // Declare private Java variables
    private EditText inputFieldA, inputFieldB;
    private Button plusButton, minusButton, returnToMainButton;
    private TextView displayResultText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculator);

        // Mapping declared Java variables with their .xml elements
        inputFieldA = findViewById(R.id.inputFieldA);
        inputFieldB = findViewById(R.id.inputFieldB);
        displayResultText = findViewById(R.id.resultDisplayText);
        plusButton = findViewById(R.id.plusButton);
        minusButton = findViewById(R.id.minusButton);
        returnToMainButton = findViewById(R.id.returnToMainButton);

        // Event-handling for plus button
        plusButton.setOnClickListener(v -> {
            plusButton.setBackgroundTintList(getResources().getColorStateList(android.R.color.holo_orange_light));
            minusButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#2196F3")));

            // Extract values from two input fields (A & B)
            String inputA = inputFieldA.getText().toString().trim();
            String inputB = inputFieldB.getText().toString().trim();

            // Check if there are values input into the fields of value A & B
            if (inputA.isEmpty() || inputB.isEmpty()) {
                Toast.makeText(CalculatorActivity.this, "Please enter values", Toast.LENGTH_SHORT).show();
            } else {
                // Converting String input values into double-type value
                double valueA = Double.parseDouble(inputA);
                double valueB = Double.parseDouble(inputB);

                double result = valueA + valueB;
                displayResultText.setText(String.valueOf(result));
            }
        });

        // Event-handling for minus button
        minusButton.setOnClickListener(v -> {

            minusButton.setBackgroundTintList(getResources().getColorStateList(android.R.color.holo_orange_light));
            plusButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#2196F3")));

            // Extract values from two input fields (A & B)
            String inputA = inputFieldA.getText().toString().trim();
            String inputB = inputFieldB.getText().toString().trim();

            // Check if there are values input into the fields of value A & B
            if (inputA.isEmpty() || inputB.isEmpty()) {
                Toast.makeText(CalculatorActivity.this, "Please enter values", Toast.LENGTH_SHORT).show();
            } else {
                // Converting String input values into double-type value
                double valueA = Double.parseDouble(inputA);
                double valueB = Double.parseDouble(inputB);

                double result = valueA - valueB;
                displayResultText.setText(String.valueOf(result));
            }
        });

        // Event-handling for 'Return to Main Page'
        returnToMainButton.setOnClickListener(v -> {
            Intent intentToMainPage = new Intent(CalculatorActivity.this, MainActivity.class);
            startActivity(intentToMainPage);
            finish();
        });
    }
}