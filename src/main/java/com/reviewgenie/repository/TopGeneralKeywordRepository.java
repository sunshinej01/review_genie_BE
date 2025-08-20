package com.reviewgenie.repository;

import com.reviewgenie.domain.TopGeneralKeyword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TopGeneralKeywordRepository extends JpaRepository<TopGeneralKeyword, Long> {
    
    @Query("SELECT t FROM TopGeneralKeyword t WHERE t.store.storeId = :storeId ORDER BY t.frequency DESC")
    List<TopGeneralKeyword> findByStoreIdOrderByFrequencyDesc(@Param("storeId") Long storeId);
    
    @Query("SELECT t FROM TopGeneralKeyword t WHERE t.store.storeId = :storeId AND t.frequency >= :minFrequency")
    List<TopGeneralKeyword> findByStoreIdAndFrequencyGreaterThanEqual(@Param("storeId") Long storeId, @Param("minFrequency") Integer minFrequency);
    
    @Query("DELETE FROM TopGeneralKeyword t WHERE t.store.storeId = :storeId")
    void deleteByStoreId(@Param("storeId") Long storeId);
}
