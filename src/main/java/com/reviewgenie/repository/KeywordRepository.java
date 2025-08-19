package com.reviewgenie.repository;

import com.reviewgenie.domain.Keyword;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface KeywordRepository extends JpaRepository<Keyword, Long> {
    
    /**
     * 단어로 키워드 찾기
     */
    Optional<Keyword> findByWord(String word);
    
    /**
     * 단어 존재 여부 확인
     */
    boolean existsByWord(String word);
}


