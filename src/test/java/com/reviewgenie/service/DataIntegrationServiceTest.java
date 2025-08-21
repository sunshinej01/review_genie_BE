package com.reviewgenie.service;

import com.reviewgenie.domain.Review;
import com.reviewgenie.repository.ReviewRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class DataIntegrationServiceTest {

    @Autowired
    private ReviewRepository reviewRepository;

    @Test
    @Transactional
    public void testDatabaseConnection() {
        System.out.println("=== 데이터베이스 연결 테스트 ===");
        
        // 간단한 리뷰 생성 및 저장 테스트
        Review testReview = Review.builder()
                .content("테스트 리뷰입니다.")
                .sentiment("POSITIVE")
                .build();
        
        Review savedReview = reviewRepository.save(testReview);
        
        System.out.println("테스트 리뷰 저장 성공:");
        System.out.println("  ID: " + savedReview.getReviewId());
        System.out.println("  Content: " + savedReview.getContent());
        System.out.println("  Sentiment: " + savedReview.getSentiment());
        
        // 검증
        assertThat(savedReview.getReviewId()).isNotNull();
        assertThat(savedReview.getContent()).isEqualTo("테스트 리뷰입니다.");
        assertThat(savedReview.getSentiment()).isEqualTo("POSITIVE");
        
        // DB에서 다시 조회
        Review foundReview = reviewRepository.findById(savedReview.getReviewId()).orElse(null);
        assertThat(foundReview).isNotNull();
        assertThat(foundReview.getContent()).isEqualTo("테스트 리뷰입니다.");
        
        System.out.println("=== 데이터베이스 연결 테스트 완료 ===");
    }

    @Test
    public void testTableCreation() {
        System.out.println("=== 테이블 생성 테스트 ===");
        
        // Repository가 정상적으로 주입되었는지 확인
        assertThat(reviewRepository).isNotNull();
        
        // 기본 카운트 조회 (테이블이 존재하는지 확인)
        long count = reviewRepository.count();
        System.out.println("현재 Review 테이블의 레코드 수: " + count);
        
        // 테이블이 정상적으로 생성되어 조회가 가능한지 확인
        assertThat(count).isGreaterThanOrEqualTo(0);
        
        System.out.println("=== 테이블 생성 테스트 완료 ===");
    }
}
