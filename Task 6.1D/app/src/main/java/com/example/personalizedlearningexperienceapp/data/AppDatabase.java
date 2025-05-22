package com.example.personalizedlearningexperienceapp.data;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {User.class, UserTopic.class, QuizAttemptEntity.class, QuestionResponseEntity.class}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract UserDao userDao();
    public abstract UserTopicDao userTopicDao();
    public abstract QuizAttemptDao quizAttemptDao();
    public abstract QuestionResponseDao questionResponseDao();
}
