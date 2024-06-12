package com.rag.chat.api.rag.chat.api;

import com.rag.chat.api.rag.chat.api.repo.VectorStoreRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(scanBasePackages = {"com.rag.chat.api.rag.chat.api"})
@EntityScan(basePackages = {"com.rag.chat.api.rag.chat.api.entity"})
@EnableAsync
@EnableJpaRepositories(basePackages = {"com.rag.chat.api.repo"},basePackageClasses = VectorStoreRepository.class)
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
