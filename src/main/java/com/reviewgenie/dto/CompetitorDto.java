package com.reviewgenie.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompetitorDto {
	private Long storeId;
	private String storeName;
	private Long competitorStoreId;
	private String competitorStoreName;
}


