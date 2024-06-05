package com.rag.chat.api.rag.chat.api.controller;

import com.rag.chat.api.rag.chat.api.model.ChatRequest;
import com.rag.chat.api.rag.chat.api.processor.PdfFileReader;
import com.rag.chat.api.rag.chat.api.service.TogetherAiService;
import com.rag.chat.api.rag.chat.api.service.VectorStoreService;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class OllamaChatController {
     private final PdfFileReader pdfFileReader;
     private final VectorStoreService vectorStoreService;
    @Value("${spring.ai.ollama.chat.options.model}")
    private String model;


    private final TogetherAiService togetherAiService;
    @Autowired
    public OllamaChatController(PdfFileReader pdfFileReader, VectorStoreService vectorStoreService,
                                TogetherAiService togetherAiService) {
         this.pdfFileReader = pdfFileReader;
        this.vectorStoreService = vectorStoreService;
        this.togetherAiService = togetherAiService;
    }

    @PostMapping(value = "/api/prompt", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> generateResponse(@RequestBody ChatRequest prompt) {
        System.out.println("Prompt:" + prompt.getPrompt());

        List<Map<String, Object>> similarDocuments=vectorStoreService.similaritySearch1(SearchRequest.query(prompt.getPrompt()));

        String information =  similarDocuments.stream()
                .map(doc -> (String) doc.get("content"))
                .collect(Collectors.joining(System.lineSeparator()));

        String response = togetherAiService.generateResponse(model, prompt.getPrompt(),information).block(); // Blocking call to get the response

        System.out.println("Response:" + response);

        Map<String, String> responseData = new HashMap<>();
        responseData.put("response", response);

        return ResponseEntity.ok(responseData);
    }

    @PostMapping("/api/upload")
    public ResponseEntity<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file) {
        String fileName = file.getResource().getFilename();
        pdfFileReader.pdfEmbedding(file);

        Map<String, String> response = new HashMap<>();
        response.put("fileName", fileName);
         response.put("fileType", file.getContentType());
        response.put("size", String.valueOf(file.getSize()));

        System.out.println("fileName:"+ fileName);
         System.out.println("fileType:"+ file.getContentType());
        System.out.println("size:"+  file.getSize());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
