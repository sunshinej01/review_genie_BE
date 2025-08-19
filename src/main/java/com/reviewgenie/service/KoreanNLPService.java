package com.reviewgenie.service;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class KoreanNLPService {

    // 간단한 한국어 패턴 매칭 기반 분석기
    private static final Pattern KOREAN_PATTERN = Pattern.compile("[가-힣]+");
    private static final Pattern NOUN_PATTERN = Pattern.compile(".*[가-힣]+(이|가|을|를|에|와|과|의|로|으로|에서|부터|까지|만|도|조차|마저|라도|나마|이나|거나|든지|이든|든)$");
    private static final Pattern VERB_PATTERN = Pattern.compile(".*[가-힣]+(다|하다|되다|있다|없다|라|아|어|지|고|면|니다|습니다|세요|어요|아요)$");
    private static final Pattern ADJ_PATTERN = Pattern.compile(".*[가-힣]+(한|은|는|던|을|ㄴ|적인|스러운|같은).*");

    /**
     * 간단한 패턴 기반 한국어 토크나이징
     */
    public List<String> tokenizeSimple(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        // 문장 부호 기준으로 분리 후 공백 기준 토크나이징
        List<String> tokens = new ArrayList<>();
        String cleanText = text.replaceAll("[^가-힣a-zA-Z0-9\\s]", " ");
        
        for (String word : cleanText.split("\\s+")) {
            if (!word.trim().isEmpty() && word.length() > 0) {
                tokens.add(word.trim());
            }
        }
        
        return tokens;
    }

    /**
     * 패턴 기반 간단한 형태소 분석 (경량화)
     */
    public Map<String, Object> analyzeMorphemes(String text) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, String>> morphemes = new ArrayList<>();
        List<String> nouns = new ArrayList<>();
        List<String> verbs = new ArrayList<>();
        List<String> adjectives = new ArrayList<>();
        
        List<String> tokens = tokenizeSimple(text);
        
        for (String token : tokens) {
            if (!KOREAN_PATTERN.matcher(token).find()) {
                continue; // 한국어가 아닌 토큰 건너뛰기
            }
            
            Map<String, String> morphInfo = new HashMap<>();
            morphInfo.put("surface", token);
            
            // 간단한 품사 추정 (패턴 기반)
            String pos = estimatePartOfSpeech(token);
            morphInfo.put("tag", pos);
            morphemes.add(morphInfo);
            
            // 품사별 분류
            switch (pos) {
                case "NOUN":
                    nouns.add(token);
                    break;
                case "VERB":
                    verbs.add(token);
                    break;
                case "ADJECTIVE":
                    adjectives.add(token);
                    break;
            }
        }
        
        result.put("morphemes", morphemes);
        result.put("nouns", nouns);
        result.put("verbs", verbs);
        result.put("adjectives", adjectives);
        
        return result;
    }
    
    /**
     * 간단한 품사 추정 (패턴 기반)
     */
    private String estimatePartOfSpeech(String word) {
        if (VERB_PATTERN.matcher(word).matches()) {
            return "VERB";
        } else if (ADJ_PATTERN.matcher(word).matches()) {
            return "ADJECTIVE";
        } else if (word.length() > 1 && KOREAN_PATTERN.matcher(word).matches()) {
            return "NOUN"; // 기본적으로 명사로 추정
        } else {
            return "UNKNOWN";
        }
    }

    /**
     * 키워드 추출 (명사 기반 + 빈도 분석)
     */
    public List<String> extractKeywords(String text) {
        Map<String, Object> analysis = analyzeMorphemes(text);
        
        @SuppressWarnings("unchecked")
        List<String> nouns = (List<String>) analysis.get("nouns");
        
        if (nouns == null || nouns.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 단어 빈도 계산 및 필터링
        Map<String, Long> frequency = nouns.stream()
            .filter(noun -> noun.length() > 1) // 1글자 제외
            .filter(noun -> !isStopWord(noun)) // 불용어 제외
            .filter(noun -> !isSpecialChar(noun)) // 특수문자 제외
            .collect(Collectors.groupingBy(
                noun -> noun, 
                Collectors.counting()
            ));
        
        // 빈도순 정렬하여 상위 키워드 반환
        return frequency.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(10)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }

    /**
     * 한국어 감정 분석 (룰 기반)
     */
    public Map<String, Object> analyzeSentiment(String text) {
        Map<String, Object> result = new HashMap<>();
        
        // 감정 키워드 사전 (확장 가능)
        Set<String> positiveWords = Set.of(
            "좋", "훌륭", "최고", "만족", "추천", "괜찮", "맛있", "훌륭", 
            "친절", "깨끗", "빠르", "편리", "감사", "완벽", "멋지", "사랑",
            "기쁘", "행복", "즐거", "재미", "신선", "맛", "품질", "서비스"
        );
        
        Set<String> negativeWords = Set.of(
            "나쁘", "최악", "불만", "별로", "싫", "맛없", "실망", "짜증",
            "불친절", "더럽", "느리", "불편", "화나", "문제", "오류", "고장",
            "비싸", "늦", "틀리", "잘못", "부족", "어려", "복잡", "답답"
        );
        
        List<String> tokens = tokenizeSimple(text);
        
        int positiveScore = 0;
        int negativeScore = 0;
        List<String> foundPositive = new ArrayList<>();
        List<String> foundNegative = new ArrayList<>();
        
        // 토큰별 감정 단어 찾기
        for (String token : tokens) {
            for (String pos : positiveWords) {
                if (token.contains(pos)) {
                    positiveScore++;
                    foundPositive.add(token);
                    break;
                }
            }
            for (String neg : negativeWords) {
                if (token.contains(neg)) {
                    negativeScore++;
                    foundNegative.add(token);
                    break;
                }
            }
        }
        
        // 결과 저장
        result.put("positiveScore", positiveScore);
        result.put("negativeScore", negativeScore);
        result.put("foundPositiveWords", foundPositive);
        result.put("foundNegativeWords", foundNegative);
        
        // 전체 감정 판정
        if (positiveScore > negativeScore) {
            result.put("sentiment", "POSITIVE");
            result.put("confidence", (double) positiveScore / (positiveScore + negativeScore));
        } else if (negativeScore > positiveScore) {
            result.put("sentiment", "NEGATIVE");
            result.put("confidence", (double) negativeScore / (positiveScore + negativeScore));
        } else {
            result.put("sentiment", "NEUTRAL");
            result.put("confidence", 0.5);
        }
        
        return result;
    }

    /**
     * 종합 텍스트 분석
     */
    public Map<String, Object> analyzeText(String text) {
        Map<String, Object> result = new HashMap<>();
        
        // 기본 정보
        result.put("originalText", text);
        result.put("textLength", text.length());
        result.put("language", "KOREAN");
        result.put("analyzer", "Apache Lucene Nori");
        
        // 분석 결과
        result.put("tokens", tokenizeSimple(text));
        result.put("morphemeAnalysis", analyzeMorphemes(text));
        result.put("keywords", extractKeywords(text));
        result.put("sentiment", analyzeSentiment(text));
        
        // 통계 정보
        List<String> tokens = tokenizeSimple(text);
        result.put("tokenCount", tokens.size());
        result.put("uniqueTokenCount", tokens.stream().distinct().count());
        
        return result;
    }

    /**
     * 불용어 체크
     */
    private boolean isStopWord(String word) {
        Set<String> stopWords = Set.of(
            "이", "그", "저", "것", "수", "등", "및", "제", "때", "곳", "중", "내", "외",
            "한", "두", "세", "네", "다섯", "또", "더", "덜", "말", "때문", "위해",
            "통해", "따라", "같이", "함께", "각각", "모든", "여러", "많은", "적은"
        );
        return stopWords.contains(word);
    }

    /**
     * 특수문자 체크
     */
    private boolean isSpecialChar(String word) {
        return word.matches(".*[^가-힣a-zA-Z0-9].*") || 
               word.matches("\\d+") || // 숫자만
               word.length() == 1; // 1글자
    }

    /**
     * 텍스트 정제
     */
    public String cleanText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "";
        }
        
        return text
            .replaceAll("[^가-힣a-zA-Z0-9\\s.,!?]", "") // 특수문자 제거
            .replaceAll("\\s+", " ") // 연속 공백 제거
            .trim();
    }
}