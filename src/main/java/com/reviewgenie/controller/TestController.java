package com.reviewgenie.controller;

import com.reviewgenie.domain.Review;
import com.reviewgenie.repository.ReviewRepository;
import com.reviewgenie.service.DataIntegrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestController {

    private final DataIntegrationService dataIntegrationService;
    private final ReviewRepository reviewRepository;

    /**
     * JSON 데이터 로딩 테스트 엔드포인트
     */
    @PostMapping("/load-reviews")
    public ResponseEntity<Map<String, Object>> loadReviewsFromJson() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 기존 데이터 개수 확인
            long beforeCount = reviewRepository.count();
            
            // JSON에서 리뷰 로딩
            dataIntegrationService.loadReviewsFromJson();
            
            // 로딩 후 데이터 개수 확인
            long afterCount = reviewRepository.count();
            long addedCount = afterCount - beforeCount;
            
            response.put("success", true);
            response.put("message", "리뷰 데이터 로딩 완료");
            response.put("beforeCount", beforeCount);
            response.put("afterCount", afterCount);
            response.put("addedCount", addedCount);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "리뷰 로딩 실패: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 저장된 리뷰 조회 엔드포인트
     */
    @GetMapping("/reviews")
    public ResponseEntity<Map<String, Object>> getAllReviews() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Review> reviews = reviewRepository.findAll();
            
            response.put("success", true);
            response.put("totalCount", reviews.size());
            response.put("reviews", reviews);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "리뷰 조회 실패: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 테이블 초기화 엔드포인트
     */
    @DeleteMapping("/reviews")
    public ResponseEntity<Map<String, Object>> clearAllReviews() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            long beforeCount = reviewRepository.count();
            reviewRepository.deleteAll();
            long afterCount = reviewRepository.count();
            
            response.put("success", true);
            response.put("message", "모든 리뷰 데이터 삭제 완료");
            response.put("deletedCount", beforeCount - afterCount);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "데이터 삭제 실패: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 데이터베이스 연결 상태 확인 엔드포인트
     */
    @GetMapping("/db-status")
    public ResponseEntity<Map<String, Object>> checkDatabaseStatus() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            long count = reviewRepository.count();
            
            // 간단한 테스트 데이터 생성
            Review testReview = Review.builder()
                    .content("DB 연결 테스트")
                    .sentiment("NEUTRAL")
                    .build();
            
            Review saved = reviewRepository.save(testReview);
            reviewRepository.delete(saved); // 테스트 후 삭제
            
            response.put("success", true);
            response.put("message", "데이터베이스 연결 정상");
            response.put("currentCount", count);
            response.put("testInsertSuccess", true);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "데이터베이스 연결 실패: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
