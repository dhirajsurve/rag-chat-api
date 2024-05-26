package com.rag.chat.api.rag.chat.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pgvector.PGvector;
import org.postgresql.util.PGobject;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingClient;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.filter.FilterExpressionConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static com.rag.chat.api.rag.chat.api.service.PgDistanceType.EUCLIDEAN_DISTANCE;

@Service
public class SearchService {
    @Autowired
    private JdbcClient jdbcClient;
    private final JdbcTemplate jdbcTemplate;

    private ObjectMapper objectMapper;
    // Inject the Spring AI Embedding client
    @Autowired
    private EmbeddingClient aiClient;
    private final EmbeddingClient embeddingClient;
    public SearchService( JdbcTemplate jdbcTemplate, EmbeddingClient embeddingClient) {
        this.jdbcTemplate = jdbcTemplate;
        this.embeddingClient = embeddingClient;
    }

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

    public List<Document> similaritySearch(SearchRequest request) {
        String jsonPathFilter = " AND metadata::jsonb @@ '" + "nativeFilterExpression" + "'::jsonpath ";


            double distance = 1.0 - request.getSimilarityThreshold();
        PGvector  queryEmbedding = new PGvector(this.toFloatArray(embeddingClient.embed(request.getQuery())));
        return this.jdbcTemplate.query( "SELECT *, embedding <-> ? AS distance FROM vector_store WHERE embedding <-> ? < ?   ORDER BY distance LIMIT ? ",
                new DocumentRowMapper(this.objectMapper), queryEmbedding, queryEmbedding, distance, request.getTopK());
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

