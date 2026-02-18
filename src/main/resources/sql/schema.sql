-- Unified DDL for all tables used in this project.
-- Execute manually when needed:
--   mysql -h localhost -P 3306 -uroot -p'YOUR_PASSWORD' springdemo < src/main/resources/sql/schema.sql

CREATE TABLE IF NOT EXISTS note (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(255),
    content VARCHAR(255),
    created_at DATETIME(6),
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS eth_wallet (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT,
    private_key VARCHAR(66) NOT NULL,
    public_key VARCHAR(132) NOT NULL,
    address VARCHAR(42) NOT NULL,
    created_at DATETIME(6),
    PRIMARY KEY (id),
    INDEX idx_eth_wallet_user_id (user_id),
    INDEX idx_eth_wallet_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
