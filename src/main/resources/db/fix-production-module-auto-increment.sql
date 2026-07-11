-- Fix: production_module.module_id AUTO_INCREMENT
-- Error 1364: Field 'module_id' doesn't have a default value
-- Error 1833: Cannot change column — drop FKs first, then re-add after MODIFY
--
-- Run each section in order in MySQL Workbench (dreams_creations_db)

USE dreams_creations_db;

-- =============================================================================
-- STEP 0 — List foreign keys pointing at production_module.module_id
-- =============================================================================
SELECT TABLE_NAME, CONSTRAINT_NAME, COLUMN_NAME
FROM information_schema.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = 'dreams_creations_db'
  AND REFERENCED_TABLE_NAME = 'production_module'
  AND REFERENCED_COLUMN_NAME = 'module_id';

-- =============================================================================
-- STEP 1 — Drop foreign keys (run every DROP that matches STEP 0)
-- Skip any line that errors "check that column/key exists"
-- =============================================================================

ALTER TABLE supervisor_module DROP FOREIGN KEY fk_sm_module;

-- If STEP 0 shows more constraints, add DROP lines here, e.g.:
-- ALTER TABLE module_assignment DROP FOREIGN KEY fk_ma_module;

-- =============================================================================
-- STEP 2 — Primary key + AUTO_INCREMENT
-- Skip ADD PRIMARY KEY if Error 1068 "Multiple primary key defined"
-- =============================================================================

-- ALTER TABLE production_module ADD PRIMARY KEY (module_id);

ALTER TABLE production_module MODIFY module_id bigint NOT NULL AUTO_INCREMENT;

SET @next_module := (SELECT COALESCE(MAX(module_id), 0) + 1 FROM production_module);
SET @sql := CONCAT('ALTER TABLE production_module AUTO_INCREMENT = ', @next_module);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- =============================================================================
-- STEP 3 — Re-create foreign keys
-- =============================================================================

ALTER TABLE supervisor_module
  ADD CONSTRAINT fk_sm_module
  FOREIGN KEY (module_id) REFERENCES production_module (module_id);

-- Re-add any other FKs you dropped in STEP 1, for example:
-- ALTER TABLE module_assignment
--   ADD CONSTRAINT fk_ma_module
--   FOREIGN KEY (module_id) REFERENCES production_module (module_id);

-- =============================================================================
-- STEP 4 — Verify
-- =============================================================================
SHOW CREATE TABLE production_module;
