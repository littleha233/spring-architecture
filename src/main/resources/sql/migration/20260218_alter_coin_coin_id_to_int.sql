-- Convert coin.coin_id from VARCHAR to INT (allowing configuration from 0).
-- If existing non-numeric values are present, convert or clean them before executing.
ALTER TABLE coin
    MODIFY COLUMN coin_id INT NOT NULL;
