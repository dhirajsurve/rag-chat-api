CREATE EXTENSION IF NOT EXISTS vector;
CREATE EXTENSION IF NOT EXISTS hstore;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS vector_store (
                                            id uuid DEFAULT uuid_generate_v4() PRIMARY KEY,
    content text,
    metadata json,
    embedding vector(1536) // 1536 is the default embedding dimension
    );

CREATE INDEX ON vector_store1 USING HNSW (embedding vector_cosine_ops);


---------
CREATE TABLE IF NOT EXISTS public.vector_store1
(
    id uuid NOT NULL DEFAULT uuid_generate_v4(),
    content text COLLATE pg_catalog."default",
    metadata text,
    embedding vector(768),
    CONSTRAINT vector_store1_pkey PRIMARY KEY (id)
    )

    TABLESPACE pg_default;