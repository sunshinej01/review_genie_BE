package com.reviewgenie.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopGeneralKeywordDto {
    
    private Long topKeywordId;
    private Long storeId;
    private String storeName;
    private String keyword;
    private Integer frequency;
    private LocalDateTime lastUpdated;
}
