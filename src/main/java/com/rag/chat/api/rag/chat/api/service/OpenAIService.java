package com.rag.chat.api.rag.chat.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rag.chat.api.rag.chat.api.entity.UserFile;
import com.rag.chat.api.rag.chat.api.model.ChatRequest;
import com.rag.chat.api.rag.chat.api.repo.UserFileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class OpenAIService {
    @Value("${openai.api.key}")
    private String openaiApiKey;

    @Value("${openai.api.url}")
    private String openaiThreadsUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    @Autowired
    UserFileRepository userFileRepository;

    public  String createThreadRun(String assistantId, String fileId) throws JsonProcessingException {

        String requestBody = """
                 {
                   "assistant_id": "%s",
                   "thread": {
                     "messages": [
                       {
                         "role": "user",
                         "content": "You are a document-reader assistant. I will upload a PDF, and you must read and index its full content.\\nYour job:\\n1. Extract all text exactly as it appears in the PDF.\\n2. When I ask a question, return ONLY the exact text from the PDF that matches my question.\\n3. Do NOT paraphrase, rewrite, summarize, or infer anything.\\n4. If I ask for a bullet, section, paragraph, table, or clause, return it exactly as written in the PDF — same wording, same formatting, same spelling.\\n5. If the answer does not exist in the PDF, respond with: \\"Not found in the PDF.\\"\\n6. Never add extra commentary or interpretation. Only return the text from the document.",
                         "attachments": [
                           {
                             "file_id": "%s",
                             "tools": [
                               {
                                 "type": "file_search"
                               }
                             ]
                           }
                         ]
                       }
                     ]
                   }
                 } 
                """.formatted(assistantId, fileId);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(openaiApiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("OpenAI-Beta", "assistants=v2");

        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        var response= restTemplate.exchange(openaiThreadsUrl+"v1/threads/runs", HttpMethod.POST, entity, String.class);
        // Parse JSON to extract thread id
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response.getBody());
        String threadId = root.path("thread_id").toString();

        System.out.println("Thread is created :"+threadId);

        return threadId;
    }

    public String sendThreadMessage(ChatRequest request) throws Exception {
        UserFile userFile = userFileRepository
                .findFirstByUserIdAndFileNameOrderByCreatedDateDesc(request.getUserId(), request.getFileName())
                .orElse(null);

        var threadId=createThreadRun("asst_K9ufIe4zr9rjxx1X9tWUp3i8",userFile.getFileId()).replace("\"","");

        //todo fetch the thread_id and assistant id related to file Name and send here
        String url = openaiThreadsUrl+"v1/threads/" + threadId + "/messages";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(openaiApiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("OpenAI-Beta", "assistants=v2");

        // Build body
        Map<String, String> body = Map.of(
                "role", "user",
                "content", request.getPrompt()
        );

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);
        //ask question
        Thread.sleep(2000);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);


        return runThread(threadId,userFile.getAssistantId());
    }

    public String runThread(String threadId, String assistantId) throws Exception {

         final String THREAD_RUN_URL = openaiThreadsUrl+"v1/threads/{threadId}/runs";
        String url = THREAD_RUN_URL.replace("{threadId}", threadId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + openaiApiKey);
        headers.set("OpenAI-Beta", "assistants=v2");

        String body = "{\"assistant_id\": \"" + assistantId + "\"}";

        HttpEntity<String> entity = new HttpEntity<>(body, headers);

        Thread.sleep(5000);

            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
        System.out.println("Thread is running .");

        Thread.sleep(7000);
        return getLastMessage(threadId);
    }

    public String getLastMessage(@RequestParam String threadId) throws Exception {
        final String THREAD_MESSAGES_URL = openaiThreadsUrl+"v1/threads/{threadId}/messages";
        String url = THREAD_MESSAGES_URL.replace("{threadId}", threadId);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + openaiApiKey);
        headers.set("OpenAI-Beta", "assistants=v2");

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response.getBody());
        JsonNode messages = root.path("data");

        if (messages.isArray() && !messages.isEmpty()) {
            JsonNode lastMessage = messages.get(0);
            JsonNode contentArray = lastMessage.path("content");

            if (contentArray.isArray() && !contentArray.isEmpty()) {
                JsonNode textNode = contentArray.get(0).path("text").path("value");
                return textNode.asText();  // return just the string value
            }

        }

        return "No messages found";
    }
}
