package com.rag.chat.api.rag.chat.api.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rag.chat.api.rag.chat.api.entity.UserFile;
import com.rag.chat.api.rag.chat.api.model.ChatRequest;
import com.rag.chat.api.rag.chat.api.repo.UserFileRepository;
import com.rag.chat.api.rag.chat.api.service.MultipartInputStreamFileResource;
import com.rag.chat.api.rag.chat.api.service.OpenAIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class ChatGptAssistantController {
    @Value("${openai.api.key}")
    private String openaiApiKey;

    @Value("${openai.api.url}")
    private String openaiApiUrl;

    @Autowired
    OpenAIService openAIService;

    @Autowired
    UserFileRepository userFileRepository;

    final String systemPrompt = "You are a document-reader assistant. I will upload a PDF, and you must read and index its full content.\nYour job:\n1. Extract all text exactly as it appears in the PDF.\n2. When I ask a question, return ONLY the exact text from the PDF that matches my question.\n3. Do NOT paraphrase, rewrite, summarize, or infer anything.\n4. If I ask for a bullet, section, paragraph, table, or clause, return it exactly as written in the PDF — same wording, same formatting, same spelling.\n5. If the answer does not exist in the PDF, respond with: \"Not found in the PDF.\"\n6. Never add extra commentary or interpretation. Only return the text from the document.";
    @PostMapping(value = "/api/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file,@RequestParam("userId") Long userId) throws IOException {

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

        //var treadId=openAIService.createThreadRun("asst_K9ufIe4zr9rjxx1X9tWUp3i8",fileId,systemPrompt);

        // save information linked to user <> filename <> file_id <> thread_id in DB .
        var userFiles=new UserFile();
        userFiles.setUserId(userId.toString());
        userFiles.setFileName(file.getOriginalFilename());
        userFiles.setAssistantId("asst_K9ufIe4zr9rjxx1X9tWUp3i8");
       // userFiles.setThreadId(treadId.replace("\"",""));
        userFiles.setFileId(fileId);
        userFiles.setCreatedDate(LocalDateTime.now());
        userFileRepository.save(userFiles);
        System.out.println("User_FIles record saved:"+userFiles);

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


    @GetMapping("/api/userfiles")
    public ResponseEntity<List<String>> getFileList(@RequestParam("userId") Long userId) {
        System.out.println("Getting list of filename for userId:."+userId);
        return   new ResponseEntity<>(userFileRepository.findDistinctFileNameByUserId(String.valueOf(userId)),HttpStatus.OK);
    }

}
