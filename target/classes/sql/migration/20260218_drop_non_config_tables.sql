-- Remove non-configuration legacy tables to converge this project into config-center scope.

DROP TABLE IF EXISTS eth_withdrawal;
DROP TABLE IF EXISTS eth_wallet;
DROP TABLE IF EXISTS note;
