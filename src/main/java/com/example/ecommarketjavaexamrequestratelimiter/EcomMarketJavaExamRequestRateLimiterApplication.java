package com.example.ecommarketjavaexamrequestratelimiter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

@SpringBootApplication(exclude = {UserDetailsServiceAutoConfiguration.class})
public class EcomMarketJavaExamRequestRateLimiterApplication {

	public static void main(String[] args) {
		SpringApplication.run(EcomMarketJavaExamRequestRateLimiterApplication.class, args);
	}

}
