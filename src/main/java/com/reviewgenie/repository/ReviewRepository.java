package com.reviewgenie.repository;

import com.reviewgenie.domain.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    
    /**
     * 감정별 리뷰 개수 조회
     */
    long countBySentiment(String sentiment);
    
    /**
     * 특정 매장의 리뷰 조회
     */
    @Query("SELECT r FROM Review r WHERE r.store.id = :storeId")
    List<Review> findByStoreId(@Param("storeId") Long storeId);
    
    /**
     * 감정별 리뷰 조회
     */
    List<Review> findBySentiment(String sentiment);
    
    /**
     * 감정 분석 통계 조회
     */
    @Query("SELECT r.sentiment, COUNT(r) FROM Review r GROUP BY r.sentiment")
    List<Object[]> getSentimentStatistics();
}


