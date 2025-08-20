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

@Embeddable
class CompetitorId implements java.io.Serializable {
	
	@Column(name = "store_id")
	private Long storeId;
	
	@Column(name = "competitor_store_id")
	private Long competitorStoreId;
	
	// 기본 생성자, equals, hashCode 메서드
	public CompetitorId() {}
	
	public CompetitorId(Long storeId, Long competitorStoreId) {
		this.storeId = storeId;
		this.competitorStoreId = competitorStoreId;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CompetitorId that = (CompetitorId) o;
		return Objects.equals(storeId, that.storeId) &&
			   Objects.equals(competitorStoreId, that.competitorStoreId);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(storeId, competitorStoreId);
	}
}


