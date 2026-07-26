-- Fix: production_stage.stage_id AUTO_INCREMENT
-- Error 1364: Field 'stage_id' doesn't have a default value
--
-- If ADD PRIMARY KEY fails with 1068 "Multiple primary key defined" — skip it,
-- your table already has a primary key. Run only the MODIFY line below.

USE dreams_creations_db;

-- Skip this line if you get Error 1068:
-- ALTER TABLE production_stage ADD PRIMARY KEY (stage_id);

ALTER TABLE production_stage MODIFY stage_id bigint NOT NULL AUTO_INCREMENT;

SET @next_stage := (SELECT COALESCE(MAX(stage_id), 0) + 1 FROM production_stage);
SET @sql := CONCAT('ALTER TABLE production_stage AUTO_INCREMENT = ', @next_stage);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SHOW CREATE TABLE production_stage;
