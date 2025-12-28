package com.dpbank.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the DP Bank API Spring Boot application that exposes the
 * account processing endpoints required by the assessment.
 */
@SpringBootApplication
public class DpBankApiApplication {

	/**
	 * Boots the Spring context and makes the HTTP endpoints available.
	 *
	 * @param args optional JVM arguments
	 */
	public static void main(String[] args) {
		SpringApplication.run(DpBankApiApplication.class, args);
	}

}
