package com.croods.errorcontrollerdemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;

@SpringBootApplication
@EnableAutoConfiguration(exclude = { ErrorMvcAutoConfiguration.class })
public class ErrorControllerDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(ErrorControllerDemoApplication.class, args);
		System.out.println("Started...");
	}

}
