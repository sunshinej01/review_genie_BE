package com.reviewgenie.repository;

import com.reviewgenie.domain.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface StoreRepository extends JpaRepository<Store, Long> {
    
    /**
     * placeId로 Store 조회
     */
    @Query("SELECT s FROM Store s WHERE s.placeId = :placeId")
    Optional<Store> findByPlaceId(@Param("placeId") String placeId);
    
    /**
     * storeName으로 Store 조회
     */
    Optional<Store> findByStoreName(String storeName);
}


