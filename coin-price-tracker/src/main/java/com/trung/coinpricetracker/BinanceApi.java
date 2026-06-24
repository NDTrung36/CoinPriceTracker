package com.trung.coinpricetracker;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface BinanceApi {

    @GET("api/v3/ticker/price")
    Call<CoinPrice> getCoinPrice(@Query("symbol") String coinSymbol);

}
