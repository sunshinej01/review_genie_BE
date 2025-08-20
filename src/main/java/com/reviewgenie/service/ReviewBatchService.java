package com.reviewgenie.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reviewgenie.domain.Review;
import com.reviewgenie.domain.Store;
import com.reviewgenie.domain.User;
import com.reviewgenie.repository.ReviewRepository;
import com.reviewgenie.repository.StoreRepository;
import com.reviewgenie.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewBatchService {

    private final ObjectMapper objectMapper;
    private final ReviewAnalysisService reviewAnalysisService;
    private final KoreanNLPService koreanNLPService;
    private final StoreRepository storeRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;

    /**
     * reviews.json 파일을 읽고 감성분석 후 결과 출력 (pos/neg만)
     */
    public void analyzeAndPrintReviews() {
        try {
            log.info("🚀 리뷰 감성분석 시작 (POSITIVE/NEGATIVE 분류만)");
            
            // 1. JSON 파일 읽기
            JsonNode rootNode = readJsonFile();
            
            // 2. My_store 분석
            analyzeMyStore(rootNode);
            
            // 3. Competitor 분석
            analyzeCompetitors(rootNode);
            
            log.info("✅ 리뷰 감성분석 완료");
            
        } catch (Exception e) {
            log.error("❌ 리뷰 감성분석 실패: {}", e.getMessage(), e);
            throw new RuntimeException("리뷰 감성분석 실패", e);
        }
    }

    /**
     * reviews.json 파일을 읽고 감성분석 후 DB에 저장 (최초 DB 생성시에만)
     * 이미 데이터가 있으면 저장하지 않음
     */
    @Transactional
    public void processReviewsFromJson() {
        try {
            log.info("🚀 리뷰 배치 처리 시작 (최초 DB 생성시에만)");
            
            // 1. 기존 데이터 확인
            if (hasExistingData()) {
                log.info("⚠️ 이미 데이터가 존재합니다. 최초 DB 생성시에만 실행됩니다.");
                return;
            }
            
            // 2. JSON 파일 읽기
            JsonNode rootNode = readJsonFile();
            
            // 3. My_store 처리
            processMyStore(rootNode);
            
            // 4. Competitor 처리
            processCompetitors(rootNode);
            
            log.info("✅ 리뷰 배치 처리 완료");
            
        } catch (Exception e) {
            log.error("❌ 리뷰 배치 처리 실패: {}", e.getMessage(), e);
            throw new RuntimeException("리뷰 배치 처리 실패", e);
        }
    }

    /**
     * 기존 데이터가 있는지 확인
     */
    private boolean hasExistingData() {
        return storeRepository.count() > 0 || reviewRepository.count() > 0;
    }

    /**
     * JSON 파일 읽기
     */
    private JsonNode readJsonFile() throws IOException {
        ClassPathResource resource = new ClassPathResource("data/reviews.json");
        return objectMapper.readTree(resource.getInputStream());
    }

    /**
     * My_store 분석 (출력용)
     */
    private void analyzeMyStore(JsonNode rootNode) {
        log.info("\n=== 🏪 MY STORE 감성분석 결과 ===");
        
        JsonNode myStoreNode = rootNode.path("store_list").path("My_store");
        
        if (myStoreNode.isMissingNode()) {
            log.warn("⚠️ My_store 데이터가 없습니다.");
            return;
        }
        
        String storeName = myStoreNode.path("place_name").asText();
        log.info("매장명: {}", storeName);
        
        JsonNode reviewsNode = myStoreNode.path("reviews");
        analyzeReviewsForPrint(reviewsNode, storeName);
    }

    /**
     * My_store 처리 (DB 저장용)
     */
    private void processMyStore(JsonNode rootNode) {
        log.info("🏪 My_store 처리 시작");
        
        JsonNode myStoreNode = rootNode.path("store_list").path("My_store");
        
        if (myStoreNode.isMissingNode()) {
            log.warn("⚠️ My_store 데이터가 없습니다.");
            return;
        }
        
        // Store 생성 또는 조회
        Store myStore = findOrCreateStore(
            myStoreNode.path("place_id").asText(),
            myStoreNode.path("place_name").asText(),
            myStoreNode.path("count").asInt(),
            "MY_STORE"
        );
        
        // Reviews 처리
        JsonNode reviewsNode = myStoreNode.path("reviews");
        List<Review> processedReviews = processReviews(reviewsNode, myStore);
        
        log.info("✅ My_store 처리 완료: {} 개 리뷰", processedReviews.size());
    }

    /**
     * Competitor 분석 (출력용)
     */
    private void analyzeCompetitors(JsonNode rootNode) {
        log.info("\n=== 🏢 COMPETITORS 감성분석 결과 ===");
        
        JsonNode competitorsNode = rootNode.path("store_list").path("Competitor");
        
        if (!competitorsNode.isArray()) {
            log.warn("⚠️ Competitor 데이터가 배열이 아닙니다.");
            return;
        }
        
        int competitorIndex = 1;
        for (JsonNode competitorNode : competitorsNode) {
            String storeName = competitorNode.path("place_name").asText();
            log.info("\n경쟁사 {}: {}", competitorIndex, storeName);
            
            JsonNode reviewsNode = competitorNode.path("reviews");
            analyzeReviewsForPrint(reviewsNode, storeName);
            
            competitorIndex++;
        }
    }

    /**
     * Competitor 처리 (DB 저장용)
     */
    private void processCompetitors(JsonNode rootNode) {
        log.info("🏢 Competitors 처리 시작");
        
        JsonNode competitorsNode = rootNode.path("store_list").path("Competitor");
        
        if (!competitorsNode.isArray()) {
            log.warn("⚠️ Competitor 데이터가 배열이 아닙니다.");
            return;
        }
        
        int totalCompetitors = 0;
        int totalReviews = 0;
        
        for (JsonNode competitorNode : competitorsNode) {
            // Store 생성 또는 조회
            Store competitorStore = findOrCreateStore(
                competitorNode.path("place_id").asText(),
                competitorNode.path("place_name").asText(),
                competitorNode.path("count").asInt(),
                "COMPETITOR"
            );
            
            // Reviews 처리
            JsonNode reviewsNode = competitorNode.path("reviews");
            List<Review> processedReviews = processReviews(reviewsNode, competitorStore);
            
            totalCompetitors++;
            totalReviews += processedReviews.size();
        }
        
        log.info("✅ Competitors 처리 완료: {} 개 매장, {} 개 리뷰", totalCompetitors, totalReviews);
    }

    /**
     * Store를 찾거나 생성 (중복 방지)
     */
    private Store findOrCreateStore(String placeId, String placeName, int reviewCount, String storeType) {
        // 기존 Store가 있는지 확인
        Optional<Store> existingStore = storeRepository.findByPlaceId(placeId);
        
        if (existingStore.isPresent()) {
            log.info("🏪 기존 Store 사용: {} ({})", placeName, storeType);
            return existingStore.get();
        }
        
        // 새로운 Store 생성
        Store store = createStore(placeId, placeName, reviewCount, storeType);
        log.info("🏪 새 Store 생성: {} ({})", placeName, storeType);
        
        return store;
    }

    /**
     * Store 생성 및 저장
     */
    private Store createStore(String placeId, String placeName, int reviewCount, String storeType) {
        // User는 기존 것을 사용하거나 생성
        User user = findOrCreateUser();
        
        Store store = Store.builder()
            .placeId(placeId)
            .storeName(placeName)
            .location("Unknown")
            .reviewCount(reviewCount)
            .user(user)
            .build();
        
        return storeRepository.save(store);
    }

    /**
     * User를 찾거나 생성
     */
    private User findOrCreateUser() {
        // 기존 User가 있는지 확인 (username으로)
        Optional<User> existingUser = userRepository.findByUsername("review_genie_user");
        
        if (existingUser.isPresent()) {
            return existingUser.get();
        }
        
        // 새로운 User 생성
        User newUser = User.builder()
            .username("review_genie_user")
            .password("review_genie_password")
            .build();
        
        return userRepository.save(newUser);
    }

    /**
     * 리뷰 분석 및 결과 출력 (pos/neg만)
     */
    private void analyzeReviewsForPrint(JsonNode reviewsNode, String storeName) {
        if (!reviewsNode.isArray()) {
            log.warn("⚠️ 리뷰 데이터가 배열이 아닙니다: {}", storeName);
            return;
        }
        
        int reviewIndex = 1;
        int positiveCount = 0;
        int negativeCount = 0;
        
        for (JsonNode reviewNode : reviewsNode) {
            String reviewText = reviewNode.asText();
            
            // 1. 텍스트 전처리
            String cleanedText = preprocessReviewText(reviewText);
            
            // 2. 감성 분석 (pos/neg만)
            String sentiment = performBinaryClassification(cleanedText);
            
            // 3. 결과 출력
            log.info("리뷰 {}: [{}]", reviewIndex, sentiment);
            log.info("내용: {}", cleanedText.length() > 100 ? 
                cleanedText.substring(0, 100) + "..." : cleanedText);
            log.info("---");
            
            if ("POSITIVE".equals(sentiment)) {
                positiveCount++;
            } else {
                negativeCount++;
            }
            
            reviewIndex++;
        }
        
        // 매장별 요약
        log.info("📊 {} 요약: 긍정 {}개, 부정 {}개", storeName, positiveCount, negativeCount);
    }

    /**
     * 리뷰 배열 처리 (DB 저장용)
     */
    private List<Review> processReviews(JsonNode reviewsNode, Store store) {
        List<Review> processedReviews = new ArrayList<>();
        
        if (!reviewsNode.isArray()) {
            log.warn("⚠️ 리뷰 데이터가 배열이 아닙니다: {}", store.getStoreName());
            return processedReviews;
        }
        
        for (JsonNode reviewNode : reviewsNode) {
            String reviewText = reviewNode.asText();
            
            // 1. 텍스트 전처리
            String cleanedText = preprocessReviewText(reviewText);
            
            // 2. 감성 분석 (pos/neg만)
            String sentiment = performBinaryClassification(cleanedText);
            
            // 3. Review 엔티티 생성
            Review review = Review.builder()
                .store(store)
                .content(cleanedText)
                .sentiment(sentiment)
                .createdAt(LocalDateTime.now())
                .build();
            
            Review savedReview = reviewRepository.save(review);
            processedReviews.add(savedReview);
            
            log.info("📝 리뷰 저장: {} - {}", store.getStoreName(), sentiment);
        }
        
        return processedReviews;
    }

    /**
     * 리뷰 텍스트 전처리
     */
    private String preprocessReviewText(String reviewText) {
        if (reviewText == null || reviewText.trim().isEmpty()) {
            return "";
        }
        
        // 1. 이모지 및 특수문자 정리
        String cleaned = reviewText.replaceAll("[^가-힣a-zA-Z0-9\\s]", " ");
        
        // 2. 줄바꿈 문자를 공백으로 변경
        cleaned = cleaned.replaceAll("\\n", " ");
        
        // 3. 연속된 공백 제거
        cleaned = cleaned.replaceAll("\\s+", " ");
        
        // 4. 앞뒤 공백 제거
        cleaned = cleaned.trim();
        
        return cleaned;
    }

    /**
     * 이진 분류 감성 분석 (POSITIVE/NEGATIVE만)
     */
    private String performBinaryClassification(String reviewText) {
        try {
            // 한국어 텍스트는 KoreanNLPService 사용
            if (containsKorean(reviewText)) {
                // 1) 주요 키워드가 포함된 문장만으로 이진 판단 (우선 적용)
                Map<String, Object> keywordBinary = koreanNLPService.classifyBinaryByKeyTerms(reviewText);
                String label = (String) keywordBinary.get("label");
                Integer matched = (Integer) keywordBinary.get("matchedSentences");

                if (matched != null && matched > 0 && ("POSITIVE".equals(label) || "NEGATIVE".equals(label))) {
                    return label;
                }

                // 2) 백업 로직: 전체 텍스트 감성 → 이진 매핑 (NEUTRAL 편향 규칙 유지)
                Map<String, Object> sentimentResult = koreanNLPService.analyzeSentiment(reviewText);
                String sentiment = (String) sentimentResult.get("sentiment");
                return classifyToBinary(sentiment, sentimentResult);
            } else {
                // 영어 텍스트는 Stanford CoreNLP 사용
                String sentiment = reviewAnalysisService.analyzeEnglishSentiment(reviewText);
                return mapSentimentToBinary(sentiment);
            }
        } catch (Exception e) {
            log.error("감성 분석 오류: {}", e.getMessage());
            return "POSITIVE"; // 기본값을 긍정으로 설정
        }
    }

    /**
     * 한국어 감성 분석 결과를 이진 분류로 변환
     */
    private String classifyToBinary(String sentiment, Map<String, Object> sentimentResult) {
        if ("POSITIVE".equals(sentiment)) {
            return "POSITIVE";
        } else if ("NEGATIVE".equals(sentiment)) {
            return "NEGATIVE";
        } else {
            // NEUTRAL인 경우 점수를 보고 판단
            Integer positiveScore = (Integer) sentimentResult.get("positiveScore");
            Integer negativeScore = (Integer) sentimentResult.get("negativeScore");
            
            if (positiveScore == null) positiveScore = 0;
            if (negativeScore == null) negativeScore = 0;
            
            // 점수가 같거나 둘 다 0인 경우 긍정으로 편향
            return positiveScore >= negativeScore ? "POSITIVE" : "NEGATIVE";
        }
    }

    /**
     * 영어 감성 분석 결과를 이진 분류로 매핑
     */
    private String mapSentimentToBinary(String sentiment) {
        if (sentiment == null) {
            return "POSITIVE";
        }
        
        switch (sentiment.toUpperCase()) {
            case "POSITIVE":
                return "POSITIVE";
            case "NEGATIVE":
                return "NEGATIVE";
            case "NEUTRAL":
            default:
                return "POSITIVE"; // NEUTRAL을 POSITIVE로 분류
        }
    }

    /**
     * 한국어 포함 여부 체크
     */
    private boolean containsKorean(String text) {
        return text.matches(".*[ㄱ-ㅎㅏ-ㅣ가-힣].*");
    }

    /**
     * 배치 처리 상태 조회
     */
    public String getBatchStatus() {
        long storeCount = storeRepository.count();
        long reviewCount = reviewRepository.count();
        long positiveCount = reviewRepository.countBySentiment("POSITIVE");
        long negativeCount = reviewRepository.countBySentiment("NEGATIVE");
        
        return String.format(
            "📊 배치 처리 현황 (이진 분류):\n" +
            "- 총 매장 수: %d\n" +
            "- 총 리뷰 수: %d\n" +
            "- 긍정 리뷰: %d\n" +
            "- 부정 리뷰: %d",
            storeCount, reviewCount, positiveCount, negativeCount
        );
    }
}
