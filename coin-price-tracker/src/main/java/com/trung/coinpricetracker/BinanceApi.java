package com.trung.coinpricetracker;
import com.trung.coinpricetracker.CoinPrice;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface BinanceApi {

    // Đường dẫn API của Binance (Không bao gồm phần domain https://api.binance.com gốc)
    @GET("api/v3/ticker/price")
    Call<CoinPrice> getCoinPrice(@Query("symbol") String coinSymbol);

}