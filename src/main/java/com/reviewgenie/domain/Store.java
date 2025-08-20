package com.reviewgenie.domain;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "store")
public class Store {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "place_id", unique = true)
	private String placeId; // JSON의 place_id와 매핑

	@Column(name = "store_name", nullable = false)
	private String storeName;

	@Column(name = "location")
	private String location;

	@Column(name = "review_count")
	private Integer reviewCount; // JSON의 count와 매핑

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;
}


