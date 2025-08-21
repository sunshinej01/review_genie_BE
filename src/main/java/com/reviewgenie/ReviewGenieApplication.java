package com.reviewgenie;

import com.reviewgenie.service.ReviewBatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

@SpringBootApplication
public class ReviewGenieApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReviewGenieApplication.class, args);
	}

	@Component
	@RequiredArgsConstructor
	public static class DataInitializer implements CommandLineRunner {
		
		private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DataInitializer.class);

		private final ReviewBatchService reviewBatchService;

		@Override
		public void run(String... args) throws Exception {
			log.info("🚀 애플리케이션 시작 - 초기 데이터 로드 시작");
			reviewBatchService.loadInitialData();
			log.info("✅ 초기 데이터 로드 완료");
		}
	}
}


