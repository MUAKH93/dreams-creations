-- Fix: size_id AUTO_INCREMENT
-- Error: Field 'size_id' doesn't have a default value
-- If MODIFY fails with error 1833 (foreign key), run STEP 1 first.

USE dreams_creations_db;

-- STEP 0 — List foreign keys on size (optional)
SELECT TABLE_NAME, CONSTRAINT_NAME
FROM information_schema.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = 'dreams_creations_db'
  AND REFERENCED_TABLE_NAME = 'size'
  AND REFERENCED_COLUMN_NAME = 'size_id';

-- STEP 1 — Only if MODIFY fails with error 1833
-- ALTER TABLE suit DROP FOREIGN KEY <constraint_name_from_step_0>;

-- STEP 2 — Primary key + AUTO_INCREMENT
ALTER TABLE size ADD PRIMARY KEY (size_id);

ALTER TABLE size MODIFY size_id bigint NOT NULL AUTO_INCREMENT;

-- STEP 3 — Re-add FK if dropped in step 1, e.g.:
-- ALTER TABLE suit ADD CONSTRAINT fk_suit_size FOREIGN KEY (size_id) REFERENCES size (size_id);

SHOW CREATE TABLE size;
