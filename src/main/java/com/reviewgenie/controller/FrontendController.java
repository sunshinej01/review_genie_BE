package com.reviewgenie.controller;

import com.reviewgenie.domain.*;
import com.reviewgenie.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/frontend")
@RequiredArgsConstructor
public class FrontendController {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final StoreRepository storeRepository;
    private final KeywordRepository keywordRepository;
    private final TopGeneralKeywordRepository topGeneralKeywordRepository;
    private final CoreKeywordSentimentRepository coreKeywordSentimentRepository;
    private final CompetitorRepository competitorRepository;

    /**
     * 대시보드 요약 데이터
     */
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardData() {
        try {
            Map<String, Object> dashboard = new HashMap<>();
            
            // 기본 통계
            dashboard.put("totalReviews", reviewRepository.count());
            dashboard.put("totalUsers", userRepository.count());
            dashboard.put("totalStores", storeRepository.count());
            dashboard.put("totalKeywords", keywordRepository.count());
            
            // 감성 분석 통계
            long positiveCount = reviewRepository.countBySentiment("POSITIVE");
            long negativeCount = reviewRepository.countBySentiment("NEGATIVE");
            long neutralCount = reviewRepository.countBySentiment("NEUTRAL");
            
            dashboard.put("sentimentStats", Map.of(
                "positive", positiveCount,
                "negative", negativeCount,
                "neutral", neutralCount
            ));
            
            // 최근 리뷰 (최대 10개) - 생성일 기준으로 정렬하여 상위 10개 추출
            List<Review> allReviews = reviewRepository.findAll();
            List<Review> recentReviews = allReviews.stream()
                .sorted((r1, r2) -> r2.getCreatedAt().compareTo(r1.getCreatedAt()))
                .limit(10)
                .collect(Collectors.toList());
            
            List<Map<String, Object>> reviewDtos = recentReviews.stream()
                .map(this::convertReviewToDto)
                .collect(Collectors.toList());
            
            dashboard.put("recentReviews", reviewDtos);
            
            return ResponseEntity.ok(dashboard);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * 모든 리뷰 데이터 (페이지네이션 지원)
     */
    @GetMapping("/reviews")
    public ResponseEntity<Map<String, Object>> getAllReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            // 간단한 페이지네이션 구현
            List<Review> allReviews = reviewRepository.findAll();
            int total = allReviews.size();
            int start = page * size;
            int end = Math.min(start + size, total);
            
            List<Review> pageReviews = allReviews.subList(start, end);
            List<Map<String, Object>> reviewDtos = pageReviews.stream()
                .map(this::convertReviewToDto)
                .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("reviews", reviewDtos);
            response.put("pagination", Map.of(
                "page", page,
                "size", size,
                "total", total,
                "totalPages", (int) Math.ceil((double) total / size)
            ));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * 매장별 리뷰 데이터
     */
    @GetMapping("/stores/{storeId}/reviews")
    public ResponseEntity<Map<String, Object>> getStoreReviews(@PathVariable Long storeId) {
        try {
            Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new RuntimeException("Store not found"));
            
            List<Review> storeReviews = reviewRepository.findByStoreId(storeId);
            List<Map<String, Object>> reviewDtos = storeReviews.stream()
                .map(this::convertReviewToDto)
                .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("store", Map.of(
                "id", store.getId(),
                "storeName", store.getStoreName(),
                "location", store.getLocation(),
                "reviewCount", store.getReviewCount()
            ));
            response.put("reviews", reviewDtos);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * 키워드 분석 데이터
     */
    @GetMapping("/keywords/analysis")
    public ResponseEntity<Map<String, Object>> getKeywordAnalysis() {
        try {
            Map<String, Object> response = new HashMap<>();
            
            // 상위 키워드
            List<TopGeneralKeyword> topKeywords = topGeneralKeywordRepository.findAll();
            List<Map<String, Object>> topKeywordDtos = topKeywords.stream()
                .map(this::convertTopKeywordToDto)
                .collect(Collectors.toList());
            
            // 키워드별 감성 분석
            List<CoreKeywordSentiment> keywordSentiments = coreKeywordSentimentRepository.findAll();
            List<Map<String, Object>> sentimentDtos = keywordSentiments.stream()
                .map(this::convertSentimentToDto)
                .collect(Collectors.toList());
            
            response.put("topKeywords", topKeywordDtos);
            response.put("keywordSentiments", sentimentDtos);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * 경쟁사 분석 데이터
     */
    @GetMapping("/competitors/analysis")
    public ResponseEntity<Map<String, Object>> getCompetitorAnalysis() {
        try {
            List<Competitor> competitors = competitorRepository.findAll();
            List<Map<String, Object>> competitorDtos = competitors.stream()
                .map(this::convertCompetitorToDto)
                .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("competitors", competitorDtos);
            response.put("totalCompetitors", competitorDtos.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    // DTO 변환 메서드들
    private Map<String, Object> convertReviewToDto(Review review) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("reviewId", review.getReviewId());
        dto.put("content", review.getContent());
        dto.put("sentiment", review.getSentiment());
        dto.put("createdAt", review.getCreatedAt());
        
        if (review.getStore() != null) {
            dto.put("store", Map.of(
                "id", review.getStore().getId(),
                "storeName", review.getStore().getStoreName()
            ));
        }
        
        return dto;
    }

    private Map<String, Object> convertTopKeywordToDto(TopGeneralKeyword topKeyword) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("id", topKeyword.getTopKeywordId());
        dto.put("keyword", topKeyword.getKeyword());
        dto.put("frequency", topKeyword.getFrequency());
        dto.put("lastUpdated", topKeyword.getLastUpdated());
        
        if (topKeyword.getStore() != null) {
            dto.put("store", Map.of(
                "id", topKeyword.getStore().getId(),
                "storeName", topKeyword.getStore().getStoreName()
            ));
        }
        
        return dto;
    }

    private Map<String, Object> convertSentimentToDto(CoreKeywordSentiment sentiment) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("id", sentiment.getSentimentId());
        dto.put("positiveCount", sentiment.getPositiveCount());
        dto.put("negativeCount", sentiment.getNegativeCount());
        dto.put("lastUpdated", sentiment.getLastUpdated());
        
        if (sentiment.getStore() != null) {
            dto.put("store", Map.of(
                "id", sentiment.getStore().getId(),
                "storeName", sentiment.getStore().getStoreName()
            ));
        }
        
        if (sentiment.getKeyword() != null) {
            dto.put("keyword", Map.of(
                "id", sentiment.getKeyword().getKeywordId(),
                "keywordName", sentiment.getKeyword().getKeywordName()
            ));
        }
        
        return dto;
    }

    private Map<String, Object> convertCompetitorToDto(Competitor competitor) {
        Map<String, Object> dto = new HashMap<>();
        
        if (competitor.getStore() != null) {
            dto.put("store", Map.of(
                "id", competitor.getStore().getId(),
                "storeName", competitor.getStore().getStoreName()
            ));
        }
        
        if (competitor.getCompetitorStore() != null) {
            dto.put("competitorStore", Map.of(
                "id", competitor.getCompetitorStore().getId(),
                "storeName", competitor.getCompetitorStore().getStoreName()
            ));
        }
        
        return dto;
    }

    // ===== 가게별 분석 API 추가 =====
    
    /**
     * 가게별 오늘 리뷰 수 조회
     */
    @GetMapping("/stores/{storeName}/reviews/count/today")
    public ResponseEntity<Map<String, Object>> getTodayReviewCount(@PathVariable String storeName) {
        try {
            Store store = storeRepository.findByStoreName(storeName)
                .orElseThrow(() -> new RuntimeException("Store not found: " + storeName));
            
            // 오늘 날짜 기준으로 리뷰 수 계산
            long todayCount = reviewRepository.findByStoreId(store.getId()).stream()
                .filter(review -> {
                    java.time.LocalDate reviewDate = review.getCreatedAt().toLocalDate();
                    return reviewDate.equals(java.time.LocalDate.now());
                })
                .count();
            
            Map<String, Object> response = new HashMap<>();
            response.put("storeName", storeName);
            response.put("todayReviewCount", todayCount);
            response.put("date", java.time.LocalDate.now().toString());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * 가게별 일주일 리뷰 수 데이터 조회
     */
    @GetMapping("/stores/{storeName}/reviews/weekly")
    public ResponseEntity<Map<String, Object>> getWeeklyReviewData(@PathVariable String storeName) {
        try {
            Store store = storeRepository.findByStoreName(storeName)
                .orElseThrow(() -> new RuntimeException("Store not found: " + storeName));
            
            java.time.LocalDate today = java.time.LocalDate.now();
            Map<String, Object> weeklyData = new HashMap<>();
            
            // 최근 7일간의 리뷰 수 계산
            for (int i = 6; i >= 0; i--) {
                java.time.LocalDate date = today.minusDays(i);
                long count = reviewRepository.findByStoreId(store.getId()).stream()
                    .filter(review -> {
                        java.time.LocalDate reviewDate = review.getCreatedAt().toLocalDate();
                        return reviewDate.equals(date);
                    })
                    .count();
                
                weeklyData.put(date.toString(), count);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("storeName", storeName);
            response.put("weeklyData", weeklyData);
            response.put("startDate", today.minusDays(6).toString());
            response.put("endDate", today.toString());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * 가게별 키워드 감성분석 데이터 조회
     */
    @GetMapping("/stores/{storeName}/keywords/sentiment")
    public ResponseEntity<Map<String, Object>> getStoreKeywordSentiment(@PathVariable String storeName) {
        try {
            Store store = storeRepository.findByStoreName(storeName)
                .orElseThrow(() -> new RuntimeException("Store not found: " + storeName));
            
            List<CoreKeywordSentiment> sentiments = coreKeywordSentimentRepository.findAll().stream()
                .filter(sentiment -> sentiment.getStore() != null && 
                                   sentiment.getStore().getId().equals(store.getId()))
                .collect(Collectors.toList());
            
            List<Map<String, Object>> sentimentDtos = sentiments.stream()
                .map(this::convertSentimentToDto)
                .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("storeName", storeName);
            response.put("keywordSentiments", sentimentDtos);
            response.put("totalKeywords", sentimentDtos.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * 가게별 키워드 분석 데이터 조회
     */
    @GetMapping("/stores/{storeName}/keywords/analysis")
    public ResponseEntity<Map<String, Object>> getStoreKeywordAnalysis(@PathVariable String storeName) {
        try {
            Store store = storeRepository.findByStoreName(storeName)
                .orElseThrow(() -> new RuntimeException("Store not found: " + storeName));
            
            // 키워드별 감성 분석
            List<CoreKeywordSentiment> sentiments = coreKeywordSentimentRepository.findAll().stream()
                .filter(sentiment -> sentiment.getStore() != null && 
                                   sentiment.getStore().getId().equals(store.getId()))
                .collect(Collectors.toList());
            
            // 상위 키워드
            List<TopGeneralKeyword> topKeywords = topGeneralKeywordRepository.findAll().stream()
                .filter(keyword -> keyword.getStore() != null && 
                                 keyword.getStore().getId().equals(store.getId()))
                .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("storeName", storeName);
            response.put("keywordSentiments", sentiments.stream()
                .map(this::convertSentimentToDto)
                .collect(Collectors.toList()));
            response.put("topKeywords", topKeywords.stream()
                .map(this::convertTopKeywordToDto)
                .collect(Collectors.toList()));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * 가게별 Top General Keyword 데이터 조회
     */
    @GetMapping("/stores/{storeName}/keywords/top")
    public ResponseEntity<Map<String, Object>> getStoreTopKeywords(@PathVariable String storeName) {
        try {
            Store store = storeRepository.findByStoreName(storeName)
                .orElseThrow(() -> new RuntimeException("Store not found: " + storeName));
            
            List<TopGeneralKeyword> topKeywords = topGeneralKeywordRepository.findAll().stream()
                .filter(keyword -> keyword.getStore() != null && 
                                 keyword.getStore().getId().equals(store.getId()))
                .sorted((k1, k2) -> Long.compare(k2.getFrequency(), k1.getFrequency()))
                .limit(10)
                .collect(Collectors.toList());
            
            List<Map<String, Object>> topKeywordDtos = topKeywords.stream()
                .map(this::convertTopKeywordToDto)
                .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("storeName", storeName);
            response.put("topKeywords", topKeywordDtos);
            response.put("totalKeywords", topKeywordDtos.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * 가게별 총 리뷰 수 조회
     */
    @GetMapping("/stores/{storeName}/reviews/count/total")
    public ResponseEntity<Map<String, Object>> getStoreTotalReviewCount(@PathVariable String storeName) {
        try {
            Store store = storeRepository.findByStoreName(storeName)
                .orElseThrow(() -> new RuntimeException("Store not found: " + storeName));
            
            long totalCount = reviewRepository.findByStoreId(store.getId()).size();
            
            Map<String, Object> response = new HashMap<>();
            response.put("storeName", storeName);
            response.put("totalReviewCount", totalCount);
            response.put("storeInfo", Map.of(
                "id", store.getId(),
                "storeName", store.getStoreName(),
                "location", store.getLocation()
            ));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
}
