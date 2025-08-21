package com.reviewgenie.service;

// 프론트엔드 연동에 불필요하여 DataIntegrationService를 완전히 비활성화
/*
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reviewgenie.domain.Review;
import com.reviewgenie.dto.ReviewDto;
import com.reviewgenie.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class DataIntegrationService {

    private final RestTemplate restTemplate;
    private final ReviewRepository reviewRepository;
    private final ReviewAnalysisService reviewAnalysisService;
    private final ObjectMapper objectMapper;

    @Value("${external-api.review-url}")
    private String reviewApiUrl;

    @PostConstruct
    public void init() {
        loadReviewsFromJson();
    }

    public void loadReviewsFromJson() {
        try {
            System.out.println("=== JSON 파일에서 리뷰 데이터 로딩 시작 ===");
            
            InputStream inputStream = new ClassPathResource("data/reviews.json").getInputStream();
            JsonNode rootNode = objectMapper.readTree(inputStream);
            System.out.println("JSON 파일 읽기 성공");

            JsonNode reviewsNode = rootNode.path("store_list").path("My_store").path("reviews");
            if (reviewsNode.isArray()) {
                System.out.println("리뷰 배열 발견, 총 " + reviewsNode.size() + "개의 리뷰");
                
                int count = 0;
                for (JsonNode reviewNode : reviewsNode) {
                    String reviewText = reviewNode.asText();
                    System.out.println("처리 중인 리뷰 " + (++count) + ": " + 
                        (reviewText.length() > 50 ? reviewText.substring(0, 50) + "..." : reviewText));

                    String sentiment = reviewAnalysisService.analyzeSentiment(reviewText);
                    System.out.println("감정 분석 결과: " + sentiment);

                    Review review = Review.builder()
                            .content(reviewText)
                            .sentiment(sentiment)
                            .build();
                    Review savedReview = reviewRepository.save(review);
                    System.out.println("DB 저장 완료 - ID: " + savedReview.getReviewId());
                }
                System.out.println("=== 총 " + count + "개의 리뷰 처리 완료 ===");
            } else {
                System.out.println("리뷰 배열을 찾을 수 없습니다.");
            }
        } catch (Exception e) {
            System.err.println("리뷰 로딩 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void fetchAndSaveReviews() {
        ReviewDto[] reviewDtos = restTemplate.getForObject(reviewApiUrl, ReviewDto[].class);

        if (reviewDtos != null && reviewDtos.length > 0) {
            Arrays.stream(reviewDtos).map(dto -> {
                String sentiment = reviewAnalysisService.analyzeSentiment(dto.getContent());
                
                return Review.builder()
                        .content(dto.getContent())
                        .sentiment(sentiment)
                        .createdAt(dto.getCreatedAt())
                        .build();
            })
            .forEach(reviewRepository::save);
        }
    }
}*/
