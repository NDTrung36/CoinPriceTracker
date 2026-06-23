package com.trung.coinpricetracker;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface TelegramApi {

    // (1) Dùng {token} để đánh dấu vị trí sẽ chèn chuỗi động vào URL
    @GET("/bot{token}/sendMessage")
    Call<Object> sendMessage(
            @Path("token") String botToken, // (2) @Path sẽ tự động nhét biến này vào chỗ {token} ở trên
            @Query("chat_id") String chatId,
            @Query("text") String text
    );
}
