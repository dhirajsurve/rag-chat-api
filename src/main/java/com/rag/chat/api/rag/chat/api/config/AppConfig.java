package com.rag.chat.api.rag.chat.api.config;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.context.annotation.*;

@Configuration
public class AppConfig {
    @Bean
    TokenTextSplitter tokenTextSplitter() {
        return new TokenTextSplitter();
    }
 }

