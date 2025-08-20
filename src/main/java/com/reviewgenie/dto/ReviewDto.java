package com.reviewgenie.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewDto {

    private Long reviewId;
    private Long storeId;
    private String content;
    private String sentiment;

    @JsonProperty("created_at") // JSON의 스네이크 케이스와 자바의 카멜 케이스를 매핑
    private LocalDateTime createdAt;
}