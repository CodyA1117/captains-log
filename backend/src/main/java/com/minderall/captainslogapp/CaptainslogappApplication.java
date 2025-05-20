package com.minderall.captainslogapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@SpringBootApplication
@EnableMethodSecurity // <--- Enables @PreAuthorize, @PostAuthorize, etc.
public class CaptainslogappApplication {
	public static void main(String[] args) {
		SpringApplication.run(CaptainslogappApplication.class, args);
	}
}
