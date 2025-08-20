package com.reviewgenie.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewDto {

    private Long reviewId;
    
    @NotNull(message = "상점 ID는 필수입니다")
    private Long storeId;
    
    @NotBlank(message = "리뷰 내용은 필수입니다")
    private String content;
    
    private String sentiment;

    @JsonProperty("created_at") // JSON의 스네이크 케이스와 자바의 카멜 케이스를 매핑
    private LocalDateTime createdAt;
}