package com.reviewgenie.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CoreKeywordSentimentDto {
    
    private Long sentimentId;
    private Long storeId;
    private String storeName;
    private Long keywordId;
    private String keywordName;
    private Integer positiveCount;
    private Integer negativeCount;
    private LocalDateTime lastUpdated;
    
    // 계산된 필드
    public Integer getTotalCount() {
        return (positiveCount != null ? positiveCount : 0) + (negativeCount != null ? negativeCount : 0);
    }
    
    public Double getPositiveRatio() {
        if (getTotalCount() == 0) return 0.0;
        return (double) positiveCount / getTotalCount();
    }
}
