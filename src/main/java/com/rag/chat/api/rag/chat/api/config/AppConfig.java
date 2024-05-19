package com.rag.chat.api.rag.chat.api.config;
import org.springframework.ai.embedding.EmbeddingClient;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.context.annotation.*;

import javax.persistence.Embeddable;

@Configuration
public class AppConfig {
    @Bean
    TokenTextSplitter tokenTextSplitter() {
        return new TokenTextSplitter();
    }

 }

