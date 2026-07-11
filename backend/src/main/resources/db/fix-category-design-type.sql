-- Quick fix: category + design_type (Error 1075)
-- Run STEP 1 first, THEN STEP 2 — never skip Step 1

USE dreams_creations_db;

-- STEP 1: Add primary keys (skip if "Multiple primary key defined")
ALTER TABLE category    ADD PRIMARY KEY (category_id);
ALTER TABLE design_type ADD PRIMARY KEY (design_type_id);

-- STEP 2: Now AUTO_INCREMENT will work
ALTER TABLE category    MODIFY category_id bigint NOT NULL AUTO_INCREMENT;
ALTER TABLE design_type MODIFY design_type_id bigint NOT NULL AUTO_INCREMENT;

-- Verify
SHOW CREATE TABLE category;
SHOW CREATE TABLE design_type;
