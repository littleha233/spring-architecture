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
    uid BIGINT,
    address VARCHAR(42) NOT NULL,
    private_key VARCHAR(66) NOT NULL,
    public_key VARCHAR(132) NOT NULL,
    create_time DATETIME(6),
    PRIMARY KEY (id),
    INDEX idx_eth_wallet_uid (uid),
    INDEX idx_eth_wallet_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
