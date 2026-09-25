package com.careerlink.careerlink_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class CareerlinkBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(CareerlinkBackendApplication.class, args);
	}

}
