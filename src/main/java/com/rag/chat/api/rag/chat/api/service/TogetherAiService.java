package com.rag.chat.api.rag.chat.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pgvector.PGvector;
import com.rag.chat.api.rag.chat.api.entity.EmbeddingVectorStore;
import com.rag.chat.api.rag.chat.api.model.ChatRequest;
import com.rag.chat.api.rag.chat.api.model.PromptRequest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.embedding.EmbeddingClient;
import org.springframework.ai.vectorstore.PgVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Service
public class TogetherAiService {
    @Value("${together.ai.url}")
    private String togetherAiUrl;

    private static final String API_URL = "https://api.together.xyz/v1/chat/completions";
    private static final String API_URL_EMBD = "https://api.together.xyz/v1/embeddings";
    private static final String BEARER_TOKEN = "";

    private final WebClient.Builder webClientBuilder;
    private final VectorStore vectorStore;
    private final SearchService searchService;
    private final JdbcTemplate jdbcTemplate;
    private final EmbeddingClient embeddingClient;
    public TogetherAiService(WebClient.Builder webClientBuilder, EmbeddingClient embeddingClient,
                             SearchService searchService, JdbcTemplate jdbcTemplate, EmbeddingClient embeddingClient1) {
        this.webClientBuilder = webClientBuilder;
        this.vectorStore = new PgVectorStore(jdbcTemplate,embeddingClient,4);
        this.searchService = searchService;
        this.jdbcTemplate = jdbcTemplate;
        this.embeddingClient = embeddingClient1;
    }

    public Mono<String> generateResponse(String model, String query, String information) {
        var systemPromptTemplate = new SystemPromptTemplate(
                """
                        You are a question answering bot, and you must follow these rules:
                                      
                                          * You must answer the question based ONLY on the provided SOURCEDATA.
                                          * Replies should have two parts, the first part is the answer, the second part is a bulleted list of SOURCEDATA links to the unique PASSAGE URL values of the referenced passages with the PASSAGE TITLE of the pages as the link text.
                                          * Use one of the TEMPLATES below when answering questions.
                                      
                                          TEMPLATE #1 - Used when you can provide an answer based on the SOURCEDATA:
                                          According to the information available to me, the answer is 42.
                                      
                                          Source(s):
                                            * [PASSAGE TITLE 1](PASSAGE URL 1)
                                            * [PASSAGE TITLE 2](PASSAGE URL 2)
                                          END TEMPLATE.
                                      
                                          TEMPLATE #2 - Used when you can't answer the question based on the SOURCEDATA:
                                          I'm sorry, I don't have enough information to answer that question.
                                          END TEMPLATE.
                                      
                                          ----------------
                                          SOURCEDATA:
                                      
                                      {information}
                """);

        var systemMessage = systemPromptTemplate.createMessage(Map.of("information", information));
        var userPromptTemplate = new PromptTemplate("{query}");
        var userMessage = userPromptTemplate.createMessage(Map.of("query", query));
        var prompt = new Prompt(List.of(systemMessage, userMessage));
        PromptRequest payload = new PromptRequest(model, prompt.toString(), false);

        WebClient webClient = WebClient.builder()
                .baseUrl(API_URL)
                .defaultHeader("Authorization", "Bearer " + BEARER_TOKEN)
                .defaultHeader("accept", "application/json")
                .defaultHeader("content-type", "application/json")
                .build();

        try {
            // Escape the information string to be valid JSON
            ObjectMapper objectMapper = new ObjectMapper();
            String escapedInformation = objectMapper.writeValueAsString(systemMessage.getContent());

            String requestBody = "{\n" +
                    "  \"model\": \"mistralai/Mixtral-8x7B-Instruct-v0.1\",\n" +
                    "  \"messages\": [\n" +
                    "    {\n" +
                    "      \"role\": \"system\",\n" +
                    "      \"content\": " + escapedInformation + "\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"role\": \"user\",\n" +
                    "      \"content\": \"" + query + "\"\n" +
                    "    }\n" +
                    "  ],\n" +
                    "  \"stream\": false\n" +
                    "}";

            return webClient.post()
                    .body(BodyInserters.fromValue(requestBody))
                    .retrieve()
                    .bodyToMono(String.class)
                    .map(res -> {
                        try {
                            // Parse the JSON response
                            ObjectMapper objectMapperResponse = new ObjectMapper();
                            JsonNode jsonNode = objectMapperResponse.readTree(res);

                            // Extract the content field
                            return jsonNode
                                    .path("choices")
                                    .get(0)
                                    .path("message")
                                    .path("content")
                                    .asText();
                        } catch (IOException e) {
                            e.printStackTrace();
                            return null;
                        }
                    });
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return Mono.error(new RuntimeException("Error processing JSON", e));
        }
    }


