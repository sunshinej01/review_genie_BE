package com.reviewgenie.domain;

import jakarta.persistence.*;
import lombok.*;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "competitor")
public class Competitor {

	@EmbeddedId
	private CompetitorId id;

	@ManyToOne(fetch = FetchType.LAZY)
	@MapsId("storeId")
	@JoinColumn(name = "store_id")
	private Store store;

	@ManyToOne(fetch = FetchType.LAZY)
	@MapsId("competitorStoreId")
	@JoinColumn(name = "competitor_store_id")
	private Store competitorStore;
}


