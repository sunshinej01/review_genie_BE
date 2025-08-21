package com.reviewgenie.controller;

import com.reviewgenie.domain.Review;
import com.reviewgenie.repository.ReviewRepository;
// import com.reviewgenie.service.DataIntegrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class TestController {

    // private final DataIntegrationService dataIntegrationService;
    private final ReviewRepository reviewRepository;

    /**
     * 루트 경로 - 애플리케이션 상태 확인
     */
    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> getRoot() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            long reviewCount = reviewRepository.count();
            
            response.put("success", true);
            response.put("message", "Review Genie Backend API가 정상적으로 실행 중입니다.");
            response.put("status", "RUNNING");
            response.put("totalReviews", reviewCount);
            response.put("endpoints", Map.of(
                "reviews", "/api/test/reviews",
                "loadReviews", "/api/test/load-reviews",
                "clearReviews", "/api/test/reviews (DELETE)",
                "analyzeSentiment", "/api/reviews/analyze/sentiment",
                "extractKeywords", "/api/reviews/analyze/keywords",
                "comprehensiveAnalysis", "/api/reviews/analyze/comprehensive"
            ));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "서버 내부 오류가 발생했습니다");
            response.put("status", "ERROR");
            response.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * JSON 데이터 로딩 테스트 엔드포인트
     */
    @PostMapping("/api/test/load-reviews")
    public ResponseEntity<Map<String, Object>> loadReviewsFromJson() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 기존 데이터 개수 확인
            long beforeCount = reviewRepository.count();
            
            // JSON에서 리뷰 로딩 (프론트엔드 연동에 불필요하여 비활성화)
            // dataIntegrationService.loadReviewsFromJson();
            
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
    @GetMapping("/api/test/reviews")
    public ResponseEntity<Map<String, Object>> getAllReviews() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Review> reviews = reviewRepository.findAll();
            
            // Hibernate 프록시 오류 방지를 위해 DTO로 변환
            List<Map<String, Object>> reviewDtos = reviews.stream()
                .map(review -> {
                    Map<String, Object> dto = new HashMap<>();
                    dto.put("reviewId", review.getReviewId());
                    dto.put("content", review.getContent());
                    dto.put("sentiment", review.getSentiment());
                    dto.put("createdAt", review.getCreatedAt());
                    dto.put("storeId", review.getStore() != null ? review.getStore().getId() : null);
                    return dto;
                })
                .collect(Collectors.toList());
            
            response.put("success", true);
            response.put("totalCount", reviewDtos.size());
            response.put("reviews", reviewDtos);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "리뷰 조회 실패: " + e.getMessage());
            response.put("error", e.getClass().getSimpleName());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 테이블 초기화 엔드포인트
     */
    @DeleteMapping("/api/test/reviews")
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
    @GetMapping("/api/test/db-status")
    public ResponseEntity<Map<String, Object>> checkDatabaseStatus() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            long reviewCount = reviewRepository.count();
            
            response.put("success", true);
            response.put("message", "데이터베이스 연결 정상");
            response.put("status", "CONNECTED");
            response.put("reviewCount", reviewCount);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "데이터베이스 연결 실패: " + e.getMessage());
            response.put("status", "DISCONNECTED");
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
