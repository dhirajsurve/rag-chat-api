CREATE TABLE user_files (
                            id SERIAL PRIMARY KEY,
                            user_id VARCHAR(255) NOT NULL,
                            created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            file_name VARCHAR(255) NOT NULL,
                            file_id VARCHAR(255) NOT NULL,
                            thread_id VARCHAR(255),
                            assistant_id VARCHAR(255)
);
