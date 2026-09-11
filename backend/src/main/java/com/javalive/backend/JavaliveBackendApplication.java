package com.javalive.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class JavaliveBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(JavaliveBackendApplication.class, args);
	}

}
