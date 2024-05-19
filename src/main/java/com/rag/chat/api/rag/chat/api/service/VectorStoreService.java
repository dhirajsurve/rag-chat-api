package com.rag.chat.api.rag.chat.api.service;
import com.rag.chat.api.rag.chat.api.entity.EmbeddingVectorStore;
import com.rag.chat.api.rag.chat.api.repo.VectorStoreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VectorStoreService {
    private final VectorStoreRepository vectorStoreRepository;

    public VectorStoreService(VectorStoreRepository vectorStoreRepository) {
        this.vectorStoreRepository = vectorStoreRepository;
    }

    @Transactional
    public EmbeddingVectorStore createVectorStore(String content, String metadata, double[] embedding) {
        EmbeddingVectorStore embeddingVectorStore = new EmbeddingVectorStore();
        embeddingVectorStore.setContent(content);
        embeddingVectorStore.setMetadata(metadata);
        embeddingVectorStore.setEmbedding(embedding);

        System.out.println("Create record in vectorstore for "+ content);
        return vectorStoreRepository.save(embeddingVectorStore);
    }

    @Transactional
    public EmbeddingVectorStore updateVectorStore(Long id, String content, String metadata, double[] embedding) {
        EmbeddingVectorStore embeddingVectorStore = vectorStoreRepository.findById(id).orElseThrow(() -> new RuntimeException("VectorStore not found"));
        embeddingVectorStore.setContent(content);
        embeddingVectorStore.setMetadata(metadata);
        embeddingVectorStore.setEmbedding(embedding);
        return vectorStoreRepository.save(embeddingVectorStore);
    }
}
