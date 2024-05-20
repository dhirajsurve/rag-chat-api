package com.rag.chat.api.rag.chat.api.service;

public enum PgDistanceType {
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
