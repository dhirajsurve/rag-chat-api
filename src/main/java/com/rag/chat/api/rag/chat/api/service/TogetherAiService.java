package com.rag.chat.api.rag.chat.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rag.chat.api.rag.chat.api.model.UploadFileResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.*;

@Service
public class TogetherAiService {
    @Value("${together.ai.url}")
    private String OpenAiUrl;
    @Value("${together.ai.token}")
    private String BEARER_TOKEN;

    @Value("${together.ai.url.completion}")
    private String API_URL_COMPLETION;
    @Value("${together.ai.url.emeddings}")
    private String API_URL_EMBD;

    private static final String FILE_CONTENT_URL = "https://api.openai.com/v1/files/{fileId}/content";


    private final WebClient.Builder webClientBuilder;
    private final JdbcTemplate jdbcTemplate;

    public TogetherAiService(WebClient.Builder webClientBuilder,
                             JdbcTemplate jdbcTemplate) {
        this.webClientBuilder = webClientBuilder;
        this.jdbcTemplate = jdbcTemplate;
    }

    public Mono<String> generateResponse( String query, String information) {
        var systemPromptTemplate = new SystemPromptTemplate(
                """
                     Role: Act as a construction manager to extract the text. 
                     Task: Your task is to extract the text from the provided source data for the given question.
                      Here is the source data:
                       
                     Context:
                      {information}
                       
                       Now, please answer the following question:
                     Question:
                       {user_question}
                       
                     Instructions: Keep the formatting as it is. 
                     Constraint:  Do not use any external knowledge or data beyond what is provided in the source. If there is no answer in the source data, respond with "no answer available." Strictly follow these instructions.
                                                                                                                               
""");
        HashMap<String,Object> map= new HashMap<>();
        if(!information.isBlank())
            map.put("information", information);
        else
            return Mono.just("");

        map.put("user_question", query);
        var systemMessage = systemPromptTemplate.createMessage(map);
        var userPromptTemplate = new PromptTemplate("{query}");
        var userMessage = userPromptTemplate.createMessage(Map.of("query", query));
        var prompt = new Prompt(List.of(systemMessage, userMessage));

        WebClient webClient = WebClient.builder()
                .baseUrl(API_URL_COMPLETION)
                .defaultHeader("Authorization", "Bearer " + BEARER_TOKEN)
                .defaultHeader("accept", "application/json")
                .defaultHeader("content-type", "application/json")
                .build();

        try {
            // Escape the information string to be valid JSON
            ObjectMapper objectMapper = new ObjectMapper();

            String escapedInformation = objectMapper.writeValueAsString(systemMessage.getContent());

            System.out.println("escapedInformation:"+escapedInformation);

            String requestBody = "{\n" +
                    "  \"model\": \"gpt-4o\",\n" +
                    "  \"messages\": [\n" +
                    "    {\n" +
                    "      \"role\": \"user\",\n" +
                    "      \"content\": " + escapedInformation + "\n" +
                    "    }\n" +
//                    "    {\n" +
//                    "      \"role\": \"user\",\n" +
//                    "      \"content\": \"" + query + "\"\n" +
//                    "    }\n" +
                    "  ],\n" +
                    "  \"stream\": false ,\n" +
                    "  \"temperature\": 0\n" +
                    "}";

//            String requestBody = "{\n" +
//                    "  \"model\": \"mistralai/Mixtral-8x7B-Instruct-v0.1\",\n" +
//                    "  \"prompt\": "+escapedInformation+
//                    "  ,\n" +
//                    "  \"stream\": false\n" +
//                    "}";

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

    public Mono<UploadFileResponse> uploadFile(MultipartFile file) throws IOException {

        WebClient webClient = WebClient.builder()
                .baseUrl(OpenAiUrl+"/v1/files")
                .defaultHeader("Authorization", "Bearer " + BEARER_TOKEN)
                .build();

        MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
        bodyBuilder.part("file", new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename();
            }
        }).contentType(MediaType.APPLICATION_OCTET_STREAM);

        bodyBuilder.part("purpose", "batch"); // OpenAI requires a "purpose" field



        return webClient.post()
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .bodyValue(bodyBuilder.build())
                .retrieve()
                .bodyToMono(UploadFileResponse.class);
    }


