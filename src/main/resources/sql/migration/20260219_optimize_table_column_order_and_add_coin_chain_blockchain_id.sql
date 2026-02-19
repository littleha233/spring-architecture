-- Optimize column order for config tables:
-- 1) time fields stay at the end
-- 2) blockchain_id is placed after id in blockchain_config
-- 3) coin_chain_config adds blockchain_id and groups chain-related fields

SET @add_coin_chain_blockchain_id = (
    SELECT IF(
        EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_schema = DATABASE()
              AND table_name = 'coin_chain_config'
              AND column_name = 'blockchain_id'
        ),
        'SELECT 1',
        'ALTER TABLE coin_chain_config ADD COLUMN blockchain_id INT NULL AFTER coin_id'
    )
);
PREPARE stmt FROM @add_coin_chain_blockchain_id;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE coin_chain_config c
LEFT JOIN blockchain_config b
  ON UPPER(c.chain_code) = UPPER(b.chain_code)
SET c.blockchain_id = b.blockchain_id
WHERE c.blockchain_id IS NULL;

UPDATE coin_chain_config
SET blockchain_id = id + 100000
WHERE blockchain_id IS NULL;

ALTER TABLE coin_chain_config
    MODIFY COLUMN blockchain_id INT NOT NULL;

SET @drop_old_coin_chain_uk = (
    SELECT IF(
        EXISTS (
            SELECT 1
            FROM information_schema.statistics
            WHERE table_schema = DATABASE()
              AND table_name = 'coin_chain_config'
              AND index_name = 'uk_coin_chain_config_coin_chain'
        ),
        'ALTER TABLE coin_chain_config DROP INDEX uk_coin_chain_config_coin_chain',
        'SELECT 1'
    )
);
PREPARE stmt FROM @drop_old_coin_chain_uk;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_new_coin_chain_uk = (
    SELECT IF(
        EXISTS (
            SELECT 1
            FROM information_schema.statistics
            WHERE table_schema = DATABASE()
              AND table_name = 'coin_chain_config'
              AND index_name = 'uk_coin_chain_config_coin_blockchain'
        ),
        'SELECT 1',
        'ALTER TABLE coin_chain_config ADD UNIQUE KEY uk_coin_chain_config_coin_blockchain (coin_id, blockchain_id)'
    )
);
PREPARE stmt FROM @add_new_coin_chain_uk;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_coin_chain_blockchain_idx = (
    SELECT IF(
        EXISTS (
            SELECT 1
            FROM information_schema.statistics
            WHERE table_schema = DATABASE()
              AND table_name = 'coin_chain_config'
              AND index_name = 'idx_coin_chain_config_blockchain_id'
        ),
        'SELECT 1',
        'ALTER TABLE coin_chain_config ADD INDEX idx_coin_chain_config_blockchain_id (blockchain_id)'
    )
);
PREPARE stmt FROM @add_coin_chain_blockchain_idx;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

ALTER TABLE coin
    MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT FIRST,
    MODIFY COLUMN coin_id INT NOT NULL AFTER id,
    MODIFY COLUMN symbol VARCHAR(32) NOT NULL AFTER coin_id,
    MODIFY COLUMN full_name VARCHAR(128) NOT NULL AFTER symbol,
    MODIFY COLUMN coin_precision INT NOT NULL AFTER full_name,
    MODIFY COLUMN icon_url VARCHAR(512) NULL AFTER coin_precision,
    MODIFY COLUMN enabled BIT(1) NOT NULL DEFAULT b'1' AFTER icon_url,
    MODIFY COLUMN create_time DATETIME(6) NULL AFTER enabled,
    MODIFY COLUMN update_time DATETIME(6) NULL AFTER create_time;

ALTER TABLE blockchain_config
    MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT FIRST,
    MODIFY COLUMN blockchain_id INT NOT NULL AFTER id,
    MODIFY COLUMN chain_code VARCHAR(32) NOT NULL AFTER blockchain_id,
    MODIFY COLUMN chain_name VARCHAR(128) NOT NULL AFTER chain_code,
    MODIFY COLUMN enabled BIT(1) NOT NULL DEFAULT b'1' AFTER chain_name,
    MODIFY COLUMN create_time DATETIME(6) NULL AFTER enabled,
    MODIFY COLUMN update_time DATETIME(6) NULL AFTER create_time;

ALTER TABLE coin_chain_config
    MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT FIRST,
    MODIFY COLUMN coin_id BIGINT NOT NULL AFTER id,
    MODIFY COLUMN blockchain_id INT NOT NULL AFTER coin_id,
    MODIFY COLUMN chain_code VARCHAR(32) NOT NULL AFTER blockchain_id,
    MODIFY COLUMN chain_name VARCHAR(128) NOT NULL AFTER chain_code,
    MODIFY COLUMN rpc_url VARCHAR(512) NOT NULL AFTER chain_name,
    MODIFY COLUMN collection_address VARCHAR(128) NOT NULL AFTER rpc_url,
    MODIFY COLUMN withdraw_address VARCHAR(128) NOT NULL AFTER collection_address,
    MODIFY COLUMN min_withdraw_amount DECIMAL(38,18) NOT NULL AFTER withdraw_address,
    MODIFY COLUMN withdraw_precision INT NOT NULL AFTER min_withdraw_amount,
    MODIFY COLUMN min_deposit_amount DECIMAL(38,18) NOT NULL AFTER withdraw_precision,
    MODIFY COLUMN deposit_precision INT NOT NULL AFTER min_deposit_amount,
    MODIFY COLUMN extra_json VARCHAR(4000) NOT NULL DEFAULT '{}' AFTER deposit_precision,
    MODIFY COLUMN enabled BIT(1) NOT NULL DEFAULT b'1' AFTER extra_json,
    MODIFY COLUMN create_time DATETIME(6) NULL AFTER enabled,
    MODIFY COLUMN update_time DATETIME(6) NULL AFTER create_time;
