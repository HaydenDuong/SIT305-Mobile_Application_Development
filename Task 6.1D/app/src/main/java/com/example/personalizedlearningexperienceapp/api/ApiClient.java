package com.example.personalizedlearningexperienceapp.api;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    // Android Emulator will mapping "127.0.0.1:5000" (where the backend LLM running)
    // to "10.0.2.2:5000"
    private static final String BASE_URL = "http://10.0.2.2:5000";
    private static Retrofit retrofit = null;
    private static QuizApi quizApi = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(100, TimeUnit.SECONDS)
                    .readTimeout(100, TimeUnit.SECONDS)
                    .writeTimeout(100, TimeUnit.SECONDS)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }

        return retrofit;
    }

    public static QuizApi getQuizApi() {
        if (quizApi == null) {
            quizApi = getClient().create(QuizApi.class);
        }
        return quizApi;
    }
}
