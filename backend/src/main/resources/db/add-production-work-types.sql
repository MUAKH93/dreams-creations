-- Production work types + size/color splits at Cutting & Stitching dispatch
-- Run in MySQL Workbench (dreams_creations_db)

USE dreams_creations_db;

-- ── Designing work types ──────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS designing_work_type (
    designing_work_type_id BIGINT NOT NULL AUTO_INCREMENT,
    type_name VARCHAR(80) NOT NULL,
    description VARCHAR(255) NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'active',
    PRIMARY KEY (designing_work_type_id),
    UNIQUE KEY uk_designing_work_type_name (type_name)
);

INSERT INTO designing_work_type (type_name, description, status)
SELECT 'Pattern Drafting', 'Initial pattern and sketch work', 'active'
WHERE NOT EXISTS (SELECT 1 FROM designing_work_type WHERE type_name = 'Pattern Drafting');

INSERT INTO designing_work_type (type_name, description, status)
SELECT 'Digital Design', 'CAD / digital pattern preparation', 'active'
WHERE NOT EXISTS (SELECT 1 FROM designing_work_type WHERE type_name = 'Digital Design');

-- ── Filling work types ────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS filling_work_type (
    filling_work_type_id BIGINT NOT NULL AUTO_INCREMENT,
    type_name VARCHAR(80) NOT NULL,
    description VARCHAR(255) NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'active',
    PRIMARY KEY (filling_work_type_id),
    UNIQUE KEY uk_filling_work_type_name (type_name)
);

INSERT INTO filling_work_type (type_name, description, status)
SELECT 'Standard Padding', 'Regular filling / padding', 'active'
WHERE NOT EXISTS (SELECT 1 FROM filling_work_type WHERE type_name = 'Standard Padding');

INSERT INTO filling_work_type (type_name, description, status)
SELECT 'Heavy Padding', 'Extra padding for formal suits', 'active'
WHERE NOT EXISTS (SELECT 1 FROM filling_work_type WHERE type_name = 'Heavy Padding');

-- ── Module assignment columns ─────────────────────────────────────────────────
ALTER TABLE module_assignment
    ADD COLUMN designing_work_type_id BIGINT NULL,
    ADD COLUMN filling_work_type_id BIGINT NULL;

ALTER TABLE module_assignment
    ADD CONSTRAINT fk_ma_designing_work_type
        FOREIGN KEY (designing_work_type_id) REFERENCES designing_work_type (designing_work_type_id);

ALTER TABLE module_assignment
    ADD CONSTRAINT fk_ma_filling_work_type
        FOREIGN KEY (filling_work_type_id) REFERENCES filling_work_type (filling_work_type_id);

-- ── Size/color lines for final stage dispatch ─────────────────────────────────
CREATE TABLE IF NOT EXISTS module_assignment_sku_line (
    line_id BIGINT NOT NULL AUTO_INCREMENT,
    assignment_id BIGINT NOT NULL,
    size_id BIGINT NOT NULL,
    color VARCHAR(30) NOT NULL,
    quantity_sent INT NOT NULL,
    quantity_returned_ok INT NOT NULL DEFAULT 0,
    quantity_damaged INT NOT NULL DEFAULT 0,
    quantity_missing INT NOT NULL DEFAULT 0,
    PRIMARY KEY (line_id),
    CONSTRAINT fk_sku_line_assignment
        FOREIGN KEY (assignment_id) REFERENCES module_assignment (assignment_id),
    CONSTRAINT fk_sku_line_size
        FOREIGN KEY (size_id) REFERENCES size (size_id)
);

SELECT 'Migration complete' AS status;
SELECT type_name FROM designing_work_type;
SELECT type_name FROM filling_work_type;
