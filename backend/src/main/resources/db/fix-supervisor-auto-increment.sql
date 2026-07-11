-- Fix: supervisor_id AUTO_INCREMENT (when FK constraints block MODIFY)
-- Error 1833: Cannot change column 'supervisor_id': used in a foreign key constraint
--
-- Run each section in order in MySQL Workbench (dreams_creations_db)

USE dreams_creations_db;

-- =============================================================================
-- STEP 0 — List all foreign keys pointing at supervisor (read-only)
-- Save the output; you will re-create these in STEP 3.
-- =============================================================================
SELECT TABLE_NAME, CONSTRAINT_NAME, COLUMN_NAME
FROM information_schema.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = 'dreams_creations_db'
  AND REFERENCED_TABLE_NAME = 'supervisor'
  AND REFERENCED_COLUMN_NAME = 'supervisor_id';

-- =============================================================================
-- STEP 1 — Drop foreign keys that reference supervisor.supervisor_id
-- Run every DROP line that matches a row from STEP 0.
-- Skip any line that errors "check that column/key exists".
-- =============================================================================

ALTER TABLE supervisor_module DROP FOREIGN KEY fk_sm_supervisor;

-- If STEP 0 shows more constraints, add DROP lines here, e.g.:
-- ALTER TABLE module_assignment DROP FOREIGN KEY fk_ma_supervisor;
-- ALTER TABLE suit_production_tracking DROP FOREIGN KEY fk_spt_supervisor;

-- =============================================================================
-- STEP 2 — Primary key + AUTO_INCREMENT
-- =============================================================================

-- Skip if "Multiple primary key defined"
ALTER TABLE supervisor ADD PRIMARY KEY (supervisor_id);

ALTER TABLE supervisor MODIFY supervisor_id bigint NOT NULL AUTO_INCREMENT;

-- Set next id above your current max (adjust if needed)
-- SELECT MAX(supervisor_id) FROM supervisor;
ALTER TABLE supervisor AUTO_INCREMENT = 10;

-- =============================================================================
-- STEP 3 — Re-create foreign keys (names must match your schema)
-- =============================================================================

ALTER TABLE supervisor_module
  ADD CONSTRAINT fk_sm_supervisor
  FOREIGN KEY (supervisor_id) REFERENCES supervisor (supervisor_id);

-- Re-add any other FKs you dropped in STEP 1, for example:
-- ALTER TABLE module_assignment
--   ADD CONSTRAINT fk_ma_supervisor
--   FOREIGN KEY (supervisor_id) REFERENCES supervisor (supervisor_id);

-- =============================================================================
-- STEP 4 — Verify
-- =============================================================================
SHOW CREATE TABLE supervisor;
