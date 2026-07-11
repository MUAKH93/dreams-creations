-- Fix: design_required_stage.design_stage_id AUTO_INCREMENT
-- Error 1364: Field 'design_stage_id' doesn't have a default value
-- Error 1075: AUTO_INCREMENT column must be defined as a key (add PRIMARY KEY first)
--
-- If ADD PRIMARY KEY fails with 1068 — PK already exists; run only MODIFY.
-- If MODIFY fails with 1833 — run STEP 0 and drop blocking FKs first.

USE dreams_creations_db;

-- STEP 0 — FKs referencing design_stage_id (usually none)
SELECT TABLE_NAME, CONSTRAINT_NAME, COLUMN_NAME
FROM information_schema.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = 'dreams_creations_db'
  AND REFERENCED_TABLE_NAME = 'design_required_stage'
  AND REFERENCED_COLUMN_NAME = 'design_stage_id';

-- STEP 1 — Primary key (required before AUTO_INCREMENT)
-- Skip this line only if you get Error 1068 "Multiple primary key defined"
ALTER TABLE design_required_stage ADD PRIMARY KEY (design_stage_id);

-- STEP 2 — AUTO_INCREMENT
ALTER TABLE design_required_stage MODIFY design_stage_id bigint NOT NULL AUTO_INCREMENT;

SET @next := (SELECT COALESCE(MAX(design_stage_id), 0) + 1 FROM design_required_stage);
SET @sql := CONCAT('ALTER TABLE design_required_stage AUTO_INCREMENT = ', @next);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SHOW CREATE TABLE design_required_stage;