    public Mono<ResponseEntity<?>> sendBatchRequest(String inputFileId) {
        Map<String, String> requestBody = Map.of(
                "input_file_id", inputFileId,
                "endpoint", "/v1/chat/completions",
                "completion_window", "24h"
        );

        WebClient webClient = WebClient.builder()
                .baseUrl(OpenAiUrl+"/v1/batches")
                .defaultHeader("Authorization", "Bearer " + BEARER_TOKEN)
                .defaultHeader("accept", "application/json")
                .defaultHeader("content-type", "application/json")
                .build();

        return webClient.post()
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .map(ResponseEntity::ok);
    }

    public Mono<ResponseEntity<?>> monitorBatch(String batchId) {
        WebClient webClient = WebClient.builder()
                .baseUrl(OpenAiUrl+"/v1/batches/"+ batchId)
                .defaultHeader("Authorization", "Bearer " + BEARER_TOKEN)
                .defaultHeader("accept", "application/json")
                .defaultHeader("content-type", "application/json")
                .build();


        return webClient.get()
                .retrieve()
                .bodyToMono(Map.class)
                .flatMap(response -> {
                    String outputFileId = (String) response.get("output_file_id");
                    if (outputFileId != null) {
                        return fetchFileContent(outputFileId);
                    }
                    return Mono.just(ResponseEntity.status(HttpStatus.NO_CONTENT).body("Output file not available yet"));
                });
    }

    private Mono<ResponseEntity<?>> fetchFileContent(String fileId) {
        WebClient webClient = WebClient.builder()
                .defaultHeader("Authorization", "Bearer " + BEARER_TOKEN)
                .defaultHeader("accept", "application/json")
                .defaultHeader("content-type", "application/json")
                .build();

        return webClient.get()
                .uri(FILE_CONTENT_URL, fileId)
                .retrieve()
                .bodyToMono(String.class)
                .map(ResponseEntity::ok);
    }

     public List<Double> embedd(String requestData) {
        WebClient webClient = WebClient.builder()
                .baseUrl(API_URL_EMBD)
                .defaultHeader("Authorization", "Bearer " + BEARER_TOKEN)
                .defaultHeader("accept", "application/json")
                .defaultHeader("content-type", "application/json")
                .build();

        ObjectMapper objectMapper = new ObjectMapper();

         try {
             requestData = objectMapper.writeValueAsString(requestData );
         } catch (JsonProcessingException e) {
             throw new RuntimeException(e);
         }

         requestData=requestData.replace("\\\\r\\\\n", "\r\n")
                 .replace("\\\"", "\"");

         System.out.println("requestData:"+requestData);

        String requestBody = "{\n" +
                "  \"model\": \"text-embedding-ada-002\",\n" +
                "  \"input\": \"" + requestData.replace("\n", "").replace("\"", "\\\"") + "\"\n" +
                "}";
        ;


        System.out.println("requestBody:"+requestBody);

        Mono<String> stringMono = webClient.post()
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

    public String embeddAsString(String question) {
        WebClient webClient = webClientBuilder.baseUrl(OpenAiUrl).build();
        Mono<String> stringMono = webClient.post()
                .uri("/v1/embeddings")
                .header("Authorization", "Bearer " + BEARER_TOKEN)
                .header("accept", "application/json")
                .header("content-type", "application/json")
                .bodyValue("{\"model\": \"hazyresearch/M2-BERT-2k-Retrieval-Encoder-V1\", \"input\": \"" + question + "\"}")
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
        for (Iterator var4 = embeddingDouble.iterator(); var4.hasNext(); embeddingFloat[i++] = d.floatValue()) {
            d = (Double) var4.next();
        }

        return embeddingFloat;
    }
}
