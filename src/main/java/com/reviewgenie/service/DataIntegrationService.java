package com.reviewgenie.service;

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
@RequiredArgsConstructor // final 필드에 대한 생성자 자동 주입
public class DataIntegrationService {

    private final RestTemplate restTemplate;
    private final ReviewRepository reviewRepository;
    private final ReviewAnalysisService reviewAnalysisService;
    private final ObjectMapper objectMapper; // Spring Boot가 자동으로 Bean 등록

    // application.yml에 정의된 URL 주입
    @Value("${external-api.review-url}")
    private String reviewApiUrl;

    @PostConstruct
    public void init() {
        loadReviewsFromJson();
    }

    /**
     * JSON 파일에서 리뷰 데이터를 읽어서 DB에 저장하는 메서드
     * 테스트용으로 분리하여 독립적으로 호출 가능
     */
    public void loadReviewsFromJson() {
        try {
            System.out.println("=== JSON 파일에서 리뷰 데이터 로딩 시작 ===");
            
            // 1. resources/data 폴더의 reviews.json 파일 읽기
            InputStream inputStream = new ClassPathResource("data/reviews.json").getInputStream();
            JsonNode rootNode = objectMapper.readTree(inputStream);
            System.out.println("JSON 파일 읽기 성공");

            // 2. "My_store"의 "reviews" 배열 순회
            JsonNode reviewsNode = rootNode.path("store_list").path("My_store").path("reviews");
            if (reviewsNode.isArray()) {
                System.out.println("리뷰 배열 발견, 총 " + reviewsNode.size() + "개의 리뷰");
                
                int count = 0;
                for (JsonNode reviewNode : reviewsNode) {
                    String reviewText = reviewNode.asText();
                    System.out.println("처리 중인 리뷰 " + (++count) + ": " + 
                        (reviewText.length() > 50 ? reviewText.substring(0, 50) + "..." : reviewText));

                    // 3. 감성 분석 서비스 호출
                    String sentiment = reviewAnalysisService.analyzeSentiment(reviewText);
                    System.out.println("감정 분석 결과: " + sentiment);

                    // 4. 분석 결과와 함께 Entity 생성 및 DB 저장
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

    /**
     * 매일 자정에 리뷰 데이터를 API로부터 가져와 DB에 저장합니다.
     * (cron = "초 분 시 일 월 요일")
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void fetchAndSaveReviews() {
        // 1. 외부 API를 호출하여 리뷰 데이터를 DTO 배열 형태로 받음
        ReviewDto[] reviewDtos = restTemplate.getForObject(reviewApiUrl, ReviewDto[].class);

        if (reviewDtos != null && reviewDtos.length > 0) {
            // 2. DTO 배열을 Review Entity 스트림으로 변환
            Arrays.stream(reviewDtos).map(dto -> {
                // 3. 감정 분석 추가
                String sentiment = reviewAnalysisService.analyzeSentiment(dto.getContent());
                
                // 4. DTO를 Entity로 변환 (store_id 등 추가 정보는 비즈니스 로직에 따라 설정)
                return Review.builder()
                        .content(dto.getContent())
                        .sentiment(sentiment)
                        .createdAt(dto.getCreatedAt())
                        // .store( /* store 정보 설정 */ ) 
                        .build();
            })
            // 5. 변환된 각 Review Entity를 DB에 저장
            .forEach(reviewRepository::save);
        }
    }
}