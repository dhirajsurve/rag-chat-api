package com.rag.chat.api.rag.chat.api;

import com.rag.chat.api.rag.chat.api.repo.VectorStoreRepository;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;


import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

@SpringBootApplication(scanBasePackages = {"com.rag.chat.api.rag.chat.api"})
@EntityScan(basePackages = {"com.rag.chat.api.rag.chat.api.entity"})
@EnableAsync
@EnableJpaRepositories(basePackages = {"com.rag.chat.api.rag.chat.api.repo"},basePackageClasses = VectorStoreRepository.class)
public class Application {

	public static void main(String[] args) throws IOException {

		SpringApplication.run(Application.class, args);
	}

}
