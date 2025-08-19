package com.reviewgenie.service;

import com.reviewgenie.domain.Review;
import com.reviewgenie.dto.ReviewDto;
import com.reviewgenie.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;

@Service
@RequiredArgsConstructor // final 필드에 대한 생성자 자동 주입
public class DataIntegrationService {

    private final RestTemplate restTemplate;
    private final ReviewRepository reviewRepository;

    // application.yml에 정의된 URL 주입
    @Value("${external-api.review-url}")
    private String reviewApiUrl;

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
                // 3. DTO를 Entity로 변환 (store_id 등 추가 정보는 비즈니스 로직에 따라 설정)
                return Review.builder()
                        .platform(dto.getPlatform())
                        .rating(dto.getRating())
                        .content(dto.getContent())
                        .createdAt(dto.getCreatedAt())
                        // .store( /* store 정보 설정 */ ) 
                        .build();
            })
            // 4. 변환된 각 Review Entity를 DB에 저장
            .forEach(reviewRepository::save);
        }
    }
}