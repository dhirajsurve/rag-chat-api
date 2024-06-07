package com.rag.chat.api.rag.chat.api.service;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pgvector.PGvector;
import com.rag.chat.api.rag.chat.api.entity.EmbeddingVectorStore;
import com.rag.chat.api.rag.chat.api.repo.VectorStoreRepository;
import org.postgresql.util.PGobject;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.filter.FilterExpressionConverter;
import org.springframework.ai.vectorstore.filter.converter.PgVectorFilterExpressionConverter;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.StatementCreatorUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.IntStream;

@Service
public class VectorStoreService {
    private final VectorStoreRepository vectorStoreRepository;
    private final TogetherAiService togetherAiService;
    private final JdbcTemplate jdbcTemplate;
    private PgDistanceType distanceType;
    public final FilterExpressionConverter filterExpressionConverter;

    public VectorStoreService(VectorStoreRepository vectorStoreRepository ,TogetherAiService togetherAiService, JdbcTemplate jdbcTemplate) {
        this.vectorStoreRepository = vectorStoreRepository;
        this.togetherAiService = togetherAiService;
        this.jdbcTemplate = jdbcTemplate;
        this.filterExpressionConverter =  new PgVectorFilterExpressionConverter();
     }

     public List<String> getListofFilesName()
     {
         return vectorStoreRepository.getFileNames();
     }

    @Transactional
    public void createVectorStore(String content, String filename, List<Double>   embedding) {

        EmbeddingVectorStore embeddingVectorStore = new EmbeddingVectorStore();
        embeddingVectorStore.setContent(content);
       embeddingVectorStore.setMetadata( filename);
        embeddingVectorStore.setEmbedding(this.toFloatArray(embedding));

        System.out.println("Create record in vectorstore for "+ filename);
        vectorStoreRepository.save(embeddingVectorStore);
    }

    public void add(final List<Document> documents) {
        final int size = documents.size();
        this.jdbcTemplate.batchUpdate("INSERT INTO vector_store1 (id, content, metadata, embedding) VALUES (?, ?, ?::jsonb, ?) ON CONFLICT (id) DO UPDATE SET content = ? , metadata = ?::jsonb , embedding = ? ", new BatchPreparedStatementSetter() {
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Document document = (Document)documents.get(i);
                String content = document.getContent();
                PGvector pGvector = new PGvector(VectorStoreService.this.toFloatArray(VectorStoreService.this.togetherAiService.embedd(document.getContent())));
                StatementCreatorUtils.setParameterValue(ps, 1, Integer.MIN_VALUE, UUID.fromString(document.getId()));
                StatementCreatorUtils.setParameterValue(ps, 2, Integer.MIN_VALUE, content);
                StatementCreatorUtils.setParameterValue(ps, 3, Integer.MIN_VALUE, null);
                StatementCreatorUtils.setParameterValue(ps, 4, Integer.MIN_VALUE, pGvector);
                StatementCreatorUtils.setParameterValue(ps, 5, Integer.MIN_VALUE, content);
                StatementCreatorUtils.setParameterValue(ps, 6, Integer.MIN_VALUE, null);
                StatementCreatorUtils.setParameterValue(ps, 7, Integer.MIN_VALUE, pGvector);
            }

            public int getBatchSize() {
                return size;
            }
        });
    }

    public List<Map<String, Object>> similaritySearch(SearchRequest request,String filename) {
        String nativeFilterExpression = request.getFilterExpression() != null ? this.filterExpressionConverter.convertExpression(request.getFilterExpression()) : "";
        String jsonPathFilter = "";
        if (StringUtils.hasText(nativeFilterExpression)) {
            jsonPathFilter = " AND metadata::jsonb @@ '" + nativeFilterExpression + "'::jsonpath ";
        }

        double distance = 1.0 - request.getSimilarityThreshold();

         PGvector queryEmbedding= new PGvector(this.toFloatArray(  togetherAiService.embedd(request.getQuery())));

        return this.jdbcTemplate.queryForList( "SELECT *, embedding <#> '"+queryEmbedding+"' AS" +
                " distance FROM vector_store1 WHERE metadata='"+filename+"' and embedding <#> '"+queryEmbedding+"' < 1.0 ORDER BY distance limit 6");
    }



    public static enum PgDistanceType {
        EUCLIDEAN_DISTANCE("<->", "vector_l2_ops", "SELECT *, embedding <-> ? AS distance FROM %s WHERE embedding <-> ? < ? %s ORDER BY distance LIMIT ? "),
        NEGATIVE_INNER_PRODUCT("<#>", "vector_ip_ops", "SELECT *, (1 + (embedding <#> ?)) AS distance FROM %s WHERE (1 + (embedding <#> ?)) < ? %s ORDER BY distance LIMIT ? "),
        COSINE_DISTANCE("<=>", "vector_cosine_ops", "SELECT *, embedding <=> ? AS distance FROM %s WHERE embedding <=> ? < ? %s ORDER BY distance LIMIT ? ");

        public final String operator;
        public final String index;
        public final String similaritySearchSqlTemplate;

        private PgDistanceType(String operator, String index, String sqlTemplate) {
            this.operator = operator;
            this.index = index;
            this.similaritySearchSqlTemplate = sqlTemplate;
        }
    }
    private float[] toFloatArray(List<Double> embeddingDouble) {
        float[] embeddingFloat = new float[embeddingDouble.size()];
        int i = 0;

        Double d;
        for(Iterator var4 = embeddingDouble.iterator(); var4.hasNext(); embeddingFloat[i++] = d.floatValue()) {
            d = (Double)var4.next();
        }

        return embeddingFloat;
    }


    private static class DocumentRowMapper implements RowMapper<Document> {
        private static final String COLUMN_EMBEDDING = "embedding";
        private static final String COLUMN_METADATA = "metadata";
        private static final String COLUMN_ID = "id";
        private static final String COLUMN_CONTENT = "content";
        private static final String COLUMN_DISTANCE = "distance";
        private ObjectMapper objectMapper;

        public DocumentRowMapper(ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
        }

        public Document mapRow(ResultSet rs, int rowNum) throws SQLException {
            String id = rs.getString("id");
            String content = rs.getString("content");
            PGobject pgMetadata = (PGobject)rs.getObject("metadata", PGobject.class);
            PGobject embedding = (PGobject)rs.getObject("embedding", PGobject.class);
            Float distance = rs.getFloat("distance");
            Map<String, Object> metadata = this.toMap(pgMetadata);
            metadata.put("distance", distance);
            Document document = new Document(id, content, metadata);
            document.setEmbedding(this.toDoubleList(embedding));
            return document;
        }

        private List<Double> toDoubleList(PGobject embedding) throws SQLException {
            float[] floatArray = (new PGvector(embedding.getValue())).toArray();
            return IntStream.range(0, floatArray.length).mapToDouble((i) -> {
                return (double)floatArray[i];
            }).boxed().toList();
        }

        private Map<String, Object> toMap(PGobject pgObject) {
            String source = pgObject.getValue();

            try {
                return (Map)this.objectMapper.readValue(source, Map.class);
            } catch (JsonProcessingException var4) {
                JsonProcessingException e = var4;
                throw new RuntimeException(e);
            }
        }
    }
}
