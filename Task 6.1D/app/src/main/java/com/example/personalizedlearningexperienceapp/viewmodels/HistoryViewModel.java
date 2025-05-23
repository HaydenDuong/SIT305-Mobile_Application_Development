package com.example.personalizedlearningexperienceapp.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.personalizedlearningexperienceapp.data.QuizAttemptEntity;
import com.example.personalizedlearningexperienceapp.data.QuizRepository;
import java.util.List;

public class HistoryViewModel extends AndroidViewModel {
    private QuizRepository quizRepository;
    private MutableLiveData<List<QuizAttemptEntity>> quizAttemptsLiveData = new MutableLiveData<>();
    private String currentActiveUserId;

    public HistoryViewModel(@NonNull Application application) {
        super(application);
        quizRepository = new QuizRepository(application);
    }

    public LiveData<List<QuizAttemptEntity>> getQuizAttemptsLiveData() {
        return quizAttemptsLiveData;
    }

    public void fetchQuizAttemptsForUser(String userId) {
        this.currentActiveUserId = userId;
        if (userId == null) {
            quizAttemptsLiveData.postValue(new java.util.ArrayList<>());
            return;
        }
        quizRepository.getQuizAttemptsForUser(userId, new QuizRepository.OnQuizAttemptsRetrievedListener() {
            @Override
            public void onRetrieved(List<QuizAttemptEntity> attempts) {
                quizAttemptsLiveData.postValue(attempts);
            }
        });
    }

    public void deleteQuizAttempt(QuizAttemptEntity attempt) {
        if (attempt == null) return;

        quizRepository.deleteQuizAttempt(attempt, new QuizRepository.OnDeletionCompleteListener() {
            @Override
            public void onDeletionComplete() {
                if (currentActiveUserId != null) {
                    fetchQuizAttemptsForUser(currentActiveUserId);
                } else {
                    quizAttemptsLiveData.postValue(new java.util.ArrayList<>());
                }
            }
        });
    }
} 