-- Merge Cutting + Stitching into ONE stage: "Cutting & Stitching"
-- Full flow: Designing(1) → Filling(2, optional) → Cutting & Stitching(3)
--
-- PREREQUISITES (if INSERT fails with Error 1364 "doesn't have a default value"):
--   fix-production-stage-auto-increment.sql       → stage_id
--   fix-production-module-auto-increment.sql      → module_id (drop fk_sm_module first if 1833)
--   fix-design-required-stage-auto-increment.sql  → design_stage_id
--
-- If stages are already merged, you can run only sections 4–6.

USE dreams_creations_db;

SET @OLD_SQL_SAFE_UPDATES = @@SQL_SAFE_UPDATES;
SET SQL_SAFE_UPDATES = 0;

-- ── 1) Add Designing and Filling if missing ─────────────────────────────────
INSERT INTO production_stage (stage_name, stage_order, is_mandatory, description)
SELECT 'Designing', 1, 1, 'Initial design and pattern preparation'
WHERE NOT EXISTS (SELECT 1 FROM production_stage WHERE stage_name = 'Designing');

INSERT INTO production_stage (stage_name, stage_order, is_mandatory, description)
SELECT 'Filling', 2, 0, 'Optional filling / padding stage'
WHERE NOT EXISTS (SELECT 1 FROM production_stage WHERE stage_name = 'Filling');

-- ── 2) Merge Cutting + Stitching into one stage ─────────────────────────────
-- 2a) Rename Cutting first (if it exists)
UPDATE production_stage
SET stage_name = 'Cutting & Stitching',
    stage_order = 3,
    is_mandatory = 1,
    description = 'Cutting and stitching combined (single stage)'
WHERE stage_name = 'Cutting';

-- 2b) If only Stitching existed (no Cutting row), rename Stitching
SET @has_combined := (
    SELECT COUNT(*) FROM production_stage WHERE stage_name = 'Cutting & Stitching'
);
UPDATE production_stage
SET stage_name = 'Cutting & Stitching',
    stage_order = 3,
    is_mandatory = 1,
    description = 'Cutting and stitching combined (single stage)'
WHERE stage_name = 'Stitching' AND @has_combined = 0;

-- 2c) Move any Stitching modules onto the combined stage
UPDATE production_module m
INNER JOIN production_stage stitching ON stitching.stage_name = 'Stitching'
INNER JOIN production_stage combined ON combined.stage_name = 'Cutting & Stitching'
SET m.stage_id = combined.stage_id
WHERE m.stage_id = stitching.stage_id;

-- 2d) Remove leftover Stitching row
DELETE drs FROM design_required_stage drs
INNER JOIN production_stage s ON drs.stage_id = s.stage_id
WHERE s.stage_name = 'Stitching';

DELETE FROM production_stage WHERE stage_name = 'Stitching';

-- ── 3) Fix stage order ────────────────────────────────────────────────────────
UPDATE production_stage SET stage_order = 1, is_mandatory = 1 WHERE stage_name = 'Designing';
UPDATE production_stage SET stage_order = 2, is_mandatory = 0 WHERE stage_name = 'Filling';
UPDATE production_stage SET stage_order = 3, is_mandatory = 1 WHERE stage_name = 'Cutting & Stitching';

-- ── 4) Modules for Designing / Filling / Cutting & Stitching if missing ─────
INSERT INTO production_module (module_name, stage_id, status, description)
SELECT 'Design Studio A', s.stage_id, 'active', 'Main design workstation'
FROM production_stage s
WHERE s.stage_name = 'Designing'
  AND NOT EXISTS (
      SELECT 1 FROM production_module m
      JOIN production_stage s2 ON m.stage_id = s2.stage_id
      WHERE s2.stage_name = 'Designing'
  );

INSERT INTO production_module (module_name, stage_id, status, description)
SELECT 'Filling Station 1', s.stage_id, 'active', 'Filling and padding'
FROM production_stage s
WHERE s.stage_name = 'Filling'
  AND NOT EXISTS (
      SELECT 1 FROM production_module m
      JOIN production_stage s2 ON m.stage_id = s2.stage_id
      WHERE s2.stage_name = 'Filling'
  );

INSERT INTO production_module (module_name, stage_id, status, description)
SELECT 'Cutting & Stitching Line A', s.stage_id, 'active', 'Combined cutting and stitching'
FROM production_stage s
WHERE s.stage_name = 'Cutting & Stitching'
  AND NOT EXISTS (
      SELECT 1 FROM production_module m
      JOIN production_stage s2 ON m.stage_id = s2.stage_id
      WHERE s2.stage_name = 'Cutting & Stitching'
  );

-- ── 5) Reset design paths: Designing → Cutting & Stitching ──────────────────
DELETE FROM design_required_stage;

INSERT INTO design_required_stage (design_id, stage_id, stage_order, is_required)
SELECT d.design_id, s.stage_id, s.stage_order, 1
FROM design d
CROSS JOIN production_stage s
WHERE s.stage_name IN ('Designing', 'Cutting & Stitching');

SET SQL_SAFE_UPDATES = @OLD_SQL_SAFE_UPDATES;

-- ── 6) Verify ───────────────────────────────────────────────────────────────
SELECT stage_id, stage_name, stage_order, is_mandatory
FROM production_stage
ORDER BY stage_order;

SELECT module_id, module_name, stage_id
FROM production_module
ORDER BY stage_id;

SELECT d.design_code, s.stage_name, drs.stage_order
FROM design_required_stage drs
JOIN design d ON d.design_id = drs.design_id
JOIN production_stage s ON s.stage_id = drs.stage_id
ORDER BY d.design_code, drs.stage_order;
