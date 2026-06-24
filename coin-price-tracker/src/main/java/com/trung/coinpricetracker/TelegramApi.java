package com.trung.coinpricetracker;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface TelegramApi {

    @GET("/bot{token}/sendMessage")
    Call<Object> sendMessage(
            @Path("token") String botToken,
            @Query("chat_id") String chatId,
            @Query("text") String text
    );
}
