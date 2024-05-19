package com.rag.chat.api.rag.chat.api.controller;

import com.rag.chat.api.rag.chat.api.processor.PdfFileReader;
import com.rag.chat.api.rag.chat.api.service.OllamaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
public class OllamaChatController {
     private final PdfFileReader pdfFileReader;
    @Value("${spring.ai.ollama.chat.options.model}")
    private String model;


    private final OllamaService ollamaService;
    @Autowired
    public OllamaChatController(PdfFileReader pdfFileReader,
                                OllamaService ollamaService) {
         this.pdfFileReader = pdfFileReader;
        this.ollamaService = ollamaService;
    }

    @PostMapping(value = "/api/prompt", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> generateResponse(@RequestBody String prompt) {
        System.out.println("Prompt:" + prompt);
        String response = ollamaService.generateResponse(model, prompt).block(); // Blocking call to get the response

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
