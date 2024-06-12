package com.rag.chat.api.rag.chat.api.entity;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "ebids.vector_store1")
public class EmbeddingVectorStore {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(columnDefinition = "text")
    private String content;

    @Column(name = "metadata")
    private String metadata;

    @Column(name = "embedding", columnDefinition = "vector(768)")
    private float[] embedding;

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

    public float[]   getEmbedding() {
        return embedding;
    }

    public void setEmbedding(float[]  embedding) {
        this.embedding = embedding;
    }
}
