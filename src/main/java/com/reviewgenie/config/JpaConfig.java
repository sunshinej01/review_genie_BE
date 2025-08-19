package com.reviewgenie.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableJpaAuditing
@EnableScheduling
public class JpaConfig {

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}
}


