package com.reviewgenie.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Review {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "store_id")
	private Store store;

	@Lob // 긴 텍스트를 위한 설정
	private String content;

	private String platform;
	private float rating;
	private String sentiment; // 긍정/부정 분석 결과 저장
	private LocalDateTime createdAt;
}


