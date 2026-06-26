package com.trung.coinpricetracker;

import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@Log4j2
public class CoinPriceTrackerApplication {

	public static void main(String[] args) {
		SpringApplication.run(CoinPriceTrackerApplication.class, args);
		log.info("Hệ thống Coin Tracker đã khởi động thành công!");
	}
}