package com.rag.chat.api.rag.chat.api.controller;

import com.rag.chat.api.rag.chat.api.model.ChatRequest;
import com.rag.chat.api.rag.chat.api.processor.PdfFileReader;
import com.rag.chat.api.rag.chat.api.service.TogetherAiService;
import com.rag.chat.api.rag.chat.api.service.VectorStoreService;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
public class ChatApiController {
     private final PdfFileReader pdfFileReader;
     private final VectorStoreService vectorStoreService;

    private final TogetherAiService togetherAiService;
    @Autowired
    public ChatApiController(PdfFileReader pdfFileReader, VectorStoreService vectorStoreService,
                             TogetherAiService togetherAiService) {
         this.pdfFileReader = pdfFileReader;
        this.vectorStoreService = vectorStoreService;
        this.togetherAiService = togetherAiService;
    }

    @PostMapping(value = "/api/prompt", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> generateResponse(@RequestBody ChatRequest prompt) {
        System.out.println("chatrequest:" + prompt);

        if(prompt.getPrompt()==null || Objects.equals(prompt.getPrompt(), "")) {
            System.out.println("Question cant be empty.");
         throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Question cant be empty.");
        }

        List<Map<String, Object>> similarDocuments=vectorStoreService.similaritySearch(SearchRequest.query(prompt.getPrompt()),prompt.getFileName());

        String information =  similarDocuments.stream()
                .map(doc -> (String) doc.get("content"))
                .collect(Collectors.joining(System.lineSeparator()));

        String response = togetherAiService.generateResponse( prompt.getPrompt(),information).block(); // Blocking call to get the response

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
        System.out.println("size:"+  file.getSize());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/api/userfiles")
    public ResponseEntity<List<String>> getFileList() {
        System.out.println("Getting list of filename.");
     return   new ResponseEntity<>(vectorStoreService.getListofFilesName(),HttpStatus.OK);
    }
    }
