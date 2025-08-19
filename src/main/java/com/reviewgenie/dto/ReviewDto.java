package com.reviewgenie.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewDto {

    private String platform;
    private float rating;
    private String content;

    @JsonProperty("created_at") // JSON의 스네이크 케이스와 자바의 카멜 케이스를 매핑
    private LocalDateTime createdAt;

}