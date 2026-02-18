-- Make eth_wallet columns and order consistent:
-- id, uid, address, private_key, public_key, create_time
-- This script is designed to be rerunnable.

SET @add_uid = (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM information_schema.columns
            WHERE table_schema = DATABASE()
              AND table_name = 'eth_wallet'
              AND column_name = 'uid'
        ),
        'SELECT 1',
        'ALTER TABLE eth_wallet ADD COLUMN uid BIGINT NULL'
    )
);
PREPARE stmt FROM @add_uid;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_create_time = (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM information_schema.columns
            WHERE table_schema = DATABASE()
              AND table_name = 'eth_wallet'
              AND column_name = 'create_time'
        ),
        'SELECT 1',
        'ALTER TABLE eth_wallet ADD COLUMN create_time DATETIME(6) NULL'
    )
);
PREPARE stmt FROM @add_create_time;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @copy_uid = (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM information_schema.columns
            WHERE table_schema = DATABASE()
              AND table_name = 'eth_wallet'
              AND column_name = 'user_id'
        ),
        'UPDATE eth_wallet SET uid = COALESCE(uid, user_id) WHERE uid IS NULL',
        'SELECT 1'
    )
);
PREPARE stmt FROM @copy_uid;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @copy_create_time = (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM information_schema.columns
            WHERE table_schema = DATABASE()
              AND table_name = 'eth_wallet'
              AND column_name = 'created_at'
        ),
        'UPDATE eth_wallet SET create_time = COALESCE(create_time, created_at) WHERE create_time IS NULL',
        'SELECT 1'
    )
);
PREPARE stmt FROM @copy_create_time;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @drop_user_id = (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM information_schema.columns
            WHERE table_schema = DATABASE()
              AND table_name = 'eth_wallet'
              AND column_name = 'user_id'
        ),
        'ALTER TABLE eth_wallet DROP COLUMN user_id',
        'SELECT 1'
    )
);
PREPARE stmt FROM @drop_user_id;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @drop_created_at = (
    SELECT IF(
        EXISTS (
            SELECT 1 FROM information_schema.columns
            WHERE table_schema = DATABASE()
              AND table_name = 'eth_wallet'
              AND column_name = 'created_at'
        ),
        'ALTER TABLE eth_wallet DROP COLUMN created_at',
        'SELECT 1'
    )
);
PREPARE stmt FROM @drop_created_at;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

ALTER TABLE eth_wallet
    MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT FIRST,
    MODIFY COLUMN uid BIGINT NULL AFTER id,
    MODIFY COLUMN address VARCHAR(42) NOT NULL AFTER uid,
    MODIFY COLUMN private_key VARCHAR(66) NOT NULL AFTER address,
    MODIFY COLUMN public_key VARCHAR(132) NOT NULL AFTER private_key,
    MODIFY COLUMN create_time DATETIME(6) NULL AFTER public_key;

SET @drop_idx_uid_legacy = (
    SELECT IF(
        EXISTS (
            SELECT 1
            FROM information_schema.statistics
            WHERE table_schema = DATABASE()
              AND table_name = 'eth_wallet'
              AND index_name = 'idx_eth_wallet_user_id'
        ),
        'DROP INDEX idx_eth_wallet_user_id ON eth_wallet',
        'SELECT 1'
    )
);
PREPARE stmt FROM @drop_idx_uid_legacy;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @drop_idx_time_legacy = (
    SELECT IF(
        EXISTS (
            SELECT 1
            FROM information_schema.statistics
            WHERE table_schema = DATABASE()
              AND table_name = 'eth_wallet'
              AND index_name = 'idx_eth_wallet_created_at'
        ),
        'DROP INDEX idx_eth_wallet_created_at ON eth_wallet',
        'SELECT 1'
    )
);
PREPARE stmt FROM @drop_idx_time_legacy;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @drop_idx_uid_new = (
    SELECT IF(
        EXISTS (
            SELECT 1
            FROM information_schema.statistics
            WHERE table_schema = DATABASE()
              AND table_name = 'eth_wallet'
              AND index_name = 'idx_eth_wallet_uid'
        ),
        'DROP INDEX idx_eth_wallet_uid ON eth_wallet',
        'SELECT 1'
    )
);
PREPARE stmt FROM @drop_idx_uid_new;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @drop_idx_time_new = (
    SELECT IF(
        EXISTS (
            SELECT 1
            FROM information_schema.statistics
            WHERE table_schema = DATABASE()
              AND table_name = 'eth_wallet'
              AND index_name = 'idx_eth_wallet_create_time'
        ),
        'DROP INDEX idx_eth_wallet_create_time ON eth_wallet',
        'SELECT 1'
    )
);
PREPARE stmt FROM @drop_idx_time_new;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

CREATE INDEX idx_eth_wallet_uid ON eth_wallet(uid);
CREATE INDEX idx_eth_wallet_create_time ON eth_wallet(create_time);
