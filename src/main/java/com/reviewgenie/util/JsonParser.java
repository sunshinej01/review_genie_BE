package com.reviewgenie.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

@Component
public class JsonParser {

	private final ObjectMapper objectMapper = new ObjectMapper();

	public Map<String, Object> readJsonResource(String path) throws IOException {
		try (InputStream is = getClass().getResourceAsStream(path)) {
			if (is == null) {
				throw new IOException("Resource not found: " + path);
			}
			return objectMapper.readValue(is, new TypeReference<>() {});
		}
	}
}


