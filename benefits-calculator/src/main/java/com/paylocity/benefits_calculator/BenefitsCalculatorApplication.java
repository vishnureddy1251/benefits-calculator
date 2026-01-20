package com.paylocity.benefits_calculator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class BenefitsCalculatorApplication {

	public static void main(String[] args) {
		SpringApplication.run(BenefitsCalculatorApplication.class, args);
	}

}
