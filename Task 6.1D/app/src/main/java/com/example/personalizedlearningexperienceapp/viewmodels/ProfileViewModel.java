package com.example.personalizedlearningexperienceapp.viewmodels; // Or .models

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.personalizedlearningexperienceapp.data.QuizRepository;
import com.example.personalizedlearningexperienceapp.data.User;
import com.example.personalizedlearningexperienceapp.fragments.SignUpFragment;

public class ProfileViewModel extends AndroidViewModel {

    private final QuizRepository quizRepository;
    private final String currentUserId;

    private final MutableLiveData<User> userLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> usernameLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> emailLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> accountTierLiveData = new MutableLiveData<>();
    private final MutableLiveData<Integer> totalQuestionsAnsweredLiveData = new MutableLiveData<>();
    private final MutableLiveData<Integer> correctAnswersLiveData = new MutableLiveData<>();
    private final MutableLiveData<Integer> incorrectAnswersLiveData = new MutableLiveData<>();

    public ProfileViewModel(@NonNull Application application) {
        super(application);
        quizRepository = new QuizRepository(application);

        SharedPreferences prefs = application.getSharedPreferences(SignUpFragment.PREFS_NAME, Context.MODE_PRIVATE);
        int userIdInt = prefs.getInt(SignUpFragment.KEY_USER_ID, SignUpFragment.DEFAULT_USER_ID);

        if (userIdInt != SignUpFragment.DEFAULT_USER_ID) {
            this.currentUserId = String.valueOf(userIdInt);
        } else {
            this.currentUserId = null; // No valid user
        }
    }

    // LiveData getters for the Fragment to observe
    public LiveData<User> getUserLiveData() { return userLiveData; }
    public LiveData<String> getUsernameLiveData() { return usernameLiveData; }
    public LiveData<String> getEmailLiveData() { return emailLiveData; }
    public LiveData<String> getAccountTierLiveData() { return accountTierLiveData; }
    public LiveData<Integer> getTotalQuestionsAnsweredLiveData() { return totalQuestionsAnsweredLiveData; }
    public LiveData<Integer> getCorrectAnswersLiveData() { return correctAnswersLiveData; }
    public LiveData<Integer> getIncorrectAnswersLiveData() { return incorrectAnswersLiveData; }

    public void loadProfileData() {
        if (currentUserId == null) {
            // Post null or default/error states if no user is logged in
            userLiveData.postValue(null); // Or a default User object
            usernameLiveData.postValue("N/A");
            emailLiveData.postValue("N/A");
            accountTierLiveData.postValue("N/A");
            totalQuestionsAnsweredLiveData.postValue(0);
            correctAnswersLiveData.postValue(0);
            incorrectAnswersLiveData.postValue(0);
            return;
        }

        // Fetch User details
        quizRepository.getUser(currentUserId, user -> {
            userLiveData.postValue(user); // User object contains username, email
        });

        // Fetch Account Tier
        quizRepository.getUserTier(currentUserId, tier -> {
            accountTierLiveData.postValue(tier);
        });

        // Fetch Total Questions Answered
        quizRepository.getTotalQuestionsAnswered(currentUserId, total -> {
            totalQuestionsAnsweredLiveData.postValue(total);
            Integer correct = correctAnswersLiveData.getValue();
            if (correct != null) {
                incorrectAnswersLiveData.postValue(total - correct);
            }
        });

        // Fetch Correct Answers
        quizRepository.getTotalCorrectAnswers(currentUserId, correct -> {
            correctAnswersLiveData.postValue(correct);
            // Calculate incorrect answers once both total and correct are fetched
            Integer total = totalQuestionsAnsweredLiveData.getValue();
            if (total != null) {
                incorrectAnswersLiveData.postValue(total - correct);
            }
        });
    }
}