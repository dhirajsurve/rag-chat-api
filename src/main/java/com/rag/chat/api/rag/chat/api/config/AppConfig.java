package com.rag.chat.api.rag.chat.api.config;

import com.rag.chat.api.rag.chat.api.embedding.OllamaEmbeddingClient;
import com.rag.chat.api.rag.chat.api.service.EmbeddingService;
import com.rag.chat.api.rag.chat.api.vectorstore.VectorStoreService;
import org.springframework.ai.autoconfigure.vectorstore.pgvector.PgVectorStoreProperties;
import org.springframework.ai.embedding.EmbeddingClient;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
@ComponentScan(basePackages = "com.rag.chat.api.rag.chat.api.*")

public class AppConfig {
    @Bean
    TokenTextSplitter tokenTextSplitter() {
        return new TokenTextSplitter();
    }

    @Bean
    @Primary
    public VectorStoreService vectorStore(JdbcTemplate jdbcTemplate, EmbeddingClient embeddingClient, PgVectorStoreProperties properties, EmbeddingService embeddingService) {
        return new VectorStoreService(jdbcTemplate, embeddingClient, properties.getDimensions(),embeddingService);
     }

     @Bean
    public  PgVectorStoreProperties pgVectorStoreProperties()
     {
         return  new PgVectorStoreProperties();
     }
     @Bean
    public EmbeddingClient embeddingClient()
     {
         return new OllamaEmbeddingClient();
     }
}

