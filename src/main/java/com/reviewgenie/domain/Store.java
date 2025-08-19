package com.reviewgenie.domain;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Store {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String name;

	private String address;
	
	@Column(name = "store_id")
	private String storeId; // JSON의 place_id
	
	@Column(name = "store_type")
	private String storeType; // MY_STORE, COMPETITOR
}


