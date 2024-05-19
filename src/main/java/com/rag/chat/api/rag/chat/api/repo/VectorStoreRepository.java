package com.rag.chat.api.rag.chat.api.repo;
import com.rag.chat.api.rag.chat.api.entity.EmbeddingVectorStore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VectorStoreRepository extends JpaRepository<EmbeddingVectorStore, Long> {
}
