-- Add Press and Packing as final production stage (after Cutting & Stitching).
-- Pieces auto-forward from Cutting & Stitching return to Press and Packing.
-- Inventory updates on Press and Packing return.
--
-- Run after fix-production-stages.sql on existing databases.

USE dreams_creations_db;

SET @OLD_SQL_SAFE_UPDATES = @@SQL_SAFE_UPDATES;
SET SQL_SAFE_UPDATES = 0;

-- ── 1) Press and Packing stage ───────────────────────────────────────────────
INSERT INTO production_stage (stage_name, stage_order, is_mandatory, description)
SELECT 'Press and Packing', 4, 1, 'Final press, fold, and pack before inventory'
WHERE NOT EXISTS (SELECT 1 FROM production_stage WHERE stage_name = 'Press and Packing');

UPDATE production_stage SET stage_order = 4, is_mandatory = 1
WHERE stage_name = 'Press and Packing';

-- ── 2) Module for Press and Packing ─────────────────────────────────────────
INSERT INTO production_module (module_name, stage_id, status, description)
SELECT 'Press & Packing Station 1', s.stage_id, 'active', 'Press, fold, and pack finished suits'
FROM production_stage s
WHERE s.stage_name = 'Press and Packing'
  AND NOT EXISTS (
      SELECT 1 FROM production_module m
      JOIN production_stage s2 ON m.stage_id = s2.stage_id
      WHERE s2.stage_name = 'Press and Packing'
  );

-- ── 3) Append Press and Packing to each design path (if not already last) ───
INSERT INTO design_required_stage (design_id, stage_id, stage_order, is_required)
SELECT d.design_id, pp.stage_id, pp.stage_order, 1
FROM design d
CROSS JOIN production_stage pp
WHERE pp.stage_name = 'Press and Packing'
  AND NOT EXISTS (
      SELECT 1 FROM design_required_stage drs
      JOIN production_stage s ON drs.stage_id = s.stage_id
      WHERE drs.design_id = d.design_id AND s.stage_name = 'Press and Packing'
  )
  AND EXISTS (
      SELECT 1 FROM design_required_stage drs2
      WHERE drs2.design_id = d.design_id
  );

-- Designs with no path yet: Designing → Cutting & Stitching → Press and Packing
INSERT INTO design_required_stage (design_id, stage_id, stage_order, is_required)
SELECT d.design_id, s.stage_id, s.stage_order, 1
FROM design d
CROSS JOIN production_stage s
WHERE s.stage_name IN ('Designing', 'Cutting & Stitching', 'Press and Packing')
  AND NOT EXISTS (
      SELECT 1 FROM design_required_stage drs WHERE drs.design_id = d.design_id
  );

-- ── 4) Default packing supervisor "Asif" ────────────────────────────────────
INSERT INTO supervisor (first_name, last_name, phone, email, hire_date, status)
SELECT 'Asif', NULL, NULL, 'asif.packing@dreamscreations.local', CURDATE(), 'active'
WHERE NOT EXISTS (
    SELECT 1 FROM supervisor WHERE LOWER(first_name) = 'asif'
);

-- ── 5) System settings table + default packing supervisor ───────────────────
CREATE TABLE IF NOT EXISTS system_setting (
    setting_key   VARCHAR(64)  NOT NULL PRIMARY KEY,
    setting_value VARCHAR(255) NULL,
    updated_at    TIMESTAMP    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

INSERT INTO system_setting (setting_key, setting_value)
SELECT 'packing_supervisor_id',
       CAST((SELECT supervisor_id FROM supervisor WHERE LOWER(first_name) = 'asif' LIMIT 1) AS CHAR)
WHERE NOT EXISTS (SELECT 1 FROM system_setting WHERE setting_key = 'packing_supervisor_id')
  AND EXISTS (SELECT 1 FROM supervisor WHERE LOWER(first_name) = 'asif');

-- ── 6) Batch design label + article number columns ──────────────────────────
SET @col_exists := (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'production_batch'
      AND COLUMN_NAME = 'design_label'
);
SET @sql := IF(@col_exists = 0,
    'ALTER TABLE production_batch ADD COLUMN design_label VARCHAR(100) NULL AFTER batch_number',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists := (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'production_batch'
      AND COLUMN_NAME = 'article_batch_number'
);
SET @sql := IF(@col_exists = 0,
    'ALTER TABLE production_batch ADD COLUMN article_batch_number VARCHAR(50) NULL AFTER design_label',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET SQL_SAFE_UPDATES = @OLD_SQL_SAFE_UPDATES;

SELECT stage_id, stage_name, stage_order, is_mandatory
FROM production_stage ORDER BY stage_order;

SELECT setting_key, setting_value FROM system_setting WHERE setting_key = 'packing_supervisor_id';
