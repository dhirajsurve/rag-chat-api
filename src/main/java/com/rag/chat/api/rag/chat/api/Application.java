package com.rag.chat.api.rag.chat.api;

import org.springframework.ai.autoconfigure.vectorstore.pgvector.PgVectorStoreProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.rag.chat.api.rag.chat.api.*")
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
