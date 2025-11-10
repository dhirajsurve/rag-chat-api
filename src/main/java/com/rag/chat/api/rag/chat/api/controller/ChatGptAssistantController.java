package com.rag.chat.api.rag.chat.api.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rag.chat.api.rag.chat.api.model.ChatRequest;
import com.rag.chat.api.rag.chat.api.service.MultipartInputStreamFileResource;
import com.rag.chat.api.rag.chat.api.service.OpenAIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
public class ChatGptAssistantController {
    @Value("${openai.api.key}")
    private String openaiApiKey;

    @Value("${openai.api.url}")
    private String openaiApiUrl;

    @Autowired
    OpenAIService openAIService;

    @PostMapping(value = "/api/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) throws IOException {

        RestTemplate restTemplate = new RestTemplate();

        // Create multipart body
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("purpose", "assistants");
        body.add("file", new MultipartInputStreamFileResource(file.getInputStream(), file.getOriginalFilename()));

        // Create headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.setBearerAuth(openaiApiKey);

        // Build request
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        // Send to OpenAI
        ResponseEntity<String> response = restTemplate.exchange(
                openaiApiUrl+"v1/files", HttpMethod.POST, requestEntity, String.class);


        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response.getBody());
        String fileId = root.path("id").asText();

        System.out.println("Uploaded file_id: " + fileId);

        openAIService.createThreadRun("asst_K9ufIe4zr9rjxx1X9tWUp3i8",fileId,"Please read the attached PDF and answer questions based on it.");


        return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
    }

    @PostMapping("/api/prompt")
    public ResponseEntity<Map<String, String>> sendMessage(@RequestBody ChatRequest request) throws Exception {
        String response = openAIService.sendThreadMessage(request);
        System.out.println("Answer:"+response);
//        return ResponseEntity.ok(response);

        Map<String, String> responseData = new HashMap<>();
        responseData.put("response", response);
        return ResponseEntity.ok(responseData);

    }

}
