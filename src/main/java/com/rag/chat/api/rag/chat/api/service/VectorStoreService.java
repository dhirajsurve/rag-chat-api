package com.rag.chat.api.rag.chat.api.service;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pgvector.PGvector;
import com.rag.chat.api.rag.chat.api.entity.EmbeddingVectorStore;
import com.rag.chat.api.rag.chat.api.repo.VectorStoreRepository;
import org.postgresql.util.PGobject;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.PgVectorStore;
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
    private ObjectMapper objectMapper;
    private PgDistanceType distanceType;
    public final FilterExpressionConverter filterExpressionConverter;

    public VectorStoreService(VectorStoreRepository vectorStoreRepository ,TogetherAiService togetherAiService, JdbcTemplate jdbcTemplate) {
        this.vectorStoreRepository = vectorStoreRepository;
        this.togetherAiService = togetherAiService;
        this.jdbcTemplate = jdbcTemplate;
        this.filterExpressionConverter =  new PgVectorFilterExpressionConverter();
     }

    @Transactional
    public EmbeddingVectorStore createVectorStore(String content, String metadata, List<Double>   embedding) {
          EmbeddingVectorStore embeddingVectorStore = new EmbeddingVectorStore();
        embeddingVectorStore.setContent(content);
        embeddingVectorStore.setMetadata(metadata);
        embeddingVectorStore.setEmbedding(this.toFloatArray(embedding));

        System.out.println("Create record in vectorstore for "+ content);
        return vectorStoreRepository.save(embeddingVectorStore);
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

//    public List<Object[]> similaritySearch(SearchRequest request) {
////        String nativeFilterExpression = request.getFilterExpression() != null ? this.filterExpressionConverter.convertExpression(request.getFilterExpression()) : "";
////        String jsonPathFilter = "";
////        if (StringUtils.hasText(nativeFilterExpression)) {
////            jsonPathFilter = " AND metadata::jsonb @@ '" + nativeFilterExpression + "'::jsonpath ";
////        }
//
//        double distance = 1.0 - request.getSimilarityThreshold();
//        //List<Double> queryEmbedding = this.togetherAiService.embedd(request.getQuery());
//        PGvector queryEmbedding= new PGvector(this.toFloatArray(togetherAiService.embedd("What is the project location")));
//
//                //this.toFloatArray( this.togetherAiService.embedd(request.getQuery())));
//        similaritySearch1(request);
//        return vectorStoreRepository.findTop4ByVector(queryEmbedding);
//        // return this.jdbcTemplate.query(String.format("SELECT *, embedding <-> ? AS distance FROM %s WHERE embedding <-> ? < ? %s ORDER BY distance LIMIT ?", "vector_store", jsonPathFilter), new PgVectorStore.DocumentRowMapper(this.objectMapper), new Object[]{queryEmbedding, queryEmbedding, distance, request.getTopK()});
//    }

    public List<Map<String, Object>> similaritySearch1(SearchRequest request) {
        String nativeFilterExpression = request.getFilterExpression() != null ? this.filterExpressionConverter.convertExpression(request.getFilterExpression()) : "";
        String jsonPathFilter = "";
        if (StringUtils.hasText(nativeFilterExpression)) {
            jsonPathFilter = " AND metadata::jsonb @@ '" + nativeFilterExpression + "'::jsonpath ";
        }

        double distance = 1.0 - request.getSimilarityThreshold();

         PGvector queryEmbedding= new PGvector(this.toFloatArray(  togetherAiService.embedd(request.getQuery())));

        return this.jdbcTemplate.queryForList( "SELECT *, embedding <#> '"+queryEmbedding+"' AS" +
                " distance FROM vector_store1 WHERE embedding <#> '"+queryEmbedding+"' < 1.0 ");
    }


    List<Double>  fixedEmbed()
    {

        String str = """
                [2.1197808,-0.97626925,-3.7140124,-7.4128084,-3.1070669,-7.959603,0.27341998,3.2520974,3.8831627,-1.1030713,3.349024,-1.6798999,-4.9807415,3.8598104,-5.626135,3.578078,2.0498517,5.1774216,0.36063492,-2.7698734,8.438203,-1.5181388,1.5816828,0.7738347,-0.07726779,-4.0758343,-5.6878095,-10.312701,1.4409933,-6.5730486,-2.437627,3.5749598,2.581784,-0.5541566,2.0727038,3.113053,-3.0852003,0.026207635,1.3032814,8.072741,-3.92954,7.1215215,0.24011803,-0.24997568,3.7483215,4.4455223,0.40463275,-2.9856315,-3.0095809,1.5877389,6.5909424,1.4725864,-4.5226483,42.29694,-1.0947646,5.9777875,1.1577278,-1.9497441,4.1770706,1.7080456,7.1335816,2.8524964,7.8160067,4.06284,5.947165,4.144667,-0.97672945,-5.6488256,3.2926333,-7.9055567,-3.5805554,2.955669,4.070928,-8.360226,-4.696828,-1.6699002,0.17105861,1.5873301,3.2642992,0.342454,-0.9381322,-2.323121,3.2175896,0.7651939,-3.030981,-9.46739,-0.8096981,2.8967292,0.7875762,0.5836525,4.2003274,-5.351759,-1.6120366,-3.1751828,-2.4666393,-2.0450442,2.6145167,-3.451575,4.794215,0.38211188,4.4476695,-3.9194832,3.9387023,-1.0245603,2.7388885,-4.677477,-5.7096705,-3.2143073,4.746694,1.7206331,-0.51158094,8.127268,1.1831797,-1.2399873,-2.5497022,-0.66732216,-1.2846631,6.153362,0.9354771,0.94886667,6.9918504,5.1292777,-2.3387363,-4.459821,-2.737387,-1.1595724,4.5888925,-5.5031652,-2.2737172,-4.82404,2.9290776,-5.5569873,0.14200868,-6.1096845,0.114124835,-3.3887267,3.2273653,2.0907562,3.0883534,-3.5469694,0.4640068,-0.09436194,-1.3680629,-2.5272489,6.4787645,-2.374119,2.0992594,-5.1997175,-2.2917607,-0.6694424,0.61117494,-3.756483,12.006359,-1.4602047,-5.2008,-13.530485,-9.658364,6.985146,3.2869163,-2.8751614,-5.2249875,-7.456347,4.658812,-0.2530326,1.4977807,-8.699255,4.588864,-4.715,3.0515234,-3.249643,-3.9623344,3.135197,-2.9839,-4.3698173,2.7328691,-1.6365595,-3.3572898,1.1032523,-3.4340565,6.2925854,-0.07370758,-1.1523756,6.2914014,1.8537751,7.278187,1.6195638,3.6731546,-0.39758334,2.4449375,0.018126287,-4.5965495,2.639547,-3.2512963,-0.3091383,-0.23759888,7.4994826,0.8804123,-0.28485626,5.9866605,5.52629,5.2681937,-1.2331648,-2.4286346,-0.08995694,-4.8867164,4.07605,-4.931806,8.325608,-7.9398475,4.917824,-3.110627,1.9366313,1.269881,-1.3606032,-2.5537992,3.4470263,-4.6819034,0.09721368,26.390432,2.1299624,4.598623,-3.5259712,2.2839673,11.785156,0.8436993,-0.67408454,-5.3555393,1.27031,0.6460333,-7.288792,-2.8087125,0.061392505,-2.2946389,1.685078,-5.989014,4.4654217,1.0484879,-3.7912061,-0.5202413,-0.81634706,5.0197635,1.799833,1.8993226,-1.4477177,-4.533531,5.0441513,-0.24477795,3.794024,-3.0236106,-2.607883,2.9065077,5.5507774,-4.734687,0.08691497,5.409993,-0.06902175,2.9461167,-0.03630698,3.573426,5.6763945,-0.45806727,0.68916696,-2.591186,3.9494371,-4.0495334,-2.342248,-1.7822046,-5.072755,-4.162776,-5.63737,6.0836043,-2.4600608,-1.9272943,2.9392908,-2.94821,-7.093515,0.90287983,-1.8927417,-0.9523235,0.31643578,7.2078905,-1.467437,4.20494,-4.084319,-3.920107,-8.572354,-5.851995,-1.9274027,-6.603401,0.46315145,5.3027506,-8.265763,-4.3774285,-2.1993232,0.49287596,-8.245264,-3.5299103,-0.6541744,0.8795161,-0.9544125,-2.9067235,-1.5694736,0.43409526,11.028566,2.2948253,4.9659457,4.412864,1.2953691,5.0097275,0.33459574,-1.3404735,3.6640406,0.060272254,5.414806,2.9241028,-0.7257433,-2.5560884,3.2371383,5.483877,-6.3178153,-0.24626376,-4.264723,-5.1464167,1.3651954,-0.5473703,-0.9395825,1.2310976,2.757888,0.8927947,-7.9148655,2.2862053,-2.908616,-0.803738,-9.509809,-0.4677816,0.36422497,-0.09076709,-9.3259325,2.5778399,0.67568624,2.5252397,-1.3787535,-4.1903286,2.0614197,2.7523732,8.08969,-4.9504843,10.478082,-5.463438,2.9284108,0.7630125,-0.43192428,-7.2560196,1.8698505,2.3147976,3.0415957,11.112221,1.5752864,-4.265348,-10.146301,-5.26324,1.3537772,3.8090436,-4.771251,-2.0024009,-7.647819,1.1669158,-0.48475125,-5.243011,8.40208,4.283802]
                """;

        // Step 2: Remove the square brackets and split the string by commas
        String[] parts = str.replaceAll("\\[|\\]", "").split(",");

        // Step 3: Convert the split strings to Double and add to the list
        List<Double> doubleList = new ArrayList<>();
        for (String part : parts) {
            doubleList.add(Double.parseDouble(part));
        }

        return doubleList;
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
