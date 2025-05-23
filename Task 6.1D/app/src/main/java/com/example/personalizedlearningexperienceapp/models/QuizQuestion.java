package com.example.personalizedlearningexperienceapp.models;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class QuizQuestion implements Parcelable {
    @SerializedName("question")
    private String question;
    @SerializedName("options")
    private List<String> options;
    @SerializedName("correct_answer")
    private String correctAnswer;
    private String userSelectedAnswer;

    // Constructor for Parcelable
    protected QuizQuestion(Parcel in) {
        question = in.readString();
        options = in.createStringArrayList();
        correctAnswer = in.readString();
        userSelectedAnswer = in.readString();
    }

    // Parcelable Creator
    public static final Creator<QuizQuestion> CREATOR = new Creator<QuizQuestion>() {
        @Override
        public QuizQuestion createFromParcel(Parcel in) {
            return new QuizQuestion(in);
        }

        @Override
        public QuizQuestion[] newArray(int size) {
            return new QuizQuestion[size];
        }
    };

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

    public String getUserSelectedAnswer() {
        return userSelectedAnswer;
    }

    public void setUserSelectedAnswer(String userSelectedAnswer) {
        this.userSelectedAnswer = userSelectedAnswer;
    }

    // Describe contents (usually returns 0)
    @Override
    public int describeContents() {
        return 0;
    }

    // Write to parcel
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(question);
        dest.writeStringList(options);
        dest.writeString(correctAnswer);
        dest.writeString(userSelectedAnswer);
    }

    public QuizQuestion(String question, List<String> options, String correctAnswer) {
        this.question = question;
        this.options = options;
        this.correctAnswer = correctAnswer;
        this.userSelectedAnswer = null;
    }
}