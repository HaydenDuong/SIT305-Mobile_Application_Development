package com.example.personalizedlearningexperienceapp.fragments; // Or your viewmodels package

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

    public HistoryViewModel(@NonNull Application application) {
        super(application);
        quizRepository = new QuizRepository(application);
    }

    public LiveData<List<QuizAttemptEntity>> getQuizAttemptsLiveData() {
        return quizAttemptsLiveData;
    }

    public void fetchQuizAttemptsForUser(String userId) {
        quizRepository.getQuizAttemptsForUser(userId, new QuizRepository.OnQuizAttemptsRetrievedListener() {
            @Override
            public void onRetrieved(List<QuizAttemptEntity> attempts) {
                // Post value to LiveData from the background thread callback
                quizAttemptsLiveData.postValue(attempts);
            }
        });
    }
} 