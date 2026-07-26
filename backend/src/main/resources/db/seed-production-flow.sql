-- Dreams Creations: Production flow seed data
-- Run in MySQL Workbench after selecting dreams_creations_db
-- Safe to re-run: uses INSERT ... WHERE NOT EXISTS patterns

USE dreams_creations_db;

-- ── Categories ──────────────────────────────────────────────────────────────
INSERT INTO category (category_name, description)
SELECT 'Kids', 'Kids suits collection'
WHERE NOT EXISTS (SELECT 1 FROM category WHERE category_name = 'Kids');

-- ── Ladies sizes: XS, S, M, L, XL ──────────────────────────────────────────
INSERT INTO size (size_value, category_id, description)
SELECT v.sz, c.category_id, NULL
FROM category c
CROSS JOIN (
    SELECT 'XS' AS sz UNION SELECT 'S' UNION SELECT 'M' UNION SELECT 'L' UNION SELECT 'XL'
) v
WHERE c.category_name = 'Ladies'
  AND NOT EXISTS (
      SELECT 1 FROM size s WHERE s.category_id = c.category_id AND s.size_value = v.sz
  );

-- ── Kids sizes: 18, 20, 22 ... 40 ───────────────────────────────────────────
INSERT INTO size (size_value, category_id, description)
SELECT v.sz, c.category_id, NULL
FROM category c
CROSS JOIN (
    SELECT '18' AS sz UNION SELECT '20' UNION SELECT '22' UNION SELECT '24'
    UNION SELECT '26' UNION SELECT '28' UNION SELECT '30' UNION SELECT '32'
    UNION SELECT '34' UNION SELECT '36' UNION SELECT '38' UNION SELECT '40'
) v
WHERE c.category_name = 'Kids'
  AND NOT EXISTS (
      SELECT 1 FROM size s WHERE s.category_id = c.category_id AND s.size_value = v.sz
  );

-- ── Production stages: Designing → Filling (optional) → Cutting & Stitching ───
INSERT INTO production_stage (stage_name, stage_order, is_mandatory, description)
SELECT 'Designing', 1, 1, 'Initial design and pattern preparation'
WHERE NOT EXISTS (SELECT 1 FROM production_stage WHERE stage_name = 'Designing');

INSERT INTO production_stage (stage_name, stage_order, is_mandatory, description)
SELECT 'Filling', 2, 0, 'Optional filling / padding stage'
WHERE NOT EXISTS (SELECT 1 FROM production_stage WHERE stage_name = 'Filling');

INSERT INTO production_stage (stage_name, stage_order, is_mandatory, description)
SELECT 'Cutting & Stitching', 3, 1, 'Cutting and stitching combined (single stage)'
WHERE NOT EXISTS (SELECT 1 FROM production_stage WHERE stage_name = 'Cutting & Stitching');

-- Migrate legacy separate Cutting / Stitching rows into one
UPDATE production_module m
JOIN production_stage cutting ON cutting.stage_name = 'Cutting'
JOIN production_stage stitching ON stitching.stage_name = 'Stitching'
SET m.stage_id = cutting.stage_id
WHERE m.stage_id = stitching.stage_id;

UPDATE production_stage
SET stage_name = 'Cutting & Stitching', stage_order = 3, is_mandatory = 1,
    description = 'Cutting and stitching combined (single stage)'
WHERE stage_name = 'Cutting';

UPDATE production_stage
SET stage_name = 'Cutting & Stitching', stage_order = 3, is_mandatory = 1,
    description = 'Cutting and stitching combined (single stage)'
WHERE stage_name = 'Stitching'
  AND NOT EXISTS (SELECT 1 FROM production_stage WHERE stage_name = 'Cutting & Stitching');

DELETE FROM production_stage WHERE stage_name = 'Stitching'
  AND EXISTS (SELECT 1 FROM production_stage WHERE stage_name = 'Cutting & Stitching');

-- ── Modules for new stages ───────────────────────────────────────────────────
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

-- ── Default stage path: Designing → Cutting & Stitching ───────────────────────
INSERT INTO design_required_stage (design_id, stage_id, stage_order, is_required)
SELECT d.design_id, s.stage_id, s.stage_order, 1
FROM design d
CROSS JOIN production_stage s
WHERE d.design_code = 'LAD-001'
  AND s.stage_name IN ('Designing', 'Cutting & Stitching')
  AND NOT EXISTS (
      SELECT 1 FROM design_required_stage drs WHERE drs.design_id = d.design_id
  );

-- ── Example: design with Filling stage (run manually when you add a design) ─
-- INSERT INTO design_required_stage (design_id, stage_id, stage_order, is_required)
-- SELECT d.design_id, s.stage_id,
--   CASE s.stage_name
--     WHEN 'Designing' THEN 1 WHEN 'Filling' THEN 2
--     WHEN 'Cutting' THEN 3 WHEN 'Stitching' THEN 4
--   END, 1
-- FROM design d CROSS JOIN production_stage s
-- WHERE d.design_code = 'YOUR-DESIGN-CODE'
--   AND s.stage_name IN ('Designing','Filling','Cutting & Stitching');

SELECT 'Seed complete' AS status;
SELECT stage_name, stage_order FROM production_stage ORDER BY stage_order;
SELECT category_name FROM category;
SELECT COUNT(*) AS ladies_sizes FROM size s JOIN category c ON s.category_id = c.category_id WHERE c.category_name = 'Ladies';
SELECT COUNT(*) AS kids_sizes FROM size s JOIN category c ON s.category_id = c.category_id WHERE c.category_name = 'Kids';
