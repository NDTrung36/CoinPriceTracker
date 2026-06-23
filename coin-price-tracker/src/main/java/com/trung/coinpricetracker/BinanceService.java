package com.trung.coinpricetracker;

import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import retrofit2.Response; // (1) Hãy chú ý import đúng class Response của Retrofit

import java.io.IOException;

@Service
@Log4j2
public class BinanceService {

    // (2) Thay vì RestTemplate, ta tiêm Interface Retrofit vào đây thông qua Constructor
    private final BinanceApi binanceApi;
    private final TelegramApi telegramApi;

    private final String BOT_TOKEN = "8984573120:AAHNUacQ6wk-dExt-EX1oQz7VpbDQrzJkzE";
    private final String CHAT_ID = "7672916303";

    // Constructor Injection chuẩn chỉnh
    public BinanceService(BinanceApi binanceApi, TelegramApi telegramApi) {
        this.binanceApi = binanceApi;
        this.telegramApi = telegramApi;
    }

    @Async
    public void trackCoinPriceContinuously(String coinSymbol) {
        Double lastPrice = null;

        while (true) {
            try {
                // (3) Đọc bản thiết kế và phát lệnh gọi mạng ĐỒNG BỘ bằng hàm .execute()
                // Vì hàm này đang chạy trong luồng @Async riêng nên việc gọi đồng bộ .execute() là hoàn toàn an toàn, không sợ nghẽn mạch chính.
                Response<CoinPrice> response = binanceApi.getCoinPrice(coinSymbol).execute();

                // (4) Kiểm tra xem Server Binance có trả về mã 200 OK thành công không
                if (response.isSuccessful() && response.body() != null) {

                    CoinPrice coinPrice = response.body(); // (5) Mở hộp lấy dữ liệu đã được Gson map sẵn
                    double currentPrice = Double.parseDouble(coinPrice.getPrice());

                    if (lastPrice != null && currentPrice != lastPrice) {
                        log.warn("🚨 PHÁT HIỆN BIẾN ĐỘNG GIÁ (RETROFIT): {} từ {} -> {}", coinSymbol, lastPrice, currentPrice);
                        sendTelegramMessage(coinSymbol, lastPrice, currentPrice);
                    } else {
                        log.info("Giá {} không đổi (Retrofit): {}", coinSymbol, currentPrice);
                    }
                    lastPrice = currentPrice;
                } else {
                    // Nếu lỗi (ví dụ sai symbol coin), Retrofit không crash mà nhảy vào đây, trả về mã lỗi (400, 404, v.v.)
                    log.error("Binance trả về lỗi HTTP: {} - {}", response.code(), response.message());
                }

                Thread.sleep(3000);

            } catch (IOException e) {
                // (6) Lỗi kết nối mạng, mất wifi, đứt cáp... khi dùng Retrofit sẽ ném ra IOException
                log.error("Lỗi kết nối mạng mạng khi gọi Retrofit cho {}: {}", coinSymbol, e.getMessage());
                try { Thread.sleep(3000); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
            } catch (Exception e) {
                log.error("Lỗi xử lý luồng {}: {}", coinSymbol, e.getMessage());
                try { Thread.sleep(3000); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
            }
        }
    }

    // (3) LÀM MỚI HÀM NÀY: Chuyển hoàn toàn sang sử dụng Retrofit
    private void sendTelegramMessage(String symbol, double oldPrice, double newPrice) {
        String state = (newPrice > oldPrice) ? "📈 TĂNG" : "📉 GIẢM";
        String content = String.format("Thông báo: %s vừa %s! Giá cũ: %.4f -> Giá mới: %.4f",
                symbol, state, oldPrice, newPrice);

        try {
            // (4) Truyền các tham số động vào hàm và phát lệnh .execute() đồng bộ
            Response<Object> response = telegramApi.sendMessage(BOT_TOKEN, CHAT_ID, content).execute();

            // (5) Kiểm tra xem Telegram nhận tin nhắn có thành công (Mã 200) hay không
            if (response.isSuccessful()) {
                log.info("[TELEGRAM-RETROFIT] Đã gửi thông báo thành công cho {}", symbol);
            } else {
                log.error("[LỖI TELEGRAM-RETROFIT] Telegram trả về lỗi mã: {} - {}", response.code(), response.message());
            }
        } catch (IOException e) {
            // Lỗi kết nối mạng riêng của Telegram
            log.error("[LỖI MẠNG TELEGRAM-RETROFIT] Không thể kết nối tới Telegram: {}", e.getMessage());
        }
    }
}