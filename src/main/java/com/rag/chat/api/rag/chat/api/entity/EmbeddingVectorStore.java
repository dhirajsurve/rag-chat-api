package com.rag.chat.api.rag.chat.api.entity;

import com.rag.chat.api.rag.chat.api.utils.DoubleArrayConverter;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "vector_store")
public class EmbeddingVectorStore {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(columnDefinition = "text")
    private String content;

    @Column(columnDefinition = "text")
    private String metadata;

    @Convert(converter = DoubleArrayConverter.class)
    @Column(columnDefinition = "text")
    private double[] embedding;

    // Getters and Setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public double[] getEmbedding() {
        return embedding;
    }

    public void setEmbedding(double[] embedding) {
        this.embedding = embedding;
    }
}
