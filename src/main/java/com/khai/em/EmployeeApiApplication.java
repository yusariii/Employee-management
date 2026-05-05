package com.khai.em;

import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableScheduling
@EnableCaching
@EnableAsync
public class EmployeeApiApplication {
	public static void main(String[] args) {
		SpringApplication.run(EmployeeApiApplication.class, args);
	}
}	
