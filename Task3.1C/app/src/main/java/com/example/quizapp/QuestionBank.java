package com.example.quizapp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QuestionBank {

    // Programming to interface in Java
    // List in an interface, while ArrayList is a specific implementation
    // 'questions' can easily be change to other forms of List, e.g: LinkedList
    private List<Question> questions = new ArrayList<>();

    // Adding questions to variable 'questions'
    public QuestionBank() {
        questions.add(new Question("Android's Natural","What is Android?", new String[]{"A mobile OS", "A programming language", "A database"}, 0));
        questions.add(new Question("Android's Language","What language is used for Android apps?", new String[]{"Python", "Java/Kotlin", "C++"}, 1));
        questions.add(new Question("Android's Knowledge","What is an Activity?", new String[]{"A database", "A UI screen", "A service"}, 1));
        questions.add(new Question("Android's Layout","What is the main layout used in Android?", new String[]{"LinearLayout", "GridLayout", "TableLayout"}, 0));
        questions.add(new Question("Android's File","What is the Android manifest file used for?", new String[]{"Styling the app", "Defining app components", "Storing data"}, 1));

        // Shuffle the order of added questions in obj 'questions'
        // to: every new quiz process, these questions will display randomly
        Collections.shuffle(questions);
    }

    public List<Question> getQuestions() {
        return questions;
    }
}
