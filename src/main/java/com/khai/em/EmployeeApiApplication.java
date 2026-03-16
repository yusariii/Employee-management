package com.khai.em;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableScheduling
public class EmployeeApiApplication {
	public static void main(String[] args) {
		SpringApplication.run(EmployeeApiApplication.class, args);
	}
}	
