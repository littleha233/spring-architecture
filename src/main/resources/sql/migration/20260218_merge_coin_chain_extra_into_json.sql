-- Merge coin_chain_config_extra into coin_chain_config.extra_json and drop old table.
-- Target model: store all extension fields as one JSON string in coin_chain_config.extra_json.

SET @add_extra_json = (
    SELECT IF(
        EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_schema = DATABASE()
              AND table_name = 'coin_chain_config'
              AND column_name = 'extra_json'
        ),
        'SELECT 1',
        'ALTER TABLE coin_chain_config ADD COLUMN extra_json VARCHAR(4000) NOT NULL DEFAULT ''{}'''
    )
);
PREPARE stmt FROM @add_extra_json;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @merge_extra = (
    SELECT IF(
        EXISTS (
            SELECT 1
            FROM information_schema.tables
            WHERE table_schema = DATABASE()
              AND table_name = 'coin_chain_config_extra'
        ),
        'UPDATE coin_chain_config c
         LEFT JOIN (
             SELECT
                 chain_config_id,
                 JSON_OBJECTAGG(
                     param_key,
                     IF(JSON_VALID(param_value), CAST(param_value AS JSON), param_value)
                 ) AS extra_json_obj
             FROM coin_chain_config_extra
             GROUP BY chain_config_id
         ) e ON e.chain_config_id = c.id
         SET c.extra_json = IFNULL(CAST(e.extra_json_obj AS CHAR), ''{}'')',
        'SELECT 1'
    )
);
PREPARE stmt FROM @merge_extra;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE coin_chain_config
SET extra_json = '{}'
WHERE extra_json IS NULL OR TRIM(extra_json) = '';

ALTER TABLE coin_chain_config
    MODIFY COLUMN extra_json VARCHAR(4000) NOT NULL DEFAULT '{}';

DROP TABLE IF EXISTS coin_chain_config_extra;
