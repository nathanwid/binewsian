package com.binewsian;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class BiNewsianApplication {

	public static void main(String[] args) {
		SpringApplication.run(BiNewsianApplication.class, args);
	}

}
