package com.trung.coinpricetracker;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Configuration // (1) Báo cho Spring biết đây là file chứa các cài đặt hệ thống
public class RetrofitConfig {

    @Bean // (2) Đưa đối tượng được tạo ra từ hàm này vào kho quản lý của Spring (IoC Container)
    public BinanceApi binanceApi() {

        // (3) Xây dựng "Nhà máy" Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.binance.com/") // (4) Địa chỉ gốc (phải kết thúc bằng dấu gạch chéo '/')
                .addConverterFactory(GsonConverterFactory.create()) // (5) Lắp bộ chuyển đổi JSON sang Java Object
                .build();

        // (6) Lệnh này bảo Retrofit: "Hãy đọc Interface BinanceApi và tạo ra cho tôi một Object hoàn chỉnh!"
        return retrofit.create(BinanceApi.class);
    }

    @Bean // Đưa nhà máy Telegram vào kho chứa của Spring
    public TelegramApi telegramApi() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.telegram.org/") // Địa chỉ gốc của Telegram (Nhớ có dấu / ở cuối)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return retrofit.create(TelegramApi.class);
    }
}