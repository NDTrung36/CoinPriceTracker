package com.trung.coinpricetracker;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class BinanceService {

    @Async // (1) Phép thuật ở đây: Lệnh này bảo Spring hãy tách hàm này ra chạy ở một Luồng (Thread) riêng!
    public void trackCoinPriceContinuously(String coinSymbol) {
        RestTemplate restTemplate = new RestTemplate();
        String url = "https://api.binance.com/api/v3/ticker/price?symbol=" + coinSymbol;

        // (2) Vòng lặp vô tận: Chạy liên tục không dừng
        while (true) {
            try {
                CoinPrice response = restTemplate.getForObject(url, CoinPrice.class);
                if (response != null) {
                    // (3) In ra tên của Luồng (Thread) đang chạy để dễ quan sát
                    String threadName = Thread.currentThread().getName();
                    System.out.println("[" + threadName + "] Giá " + response.getSymbol() + " : " + response.getPrice());
                }

                // (4) Lệnh Sleep: Ép luồng này ngủ đúng 3 giây (3000 milliseconds) rồi mới chạy lại vòng lặp
                Thread.sleep(3000);

            } catch (Exception e) {
                System.out.println("Lỗi khi lấy giá " + coinSymbol + ": " + e.getMessage());
                // Nếu bị lỗi mạng, cũng phải cho luồng ngủ 3 giây rồi mới thử lại, tránh spam gây sập máy
                try { Thread.sleep(3000); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
            }
        }
    }
}
