package com.example.personalizedlearningexperienceapp.data;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class QuizRepository {
    private QuizAttemptDao quizAttemptDao;
    private QuestionResponseDao questionResponseDao;
    private UserDao userDao;
    private ExecutorService executorService;

    public QuizRepository(Application application) {
        AppDatabase db = DatabaseClient.getInstance(application).getAppDatabase();
        quizAttemptDao = db.quizAttemptDao();
        questionResponseDao = db.questionResponseDao();
        userDao = db.userDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    public void insertQuizAttemptWithResponses(final QuizAttemptEntity attempt, final List<QuestionResponseEntity> responses) {
        executorService.execute(() -> {
            long attemptId = quizAttemptDao.insertQuizAttempt(attempt);
            for (QuestionResponseEntity response : responses) {
                response.quizAttemptId = (int) attemptId;
            }
            questionResponseDao.insertAllQuestionResponses(responses);
        });
    }

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

    public void updateUserTier(String userId, String newTier) {
        executorService.execute(() -> {
            userDao.updateUserTier(Integer.parseInt(userId), newTier);
        });
    }

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
            final List<QuestionResponseEntity> responses = questionResponseDao.getResponsesForQuizAttempt(attemptId);
            new Handler(Looper.getMainLooper()).post(() -> {
                if (callback != null) {
                    callback.onResponsesLoaded(responses);
                }
            });
        });
    }

    // Method to delete a QuizAttempt
    public void deleteQuizAttempt(final QuizAttemptEntity quizAttempt, final OnDeletionCompleteListener listener) {
        executorService.execute(() -> {
            quizAttemptDao.deleteQuizAttempt(quizAttempt);
            new Handler(Looper.getMainLooper()).post(() -> {
                if (listener != null) {
                    listener.onDeletionComplete();
                }
            });
        });
    }

    // Callback for deletion completion
    public interface OnDeletionCompleteListener {
        void onDeletionComplete();
    }

    public interface OnUserFetchedListener {
        void onDataFetched(User user);
    }

    public interface OnStringFetchedListener {
        void onDataFetched(String value);
    }

    public interface OnIntegerFetchedListener {
        void onDataFetched(Integer value);
    }
} 