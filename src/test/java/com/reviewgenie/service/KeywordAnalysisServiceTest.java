package com.reviewgenie.service;

import com.reviewgenie.domain.Keyword;
import com.reviewgenie.domain.Store;
import com.reviewgenie.domain.User;
import com.reviewgenie.dto.CoreKeywordSentimentDto;
import com.reviewgenie.dto.TopGeneralKeywordDto;
import com.reviewgenie.repository.CoreKeywordSentimentRepository;
import com.reviewgenie.repository.KeywordRepository;
import com.reviewgenie.repository.StoreRepository;
import com.reviewgenie.repository.TopGeneralKeywordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KeywordAnalysisServiceTest {

    @Mock
    private TopGeneralKeywordRepository topGeneralKeywordRepository;
    
    @Mock
    private CoreKeywordSentimentRepository coreKeywordSentimentRepository;
    
    @Mock
    private KeywordRepository keywordRepository;
    
    @Mock
    private StoreRepository storeRepository;
    
    @InjectMocks
    private KeywordAnalysisService keywordAnalysisService;
    
    private User testUser;
    private Store testStore;
    private Keyword testKeyword;
    
    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .userId(1L)
                .username("testuser")
                .password("password")
                .build();
        
        testStore = Store.builder()
                .id(1L)
                .storeName("Test Store")
                .location("Test Location")
                .user(testUser)
                .build();
        
        testKeyword = Keyword.builder()
                .keywordId(1L)
                .keywordName("테스트")
                .build();
    }
    
    @Test
    void analyzeAndSaveTopKeywords_Success() {
        // Given
        Map<String, Integer> keywordFrequency = new HashMap<>();
        keywordFrequency.put("테스트", 10);
        keywordFrequency.put("맛집", 5);
        
        when(storeRepository.findById(1L)).thenReturn(Optional.of(testStore));
        when(topGeneralKeywordRepository.saveAll(any())).thenReturn(Arrays.asList());
        
        // When
        assertDoesNotThrow(() -> 
            keywordAnalysisService.analyzeAndSaveTopKeywords(1L, keywordFrequency)
        );
        
        // Then
        verify(topGeneralKeywordRepository).deleteByStoreId(1L);
        verify(topGeneralKeywordRepository).saveAll(any());
    }
    
    @Test
    void getTopKeywordsByStore_Success() {
        // Given
        when(topGeneralKeywordRepository.findByStoreIdOrderByFrequencyDesc(1L))
                .thenReturn(Arrays.asList());
        
        // When
        List<TopGeneralKeywordDto> result = keywordAnalysisService.getTopKeywordsByStore(1L);
        
        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
    
    @Test
    void saveCoreKeywordSentiment_Success() {
        // Given
        when(storeRepository.findById(1L)).thenReturn(Optional.of(testStore));
        when(keywordRepository.findById(1L)).thenReturn(Optional.of(testKeyword));
        when(coreKeywordSentimentRepository.save(any())).thenReturn(null);
        
        // When
        assertDoesNotThrow(() -> 
            keywordAnalysisService.saveCoreKeywordSentiment(1L, 1L, 5, 2)
        );
        
        // Then
        verify(coreKeywordSentimentRepository).save(any());
    }
    
    @Test
    void getCoreKeywordSentimentsByStore_Success() {
        // Given
        when(coreKeywordSentimentRepository.findByStoreId(1L))
                .thenReturn(Arrays.asList());
        
        // When
        List<CoreKeywordSentimentDto> result = keywordAnalysisService.getCoreKeywordSentimentsByStore(1L);
        
        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