    public List<Double> embedd(String requestData)
    {
        WebClient webClient = WebClient.builder()
                .baseUrl(API_URL_EMBD)
                .defaultHeader("Authorization", "Bearer " + BEARER_TOKEN)
                .defaultHeader("accept", "application/json")
                .defaultHeader("content-type", "application/json")
                .build();

        String requestBody = "{\n" +
                "  \"model\": \"togethercomputer/m2-bert-80M-8k-retrieval\",\n" +
                "  \"input\": \"" + requestData.replace("\n", "\\n").replace("\"", "\\\"") + "\"\n" +
                "}";;




         Mono<String> stringMono= webClient.post()
                 .body(BodyInserters.fromValue(requestBody))
            .retrieve()
            .bodyToMono(String.class);

        JSONObject jsonObject = new JSONObject(stringMono.block());

        // Extract the "data" array
        JSONArray dataArray = jsonObject.getJSONArray("data");

        // Get the first element in the "data" array
        JSONObject embeddingObject = dataArray.getJSONObject(0);

        // Extract the "embedding" array
        JSONArray embeddingArray = embeddingObject.getJSONArray("embedding");

        // Get the value of the embedding
        double embeddingValue = embeddingArray.getDouble(0);

        // Print the value
        System.out.println("Embedding Value: " + embeddingValue);
        List<Double> embeddingList = new ArrayList<>(embeddingArray.length());

        // Use a more efficient approach to convert the JSON array to a List<Double>
        for (int i = 0; i < embeddingArray.length(); i++) {
            embeddingList.add(embeddingArray.getDouble(i));
        }
        return embeddingList;
    }

    public String  embeddAsString(String question)
    {
        WebClient webClient = webClientBuilder.baseUrl(togetherAiUrl).build();
        Mono<String> stringMono= webClient.post()
                .uri("/v1/embeddings")
                .header("Authorization", "Bearer " + "")
                .header("accept", "application/json")
                .header("content-type", "application/json")
                .bodyValue("{\"model\": \"togethercomputer/m2-bert-80M-8k-retrieval\", \"input\": \"" + question + "\"}")
                .retrieve()
                .bodyToMono(String.class);
        JSONObject jsonObject = new JSONObject(stringMono.block());

        // Extract the "data" array
        JSONArray dataArray = jsonObject.getJSONArray("data");

        // Get the first element in the "data" array
        JSONObject embeddingObject = dataArray.getJSONObject(0);

        // Extract the "embedding" array
        JSONArray embeddingArray = embeddingObject.getJSONArray("embedding");
        return embeddingArray.toString();
    }

    private float[] toFloatArray(List<Double> embeddingDouble) {
        float[] embeddingFloat = new float[embeddingDouble.size()];
        int i = 0;

        Double d;
        for(Iterator var4 = embeddingDouble.iterator(); var4.hasNext(); embeddingFloat[i++] = d.floatValue()) {
            d = (Double)var4.next();
        }

        return embeddingFloat;
    }
}
