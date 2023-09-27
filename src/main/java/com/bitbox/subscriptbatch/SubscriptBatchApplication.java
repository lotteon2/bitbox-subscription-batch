package com.bitbox.subscriptbatch;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableBatchProcessing
public class SubscriptBatchApplication {

	public static void main(String[] args) {
		SpringApplication.run(SubscriptBatchApplication.class, args);
	}

}