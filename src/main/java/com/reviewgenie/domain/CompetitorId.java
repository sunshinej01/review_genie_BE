package com.reviewgenie.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.Objects;

@Embeddable
public class CompetitorId implements java.io.Serializable {
	
	@Column(name = "store_id")
	private Long storeId;
	
	@Column(name = "competitor_store_id")
	private Long competitorStoreId;
	
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
