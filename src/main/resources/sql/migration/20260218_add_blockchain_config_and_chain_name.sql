-- Add blockchain_config table and add chain_name into coin_chain_config.

CREATE TABLE IF NOT EXISTS blockchain_config (
    id BIGINT NOT NULL AUTO_INCREMENT,
    chain_code VARCHAR(32) NOT NULL,
    chain_name VARCHAR(128) NOT NULL,
    enabled BIT(1) NOT NULL DEFAULT b'1',
    create_time DATETIME(6),
    update_time DATETIME(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_blockchain_config_chain_code (chain_code),
    INDEX idx_blockchain_config_enabled (enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SET @add_chain_name = (
    SELECT IF(
        EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_schema = DATABASE()
              AND table_name = 'coin_chain_config'
              AND column_name = 'chain_name'
        ),
        'SELECT 1',
        'ALTER TABLE coin_chain_config ADD COLUMN chain_name VARCHAR(128) NULL AFTER chain_code'
    )
);
PREPARE stmt FROM @add_chain_name;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE coin_chain_config
SET chain_name = chain_code
WHERE chain_name IS NULL OR TRIM(chain_name) = '';

ALTER TABLE coin_chain_config
    MODIFY COLUMN chain_name VARCHAR(128) NOT NULL;

INSERT IGNORE INTO blockchain_config (chain_code, chain_name, enabled, create_time, update_time)
VALUES
    ('ETH', 'Ethereum', b'1', NOW(6), NOW(6)),
    ('BSC', 'Binance Smart Chain', b'1', NOW(6), NOW(6)),
    ('SOL', 'Solana', b'1', NOW(6), NOW(6));

UPDATE coin_chain_config c
JOIN blockchain_config b
  ON UPPER(c.chain_code) = UPPER(b.chain_code)
SET c.chain_name = b.chain_name
WHERE c.chain_name IS NULL
   OR TRIM(c.chain_name) = ''
   OR UPPER(c.chain_name) = UPPER(c.chain_code);
