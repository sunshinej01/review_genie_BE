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
import java.util.concurrent.atomic.AtomicInteger;

public class debug {

    public static void main(String[] args) {
        // 1. 서비스 인스턴스 생성
        KoreanNLPService nlpService = new KoreanNLPService(null); // Repository는 null로 설정 (DB 저장 안함)
        
        // 2. 출력 파일 및 JSON 파일 경로 설정
        File outputFile = new File("output/reviews_keyword_analysis.txt");
        File jsonFile = new File("src/main/resources/data/reviews.json");

        // 출력 디렉터리가 없으면 생성
        outputFile.getParentFile().mkdirs();

        System.out.println("✅ reviews.json 파일 키워드 분석을 시작합니다.");
        System.out.println("➡️ 결과는 이 경로에 저장됩니다: " + outputFile.getAbsolutePath());
        
        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(outputFile)))) {
            
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> data = objectMapper.readValue(
                jsonFile, new TypeReference<Map<String, Object>>() {}
            );

            writer.println("## 📊 reviews.json 파일 키워드 분석 결과 ##");
            writer.println("==============================================");
            writer.println("🎯 핵심 키워드: [맛, 가격, 대기시간, 서비스, 예약, 포장, 청결, 인테리어, 메뉴]");
            writer.println("📊 일반 키워드: 명사 위주 키워드 (최소 10개, 최대 20개)");
            writer.println();

            // 전체 키워드 통계를 위한 변수들
            java.util.Map<String, Integer> globalKeyTermsCount = new java.util.HashMap<>();
            java.util.Map<String, Integer> globalGeneralKeywords = new java.util.HashMap<>();
            final AtomicInteger totalReviews = new AtomicInteger(0);

            // 3. store_list에서 모든 스토어의 리뷰를 순회하며 키워드 분석
            @SuppressWarnings("unchecked")
            Map<String, Object> storeList = (Map<String, Object>) data.get("store_list");
            
            for (Map.Entry<String, Object> storeEntry : storeList.entrySet()) {
                String storeName = storeEntry.getKey();
                Object storeValue = storeEntry.getValue();
                
                writer.printf("### [%s] 키워드 분석 ###%n", storeName);
                writer.println("------------------------------------------");
                
                if (storeValue instanceof Map) {
                    // My_store와 같은 단일 스토어 처리
                    @SuppressWarnings("unchecked")
                    Map<String, Object> store = (Map<String, Object>) storeValue;
                    @SuppressWarnings("unchecked")
                    List<String> reviews = (List<String>) store.get("reviews");
                    
                    if (reviews != null) {
                        int maxReviews = Math.min(reviews.size(), 10); // 최대 10개까지만 분석
                        writer.printf("📊 총 %d개 리뷰 중 처음 %d개 분석%n%n", reviews.size(), maxReviews);
                        
                        // Store별 키워드 통계를 위한 변수들
                        java.util.Map<String, Integer> storeKeyTermsCount = new java.util.HashMap<>();
                        java.util.Map<String, Integer> storeGeneralKeywords = new java.util.HashMap<>();
                        final AtomicInteger storeReviewCount = new AtomicInteger(0);
                        
                        for (int i = 0; i < maxReviews; i++) {
                            String content = reviews.get(i);
                            if (content != null && !content.isEmpty()) {
                                totalReviews.incrementAndGet();
                                storeReviewCount.incrementAndGet();
                                
                                // 키워드 순위 분석
                                Map<String, Object> keywordResult = nlpService.extractKeywordRankings(content);
                                
                                writer.printf("◆ 리뷰 #%d%n", i + 1);
                                writer.printf("내용: %s%n", content.length() > 100 ? content.substring(0, 100) + "..." : content);
                                
                                // 핵심 키워드 분석 결과
                                @SuppressWarnings("unchecked")
                                Map<String, Long> keyTermsCount = (Map<String, Long>) keywordResult.get("keyTermsCount");
                                writer.println("🎯 핵심 키워드 발견:");
                                if (keyTermsCount.isEmpty()) {
                                    writer.println("  - 발견된 핵심 키워드 없음");
                                } else {
                                    for (Map.Entry<String, Long> entry : keyTermsCount.entrySet()) {
                                        String word = entry.getKey();
                                        Long count = entry.getValue();
                                        writer.printf("  ✓ %s: %d회 언급%n", word, count);
                                        
                                        // 전체 및 Store별 핵심 키워드 카운트 업데이트
                                        globalKeyTermsCount.put(word, globalKeyTermsCount.getOrDefault(word, 0) + count.intValue());
                                        storeKeyTermsCount.put(word, storeKeyTermsCount.getOrDefault(word, 0) + count.intValue());
                                    }
                                }
                                
                                // 일반 키워드 순위 결과
                                @SuppressWarnings("unchecked")
                                List<Map.Entry<String, Long>> generalKeywords = (List<Map.Entry<String, Long>>) keywordResult.get("generalKeywordsRank");
                                writer.printf("📊 명사 중심 키워드 순위 (총 %d개):%n", generalKeywords.size());
                                if (generalKeywords.isEmpty()) {
                                    writer.println("  - 발견된 일반 키워드 없음");
                                } else {
                                    int rank = 1;
                                    for (Map.Entry<String, Long> entry : generalKeywords) {
                                        String word = entry.getKey();
                                        Long frequency = entry.getValue();
                                        
                                        writer.printf("  %d위: %s (%d회)%n", rank++, word, frequency);
                                        
                                        // 전체 및 Store별 일반 키워드 카운트 업데이트
                                        globalGeneralKeywords.put(word, globalGeneralKeywords.getOrDefault(word, 0) + frequency.intValue());
                                        storeGeneralKeywords.put(word, storeGeneralKeywords.getOrDefault(word, 0) + frequency.intValue());
                                    }
                                }
                                writer.println();
                            }
                        }
                        
                        // Store별 키워드 분석 통계 출력
                        writer.println("==============================================");
                        writer.printf("## 📈 [%s] 키워드 분석 통계 ##%n", storeName);
                        writer.println("==============================================");
                        writer.printf("📊 분석된 리뷰 수: %d개%n", storeReviewCount.get());
                        writer.println();
                        
                        // 핵심 키워드 통계
                        writer.println("🎯 핵심 키워드 통계:");
                        writer.println("------------------------------------------");
                        if (storeKeyTermsCount.isEmpty()) {
                            writer.println("발견된 핵심 키워드가 없습니다.");
                        } else {
                            int finalStoreReviewCount = storeReviewCount.get();
                            storeKeyTermsCount.entrySet().stream()
                                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                                    .forEach(entry -> {
                                        String word = entry.getKey();
                                        Integer count = entry.getValue();
                                        double mentionRate = (double) count / finalStoreReviewCount * 100;
                                        writer.printf("🔸 %s: 총 %d회 언급 (%.1f%% 비율)%n", 
                                                word, count, mentionRate);
                                    });
                        }
                        writer.println();
                        
                        // 일반 키워드 통계 (상위 15개)
                        writer.println("📊 명사 중심 키워드 통계 (상위 15개):");
                        writer.println("------------------------------------------");
                        if (storeGeneralKeywords.isEmpty()) {
                            writer.println("발견된 일반 키워드가 없습니다.");
                        } else {
                            int finalStoreReviewCount2 = storeReviewCount.get();
                            storeGeneralKeywords.entrySet().stream()
                                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                                    .limit(15) // 상위 15개만 출력
                                    .forEach(entry -> {
                                        String word = entry.getKey();
                                        Integer count = entry.getValue();
                                        double mentionRate = (double) count / finalStoreReviewCount2 * 100;
                                        writer.printf("🔸 %s: 총 %d회 언급 (%.1f%% 비율)%n", 
                                                word, count, mentionRate);
                                    });
                        }
                        writer.println();
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
                            int maxReviews = Math.min(reviews.size(), 10); // 최대 10개까지만 분석
                            writer.printf("📊 총 %d개 리뷰 중 처음 %d개 분석%n%n", reviews.size(), maxReviews);
                            
                            // Store별 키워드 통계를 위한 변수들
                            java.util.Map<String, Integer> storeKeyTermsCount = new java.util.HashMap<>();
                            java.util.Map<String, Integer> storeGeneralKeywords = new java.util.HashMap<>();
                            final AtomicInteger storeReviewCount = new AtomicInteger(0);
                            
                            for (int i = 0; i < maxReviews; i++) {
                                String content = reviews.get(i);
                                if (content != null && !content.isEmpty()) {
                                    totalReviews.incrementAndGet();
                                    storeReviewCount.incrementAndGet();
                                    
                                    // 키워드 순위 분석
                                    Map<String, Object> keywordResult = nlpService.extractKeywordRankings(content);
                                    
                                    writer.printf("◆ 리뷰 #%d%n", i + 1);
                                    writer.printf("내용: %s%n", content.length() > 100 ? content.substring(0, 100) + "..." : content);
                                    
                                    // 핵심 키워드 분석 결과
                                    @SuppressWarnings("unchecked")
                                    Map<String, Long> keyTermsCount = (Map<String, Long>) keywordResult.get("keyTermsCount");
                                    writer.println("🎯 핵심 키워드 발견:");
                                    if (keyTermsCount.isEmpty()) {
                                        writer.println("  - 발견된 핵심 키워드 없음");
                                    } else {
                                        for (Map.Entry<String, Long> entry : keyTermsCount.entrySet()) {
                                            String word = entry.getKey();
                                            Long count = entry.getValue();
                                            writer.printf("  ✓ %s: %d회 언급%n", word, count);
                                            
                                            // 전체 및 Store별 핵심 키워드 카운트 업데이트
                                            globalKeyTermsCount.put(word, globalKeyTermsCount.getOrDefault(word, 0) + count.intValue());
                                            storeKeyTermsCount.put(word, storeKeyTermsCount.getOrDefault(word, 0) + count.intValue());
                                        }
                                    }
                                    
                                    // 일반 키워드 순위 결과
                                    @SuppressWarnings("unchecked")
                                    List<Map.Entry<String, Long>> generalKeywords = (List<Map.Entry<String, Long>>) keywordResult.get("generalKeywordsRank");
                                    writer.printf("📊 명사 중심 키워드 순위 (총 %d개):%n", generalKeywords.size());
                                    if (generalKeywords.isEmpty()) {
                                        writer.println("  - 발견된 일반 키워드 없음");
                                    } else {
                                        int rank = 1;
                                        for (Map.Entry<String, Long> entry : generalKeywords) {
                                            String word = entry.getKey();
                                            Long frequency = entry.getValue();
                                            
                                            writer.printf("  %d위: %s (%d회)%n", rank++, word, frequency);
                                            
                                            // 전체 및 Store별 일반 키워드 카운트 업데이트
                                            globalGeneralKeywords.put(word, globalGeneralKeywords.getOrDefault(word, 0) + frequency.intValue());
                                            storeGeneralKeywords.put(word, storeGeneralKeywords.getOrDefault(word, 0) + frequency.intValue());
                                        }
                                    }
                                    writer.println();
                                }
                            }
                            
                            // Store별 키워드 분석 통계 출력
                            writer.println("==============================================");
                            writer.printf("## 📈 [%s] 키워드 분석 통계 ##%n", placeName);
                            writer.println("==============================================");
                            writer.printf("📊 분석된 리뷰 수: %d개%n", storeReviewCount.get());
                            writer.println();
                            
                            // 핵심 키워드 통계
                            writer.println("🎯 핵심 키워드 통계:");
                            writer.println("------------------------------------------");
                            if (storeKeyTermsCount.isEmpty()) {
                                writer.println("발견된 핵심 키워드가 없습니다.");
                            } else {
                                                            int finalStoreReviewCount = storeReviewCount.get();
                            storeKeyTermsCount.entrySet().stream()
                                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                                    .forEach(entry -> {
                                        String word = entry.getKey();
                                        Integer count = entry.getValue();
                                        double mentionRate = (double) count / finalStoreReviewCount * 100;
                                        writer.printf("🔸 %s: 총 %d회 언급 (%.1f%% 비율)%n", 
                                                word, count, mentionRate);
                                    });
                            }
                            writer.println();
                            
                            // 일반 키워드 통계 (상위 15개)
                            writer.println("📊 명사 중심 키워드 통계 (상위 15개):");
                            writer.println("------------------------------------------");
                            if (storeGeneralKeywords.isEmpty()) {
                                writer.println("발견된 일반 키워드가 없습니다.");
                            } else {
                                                            int finalStoreReviewCount2 = storeReviewCount.get();
                            storeGeneralKeywords.entrySet().stream()
                                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                                    .limit(15) // 상위 15개만 출력
                                    .forEach(entry -> {
                                        String word = entry.getKey();
                                        Integer count = entry.getValue();
                                        double mentionRate = (double) count / finalStoreReviewCount2 * 100;
                                        writer.printf("🔸 %s: 총 %d회 언급 (%.1f%% 비율)%n", 
                                                word, count, mentionRate);
                                    });
                            }
                        }
                        writer.println();
                    }
                }
                writer.println();
            }
            
            // 4. 전체 키워드 통계 및 분석 결과 출력
            writer.println("==============================================");
            writer.println("## 📊 전체 키워드 분석 통계 ##");
            writer.println("==============================================");
            writer.printf("📊 총 리뷰 수: %d개%n", totalReviews.get());
            writer.println();
            
            // 핵심 키워드 전체 통계
            writer.println("🎯 핵심 키워드 전체 통계:");
            writer.println("------------------------------------------");
            if (globalKeyTermsCount.isEmpty()) {
                writer.println("발견된 핵심 키워드가 없습니다.");
            } else {
                globalKeyTermsCount.entrySet().stream()
                        .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                        .forEach(entry -> {
                            String word = entry.getKey();
                            Integer totalCount = entry.getValue();
                            double mentionRate = (double) totalCount / totalReviews.get() * 100;
                            
                            writer.printf("🔸 %s: 총 %d회 언급 (%.1f%% 비율)%n", 
                                    word, totalCount, mentionRate);
                        });
            }
            writer.println();
            
            // 일반 키워드 전체 통계 (상위 30개)
            writer.println("📊 명사 중심 키워드 전체 통계 (상위 30개):");
            writer.println("------------------------------------------");
            if (globalGeneralKeywords.isEmpty()) {
                writer.println("발견된 일반 키워드가 없습니다.");
            } else {
                globalGeneralKeywords.entrySet().stream()
                        .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                        .limit(30) // 상위 30개만 출력
                        .forEach(entry -> {
                            String word = entry.getKey();
                            Integer totalCount = entry.getValue();
                            double mentionRate = (double) totalCount / totalReviews.get() * 100;
                            
                            writer.printf("🔸 %s: 총 %d회 언급 (%.1f%% 비율)%n", 
                                    word, totalCount, mentionRate);
                        });
            }

            writer.println();
            writer.println("==============================================");
            writer.println("✅ reviews.json 파일 키워드 분석 완료.");
            writer.println("📈 명사 위주 키워드가 최소 10개, 최대 20개로 분석되었습니다.");

        } catch (IOException e) {
            System.out.println("❌ 파일 처리 중 오류가 발생했습니다: " + e.getMessage());
            e.printStackTrace();
            return;
        }
        
        System.out.println("✅ reviews.json 키워드 분석 결과 파일이 성공적으로 저장되었습니다.");
    }
}