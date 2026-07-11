-- Fix: design_image.design_image_id AUTO_INCREMENT
-- Error 1364: Field 'design_image_id' doesn't have a default value
-- Error 1075: add PRIMARY KEY first, then MODIFY AUTO_INCREMENT
-- Skip ADD PRIMARY KEY if Error 1068 "Multiple primary key defined"

USE dreams_creations_db;

ALTER TABLE design_image ADD PRIMARY KEY (design_image_id);

ALTER TABLE design_image MODIFY design_image_id bigint NOT NULL AUTO_INCREMENT;

SET @next := (SELECT COALESCE(MAX(design_image_id), 0) + 1 FROM design_image);
SET @sql := CONCAT('ALTER TABLE design_image AUTO_INCREMENT = ', @next);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SHOW CREATE TABLE design_image;
