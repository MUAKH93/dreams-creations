-- Quick fix: inventory_id AUTO_INCREMENT
-- Run this in MySQL Workbench if "Record Return" fails with:
-- Field 'inventory_id' doesn't have a default value

USE dreams_creations_db;

-- Step 1: Add primary key (skip if "Multiple primary key defined")
ALTER TABLE inventory ADD PRIMARY KEY (inventory_id);

-- Step 2: Enable AUTO_INCREMENT
ALTER TABLE inventory MODIFY inventory_id bigint NOT NULL AUTO_INCREMENT;

-- Verify
SHOW CREATE TABLE inventory;
