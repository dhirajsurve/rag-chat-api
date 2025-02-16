package com.rag.chat.api.rag.chat.api.controller;

import com.rag.chat.api.rag.chat.api.model.ChatRequest;
import com.rag.chat.api.rag.chat.api.processor.PdfFileReader;
import com.rag.chat.api.rag.chat.api.service.TextFileSplitService;
import com.rag.chat.api.rag.chat.api.service.TogetherAiService;
import com.rag.chat.api.rag.chat.api.service.VectorStoreService;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@RestController
public class ChatApiController {
     private final PdfFileReader pdfFileReader;
     private final VectorStoreService vectorStoreService;

    private final TogetherAiService togetherAiService;
    private final TextFileSplitService fileSplitService;
    @Autowired
    public ChatApiController(PdfFileReader pdfFileReader, VectorStoreService vectorStoreService,
                             TogetherAiService togetherAiService, TextFileSplitService fileSplitService) {
         this.pdfFileReader = pdfFileReader;
        this.vectorStoreService = vectorStoreService;
        this.togetherAiService = togetherAiService;
        this.fileSplitService = fileSplitService;
    }

    @PostMapping(value = "/api/prompt", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> generateResponse(@RequestBody ChatRequest prompt) throws InterruptedException {
        System.out.println("chatrequest:" + prompt);

        if(prompt.getPrompt()==null || Objects.equals(prompt.getPrompt(), "")) {
            System.out.println("Question cant be empty.");
         throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Question cant be empty.");
        }


       List<String> ids= vectorStoreService.listOfIds(prompt).stream()
                .map(doc ->   doc.get("id").toString()).toList();

        System.out.println("count:"+ids.size());

        StringBuilder response= new StringBuilder();
        List<Map<String, Object>> similarDocuments = new ArrayList<>();
        if (ids.size() > 30) {
            List<List<String>> chunks = chunkList(ids, 10);
            for (List<String> chunk : chunks) {
                Thread.sleep(2000);
                 similarDocuments=vectorStoreService.similaritySearchByIds(prompt,chunk);
                String information =  similarDocuments.stream()
                        .map(doc -> (String) doc.get("content"))
                        .collect(Collectors.joining(System.lineSeparator()));

                System.out.println("information:"+information);

                Thread.sleep(5000);
                var temp_response=togetherAiService.generateResponse(prompt.getPrompt(), information).block();


                if ( !temp_response.isBlank() && !temp_response.contains("NOT FOUND") && !temp_response.contains("unable to find")
                        && !temp_response.contains("no specific")
                        && !temp_response.contains("cannot provide")
                        && !temp_response.contains("cannot find")
                        && !temp_response.contains("I am sorry")
                        && !temp_response.contains("I'm sorry")
                        && !temp_response.contains("couldn't find")
                        && !temp_response.contains("no separate")
                        && !temp_response.contains("I am sorry"))
                      response.append(" \n ").append(temp_response); // Blocking call to get the response

                System.out.println("temp_response:"+temp_response);

            }
        } else {
            similarDocuments = vectorStoreService.similaritySearch(prompt);
            String information =  similarDocuments.stream()
                    .map(doc -> (String) doc.get("content"))
                    .collect(Collectors.joining(System.lineSeparator()));

              response = new StringBuilder(togetherAiService.generateResponse(prompt.getPrompt(), information).block()); // Blocking call to get the response

        }


        System.out.println("Response:" + response);

        Map<String, String> responseData = new HashMap<>();
        responseData.put("response", response.toString());

        return ResponseEntity.ok(responseData);
    }

    @PostMapping("/api/upload")
    public ResponseEntity<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file,@RequestParam("userId") Long userId) {
        String fileName = file.getResource().getFilename();
        if(isTextFile(file))
        {
            try {
                List<String> chunks = fileSplitService.splitFile(file.getInputStream(), 10000);
                fileSplitService.textEmbedding(chunks,file.getOriginalFilename(),userId);
            } catch (IOException e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,"Failed to process file.");
            }
        }
        else
            pdfFileReader.pdfEmbedding(file,userId);

        Map<String, String> response = new HashMap<>();
        response.put("fileName", fileName);
         response.put("fileType", file.getContentType());
        response.put("size", String.valueOf(file.getSize()));

        System.out.println("fileName:"+ fileName);
        System.out.println("size:"+  file.getSize());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/api/userfiles")
    public ResponseEntity<List<String>> getFileList(@RequestParam("userId") Long userId) {
        System.out.println("Getting list of filename for userId:."+userId);
     return   new ResponseEntity<>(vectorStoreService.getListofFilesName(userId),HttpStatus.OK);
    }

    @PostMapping(value = "api/upload/batchfile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<ResponseEntity<?>> uploadJsonlFile(@RequestParam("file") MultipartFile file) throws IOException {
        return togetherAiService.uploadFile(file)
                .flatMap(response -> togetherAiService.sendBatchRequest(response.getId()))
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage())));
    }

    @GetMapping("api/monitor/{batchId}")
    public Mono<ResponseEntity<?>> monitorBatch(@PathVariable String batchId) {
        return togetherAiService.monitorBatch(batchId);
    }

    private boolean isTextFile(MultipartFile file) {
        String contentType = file.getContentType();
        String fileName = file.getOriginalFilename();
        return (contentType != null && contentType.startsWith("text")) ||
                (fileName != null && (fileName.endsWith(".txt") || fileName.endsWith(".csv") || fileName.endsWith(".log")));
    }
    private List<List<String>> chunkList(List<String> list, int chunkSize) {
        List<List<String>> chunks = new ArrayList<>();
        for (int i = 0; i < list.size(); i += chunkSize) {
            chunks.add(list.subList(i, Math.min(list.size(), i + chunkSize)));
        }
        return chunks;
    }
    }
