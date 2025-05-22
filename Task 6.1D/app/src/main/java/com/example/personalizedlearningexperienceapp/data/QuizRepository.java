package com.example.personalizedlearningexperienceapp.data;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class QuizRepository {
    private QuizAttemptDao quizAttemptDao;
    private QuestionResponseDao questionResponseDao;
    private UserDao userDao; // For user tier info if needed directly by repo
    private ExecutorService executorService;

    public QuizRepository(Application application) {
        AppDatabase db = DatabaseClient.getInstance(application).getAppDatabase();
        quizAttemptDao = db.quizAttemptDao();
        questionResponseDao = db.questionResponseDao();
        userDao = db.userDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    // Insert Quiz Attempt and its Question Responses
    // This should ideally be in a transaction if Room supported it directly for DAOs with different entities easily.
    // For simplicity, we insert attempt then responses.
    public void insertQuizAttemptWithResponses(final QuizAttemptEntity attempt, final List<QuestionResponseEntity> responses) {
        executorService.execute(() -> {
            long attemptId = quizAttemptDao.insertQuizAttempt(attempt);
            // Set the attemptId for each response and insert them
            for (QuestionResponseEntity response : responses) {
                response.quizAttemptId = (int) attemptId;
            }
            questionResponseDao.insertAllQuestionResponses(responses);
        });
    }

    // Get all quiz attempts for a user (for HistoryFragment)
    // Using LiveData would be better for observing changes, but for Day 1, a simple callback/direct list for now.
    public void getQuizAttemptsForUser(String userId, final OnQuizAttemptsRetrievedListener listener) {
        executorService.execute(() -> {
            List<QuizAttemptEntity> attempts = quizAttemptDao.getQuizAttemptsForUser(Integer.parseInt(userId)); // Assuming userId is passed as String
            if (listener != null) {
                listener.onRetrieved(attempts);
            }
        });
    }

    public interface OnQuizAttemptsRetrievedListener {
        void onRetrieved(List<QuizAttemptEntity> attempts);
    }

    // Get total questions answered by user
    public void getTotalQuestionsAnswered(String userId, final OnStatRetrievedListener listener) {
        executorService.execute(() -> {
            int total = questionResponseDao.getTotalQuestionsAnsweredByUser(Integer.parseInt(userId));
            if (listener != null) {
                listener.onStatRetrieved(total);
            }
        });
    }

    // Get total correct answers by user
    public void getTotalCorrectAnswers(String userId, final OnStatRetrievedListener listener) {
        executorService.execute(() -> {
            int correct = questionResponseDao.getTotalCorrectAnswersByUser(Integer.parseInt(userId));
            if (listener != null) {
                listener.onStatRetrieved(correct);
            }
        });
    }
    
    // Get User Tier
    public void getUserTier(String userId, final OnUserTierRetrievedListener listener) {
        executorService.execute(() -> {
            String tier = userDao.getCurrentTierByUserId(Integer.parseInt(userId));
            if (listener != null) {
                listener.onTierRetrieved(tier != null ? tier : "starter");
            }
        });
    }

    public interface OnStatRetrievedListener {
        void onStatRetrieved(int value);
    }
    
    public interface OnUserTierRetrievedListener {
        void onTierRetrieved(String tier);
    }

    // Method to update user tier (will be used on Day 4)
    public void updateUserTier(String userId, String newTier) {
        executorService.execute(() -> {
            userDao.updateUserTier(Integer.parseInt(userId), newTier);
        });
    }
    
    // Method to get UserEntity (could be useful for Profile)
    public void getUser(String userId, final OnUserRetrievedListener listener) {
        executorService.execute(() -> {
            User user = userDao.getUserById(Integer.parseInt(userId));
            if (listener != null) {
                listener.onUserRetrieved(user);
            }
        });
    }

    public interface OnUserRetrievedListener {
        void onUserRetrieved(User user);
    }

    // Callback for a single QuizAttemptEntity
    public interface QuizAttemptCallback {
        void onAttemptLoaded(QuizAttemptEntity attempt);
    }

    // Callback for a list of QuestionResponseEntity
    public interface QuestionResponsesCallback {
        void onResponsesLoaded(List<QuestionResponseEntity> responses);
    }

    public void getQuizAttemptById(final int attemptId, final QuizAttemptCallback callback) {
        executorService.execute(() -> {
            final QuizAttemptEntity attempt = quizAttemptDao.getAttemptById(attemptId);
            new Handler(Looper.getMainLooper()).post(() -> {
                if (callback != null) {
                    callback.onAttemptLoaded(attempt);
                }
            });
        });
    }

    public void getResponsesForAttempt(final int attemptId, final QuestionResponsesCallback callback) {
        executorService.execute(() -> {
            // Assuming your DAO method is getResponsesForQuizAttempt as per your QuestionResponseDao.java
            final List<QuestionResponseEntity> responses = questionResponseDao.getResponsesForQuizAttempt(attemptId);
            new Handler(Looper.getMainLooper()).post(() -> {
                if (callback != null) {
                    callback.onResponsesLoaded(responses);
                }
            });
        });
    }
} 