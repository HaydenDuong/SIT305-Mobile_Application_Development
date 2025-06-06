package com.example.personalizedlearningexperienceapp.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.personalizedlearningexperienceapp.data.QuizAttemptEntity;
import com.example.personalizedlearningexperienceapp.data.QuestionResponseEntity;
import com.example.personalizedlearningexperienceapp.data.QuizRepository;
import java.util.List;

public class QuizAttemptDetailViewModel extends AndroidViewModel {

    private final QuizRepository quizRepository;
    private final MutableLiveData<QuizAttemptEntity> quizAttempt = new MutableLiveData<>();
    private final MutableLiveData<List<QuestionResponseEntity>> questionResponses = new MutableLiveData<>();

    public QuizAttemptDetailViewModel(@NonNull Application application) {
        super(application);
        quizRepository = new QuizRepository(application);
    }

    public LiveData<QuizAttemptEntity> getQuizAttempt() {
        return quizAttempt;
    }

    public LiveData<List<QuestionResponseEntity>> getQuestionResponses() {
        return questionResponses;
    }

    public void loadQuizAttemptDetails(int quizAttemptId) {
        quizRepository.getQuizAttemptById(quizAttemptId, attempt -> {
            if (attempt != null) {
                quizAttempt.postValue(attempt);
                quizRepository.getResponsesForAttempt(attempt.id, responses -> {
                    questionResponses.postValue(responses);
                });
            } else {
                quizAttempt.postValue(null);
                questionResponses.postValue(null);
            }
        });
    }
}