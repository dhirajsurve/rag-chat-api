package com.rag.chat.api.rag.chat.api.repo;
import com.pgvector.PGvector;
import com.rag.chat.api.rag.chat.api.entity.EmbeddingVectorStore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VectorStoreRepository extends JpaRepository<EmbeddingVectorStore, Long> {

    @Query(value = " SELECT *, (1 + (embedding <#> :param1)) AS distance " +
            "FROM ebids.vector_store1 WHERE (1 + (embedding <#> :param1)) < 1.0 ORDER BY distance LIMIT 4"
            , nativeQuery = true)
    List<Object[]> findTop4ByVector(@Param("param1") PGvector vector);

    @Query(value = " SELECT distinct metadata FROM ebids.vector_store1"
            , nativeQuery = true)
    List<String> getFileNames();

}
