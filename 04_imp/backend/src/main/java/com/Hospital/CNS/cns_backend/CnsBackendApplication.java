package com.Hospital.CNS.cns_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CnsBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(CnsBackendApplication.class, args);
	}

}
