package com.trung.coinpricetracker;

import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import retrofit2.Response;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@Log4j2
public class BinanceService {

    private final BinanceApi binanceApi;
    private final TelegramApi telegramApi;

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.chat.id}")
    private String chatId;

    @Value("${app.coins}")
    private List<String> coinsToTrack;

    @Value("${app.alert.threshold}")
    private double alertThreshold;

    // Map chứa giá mới nhất do các luồng Worker cập nhật (Thread-safe)
    private final Map<String, Double> currentPricesMap = new ConcurrentHashMap<>();

    // Map chứa giá tại thời điểm gửi Telegram lần cuối cùng
    private final Map<String, Double> lastAlertedPricesMap = new ConcurrentHashMap<>();

    public BinanceService(BinanceApi binanceApi, TelegramApi telegramApi) {
        this.binanceApi = binanceApi;
        this.telegramApi = telegramApi;
    }

    // @PostConstruct ra lệnh cho Spring chạy hàm này ngay sau khi Service được khởi tạo xong
    @PostConstruct
    public void startTrackingSystem() {
        // Khởi tạo ThreadPool có số luồng = Số lượng coin + 1 luồng để gửi Telegram
        int threadCount = coinsToTrack.size() + 1;
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(threadCount);

        log.info("Khởi động hệ thống ThreadPool với {} luồng...", threadCount);

        // 1. Phân công các luồng Worker (Producers) đi lấy giá mỗi 3 giây
        for (String coin : coinsToTrack) {
            executorService.scheduleAtFixedRate(() -> fetchCoinPrice(coin), 0, 3, TimeUnit.SECONDS);
        }

        // 2. Phân công 1 luồng Reporter (Consumer) đi kiểm tra biến động và gửi Telegram mỗi 5 giây
        executorService.scheduleAtFixedRate(this::checkAndReportPrices, 5, 5, TimeUnit.SECONDS);
    }

    // Nhiệm vụ của Worker: Lấy giá và cất vào Map
    private void fetchCoinPrice(String coinSymbol) {
        try {
            Response<CoinPrice> response = binanceApi.getCoinPrice(coinSymbol).execute();

            if (response.isSuccessful() && response.body() != null) {
                double currentPrice = Double.parseDouble(response.body().getPrice());

                // Cập nhật giá mới nhất vào kho chứa chung
                currentPricesMap.put(coinSymbol, currentPrice);

                // Ghi nhận lần đầu tiên để làm mốc so sánh
                lastAlertedPricesMap.putIfAbsent(coinSymbol, currentPrice);

                log.info("[Lấy giá] {} = {}", coinSymbol, currentPrice);
            } else {
                log.error("Lỗi API Binance cho {}: {}", coinSymbol, response.code());
            }
        } catch (IOException e) {
            log.error("Lỗi mạng khi lấy giá {}: {}", coinSymbol, e.getMessage());
        } catch (Exception e) {
            log.error("Lỗi luồng Worker {}: {}", coinSymbol, e.getMessage());
        }
    }

    // Nhiệm vụ của Reporter: Đọc Map, tính %, gom tin nhắn
    private void checkAndReportPrices() {
        if (currentPricesMap.isEmpty()) return; // Chưa có dữ liệu thì bỏ qua

        StringBuilder alertMessage = new StringBuilder("🚨 CẢNH BÁO BIẾN ĐỘNG GIÁ (>" + alertThreshold + "%)\n\n");
        boolean hasChanges = false;

        // Duyệt qua kho chứa giá hiện tại
        for (Map.Entry<String, Double> entry : currentPricesMap.entrySet()) {
            String symbol = entry.getKey();
            double currentPrice = entry.getValue();
            double lastAlertedPrice = lastAlertedPricesMap.get(symbol);

            // Thuật toán tính phần trăm chênh lệch
            double percentChange = Math.abs((currentPrice - lastAlertedPrice) / lastAlertedPrice) * 100.0;

            // Nếu mức biến động vượt ngưỡng cho phép
            if (percentChange >= alertThreshold) {
                hasChanges = true;

                // 1. Tách logic kiểm tra tăng/giảm ra một biến riêng cho rõ ràng
                boolean isUp = currentPrice > lastAlertedPrice;

                // 2. Định nghĩa trạng thái và dấu tương ứng
                String state = isUp ? "🟢 TĂNG" : "🔴 GIẢM";
                String sign = isUp ? "+" : "-";

                alertMessage.append(String.format("• %s %s\n", symbol, state))
                        .append(String.format("  Cũ: %f\n", lastAlertedPrice))
                        // 3. Thay dấu + cứng thành %s để truyền biến sign vào
                        .append(String.format("  Mới: %f (%s%.2f%%)\n\n", currentPrice, sign, percentChange));

                // Cập nhật lại mốc giá đã báo cáo
                lastAlertedPricesMap.put(symbol, currentPrice);
            }
        }

        // Nếu có ít nhất 1 coin biến động vượt ngưỡng, bắn DUY NHẤT 1 tin nhắn Telegram
        if (hasChanges) {
            sendTelegramMessage(alertMessage.toString());
        }
    }

    private void sendTelegramMessage(String content) {
        try {
            Response<Object> response = telegramApi.sendMessage(botToken, chatId, content).execute();
            if (response.isSuccessful()) {
                log.warn("[TELEGRAM] Đã gửi báo cáo gom nhóm thành công!");
            } else {
                log.error("[TELEGRAM LỖI] {}", response.code());
            }
        } catch (IOException e) {
            log.error("[TELEGRAM LỖI MẠNG] {}", e.getMessage());
        }
    }
}