package com.reviewgenie.dto;

import lombok.*;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {
	private Long userId;
	
	@NotBlank(message = "사용자명은 필수입니다")
	@Size(min = 3, max = 50, message = "사용자명은 3자 이상 50자 이하여야 합니다")
	private String username;
	
	@NotBlank(message = "비밀번호는 필수입니다")
	@Size(min = 6, max = 100, message = "비밀번호는 6자 이상 100자 이하여야 합니다")
	private String password;
}


