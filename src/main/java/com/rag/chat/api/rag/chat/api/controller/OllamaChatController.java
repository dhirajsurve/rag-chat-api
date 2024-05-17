package com.rag.chat.api.rag.chat.api.controller;

import com.rag.chat.api.rag.chat.api.processor.PdfFileReader;
import com.rag.chat.api.rag.chat.api.service.EmbeddingService;
import com.rag.chat.api.rag.chat.api.vectorstore.VectorStoreService;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatClient;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.Map;

@RestController
public class OllamaChatController {
    private final OllamaChatClient chatClient;
    private final EmbeddingService embeddingService;
    private final PdfFileReader pdfFileReader;

    @Autowired
    public OllamaChatController(OllamaChatClient chatClient, EmbeddingService embeddingService,  PdfFileReader pdfFileReader) {
        this.chatClient = chatClient;
        this.embeddingService = embeddingService;
        this.pdfFileReader = pdfFileReader;
    }

    @GetMapping("/ai/generate")
    public Map generate(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
        pdfFileReader.pdfEmbedding();
        return Map.of("generation", chatClient.call(message));
    }

    @GetMapping("/ai/embedded")
    public String generateStream(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {

        embeddingService.embed(message);
        return "done" ;
    }
}
