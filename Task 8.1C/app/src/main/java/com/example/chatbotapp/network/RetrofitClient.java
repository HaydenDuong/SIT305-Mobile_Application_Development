package com.example.chatbotapp.network;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class RetrofitClient {

    private static final String BASE_URL = "http://10.0.2.2:5000/";

    private static Retrofit retrofitInstance = null;
    private static ApiService apiServiceInstance = null;

    private static Retrofit getRetrofitInstance() {
        if (retrofitInstance == null) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);


            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(60, TimeUnit.SECONDS) // Set connect timeout
                    .readTimeout(60, TimeUnit.SECONDS)    // Set read timeout
                    .writeTimeout(60, TimeUnit.SECONDS)   // Set write timeout
                    .addInterceptor(loggingInterceptor)
                    .build();

            retrofitInstance = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttpClient) // Set the custom OkHttpClient
                    .addConverterFactory(ScalarsConverterFactory.create()) // For plain text responses
                    .build();
        }
        return retrofitInstance;
    }

    public static ApiService getApiService() {
        if (apiServiceInstance == null) {
            apiServiceInstance = getRetrofitInstance().create(ApiService.class);
        }
        return apiServiceInstance;
    }
}
