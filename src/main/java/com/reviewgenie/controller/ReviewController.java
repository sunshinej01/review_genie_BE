package com.reviewgenie.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reviewgenie.dto.ReviewDto;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

	private final ObjectMapper objectMapper = new ObjectMapper();

	@GetMapping
	public ResponseEntity<List<ReviewDto>> getAll() throws Exception {
		ClassPathResource resource = new ClassPathResource("data/reviews.json");
		try (InputStream is = resource.getInputStream()) {
			Map<String, Object> root = objectMapper.readValue(is, new TypeReference<>() {});
			// "reviews": [ { ... } ] 배열을 ReviewDto 리스트로 매핑
			List<ReviewDto> reviews = objectMapper.convertValue(root.get("reviews"), new TypeReference<List<ReviewDto>>() {});
			return ResponseEntity.ok(reviews);
		}
	}
}


