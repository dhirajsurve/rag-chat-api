package com.rag.chat.api.rag.chat.api.service;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SearchService {
    @Autowired
    private JdbcClient jdbcClient;

    // Inject the Spring AI Embedding client
    @Autowired
    private EmbeddingClient aiClient;

    public List<Document> searchPlaces(String prompt) {
        // Use the Embedding client to generate a vector for the user prompt
        List<Double> promptEmbedding = aiClient.embed(prompt);

        // Perform the vector similarity search
        JdbcClient.StatementSpec query = jdbcClient.sql(
                        "SELECT name, description, price " +
                                "FROM airbnb_listing WHERE 1 - (description_embedding <=> :user_promt::vector) > 0.7 " +
                                "ORDER BY description_embedding <=> :user_promt::vector LIMIT 3")
                .param("user_promt", promptEmbedding.toString());

        // Return the recommended places
        return query.query(Document.class).list();
    }
}
