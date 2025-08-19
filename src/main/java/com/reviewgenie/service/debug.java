package com.reviewgenie.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public class debug {

    public static void main(String[] args) {
        // 1. 서비스 인스턴스 생성 (KoreanNLPService 모듈 import)
        KoreanNLPService nlpService = new KoreanNLPService(null);
        
        // 2. 출력 파일 및 JSON 파일 경로 설정
        File outputFile = new File("output/sentiment_analysis_results.txt");
        File jsonFile = new File("src/main/resources/data/reviews.json");

        // 출력 디렉터리가 없으면 생성
        outputFile.getParentFile().mkdirs();

        System.out.println("✅ reviews.json 파일 키워드별 감성분석을 시작합니다.");
        System.out.println("➡️ 결과는 이 경로에 저장됩니다: " + outputFile.getAbsolutePath());
        
        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(outputFile)))) {
            
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> data = objectMapper.readValue(
                jsonFile, new TypeReference<Map<String, Object>>() {}
            );

            writer.println("## 📊 키워드 중심 이진 감성분석 테스트 결과 ##");
            writer.println("==============================================");
            writer.println("🎯 핵심 키워드: [맛, 가격, 대기시간, 서비스, 예약, 포장, 청결, 인테리어, 메뉴]");
            writer.println("❤️ 감성분석: 키워드별 긍정/부정 분류 → 전체 POSITIVE/NEGATIVE 결정 (중립 없음, positive bias)");
            writer.println();

            // 전체 통계를 위한 변수들
            final AtomicInteger totalReviews = new AtomicInteger(0);
            java.util.Map<String, AtomicInteger> globalKeywordPositive = new java.util.HashMap<>();
            java.util.Map<String, AtomicInteger> globalKeywordNegative = new java.util.HashMap<>();

            // 3. store_list에서 모든 스토어의 리뷰를 순회하며 감성분석
            @SuppressWarnings("unchecked")
            Map<String, Object> storeList = (Map<String, Object>) data.get("store_list");
            
            for (Map.Entry<String, Object> storeEntry : storeList.entrySet()) {
                String storeName = storeEntry.getKey();
                Object storeValue = storeEntry.getValue();
                
                writer.printf("### [%s] 감성분석 결과 ###%n", storeName);
                writer.println("------------------------------------------");
                
                if (storeValue instanceof Map) {
                    // My_store와 같은 단일 스토어 처리
                    @SuppressWarnings("unchecked")
                    Map<String, Object> store = (Map<String, Object>) storeValue;
                    @SuppressWarnings("unchecked")
                    List<String> reviews = (List<String>) store.get("reviews");
                    String placeName = (String) store.get("place_name");
                    
                    if (reviews != null) {
                        processSentimentAnalysis(writer, nlpService, reviews, placeName, totalReviews, 
                                globalKeywordPositive, globalKeywordNegative);
                    }
                } else if (storeValue instanceof List) {
                    // Competitor와 같은 스토어 배열 처리
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> competitors = (List<Map<String, Object>>) storeValue;
                    
                    for (Map<String, Object> competitor : competitors) {
                        @SuppressWarnings("unchecked")
                        List<String> reviews = (List<String>) competitor.get("reviews");
                        String placeName = (String) competitor.get("place_name");
                        
                        writer.printf("▶ %s%n", placeName);
                        
                        if (reviews != null) {
                            processSentimentAnalysis(writer, nlpService, reviews, placeName, totalReviews, 
                                    globalKeywordPositive, globalKeywordNegative);
                        }
                    }
                }
                writer.println();
            }
            
            // 전체 감성분석 통계 출력
            writer.println();
            writer.println("==============================================");
            writer.println("## 📊 전체 감성분석 통계 ##");
            writer.println("==============================================");
            writer.printf("📊 총 리뷰 수: %d개%n", totalReviews.get());

            // 전체 긍정/부정 계산
            int totalPositive = 0;
            int totalNegative = 0;
            for (AtomicInteger pos : globalKeywordPositive.values()) {
                totalPositive += pos.get();
            }
            for (AtomicInteger neg : globalKeywordNegative.values()) {
                totalNegative += neg.get();
            }
            int totalSentimentReviews = totalPositive + totalNegative;
            if (totalSentimentReviews > 0) {
                double posRate = (double) totalPositive / totalSentimentReviews * 100;
                double negRate = (double) totalNegative / totalSentimentReviews * 100;
                writer.printf("💝 전체 감성: 긍정 %d개 (%.1f%%), 부정 %d개 (%.1f%%)%n", totalPositive, posRate, totalNegative, negRate);
            }

            writer.println();
            writer.println("🎯 전체 키워드별 감성 통계:");
            writer.println("------------------------------------------");

            // 전체 키워드별 통계 출력
            Map<String, Integer> totalKeywordCounts = new HashMap<>();
            for (String keyword : globalKeywordPositive.keySet()) {
                int pos = globalKeywordPositive.get(keyword).get();
                int neg = globalKeywordNegative.getOrDefault(keyword, new AtomicInteger(0)).get();
                totalKeywordCounts.put(keyword, pos + neg);
            }
            for (String keyword : globalKeywordNegative.keySet()) {
                if (!totalKeywordCounts.containsKey(keyword)) {
                    int pos = globalKeywordPositive.getOrDefault(keyword, new AtomicInteger(0)).get();
                    int neg = globalKeywordNegative.get(keyword).get();
                    totalKeywordCounts.put(keyword, pos + neg);
                }
            }

            totalKeywordCounts.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .forEach(entry -> {
                        String keyword = entry.getKey();
                        int total = entry.getValue();
                        int pos = globalKeywordPositive.getOrDefault(keyword, new AtomicInteger(0)).get();
                        int neg = globalKeywordNegative.getOrDefault(keyword, new AtomicInteger(0)).get();
                        double posRate = total > 0 ? (double) pos / total * 100 : 0.0;
                        double negRate = total > 0 ? (double) neg / total * 100 : 0.0;
                        writer.printf("🔸 %s: 긍정 %d개 (%.1f%%), 부정 %d개 (%.1f%%) [총 %d회 언급]%n", 
                                keyword, pos, posRate, neg, negRate, total);
                    });

            writer.println();
            writer.println("==============================================");
            writer.println("✅ 키워드 중심 감성분석 테스트 완료.");
            writer.println("📈 주요 키워드별로 정확한 이진 감성분석이 수행되었습니다.");
            writer.println();

        } catch (IOException e) {
            System.out.println("❌ 파일 처리 중 오류가 발생했습니다: " + e.getMessage());
            e.printStackTrace();
            return;
        }
        
        System.out.println("✅ 키워드별 감성분석 결과 파일이 성공적으로 저장되었습니다.");
    }

    private static void processSentimentAnalysis(PrintWriter writer, KoreanNLPService nlpService, 
            List<String> reviews, String storeName, AtomicInteger totalReviews,
            Map<String, AtomicInteger> globalKeywordPositive, Map<String, AtomicInteger> globalKeywordNegative) {
        
        int maxReviews = reviews.size(); // 전체 리뷰 분석
        writer.printf("📊 총 %d개 리뷰 전체 분석 (상세 출력: 처음 3개)%n%n", reviews.size());

        // Store별 감성분석 통계를 위한 변수들
        final AtomicInteger storeReviewCount = new AtomicInteger(0);
        final AtomicInteger storePositiveCount = new AtomicInteger(0);
        final AtomicInteger storeNegativeCount = new AtomicInteger(0);
        java.util.Map<String, AtomicInteger> storeKeywordPositive = new java.util.HashMap<>();
        java.util.Map<String, AtomicInteger> storeKeywordNegative = new java.util.HashMap<>();

        // 처음 3개 리뷰만 상세 분석 출력, 나머지는 통계만 계산
        int detailOutputLimit = Math.min(3, maxReviews);
        
        for (int i = 0; i < maxReviews; i++) {
            String content = reviews.get(i);
            if (content != null && !content.isEmpty()) {
                totalReviews.incrementAndGet();
                storeReviewCount.incrementAndGet();

                boolean showDetails = (i < detailOutputLimit);
                
                if (showDetails) {
                    writer.printf("◆ 리뷰 #%d%n", i + 1);
                    writer.printf("내용: %s%n", content.length() > 100 ? content.substring(0, 100) + "..." : content);

                    // [DEBUG] 토큰화 결과 확인
                    List<String> tokens = nlpService.tokenizeSimple(content);
                    writer.printf("🔍 토큰화 결과: %s%n", tokens.toString());
                    
                    // [DEBUG] 맥락 기반 키워드 감지 확인
                    Set<String> detectedKeywords = detectKeywordsWithContext(tokens, content);
                    writer.printf("🎯 맥락 기반 감지된 키워드: %s%n", detectedKeywords.toString());
                }

                // KoreanNLPService의 키워드별 감성분석 모듈 사용
                Map<String, Map<String, Integer>> keywordSentiment = nlpService.analyzeSentimentByKeyTerms(content);
                
                // 감성별 키워드 카운트
                int positiveKeywords = 0;
                int negativeKeywords = 0;
                int totalKeywordMentions = 0;

                for (Map.Entry<String, Map<String, Integer>> entry : keywordSentiment.entrySet()) {
                    String keyword = entry.getKey();
                    Map<String, Integer> sentimentCounts = entry.getValue();
                    int pos = sentimentCounts.get("POSITIVE");
                    int neg = sentimentCounts.get("NEGATIVE");
                    
                    if (pos > 0 || neg > 0) {
                        totalKeywordMentions++;
                        if (pos > neg) {
                            positiveKeywords++;
                            storeKeywordPositive.computeIfAbsent(keyword, k -> new AtomicInteger(0)).incrementAndGet();
                            globalKeywordPositive.computeIfAbsent(keyword, k -> new AtomicInteger(0)).incrementAndGet();
                        } else if (neg > pos) {
                            negativeKeywords++;
                            storeKeywordNegative.computeIfAbsent(keyword, k -> new AtomicInteger(0)).incrementAndGet();
                            globalKeywordNegative.computeIfAbsent(keyword, k -> new AtomicInteger(0)).incrementAndGet();
                        }
                        // pos == neg인 경우는 positive bias에 의해 이미 KoreanNLPService에서 처리됨
                    }
                }

                // 전체 감성 결정 (중립 없음, positive bias 적용)
                String finalSentiment;
                double confidence;
                if (totalKeywordMentions == 0) {
                    // 키워드가 없는 경우도 기본 감성분석으로 positive/negative 결정
                    Map<String, Object> binaryResult = nlpService.classifyBinaryByKeyTerms(content);
                    String label = (String) binaryResult.get("label");
                    if ("UNKNOWN".equals(label)) {
                        // fallback으로 전체 감성분석 후 이진 분류
                        Map<String, Object> overallSentiment = nlpService.analyzeSentiment(content);
                        String sentiment = (String) overallSentiment.get("sentiment");
                        finalSentiment = "NEUTRAL".equals(sentiment) ? "POSITIVE" : sentiment; // 중립도 positive로
                    } else {
                        finalSentiment = label;
                    }
                    confidence = 0.0; // 키워드가 없어서 낮은 신뢰도
                } else if (positiveKeywords >= negativeKeywords) { // positive bias 적용
                    finalSentiment = "POSITIVE";
                    confidence = (double) positiveKeywords / totalKeywordMentions;
                } else {
                    finalSentiment = "NEGATIVE";
                    confidence = (double) negativeKeywords / totalKeywordMentions;
                }

                // 전체 통계 업데이트
                if ("POSITIVE".equals(finalSentiment)) {
                    storePositiveCount.incrementAndGet();
                } else {
                    storeNegativeCount.incrementAndGet();
                }

                if (showDetails) {
                    writer.printf("💝 전체 감성: %s (신뢰도: %.2f, 분석된 키워드: %d개)%n", finalSentiment, confidence, totalKeywordMentions);
                    writer.println("🎯 키워드별 감성:");

                    // 키워드별 감성 출력 (상세)
                    for (Map.Entry<String, Map<String, Integer>> entry : keywordSentiment.entrySet()) {
                        String keyword = entry.getKey();
                        Map<String, Integer> sentimentCounts = entry.getValue();
                        int pos = sentimentCounts.get("POSITIVE");
                        int neg = sentimentCounts.get("NEGATIVE");
                        
                        if (pos > 0 || neg > 0) {
                            if (pos >= neg) { // positive bias: 동점도 positive로
                                writer.printf("  😊 %s: POSITIVE (긍정 %d, 부정 %d)%n", keyword, pos, neg);
                            } else {
                                writer.printf("  😞 %s: NEGATIVE (긍정 %d, 부정 %d)%n", keyword, pos, neg);
                            }
                        }
                    }
                    
                    // [DEBUG] 맥락 분석 정보
                    writer.println("📚 맥락 분석 정보:");
                    analyzeContextForKeywords(writer, content, nlpService.tokenizeSimple(content));

                    writer.println(); // 빈 줄 추가
                }
            }
        }

        // Store별 감성분석 통계 출력
        writer.println("==============================================");
        writer.printf("## 📈 [%s] 감성분석 통계 ##%n", storeName);
        writer.println("==============================================");
        writer.printf("📊 분석된 리뷰 수: %d개%n", storeReviewCount.get());
        writer.printf("💝 전체 감성: 긍정 %d개, 부정 %d개%n", storePositiveCount.get(), storeNegativeCount.get());
        writer.println();

        // Store별 키워드 감성 통계
        writer.println("🎯 키워드별 감성 통계 (전체 리뷰 기준, 상위순):");
        writer.println("------------------------------------------");
        
        // 키워드별 총 언급수 계산
        Map<String, Integer> keywordTotalCounts = new HashMap<>();
        for (String keyword : storeKeywordPositive.keySet()) {
            int pos = storeKeywordPositive.get(keyword).get();
            int neg = storeKeywordNegative.getOrDefault(keyword, new AtomicInteger(0)).get();
            keywordTotalCounts.put(keyword, pos + neg);
        }
        for (String keyword : storeKeywordNegative.keySet()) {
            if (!keywordTotalCounts.containsKey(keyword)) {
                int pos = storeKeywordPositive.getOrDefault(keyword, new AtomicInteger(0)).get();
                int neg = storeKeywordNegative.get(keyword).get();
                keywordTotalCounts.put(keyword, pos + neg);
            }
        }

        // 언급수 순으로 정렬하여 출력
        keywordTotalCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(entry -> {
                    String keyword = entry.getKey();
                    int total = entry.getValue();
                    int pos = storeKeywordPositive.getOrDefault(keyword, new AtomicInteger(0)).get();
                    int neg = storeKeywordNegative.getOrDefault(keyword, new AtomicInteger(0)).get();
                    double posRate = total > 0 ? (double) pos / total * 100 : 0.0;
                    double negRate = total > 0 ? (double) neg / total * 100 : 0.0;
                    writer.printf("🔸 %s: 총 %d회 | 긍정 %d개 (%.1f%%), 부정 %d개 (%.1f%%)%n", 
                            keyword, total, pos, posRate, neg, negRate);
                });

        // 전체 리뷰 키워드별 감성분석 (모든 리뷰)
        writer.println("==============================================");
        writer.printf("## 📈 [%s] 전체 리뷰 키워드별 감성 통계 ##%n", storeName);
        writer.println("==============================================");
        
        // 전체 리뷰 분석
        Map<String, AtomicInteger> allReviewsKeywordPositive = new HashMap<>();
        Map<String, AtomicInteger> allReviewsKeywordNegative = new HashMap<>();
        
        for (String content : reviews) {
            if (content != null && !content.isEmpty()) {
                Map<String, Map<String, Integer>> keywordSentiment = nlpService.analyzeSentimentByKeyTerms(content);
                
                for (Map.Entry<String, Map<String, Integer>> entry : keywordSentiment.entrySet()) {
                    String keyword = entry.getKey();
                    Map<String, Integer> sentimentCounts = entry.getValue();
                    int pos = sentimentCounts.get("POSITIVE");
                    int neg = sentimentCounts.get("NEGATIVE");
                    
                    if (pos > neg && pos > 0) {
                        allReviewsKeywordPositive.computeIfAbsent(keyword, k -> new AtomicInteger(0)).incrementAndGet();
                    } else if (neg > pos && neg > 0) {
                        allReviewsKeywordNegative.computeIfAbsent(keyword, k -> new AtomicInteger(0)).incrementAndGet();
                    }
                }
            }
        }

        // 전체 리뷰 키워드별 총 언급수 계산
        Map<String, Integer> allKeywordTotalCounts = new HashMap<>();
        for (String keyword : allReviewsKeywordPositive.keySet()) {
            int pos = allReviewsKeywordPositive.get(keyword).get();
            int neg = allReviewsKeywordNegative.getOrDefault(keyword, new AtomicInteger(0)).get();
            allKeywordTotalCounts.put(keyword, pos + neg);
        }
        for (String keyword : allReviewsKeywordNegative.keySet()) {
            if (!allKeywordTotalCounts.containsKey(keyword)) {
                int pos = allReviewsKeywordPositive.getOrDefault(keyword, new AtomicInteger(0)).get();
                int neg = allReviewsKeywordNegative.get(keyword).get();
                allKeywordTotalCounts.put(keyword, pos + neg);
            }
        }

        // 전체 리뷰 키워드 통계 출력
        allKeywordTotalCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(entry -> {
                    String keyword = entry.getKey();
                    int total = entry.getValue();
                    int pos = allReviewsKeywordPositive.getOrDefault(keyword, new AtomicInteger(0)).get();
                    int neg = allReviewsKeywordNegative.getOrDefault(keyword, new AtomicInteger(0)).get();
                    double posRate = total > 0 ? (double) pos / total * 100 : 0.0;
                    double negRate = total > 0 ? (double) neg / total * 100 : 0.0;
                    writer.printf("🔸 %s: 총 %d회 | 긍정 %d개 (%.1f%%), 부정 %d개 (%.1f%%)%n", 
                            keyword, total, pos, posRate, neg, negRate);
                });

        writer.println();
    }
    
    // 헬퍼 메서드들
    private static String normalizeNoun(String noun) {
        if (noun == null || noun.length() <= 1) {
            return noun;
        }
        return noun.replaceAll("(도|나|는|은|이|가|을|를)$", "");
    }
    
    private static boolean isKeyTerm(String word) {
        Set<String> KEY_TERMS = Set.of("맛", "가격", "대기시간", "서비스", "예약", "포장", "청결", "인테리어", "메뉴");
        return KEY_TERMS.contains(word);
    }
    
    /**
     * 맥락 기반 키워드 감지 (KoreanNLPService의 inferKeywordsFromContext와 동일한 로직)
     */
    private static Set<String> detectKeywordsWithContext(List<String> tokens, String content) {
        Set<String> detectedKeywords = new HashSet<>();
        String text = String.join(" ", tokens);
        
        // 1. 직접적인 키워드 매칭
        for (String token : tokens) {
            String normalized = normalizeNoun(token);
            if (isKeyTerm(normalized)) {
                detectedKeywords.add(normalized);
            }
        }
        
        // 2. 맥락 기반 키워드 추론
        // 인테리어 관련 감성어가 있으면 인테리어 키워드 추론
        if (containsAny(text, Arrays.asList("러블리", "예쁜", "예쁘", "꾸며져", "아기자기", "멋지", "이쁘", "분위기", "인테리어"))) {
            detectedKeywords.add("인테리어");
        }
        
        // 맛 관련 감성어가 있으면 맛 키워드 추론
        if (containsAny(text, Arrays.asList("맛있", "맛나", "짱", "달콤", "고소", "맛없", "별로", "달고", "진해", "맛집"))) {
            detectedKeywords.add("맛");
        }
        
        // 대기시간 관련 감성어가 있으면 대기시간 키워드 추론
        if (containsAny(text, Arrays.asList("줄서", "기다려", "웨이팅", "대기", "길어", "오래", "바로", "금방", "빠르"))) {
            detectedKeywords.add("대기시간");
        }
        
        // 서비스 관련 감성어가 있으면 서비스 키워드 추론
        if (containsAny(text, Arrays.asList("친절", "불친절", "서비스", "직원", "배려", "정성", "세심"))) {
            detectedKeywords.add("서비스");
        }
        
        // 메뉴 관련 감성어가 있으면 메뉴 키워드 추론
        if (containsAny(text, Arrays.asList("디저트", "케이크", "음료", "종류", "다양", "선택", "메뉴"))) {
            detectedKeywords.add("메뉴");
        }
        
        return detectedKeywords;
    }
    
    private static void analyzeContextForKeywords(PrintWriter writer, String content, List<String> tokens) {
        String text = String.join(" ", tokens);
        
        // 키워드 추론 및 맥락 분석
        writer.println("  🔍 키워드 추론 및 맥락 분석:");
        
        // 인테리어 맥락 분석
        if (containsAny(text, Arrays.asList("러블리", "예쁜", "예쁘", "꾸며져", "아기자기", "멋지", "이쁘", "분위기"))) {
            writer.println("    🏠 인테리어 관련 맥락 감지:");
            if (text.contains("러블리")) writer.println("      - '러블리' 발견 → 인테리어 긍정");
            if (text.contains("꾸며져")) writer.println("      - '꾸며져' 발견 → 인테리어 긍정");
            if (text.contains("예쁘") || text.contains("예쁜")) writer.println("      - '예쁘다' 발견 → 인테리어 긍정");
            if (text.contains("아기자기")) writer.println("      - '아기자기' 발견 → 인테리어 긍정");
        }
        
        // 맛 맥락 분석
        if (containsAny(text, Arrays.asList("맛있", "맛나", "짱", "달콤", "고소", "맛없", "별로", "달고", "진해"))) {
            writer.println("    🍽️ 맛 관련 맥락 감지:");
            if (text.contains("짱")) writer.println("      - '짱' 발견 → 맛 긍정");
            if (text.contains("맛있")) writer.println("      - '맛있다' 발견 → 맛 긍정");
            if (text.contains("달고") && text.contains("진해")) writer.println("      - '달고 진해서' 발견 → 맛 부정");
            if (text.contains("아쉬웠어요")) writer.println("      - '아쉬웠어요' 발견 → 부정 맥락");
        }
        
        // 대기시간 맥락 분석
        if (containsAny(text, Arrays.asList("줄서", "기다려", "웨이팅", "대기", "길어", "오래", "바로", "금방"))) {
            writer.println("    ⏰ 대기시간 관련 맥락 감지:");
            if (text.contains("줄서")) writer.println("      - '줄서다' 발견 → 대기시간 부정");
            if (text.contains("기다려")) writer.println("      - '기다리다' 발견 → 대기시간 부정");
            if (text.contains("웨이팅")) writer.println("      - '웨이팅' 발견 → 대기시간 부정");
            if (text.contains("바로")) writer.println("      - '바로' 발견 → 대기시간 긍정");
        }
        
        // 서비스 맥락 분석
        if (containsAny(text, Arrays.asList("친절", "불친절", "직원", "배려", "정성"))) {
            writer.println("    👥 서비스 관련 맥락 감지:");
            if (text.contains("친절")) writer.println("      - '친절' 발견 → 서비스 긍정");
            if (text.contains("불친절")) writer.println("      - '불친절' 발견 → 서비스 부정");
            if (text.contains("직원")) writer.println("      - '직원' 발견 → 서비스 관련");
        }
        
        // 메뉴 맥락 분석
        if (containsAny(text, Arrays.asList("디저트", "케이크", "음료", "종류", "다양", "선택"))) {
            writer.println("    📋 메뉴 관련 맥락 감지:");
            if (text.contains("다양")) writer.println("      - '다양' 발견 → 메뉴 긍정");
            if (text.contains("종류")) writer.println("      - '종류' 발견 → 메뉴 관련");
            if (text.contains("디저트")) writer.println("      - '디저트' 발견 → 메뉴 관련");
            if (text.contains("케이크")) writer.println("      - '케이크' 발견 → 메뉴 관련");
        }
    }
    
    private static boolean containsAny(String text, List<String> words) {
        for (String word : words) {
            if (text.contains(word)) {
                return true;
            }
        }
        return false;
    }
}