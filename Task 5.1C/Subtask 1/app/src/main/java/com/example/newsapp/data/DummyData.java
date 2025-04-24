package com.example.newsapp.data;

import com.example.newsapp.R;

import java.util.Arrays;
import java.util.List;

public class DummyData {
    // Top Stories
    public static final List<News> TOP_STORIES = Arrays.asList(
            new News(1, R.drawable.news1, "Spotify Launches New Feature", "Spotify introduces Friends Mix, connecting friends through shared music."),
            new News(2, R.drawable.news2, "Chirac's Quirky Habit", "Former President Chirac's odd behavior ignites public discussion."),
            new News(3, R.drawable.news3, "Cautions Return To Normal Life Begins", "Post-pandemic life resumes with careful steps, experts advise caution")
    );

    public static final List<News> NEWS_LIST = Arrays.asList(
            new News(4, R.drawable.news4, "title 4", "description 4"),
            new News(5, R.drawable.news5, "title 5", "description 5"),
            new News(6, R.drawable.news6, "title 6", "description 6")
    );
}
