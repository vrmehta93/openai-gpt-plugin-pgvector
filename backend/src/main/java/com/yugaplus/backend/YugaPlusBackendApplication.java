package com.yugaplus.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class YugaPlusBackendApplication {

	public static void main(String[] args) {
		System.out.println("Starting app");
		SpringApplication.run(YugaPlusBackendApplication.class, args);
	}

}
