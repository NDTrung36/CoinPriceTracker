package com.trung.coinpricetracker;

import lombok.extern.log4j.Log4j2;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.Arrays;
import java.util.List;

@SpringBootApplication
@EnableAsync
@Log4j2
public class CoinPriceTrackerApplication implements CommandLineRunner {

	private final BinanceService binanceService;

	public CoinPriceTrackerApplication(BinanceService binanceService) {
		this.binanceService = binanceService;
	}

	public static void main(String[] args) {
		SpringApplication.run(CoinPriceTrackerApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		List<String> coins = Arrays.asList("BTCUSDT", "ETHUSDT", "BNBUSDT");

		log.info("Bắt đầu khởi động các luồng theo dõi coin...");

		for (String coin : coins) {
			binanceService.trackCoinPriceContinuously(coin);
		}
	}
}
