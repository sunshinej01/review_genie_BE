package com.reviewgenie.repository;

import com.reviewgenie.domain.Keyword;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface KeywordRepository extends JpaRepository<Keyword, Long> {
    
    /**
     * 키워드명으로 키워드 찾기
     */
    Optional<Keyword> findByKeywordName(String keywordName);
    
    /**
     * 키워드명 존재 여부 확인
     */
    boolean existsByKeywordName(String keywordName);
}


