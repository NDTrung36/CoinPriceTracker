package com.trung.coinpricetracker;

import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import retrofit2.Response;

import java.io.IOException;

@Service
@Log4j2
public class BinanceService {

    private final BinanceApi binanceApi;
    private final TelegramApi telegramApi;

    private final String BOT_TOKEN = "8984573120:AAHNUacQ6wk-dExt-EX1oQz7VpbDQrzJkzE";
    private final String CHAT_ID = "7672916303";

    public BinanceService(BinanceApi binanceApi, TelegramApi telegramApi) {
        this.binanceApi = binanceApi;
        this.telegramApi = telegramApi;
    }

    @Async
    public void trackCoinPriceContinuously(String coinSymbol) {
        Double lastPrice = null;

        while (true) {
            try {
                //dùng execute() gọi mạng đồng bộ
                //Response<CoinPrice> response = binanceApi.getCoinPrice(coinSymbol).execute();
                // Bước 1: Gọi hàm để tạo ra đối tượng Call (chưa chạy mạng)
                retrofit2.Call<CoinPrice> coinPriceCall = binanceApi.getCoinPrice(coinSymbol);

                // Bước 2: Thực thi đối tượng Call đó để lấy về Response (chạy mạng đồng bộ)
                Response<CoinPrice> response = coinPriceCall.execute();


                if (response.isSuccessful() && response.body() != null) {
                    CoinPrice coinPrice = response.body();
                    double currentPrice = Double.parseDouble(coinPrice.getPrice());

                    if (lastPrice != null && currentPrice != lastPrice) {
                        log.warn("🚨 PHÁT HIỆN BIẾN ĐỘNG GIÁ (RETROFIT): {} từ {} -> {}", coinSymbol, lastPrice, currentPrice);
                        sendTelegramMessage(coinSymbol, lastPrice, currentPrice);
                    }
                    else {
                        log.info("Giá {} không đổi (Retrofit): {}", coinSymbol, currentPrice);
                    }
                    lastPrice = currentPrice;
                }
                else {
                    log.error("Binance trả về lỗi HTTP: {} - {}", response.code(), response.message());
                }

                Thread.sleep(3000);

            }
            catch (IOException e) {
                log.error("Lỗi kết nối mạng khi gọi Retrofit cho {}: {}", coinSymbol, e.getMessage());
                try { Thread.sleep(3000); }
                catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
            }
            catch (Exception e) {
                log.error("Lỗi xử lý luồng {}: {}", coinSymbol, e.getMessage());
                try { Thread.sleep(3000); }
                catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
            }
        }
    }

    private void sendTelegramMessage(String symbol, double oldPrice, double newPrice) {
        String state = (newPrice > oldPrice) ? "📈 TĂNG" : "📉 GIẢM";
        String content = String.format("Thông báo: %s vừa %s! Giá cũ: %.4f -> Giá mới: %.4f",
                symbol, state, oldPrice, newPrice);

        try {
            Response<Object> response = telegramApi.sendMessage(BOT_TOKEN, CHAT_ID, content).execute();

            if (response.isSuccessful()) {
                log.info("[TELEGRAM-RETROFIT] Đã gửi thông báo thành công cho {}", symbol);
            }
            else {
                log.error("[LỖI TELEGRAM-RETROFIT] Telegram trả về lỗi mã: {} - {}", response.code(), response.message());
            }
        }
        catch (IOException e) {
            log.error("[LỖI MẠNG TELEGRAM-RETROFIT] Không thể kết nối tới Telegram: {}", e.getMessage());
        }
    }
}
