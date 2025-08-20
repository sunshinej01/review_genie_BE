package com.reviewgenie.service;

import com.reviewgenie.domain.Review;
import com.reviewgenie.domain.Store;
import com.reviewgenie.dto.ReviewDto;
import com.reviewgenie.repository.ReviewRepository;
import com.reviewgenie.repository.StoreRepository;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewAnalysisService {

    private final StanfordCoreNLP stanfordCoreNLP;
    private final KoreanNLPService koreanNLPService;
    private final ReviewRepository reviewRepository;
    private final StoreRepository storeRepository;

    // 간단한 한국어 불용어 목록 (필요에 따라 확장)
    private static final List<String> STOPWORDS = Arrays.asList(
        "이", "그", "저", "것", "수", "등", "및", "제", "저희"
    );

	/**
	 * 리뷰 저장
	 */
	@Transactional
	public Review saveReview(Long storeId, String content) {
		Store store = storeRepository.findById(storeId)
				.orElseThrow(() -> new RuntimeException("Store not found"));
		
		String sentiment = analyzeSentiment(content);
		
		Review review = Review.builder()
				.store(store)
				.content(content)
				.sentiment(sentiment)
				.createdAt(LocalDateTime.now())
				.build();
		
		return reviewRepository.save(review);
	}
	
	/**
	 * 상점별 리뷰 조회
	 */
	public List<ReviewDto> getReviewsByStore(Long storeId) {
		List<Review> reviews = reviewRepository.findByStoreId(storeId);
		return reviews.stream()
				.map(this::convertToDto)
				.collect(Collectors.toList());
	}
	
	/**
	 * 감정별 리뷰 통계
	 */
	public Map<String, Long> getSentimentStatistics() {
		List<Object[]> stats = reviewRepository.getSentimentStatistics();
		return stats.stream()
				.collect(Collectors.toMap(
					row -> (String) row[0],
					row -> (Long) row[1]
				));
	}
	
	/**
	 * DTO 변환
	 */
	private ReviewDto convertToDto(Review review) {
		return ReviewDto.builder()
				.reviewId(review.getReviewId())
				.storeId(review.getStore().getId())
				.content(review.getContent())
				.sentiment(review.getSentiment())
				.createdAt(review.getCreatedAt())
				.build();
	}

    public String analyzeSentiment(String reviewText) {
        // 한국어인지 체크 (한글 포함 여부로 간단 판단)
        if (containsKorean(reviewText)) {
            return analyzeKoreanSentiment(reviewText);
        } else {
            return analyzeEnglishSentiment(reviewText);
        }
    }
    
    /**
     * 한국어 감정 분석
     */
    public String analyzeKoreanSentiment(String reviewText) {
        try {
            Map<String, Object> sentimentResult = koreanNLPService.analyzeSentiment(reviewText);
            return (String) sentimentResult.get("sentiment");
        } catch (Exception e) {
            System.err.println("한국어 감정 분석 오류: " + e.getMessage());
            return "NEUTRAL";
        }
    }
    
    /**
     * 영어 감정 분석 (기존 Stanford CoreNLP 사용)
     */
    public String analyzeEnglishSentiment(String reviewText) {
        String preprocessedText = preprocessText(reviewText);

        Annotation annotation = new Annotation(preprocessedText);
        stanfordCoreNLP.annotate(annotation);

        // CoreNLP는 문장 단위로 감성을 분석
        for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
            String sentiment = sentence.get(SentimentCoreAnnotations.SentimentClass.class);
            // "Very Positive", "Positive" -> POSITIVE
            // "Very Negative", "Negative" -> NEGATIVE
            // "Neutral" -> NEUTRAL
            if (sentiment.contains("Positive")) return "POSITIVE";
            if (sentiment.contains("Negative")) return "NEGATIVE";
        }
        return "NEUTRAL";
    }
    
    /**
     * 한국어 키워드 추출
     */
    public List<String> extractKoreanKeywords(String reviewText) {
        try {
            // extractKeywords 메서드가 없으므로 extractKeywordRankings을 사용하여 대체
            Map<String, Object> keywordResult = koreanNLPService.extractKeywordRankings(reviewText);
            @SuppressWarnings("unchecked")
            List<Map.Entry<String, Long>> generalKeywords = (List<Map.Entry<String, Long>>) keywordResult.get("generalKeywordsRank");
            
            return generalKeywords.stream()
                .map(entry -> entry.getKey())
                .collect(java.util.stream.Collectors.toList());
        } catch (Exception e) {
            System.err.println("한국어 키워드 추출 오류: " + e.getMessage());
            return Collections.emptyList();
        }
    }
    
    /**
     * 종합 리뷰 분석 (한국어/영어 자동 감지)
     */
    public Map<String, Object> analyzeReview(String reviewText) {
        if (containsKorean(reviewText)) {
            // analyzeText 메서드가 없으므로 여러 메서드를 조합하여 대체
            Map<String, Object> sentimentResult = koreanNLPService.analyzeSentiment(reviewText);
            Map<String, Object> keywordResult = koreanNLPService.extractKeywordRankings(reviewText);
            List<String> tokens = koreanNLPService.tokenizeSimple(reviewText);
            
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("originalText", reviewText);
            result.put("sentiment", sentimentResult.get("sentiment"));
            result.put("keywordRankings", keywordResult);
            result.put("tokens", tokens);
            result.put("language", "KOREAN");
            
            return result;
        } else {
            // 영어 분석 로직 (추후 구현 가능)
            return Map.of(
                "originalText", reviewText,
                "sentiment", analyzeEnglishSentiment(reviewText),
                "language", "ENGLISH"
            );
        }
    }

    /**
     * 이진 감성 분석 (주요 키워드 중심 → 없으면 백업 로직)
     * 반환: { label, matchedSentences, posCount, negCount, fallbackUsed, language }
     */
    public Map<String, Object> analyzeBinary(String reviewText) {
        if (containsKorean(reviewText)) {
            Map<String, Object> byKeyTerms = koreanNLPService.classifyBinaryByKeyTerms(reviewText);
            String label = (String) byKeyTerms.get("label");
            Integer matched = (Integer) byKeyTerms.get("matchedSentences");
            if (matched != null && matched > 0 && ("POSITIVE".equals(label) || "NEGATIVE".equals(label))) {
                byKeyTerms.put("fallbackUsed", false);
                byKeyTerms.put("language", "KOREAN");
                return byKeyTerms;
            }

            // 백업: 한국어 전체 감성 → 이진 매핑(NEUTRAL은 POSITIVE로)
            Map<String, Object> sentiment = koreanNLPService.analyzeSentiment(reviewText);
            String overall = (String) sentiment.get("sentiment");
            String binary = "NEGATIVE".equalsIgnoreCase(overall) ? "NEGATIVE" : "POSITIVE";
            return Map.of(
                "label", binary,
                "matchedSentences", 0,
                "posCount", 0,
                "negCount", 0,
                "fallbackUsed", true,
                "language", "KOREAN"
            );
        } else {
            // 영어: 기존 영어 감성 → 이진 매핑(NEUTRAL은 POSITIVE)
            String sentiment = analyzeEnglishSentiment(reviewText);
            String binary = "NEGATIVE".equalsIgnoreCase(sentiment) ? "NEGATIVE" : "POSITIVE";
            return Map.of(
                "label", binary,
                "matchedSentences", 0,
                "posCount", 0,
                "negCount", 0,
                "fallbackUsed", true,
                "language", "ENGLISH"
            );
        }
    }
    
    /**
     * 한국어 포함 여부 체크
     */
    private boolean containsKorean(String text) {
        return text.matches(".*[ㄱ-ㅎㅏ-ㅣ가-힣].*");
    }

    private String preprocessText(String text) {
        // 1. 이모티콘 및 특수문자 제거 (정규표현식)
        String emojiRemoved = text.replaceAll("[\\p{So}\\p{Cn}]", "");
        
        // 2. 불용어 제거
        StringBuilder sb = new StringBuilder();
        for (String word : emojiRemoved.split("\\s+")) {
            if (!STOPWORDS.contains(word.trim())) {
                sb.append(word).append(" ");
            }
        }
        return sb.toString().trim();
    }
}


