-- Design active/inactive + bill previous balance / grand total

USE dreams_creations_db;

SET @OLD_SQL_SAFE_UPDATES = @@SQL_SAFE_UPDATES;
SET SQL_SAFE_UPDATES = 0;

SET @col_exists := (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'design' AND COLUMN_NAME = 'status'
);
SET @sql := IF(@col_exists = 0,
    'ALTER TABLE design ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT ''active'' AFTER is_featured',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

UPDATE design SET status = 'active' WHERE status IS NULL OR status = '';

SET @col_exists := (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'bill' AND COLUMN_NAME = 'previous_balance'
);
SET @sql := IF(@col_exists = 0,
    'ALTER TABLE bill ADD COLUMN previous_balance DECIMAL(12,2) NOT NULL DEFAULT 0 AFTER final_amount',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists := (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'bill' AND COLUMN_NAME = 'grand_total'
);
SET @sql := IF(@col_exists = 0,
    'ALTER TABLE bill ADD COLUMN grand_total DECIMAL(12,2) NOT NULL DEFAULT 0 AFTER previous_balance',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

UPDATE bill SET grand_total = final_amount WHERE grand_total = 0 OR grand_total IS NULL;

SET SQL_SAFE_UPDATES = @OLD_SQL_SAFE_UPDATES;
