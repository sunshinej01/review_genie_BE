package com.reviewgenie.service;

import com.reviewgenie.domain.CoreKeywordSentiment;
import com.reviewgenie.domain.Keyword;
import com.reviewgenie.domain.Store;
import com.reviewgenie.domain.TopGeneralKeyword;
import com.reviewgenie.dto.CoreKeywordSentimentDto;
import com.reviewgenie.dto.TopGeneralKeywordDto;
import com.reviewgenie.repository.CoreKeywordSentimentRepository;
import com.reviewgenie.repository.KeywordRepository;
import com.reviewgenie.repository.StoreRepository;
import com.reviewgenie.repository.TopGeneralKeywordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class KeywordAnalysisService {
    
    private final TopGeneralKeywordRepository topGeneralKeywordRepository;
    private final CoreKeywordSentimentRepository coreKeywordSentimentRepository;
    private final KeywordRepository keywordRepository;
    private final StoreRepository storeRepository;
    
    /**
     * 상점별 상위 키워드 분석 및 저장
     */
    @Transactional
    public void analyzeAndSaveTopKeywords(Long storeId, Map<String, Integer> keywordFrequency) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new RuntimeException("Store not found"));
        
        // 기존 데이터 삭제
        topGeneralKeywordRepository.deleteByStoreId(storeId);
        
        // 새로운 데이터 저장
        List<TopGeneralKeyword> topKeywords = keywordFrequency.entrySet().stream()
                .map(entry -> TopGeneralKeyword.builder()
                        .store(store)
                        .keyword(entry.getKey())
                        .frequency(entry.getValue())
                        .lastUpdated(LocalDateTime.now())
                        .build())
                .collect(Collectors.toList());
        
        topGeneralKeywordRepository.saveAll(topKeywords);
    }
    
    /**
     * 상점별 상위 키워드 조회
     */
    public List<TopGeneralKeywordDto> getTopKeywordsByStore(Long storeId) {
        return topGeneralKeywordRepository.findByStoreIdOrderByFrequencyDesc(storeId)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * 핵심 키워드 감정 분석 결과 저장
     */
    @Transactional
    public void saveCoreKeywordSentiment(Long storeId, Long keywordId, int positiveCount, int negativeCount) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new RuntimeException("Store not found"));
        
        Keyword keyword = keywordRepository.findById(keywordId)
                .orElseThrow(() -> new RuntimeException("Keyword not found"));
        
        CoreKeywordSentiment sentiment = CoreKeywordSentiment.builder()
                .store(store)
                .keyword(keyword)
                .positiveCount(positiveCount)
                .negativeCount(negativeCount)
                .lastUpdated(LocalDateTime.now())
                .build();
        
        coreKeywordSentimentRepository.save(sentiment);
    }
    
    /**
     * 상점별 핵심 키워드 감정 분석 결과 조회
     */
    public List<CoreKeywordSentimentDto> getCoreKeywordSentimentsByStore(Long storeId) {
        return coreKeywordSentimentRepository.findByStoreId(storeId)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    private TopGeneralKeywordDto convertToDto(TopGeneralKeyword entity) {
        return TopGeneralKeywordDto.builder()
                .topKeywordId(entity.getTopKeywordId())
                .storeId(entity.getStore().getStoreId())
                .storeName(entity.getStore().getStoreName())
                .keyword(entity.getKeyword())
                .frequency(entity.getFrequency())
                .lastUpdated(entity.getLastUpdated())
                .build();
    }
    
    private CoreKeywordSentimentDto convertToDto(CoreKeywordSentiment entity) {
        return CoreKeywordSentimentDto.builder()
                .sentimentId(entity.getSentimentId())
                .storeId(entity.getStore().getStoreId())
                .storeName(entity.getStore().getStoreName())
                .keywordId(entity.getKeyword().getKeywordId())
                .keywordName(entity.getKeyword().getKeywordName())
                .positiveCount(entity.getPositiveCount())
                .negativeCount(entity.getNegativeCount())
                .lastUpdated(entity.getLastUpdated())
                .build();
    }
}
