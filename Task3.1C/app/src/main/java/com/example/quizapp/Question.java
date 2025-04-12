package com.example.quizapp;

import java.io.Serializable;

public class Question  implements Serializable {
    private String questionTitle;
    private String questionText;
    private String[] answers;
    private int correctAnswerIndex;

    public Question(String questionTitle, String questionText, String[] answers, int correctAnswerIndex) {
        this.questionTitle = questionTitle;
        this.questionText = questionText;
        this.answers = answers;
        this.correctAnswerIndex = correctAnswerIndex;
    }

    public String getQuestionTitle() {
        return questionTitle;
    }

    public String getQuestionText() {
        return questionText;
    }

    public String[] getAnswers() {
        return answers;
    }

    public int getCorrectAnswerIndex() {
        return correctAnswerIndex;
    }
}
