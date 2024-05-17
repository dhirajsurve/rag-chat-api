package com.rag.chat.api.rag.chat.api.embedding;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.AbstractEmbeddingClient;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;

import java.util.List;

public class OllamaEmbeddingClient extends AbstractEmbeddingClient {
    @Override
    public EmbeddingResponse call(EmbeddingRequest request) {
        return null;
    }

    @Override
    public List<Double> embed(String text) {
        return super.embed(text);
    }

    @Override
    public List<Double> embed(Document document) {
        return List.of();
    }

    @Override
    public List<List<Double>> embed(List<String> texts) {
        return super.embed(texts);
    }

    @Override
    public EmbeddingResponse embedForResponse(List<String> texts) {
        return super.embedForResponse(texts);
    }
}
