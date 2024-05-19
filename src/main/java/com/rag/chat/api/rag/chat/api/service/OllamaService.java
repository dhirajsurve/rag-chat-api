package com.rag.chat.api.rag.chat.api.service;

import com.rag.chat.api.rag.chat.api.model.PromptRequest;
import com.rag.chat.api.rag.chat.api.model.PromptResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingClient;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OllamaService {
    @Value("${spring.ai.ollama.base-url}")
    private String ollamaApiUrl;

    private final WebClient.Builder webClientBuilder;
    private final VectorStore vectorStore;


    public OllamaService(WebClient.Builder webClientBuilder, EmbeddingClient embeddingClient) {
        this.webClientBuilder = webClientBuilder;
         this.vectorStore = new SimpleVectorStore(embeddingClient);
    }


    public Mono<String> generateResponse(String model, String query) {
        // Prepare the request payload
        WebClient webClient = webClientBuilder.baseUrl(ollamaApiUrl).build();

        List<Document> similarDocuments = vectorStore.similaritySearch(query);
        String information = similarDocuments.stream()
                .map(Document::getContent)
                .collect(Collectors.joining(System.lineSeparator()));
        var systemPromptTemplate = new SystemPromptTemplate(
                """
                           
                                                                    You are a helpful assistant.
                                                                    Use only the following information to answer the question.
                                                                    Do not use any other information. If you do not know, simply answer: Unknown.
                                        
                                                                    {information}
                        """);
        var systemMessage = systemPromptTemplate.createMessage(Map.of("information", information));
        var userPromptTemplate = new PromptTemplate("{query}");
        var userMessage = userPromptTemplate.createMessage(Map.of("query", query));
        var prompt  = new Prompt(List.of(systemMessage, userMessage));
        PromptRequest payload = new PromptRequest(model, prompt.toString(), false);

        // Make the POST request to the external API
        return webClient.post()
                .uri("api/generate")
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(PromptResponse.class)
                .map(PromptResponse::getResponse);
    }
}
