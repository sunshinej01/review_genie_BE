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
@Table(name = "review")
public class Review {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "review_id")
	private Long reviewId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "store_id", nullable = false)
	private Store store;

	@Lob // 긴 텍스트를 위한 설정
	@Column(name = "content")
	private String content;

	@Column(name = "sentiment")
	private String sentiment; // 긍정/부정 분석 결과 저장

	@Column(name = "created_at")
	private LocalDateTime createdAt;
}


