package com.reviewgenie.repository;

import com.reviewgenie.domain.CoreKeywordSentiment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CoreKeywordSentimentRepository extends JpaRepository<CoreKeywordSentiment, Long> {
    
    @Query("SELECT c FROM CoreKeywordSentiment c WHERE c.store.storeId = :storeId")
    List<CoreKeywordSentiment> findByStoreId(@Param("storeId") Long storeId);
    
    @Query("SELECT c FROM CoreKeywordSentiment c WHERE c.store.storeId = :storeId AND c.keyword.keywordId = :keywordId")
    Optional<CoreKeywordSentiment> findByStoreIdAndKeywordId(@Param("storeId") Long storeId, @Param("keywordId") Long keywordId);
    
    @Query("SELECT c FROM CoreKeywordSentiment c WHERE c.keyword.keywordId = :keywordId ORDER BY (c.positiveCount + c.negativeCount) DESC")
    List<CoreKeywordSentiment> findByKeywordIdOrderByTotalMentionsDesc(@Param("keywordId") Long keywordId);
}
