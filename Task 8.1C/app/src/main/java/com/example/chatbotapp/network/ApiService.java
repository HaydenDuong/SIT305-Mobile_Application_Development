package com.example.chatbotapp.network;

import com.example.chatbotapp.responses.InterestsResponse;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {
    @FormUrlEncoded
    @POST("chat")
    Call<String> sendMessage(
            @Field("user_id") String userId,
            @Field("displayName") String displayName,
            @Field("userMessage") String userMessage
    );

    @GET("user_interests")
    Call<InterestsResponse> getUserInterests(@Query("uid") String userId);

    @FormUrlEncoded
    @POST("add_interest")
    Call<Void> addInterest(
            @Field("user_id") String userId,
            @Field("interest") String interest
    );

    @FormUrlEncoded
    @POST("delete_interest")
    Call<Void> deleteInterest(
            @Field("user_id") String userId,
            @Field("interest") String interest
    );


}
