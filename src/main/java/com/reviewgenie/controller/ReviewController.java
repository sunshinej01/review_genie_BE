package com.reviewgenie.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reviewgenie.dto.ReviewDto;
import com.reviewgenie.service.ReviewAnalysisService;
import com.reviewgenie.service.KoreanNLPService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

	private final ObjectMapper objectMapper = new ObjectMapper();
	private final ReviewAnalysisService reviewAnalysisService;
	private final KoreanNLPService koreanNLPService;

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
	
	/**
	 * 텍스트 감정 분석 (한국어/영어 자동 감지)
	 */
	@PostMapping("/analyze/sentiment")
	public ResponseEntity<Map<String, Object>> analyzeSentiment(@RequestBody Map<String, String> request) {
		String text = request.get("text");
		if (text == null || text.trim().isEmpty()) {
			return ResponseEntity.badRequest().body(Map.of("error", "텍스트가 필요합니다."));
		}
		
		try {
			String sentiment = reviewAnalysisService.analyzeSentiment(text);
			return ResponseEntity.ok(Map.of(
				"text", text,
				"sentiment", sentiment,
				"language", text.matches(".*[ㄱ-ㅎㅏ-ㅣ가-힣].*") ? "KOREAN" : "ENGLISH"
			));
		} catch (Exception e) {
			return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
		}
	}
	
	/**
	 * 한국어 키워드 추출
	 */
	@PostMapping("/analyze/keywords")
	public ResponseEntity<Map<String, Object>> extractKeywords(@RequestBody Map<String, String> request) {
		String text = request.get("text");
		if (text == null || text.trim().isEmpty()) {
			return ResponseEntity.badRequest().body(Map.of("error", "텍스트가 필요합니다."));
		}
		
		try {
			List<String> keywords = reviewAnalysisService.extractKoreanKeywords(text);
			return ResponseEntity.ok(Map.of(
				"text", text,
				"keywords", keywords
			));
		} catch (Exception e) {
			return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
		}
	}
	
	/**
	 * 종합 텍스트 분석 (형태소 분석, 감정 분석, 키워드 추출)
	 */
	@PostMapping("/analyze/comprehensive")
	public ResponseEntity<Map<String, Object>> analyzeComprehensive(@RequestBody Map<String, String> request) {
		String text = request.get("text");
		if (text == null || text.trim().isEmpty()) {
			return ResponseEntity.badRequest().body(Map.of("error", "텍스트가 필요합니다."));
		}
		
		try {
			Map<String, Object> analysis = reviewAnalysisService.analyzeReview(text);
			return ResponseEntity.ok(analysis);
		} catch (Exception e) {
			return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
		}
	}
	
	/**
	 * 한국어 형태소 분석
	 */
	@PostMapping("/analyze/morphemes")
	public ResponseEntity<Map<String, Object>> analyzeMorphemes(@RequestBody Map<String, String> request) {
		String text = request.get("text");
		if (text == null || text.trim().isEmpty()) {
			return ResponseEntity.badRequest().body(Map.of("error", "텍스트가 필요합니다."));
		}
		
		try {
			Map<String, Object> morphemes = koreanNLPService.analyzeMorphemes(text);
			return ResponseEntity.ok(morphemes);
		} catch (Exception e) {
			return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
		}
	}
}


