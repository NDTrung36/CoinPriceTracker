package com.trung.coinpricetracker;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import lombok.extern.log4j.Log4j2;

import java.util.Arrays;
import java.util.List;

@SpringBootApplication
@EnableAsync // (1) Bật "công tắc" cho phép ứng dụng chạy đa luồng
@Log4j2
public class CoinPriceTrackerApplication implements CommandLineRunner {

	private final BinanceService binanceService;

	// (2) Dùng Constructor Injection thay cho @Autowired
	public CoinPriceTrackerApplication(BinanceService binanceService) {
		this.binanceService = binanceService;
	}

	public static void main(String[] args) {
		SpringApplication.run(CoinPriceTrackerApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		// (3) Tạo một danh sách các loại coin muốn theo dõi
		List<String> coins = Arrays.asList("BTCUSDT", "ETHUSDT", "BNBUSDT");

		log.info("Bắt đầu khởi động các luồng theo dõi coin...");

		// (4) Dùng vòng lặp gọi service cho từng coin
		for (String coin : coins) {
			binanceService.trackCoinPriceContinuously(coin);
		}
	}
}
