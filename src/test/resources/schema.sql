-- Schema for capability_db integration tests

CREATE TABLE IF NOT EXISTS capabilities (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_name (name)
);

CREATE TABLE IF NOT EXISTS capability_technologies (
    capability_id BIGINT NOT NULL,
    technology_id BIGINT NOT NULL,
    PRIMARY KEY (capability_id, technology_id),
    FOREIGN KEY (capability_id) REFERENCES capabilities(id) ON DELETE CASCADE,
    INDEX idx_technology_id (technology_id)
);
