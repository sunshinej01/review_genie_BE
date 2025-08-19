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
    private DataIntegrationService dataIntegrationService;

    @Autowired
    private ReviewRepository reviewRepository;

    @Test
    @Transactional
    public void testLoadReviewsFromJson() {
        System.out.println("=== 테스트 시작: JSON 데이터 로딩 및 DB 저장 ===");
        
        // 테스트 전 기존 데이터 삭제
        reviewRepository.deleteAll();
        
        // JSON에서 리뷰 데이터 로딩 테스트
        dataIntegrationService.loadReviewsFromJson();
        
        // DB에 저장된 데이터 확인
        List<Review> savedReviews = reviewRepository.findAll();
        
        System.out.println("=== 테스트 결과 ===");
        System.out.println("저장된 리뷰 개수: " + savedReviews.size());
        
        // 검증
        assertThat(savedReviews).isNotEmpty();
        assertThat(savedReviews.size()).isEqualTo(2); // reviews.json에 2개의 리뷰가 있음
        
        // 각 리뷰 내용 확인
        for (int i = 0; i < savedReviews.size(); i++) {
            Review review = savedReviews.get(i);
            System.out.println("리뷰 " + (i + 1) + ":");
            System.out.println("  ID: " + review.getId());
            System.out.println("  Content: " + (review.getContent().length() > 100 ? 
                review.getContent().substring(0, 100) + "..." : review.getContent()));
            System.out.println("  Sentiment: " + review.getSentiment());
            System.out.println("  Created At: " + review.getCreatedAt());
            System.out.println();
            
            // 필수 필드 검증
            assertThat(review.getId()).isNotNull();
            assertThat(review.getContent()).isNotNull().isNotEmpty();
            assertThat(review.getSentiment()).isNotNull().isNotEmpty();
        }
        
        System.out.println("=== 테스트 완료: 모든 검증 통과 ===");
    }

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
        System.out.println("  ID: " + savedReview.getId());
        System.out.println("  Content: " + savedReview.getContent());
        System.out.println("  Sentiment: " + savedReview.getSentiment());
        
        // 검증
        assertThat(savedReview.getId()).isNotNull();
        assertThat(savedReview.getContent()).isEqualTo("테스트 리뷰입니다.");
        assertThat(savedReview.getSentiment()).isEqualTo("POSITIVE");
        
        // DB에서 다시 조회
        Review foundReview = reviewRepository.findById(savedReview.getId()).orElse(null);
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
