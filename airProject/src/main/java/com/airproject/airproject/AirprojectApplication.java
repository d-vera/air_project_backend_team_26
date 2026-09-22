package com.airproject.airproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AirprojectApplication {

	public static void main(String[] args) {
		SpringApplication.run(AirprojectApplication.class, args);
	}

}
