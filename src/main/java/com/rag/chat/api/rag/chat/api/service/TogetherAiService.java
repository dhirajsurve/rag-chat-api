package com.rag.chat.api.rag.chat.api.service;

import com.rag.chat.api.rag.chat.api.model.PromptRequest;
import com.rag.chat.api.rag.chat.api.model.PromptResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingClient;
import org.springframework.ai.vectorstore.PgVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TogetherAiService {
    @Value("${together.ai.url}")
    private String ollamaApiUrl;

    private final WebClient.Builder webClientBuilder;
    private final VectorStore vectorStore;
    private final SearchService searchService;
    private final JdbcTemplate jdbcTemplate;
    private final EmbeddingClient embeddingClient;

    public TogetherAiService(WebClient.Builder webClientBuilder, EmbeddingClient embeddingClient, SearchService searchService, JdbcTemplate jdbcTemplate, EmbeddingClient embeddingClient1) {
        this.webClientBuilder = webClientBuilder;
         this.vectorStore = new PgVectorStore(jdbcTemplate,embeddingClient,4);
        this.searchService = searchService;
        this.jdbcTemplate = jdbcTemplate;
        this.embeddingClient = embeddingClient1;
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
                .uri("/v1/chat/completions")
                .header("Authorization","Bearer d8ae23160fb697bbdd8e5f8d6402c0ed2470bf18aa0fdf823e7e93cbb35ec548")
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(PromptResponse.class)
                .map(PromptResponse::getResponse);
    }

   public Mono<String> embedd(String question)
    {
        WebClient webClient = webClientBuilder.baseUrl(ollamaApiUrl).build();
        return webClient.post()
            .uri("/v1/embeddings")
            .header("Authorization", "Bearer " + "d8ae23160fb697bbdd8e5f8d6402c0ed2470bf18aa0fdf823e7e93cbb35ec548")
            .header("accept", "application/json")
            .header("content-type", "application/json")
            .bodyValue("{\"model\": \"togethercomputer/m2-bert-80M-8k-retrieval\", \"input\": \"" + question + "\"}")
            .retrieve()
            .bodyToMono(String.class);
    }
}
