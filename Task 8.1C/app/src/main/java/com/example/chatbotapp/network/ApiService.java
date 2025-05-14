package com.example.chatbotapp.network;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {

    @POST("chat")
    Call<String> sendMessage(@Body String userMessage);
}
