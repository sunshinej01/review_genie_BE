package com.reviewgenie.controller;

import com.reviewgenie.dto.CoreKeywordSentimentDto;
import com.reviewgenie.dto.TopGeneralKeywordDto;
import com.reviewgenie.service.KeywordAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/keywords")
@RequiredArgsConstructor
public class KeywordController {
    
    private final KeywordAnalysisService keywordAnalysisService;
    
    /**
     * 상점별 상위 키워드 조회
     */
    @GetMapping("/top/{storeId}")
    public ResponseEntity<List<TopGeneralKeywordDto>> getTopKeywordsByStore(@PathVariable Long storeId) {
        List<TopGeneralKeywordDto> topKeywords = keywordAnalysisService.getTopKeywordsByStore(storeId);
        return ResponseEntity.ok(topKeywords);
    }
    
    /**
     * 상점별 핵심 키워드 감정 분석 결과 조회
     */
    @GetMapping("/sentiment/{storeId}")
    public ResponseEntity<List<CoreKeywordSentimentDto>> getCoreKeywordSentimentsByStore(@PathVariable Long storeId) {
        List<CoreKeywordSentimentDto> sentiments = keywordAnalysisService.getCoreKeywordSentimentsByStore(storeId);
        return ResponseEntity.ok(sentiments);
    }
    
    /**
     * 상점별 상위 키워드 분석 및 저장
     */
    @PostMapping("/analyze/{storeId}")
    public ResponseEntity<String> analyzeAndSaveTopKeywords(
            @PathVariable Long storeId,
            @RequestBody Map<String, Integer> keywordFrequency) {
        try {
            keywordAnalysisService.analyzeAndSaveTopKeywords(storeId, keywordFrequency);
            return ResponseEntity.ok("키워드 분석이 완료되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("키워드 분석 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 핵심 키워드 감정 분석 결과 저장
     */
    @PostMapping("/sentiment")
    public ResponseEntity<String> saveCoreKeywordSentiment(
            @RequestParam Long storeId,
            @RequestParam Long keywordId,
            @RequestParam int positiveCount,
            @RequestParam int negativeCount) {
        try {
            keywordAnalysisService.saveCoreKeywordSentiment(storeId, keywordId, positiveCount, negativeCount);
            return ResponseEntity.ok("감정 분석 결과가 저장되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("감정 분석 결과 저장 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}
