package com.example.quizapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;

import java.util.List;


public class QuizActivity extends AppCompatActivity {

    // Define private Java Variables
    private TextView welcomeText, questionNumber, questionTitle, questionText;
    private Button answer1Button, answer2Button, answer3Button, submitButton;
    private ProgressBar progressBar;
    private List<Question> questions;
    private int currentQuestionIndex = 0;
    private int selectedAnswerIndex = -1;
    private int score = 0;
    private boolean isSubmitted = false;

    // Functions that handling logic in this Activity:

    // Function for returning the selected button for changing its property: background-color
    // Used in conjunction with 'checkAnswer()' function
    private Button getAnswerButton(int index) {
        switch (index) {
            case 0: return answer1Button;
            case 1: return answer2Button;
            case 2: return answer3Button;
            default: return null;
        }
    }

    // Function for checking a selected button & change that button background-color accordingly
    // Green for correct answer
    // Red for wrong answer
    private void checkAnswer() {
        Question currentQuestion = questions.get(currentQuestionIndex);
        int correctAnswerIndex = currentQuestion.getCorrectAnswerIndex();

        // Check if the selected answer is a correct one or not
        // If true, the selected answer button will turn to green
        // If false, the selected answer button will turn to red & display the correct answer colored with green
        if (selectedAnswerIndex == correctAnswerIndex) {
            getAnswerButton(selectedAnswerIndex).setBackgroundTintList(getResources().getColorStateList(android.R.color.holo_green_light));
            score++;
        } else {
            getAnswerButton(selectedAnswerIndex).setBackgroundTintList(getResources().getColorStateList(android.R.color.holo_red_light));
            getAnswerButton(correctAnswerIndex).setBackgroundTintList(getResources().getColorStateList(android.R.color.holo_green_light));
        }
    }

    // Function for reset all button colors from previous selection (if any)
    private void resetButtonColors() {
        answer1Button.setBackgroundTintList(null);
        answer2Button.setBackgroundTintList(null);
        answer3Button.setBackgroundTintList(null);
    }

    // Function for displaying question from object 'questions' - created from QuestionBank class
    private void displayQuestion() {
        Question currentQuestion = questions.get(currentQuestionIndex);
        questionNumber.setText((currentQuestionIndex + 1) + "/" + questions.size());
        questionTitle.setText(currentQuestion.getQuestionTitle());
        questionText.setText(currentQuestion.getQuestionText());

        answer1Button.setText(currentQuestion.getAnswers()[0]);
        answer2Button.setText(currentQuestion.getAnswers()[1]);
        answer3Button.setText(currentQuestion.getAnswers()[2]);

        progressBar.setProgress(currentQuestionIndex + 1);

        // Reset 'selectedAnswerIndex' from previous selection
        selectedAnswerIndex = -1;

        // Reset button colors from the previous selection
        resetButtonColors();
    }

    // Function for generating the overall structure of this activity:
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        // Mapping declared private Java Variables with their .xml elements
        welcomeText = findViewById(R.id.welcomeUserName);
        questionNumber = findViewById(R.id.currentQuestionNumber);
        questionTitle = findViewById(R.id.questionTitle);
        questionText = findViewById(R.id.questionText);
        answer1Button = findViewById(R.id.answer1Button);
        answer2Button = findViewById(R.id.answer2Button);
        answer3Button = findViewById(R.id.answer3Button);
        submitButton = findViewById(R.id.submitButton);
        progressBar = findViewById(R.id.progessBar);

        // Display username in 'WelcomeText' by getting 'saved' value from intentToQuizActivity
        String userName = getIntent().getStringExtra("USER_NAME");
        welcomeText.setText("Welcome " + userName + "!");

        // Create object 'questionBank' from class 'QuestionBank'
        QuestionBank questionBank = new QuestionBank();
        questions = questionBank.getQuestions();

        // Setup progressBar - start with number 1
        progressBar.setMax(questions.size());
        progressBar.setProgress(1);

        // display the question
        displayQuestion();

        // Event-handling for selecting a answer button
        answer1Button.setOnClickListener(v -> {
            if (!isSubmitted) {
                resetButtonColors();
                selectedAnswerIndex = 0;
                answer1Button.setBackgroundTintList(getResources().getColorStateList(android.R.color.holo_orange_light));
            }
        });

        answer2Button.setOnClickListener(v -> {
            if (!isSubmitted) {
                resetButtonColors();
                selectedAnswerIndex = 1;
                answer2Button.setBackgroundTintList(getResources().getColorStateList(android.R.color.holo_orange_light));
            }
        });

        answer3Button.setOnClickListener(v -> {
            if (!isSubmitted) {
                resetButtonColors();
                selectedAnswerIndex = 2;
                answer3Button.setBackgroundTintList(getResources().getColorStateList(android.R.color.holo_orange_light));
            }
        });

        // Event-handling for selecting Submit / Next / Done button
        submitButton.setOnClickListener(v -> {

            // If not yet click submit button
            // !isSubmitted == 'isSubmitted == false'
            if (!isSubmitted) {

                // Check if an answer button is selected: through checking 'selectedAnswerIndex' value
                // 'selectedAnswerIndex' != -1 means an answer button is selected
                // 'selectedAnswerIndex' = -1 means no answer button is selected
                if (selectedAnswerIndex != -1) {
                    checkAnswer();
                    isSubmitted = true;
                    currentQuestionIndex++;

                    // Check if the current question number has react the end-question of question list
                    // If not, the text on submit button is changes to "Next" to move to new question
                    // If yes, the test on submit button is changes to "Done" to move to ResultActivity
                    if (currentQuestionIndex < questions.size()) {
                        submitButton.setText("Next");
                    } else {
                        submitButton.setText("Done");
                    }

                    // Prevent user from changing his/her selected answer with a new one after submitted the previous one
                    answer1Button.setEnabled(false);
                    answer2Button.setEnabled(false);
                    answer3Button.setEnabled(false);

                } else {
                    Toast.makeText(QuizActivity.this, "Please select an answer", Toast.LENGTH_SHORT).show();
                }
            } else {
                // If the submit button is clicked and its text had been changed
                // the current question is not yet the last question of the list
                if (currentQuestionIndex < questions.size()) {
                    displayQuestion();
                    isSubmitted = false;
                    submitButton.setText("Submit");

                    // Re-enable for selecting answer button
                    answer1Button.setEnabled(true);
                    answer2Button.setEnabled(true);
                    answer3Button.setEnabled(true);
                }

                // If the previous question is the last question of the list
                else {
                    Intent intentToResultActivity = new Intent(QuizActivity.this, ResultActivity.class);
                    intentToResultActivity.putExtra("USER_NAME", userName);
                    intentToResultActivity.putExtra("SCORE", score);
                    intentToResultActivity.putExtra("TOTAL_QUESTIONS", questions.size());
                    startActivity(intentToResultActivity);

                    // Finish QuizActivity (current opened activity)
                    finish();
                }
            }
        });

    }
}