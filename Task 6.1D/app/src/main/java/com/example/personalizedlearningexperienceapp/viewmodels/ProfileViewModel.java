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
            this.currentUserId = null;
        }
    }

    // LiveData getters for the Fragment to observe
    public LiveData<User> getUserLiveData() { return userLiveData; }
    public LiveData<String> getAccountTierLiveData() { return accountTierLiveData; }
    public LiveData<Integer> getTotalQuestionsAnsweredLiveData() { return totalQuestionsAnsweredLiveData; }
    public LiveData<Integer> getCorrectAnswersLiveData() { return correctAnswersLiveData; }
    public LiveData<Integer> getIncorrectAnswersLiveData() { return incorrectAnswersLiveData; }

    private void calculateIncorrectAnswers() {
        Integer total = totalQuestionsAnsweredLiveData.getValue();
        Integer correct = correctAnswersLiveData.getValue();

        Log.d("ProfileViewModel", "Calculating incorrect answers - Total: " + total + ", Correct: " + correct);

        if (total != null && correct != null) {
            int incorrect = Math.max(0, total - correct);
            Log.d("ProfileViewModel", "Setting incorrect answers to: " + incorrect);
            incorrectAnswersLiveData.setValue(incorrect);
        }
    }

    public void loadProfileData() {
        if (currentUserId == null) {
            userLiveData.postValue(null);
            usernameLiveData.postValue("N/A");
            emailLiveData.postValue("N/A");
            accountTierLiveData.postValue("N/A");
            totalQuestionsAnsweredLiveData.postValue(0);
            correctAnswersLiveData.postValue(0);
            incorrectAnswersLiveData.postValue(0);
            return;
        }

        final String userId = currentUserId;

        // Fetch User details
        quizRepository.getUser(currentUserId, user -> {
            userLiveData.postValue(user);
        });

        // Fetch Account Tier
        quizRepository.getUserTier(currentUserId, tier -> {
            accountTierLiveData.postValue(tier);
        });

        // Fetch Total Questions Answered
        quizRepository.getTotalQuestionsAnswered(currentUserId, total -> {
            totalQuestionsAnsweredLiveData.postValue(total);

            // Calculate after total is updated
            quizRepository.getTotalCorrectAnswers(userId, correct -> {
                correctAnswersLiveData.postValue(correct);
                int incorrect = Math.max(0, total - correct);
                incorrectAnswersLiveData.postValue(incorrect);
            });
        });
    }
}