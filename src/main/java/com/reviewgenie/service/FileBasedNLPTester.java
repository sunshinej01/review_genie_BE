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

public class FileBasedNLPTester {

    public static void main(String[] args) {
        // 1. 서비스 인스턴스 생성 및 카운터 변수 초기화
        KoreanNLPService nlpService = new KoreanNLPService();
        int positiveCount = 0;
        int negativeCount = 0;
        
        // 2. 출력 파일 및 JSON 파일 경로 설정
        File outputFile = new File("output/sentiment_analysis_test.txt");
        File jsonFile = new File("src/main/resources/data/reviews.json");

        // 출력 디렉터리가 없으면 생성
        outputFile.getParentFile().mkdirs();

        System.out.println("✅ 감성 분석 테스트(통계 포함)를 시작합니다.");
        System.out.println("➡️ 결과는 이 경로에 저장됩니다: " + outputFile.getAbsolutePath());
        
        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(outputFile)))) {
            
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> data = objectMapper.readValue(
                jsonFile, new TypeReference<Map<String, Object>>() {}
            );

            writer.println("## 리뷰 지니 감성 분석 결과 ##");
            writer.println("----------------------------------------");

            // 3. store_list에서 모든 스토어의 리뷰를 순회하며 분석 및 카운트
            @SuppressWarnings("unchecked")
            Map<String, Object> storeList = (Map<String, Object>) data.get("store_list");
            int reviewIndex = 1;
            
            for (Map.Entry<String, Object> storeEntry : storeList.entrySet()) {
                String storeName = storeEntry.getKey();
                Object storeValue = storeEntry.getValue();
                
                if (storeValue instanceof Map) {
                    // My_store와 같은 단일 스토어 처리
                    @SuppressWarnings("unchecked")
                    Map<String, Object> store = (Map<String, Object>) storeValue;
                    @SuppressWarnings("unchecked")
                    List<String> reviews = (List<String>) store.get("reviews");
                    
                    if (reviews != null) {
                        for (String content : reviews) {
                            if (content != null && !content.isEmpty()) {
                                // 서비스의 분석 메소드 호출
                                Map<String, Object> sentimentResult = nlpService.analyzeSentiment(content);
                                String sentiment = (String) sentimentResult.get("sentiment");
                                
                                // 결과에 따라 카운트 증가
                                if ("POSITIVE".equals(sentiment)) {
                                    positiveCount++;
                                } else if ("NEGATIVE".equals(sentiment)) {
                                    negativeCount++;
                                }
                                
                                // 파일에 개별 결과 작성
                                String sentimentKorean = "POSITIVE".equals(sentiment) ? "긍정" : 
                                                        "NEGATIVE".equals(sentiment) ? "부정" : "중립";
                                writer.printf("[리뷰 #%d] 스토어: %s%n", reviewIndex++, storeName);
                                writer.printf(" - 원문: %s%n", content);
                                writer.printf(" - 결과: %s (%s)%n%n", sentimentKorean, sentiment);
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
                        
                        if (reviews != null) {
                            for (String content : reviews) {
                                if (content != null && !content.isEmpty()) {
                                    // 서비스의 분석 메소드 호출
                                    Map<String, Object> sentimentResult = nlpService.analyzeSentiment(content);
                                    String sentiment = (String) sentimentResult.get("sentiment");
                                    
                                    // 결과에 따라 카운트 증가
                                    if ("POSITIVE".equals(sentiment)) {
                                        positiveCount++;
                                    } else if ("NEGATIVE".equals(sentiment)) {
                                        negativeCount++;
                                    }
                                    
                                    // 파일에 개별 결과 작성
                                    String sentimentKorean = "POSITIVE".equals(sentiment) ? "긍정" : 
                                                            "NEGATIVE".equals(sentiment) ? "부정" : "중립";
                                    writer.printf("[리뷰 #%d] 스토어: %s (%s)%n", reviewIndex++, storeName, placeName);
                                    writer.printf(" - 원문: %s%n", content);
                                    writer.printf(" - 결과: %s (%s)%n%n", sentimentKorean, sentiment);
                                }
                            }
                        }
                    }
                }
            }
            
            // 4. 모든 리뷰 분석 후, 파일 마지막에 통계 정보 추가
            writer.println("----------------------------------------");
            writer.println("## 최종 통계 ##");
            writer.printf(" - 긍정 리뷰: %d개%n", positiveCount);
            writer.printf(" - 부정 리뷰: %d개%n", negativeCount);
            writer.printf(" - 전체 리뷰: %d개%n", (positiveCount + negativeCount));
            writer.println("----------------------------------------");
            writer.println("분석 완료.");

        } catch (IOException e) {
            System.out.println("❌ 파일 처리 중 오류가 발생했습니다: " + e.getMessage());
            e.printStackTrace();
            return;
        }
        
        System.out.println("✅ 성공적으로 통계가 포함된 결과 파일을 저장했습니다.");
    }
}