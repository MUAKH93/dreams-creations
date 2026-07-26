-- Fix: payment_method.payment_method_id AUTO_INCREMENT
-- Error 1364: Field 'payment_method_id' doesn't have a default value
-- Error 1075: add PRIMARY KEY first, then MODIFY AUTO_INCREMENT

USE dreams_creations_db;

-- Skip if Error 1068 "Multiple primary key defined":
ALTER TABLE payment_method ADD PRIMARY KEY (payment_method_id);

ALTER TABLE payment_method MODIFY payment_method_id bigint NOT NULL AUTO_INCREMENT;

SET @next := (SELECT COALESCE(MAX(payment_method_id), 0) + 1 FROM payment_method);
SET @sql := CONCAT('ALTER TABLE payment_method AUTO_INCREMENT = ', @next);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SHOW CREATE TABLE payment_method;
