package com.trung.coinpricetracker;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Configuration
public class RetrofitConfig {

    @Bean
    public BinanceApi binanceApi() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.binance.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return retrofit.create(BinanceApi.class);
    }

    @Bean
    public TelegramApi telegramApi() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.telegram.org/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return retrofit.create(TelegramApi.class);
    }
}
