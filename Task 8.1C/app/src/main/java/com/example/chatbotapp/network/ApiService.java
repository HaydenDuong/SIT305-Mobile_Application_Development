package com.example.chatbotapp.network;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface ApiService {
    @FormUrlEncoded
    @POST("chat")
    Call<String> sendMessage(
            @Field("user_id") String userId,
            @Field("userMessage") String userMessage
    );
}
