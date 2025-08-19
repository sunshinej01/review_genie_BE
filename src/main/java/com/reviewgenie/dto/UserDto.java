package com.reviewgenie.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {
	private Long id;
	private String email;
	private String name;
}


