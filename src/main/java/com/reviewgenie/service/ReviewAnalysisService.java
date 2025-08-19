package com.reviewgenie.service;

import com.reviewgenie.dto.ReviewDto;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReviewAnalysisService {

    private final StanfordCoreNLP stanfordCoreNLP;
    private final KoreanNLPService koreanNLPService;

    // 간단한 한국어 불용어 목록 (필요에 따라 확장)
    private static final List<String> STOPWORDS = Arrays.asList(
        "이", "그", "저", "것", "수", "등", "및", "제", "저희"
    );

	public List<ReviewDto> getSampleReviews() {
		return Collections.emptyList();
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
            return koreanNLPService.extractKeywords(reviewText);
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
            return koreanNLPService.analyzeText(reviewText);
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


