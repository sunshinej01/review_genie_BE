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
        File outputFile = new File("output/keyword_analysis.txt");
        File jsonFile = new File("src/main/resources/data/reviews.json");

        // 출력 디렉터리가 없으면 생성
        outputFile.getParentFile().mkdirs();

        System.out.println("✅ 키워드 분석 테스트를 시작합니다.");
        System.out.println("➡️ 결과는 이 경로에 저장됩니다: " + outputFile.getAbsolutePath());
        
        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(outputFile)))) {
            
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> data = objectMapper.readValue(
                jsonFile, new TypeReference<Map<String, Object>>() {}
            );

            writer.println("## 리뷰 지니 키워드 분석 결과 ##");
            writer.println("==========================================");
            writer.println();

            // 전체 키워드 빈도 계산을 위한 맵
            java.util.Map<String, Integer> globalKeywordCount = new java.util.HashMap<>();
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
                        for (int i = 0; i < reviews.size(); i++) {
                            String content = reviews.get(i);
                            if (content != null && !content.isEmpty()) {
                                totalReviews.incrementAndGet();
                                
                                // 키워드 순위 분석
                                Map<String, Object> keywordResult = nlpService.extractKeywordRankings(content);
                                @SuppressWarnings("unchecked")
                                List<Map<String, Object>> rankings = (List<Map<String, Object>>) keywordResult.get("rankings");
                                
                                writer.printf("◆ 리뷰 #%d%n", i + 1);
                                writer.printf("내용: %s%n", content.length() > 100 ? content.substring(0, 100) + "..." : content);
                                writer.println("키워드 순위:");
                                
                                for (Map<String, Object> ranking : rankings) {
                                    String word = (String) ranking.get("word");
                                    Long frequency = (Long) ranking.get("frequency");
                                    Integer rank = (Integer) ranking.get("rank");
                                    
                                    writer.printf("  %d위: %s (빈도: %d)%n", rank, word, frequency);
                                    
                                    // 전체 키워드 카운트 업데이트
                                    globalKeywordCount.put(word, globalKeywordCount.getOrDefault(word, 0) + frequency.intValue());
                                }
                                writer.println();
                            }
                        }
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
                            for (int i = 0; i < reviews.size(); i++) {
                                String content = reviews.get(i);
                                if (content != null && !content.isEmpty()) {
                                    totalReviews.incrementAndGet();
                                    
                                    // 키워드 순위 분석
                                    Map<String, Object> keywordResult = nlpService.extractKeywordRankings(content);
                                    @SuppressWarnings("unchecked")
                                    List<Map<String, Object>> rankings = (List<Map<String, Object>>) keywordResult.get("rankings");
                                    
                                    writer.printf("◆ 리뷰 #%d%n", i + 1);
                                    writer.printf("내용: %s%n", content.length() > 100 ? content.substring(0, 100) + "..." : content);
                                    writer.println("키워드 순위:");
                                    
                                    for (Map<String, Object> ranking : rankings) {
                                        String word = (String) ranking.get("word");
                                        Long frequency = (Long) ranking.get("frequency");
                                        Integer rank = (Integer) ranking.get("rank");
                                        
                                        writer.printf("  %d위: %s (빈도: %d)%n", rank, word, frequency);
                                        
                                        // 전체 키워드 카운트 업데이트
                                        globalKeywordCount.put(word, globalKeywordCount.getOrDefault(word, 0) + frequency.intValue());
                                    }
                                    writer.println();
                                }
                            }
                        }
                        writer.println();
                    }
                }
                writer.println();
            }
            
            // 4. 전체 키워드 통계 및 언급 비율 출력
            writer.println("==========================================");
            writer.println("## 전체 키워드 순위 및 언급 비율 ##");
            writer.println("==========================================");
            writer.printf("📊 총 리뷰 수: %d개%n", totalReviews.get());
            writer.printf("📝 발견된 키워드 종류: %d개%n", globalKeywordCount.size());
            writer.println();
            
            // 전체 키워드를 빈도순으로 정렬
            globalKeywordCount.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .limit(20) // 상위 20개만 출력
                    .forEach(entry -> {
                        String word = entry.getKey();
                        Integer totalCount = entry.getValue();
                        double mentionRate = (double) totalCount / totalReviews.get() * 100;
                        
                        writer.printf("🔸 %s: 총 %d회 언급 (%.1f%% 비율)%n", 
                                word, totalCount, mentionRate);
                    });

            writer.println();
            writer.println("==========================================");
            writer.println("분석 완료.");

        } catch (IOException e) {
            System.out.println("❌ 파일 처리 중 오류가 발생했습니다: " + e.getMessage());
            e.printStackTrace();
            return;
        }
        
        System.out.println("✅ 키워드 분석 결과 파일이 성공적으로 저장되었습니다.");
    }
}
