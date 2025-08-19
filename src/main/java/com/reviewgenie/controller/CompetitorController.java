package com.reviewgenie.controller;

import com.reviewgenie.dto.CompetitorDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/competitors")
public class CompetitorController {

	@GetMapping
	public ResponseEntity<List<CompetitorDto>> list() {
		return ResponseEntity.ok(List.of());
	}
}


