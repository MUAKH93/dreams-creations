-- =============================================================================
-- Dreams Creations — Database key + AUTO_INCREMENT repair
-- Run in MySQL Workbench (dreams_creations_db)
--
-- Error 1075 happens when AUTO_INCREMENT is set on a column that is NOT
-- a PRIMARY KEY. This usually means the import created tables but skipped
-- the ALTER TABLE ... ADD PRIMARY KEY section at the end of the dump.
--
-- STEP 0 (optional): Check one table
--   SHOW CREATE TABLE production_batch;
--   SHOW INDEX FROM production_batch;
-- If Key_name = PRIMARY is missing on batch_id, run STEP 1 then STEP 2.
-- =============================================================================

USE dreams_creations_db;

-- =============================================================================
-- STEP 1: Add PRIMARY KEY on each table (skip any line that errors
--         "Multiple primary key defined" — that table is already OK)
-- =============================================================================

ALTER TABLE production_batch  ADD PRIMARY KEY (batch_id);
ALTER TABLE customer          ADD PRIMARY KEY (customer_id);
ALTER TABLE module_assignment ADD PRIMARY KEY (assignment_id);
ALTER TABLE bill              ADD PRIMARY KEY (bill_id);
ALTER TABLE bill_item         ADD PRIMARY KEY (bill_item_id);
ALTER TABLE payment           ADD PRIMARY KEY (payment_id);
ALTER TABLE suit              ADD PRIMARY KEY (suit_id);
ALTER TABLE product           ADD PRIMARY KEY (product_id);
ALTER TABLE design            ADD PRIMARY KEY (design_id);
ALTER TABLE alert             ADD PRIMARY KEY (alert_id);
ALTER TABLE category          ADD PRIMARY KEY (category_id);
ALTER TABLE design_type       ADD PRIMARY KEY (design_type_id);
ALTER TABLE size              ADD PRIMARY KEY (size_id);
ALTER TABLE production_stage  ADD PRIMARY KEY (stage_id);
ALTER TABLE production_module ADD PRIMARY KEY (module_id);
ALTER TABLE payment_method    ADD PRIMARY KEY (payment_method_id);
ALTER TABLE supervisor        ADD PRIMARY KEY (supervisor_id);
ALTER TABLE `user`            ADD PRIMARY KEY (user_id);
ALTER TABLE role              ADD PRIMARY KEY (role_id);
ALTER TABLE inventory         ADD PRIMARY KEY (inventory_id);
ALTER TABLE design_required_stage ADD PRIMARY KEY (design_stage_id);
ALTER TABLE permission        ADD PRIMARY KEY (permission_id);
ALTER TABLE role_permission   ADD PRIMARY KEY (role_permission_id);

-- =============================================================================
-- STEP 2: Enable AUTO_INCREMENT (safe after PRIMARY KEY exists)
-- =============================================================================

ALTER TABLE production_batch  MODIFY batch_id bigint NOT NULL AUTO_INCREMENT;
ALTER TABLE customer          MODIFY customer_id bigint NOT NULL AUTO_INCREMENT;
ALTER TABLE module_assignment MODIFY assignment_id bigint NOT NULL AUTO_INCREMENT;
ALTER TABLE bill              MODIFY bill_id bigint NOT NULL AUTO_INCREMENT;
ALTER TABLE bill_item         MODIFY bill_item_id bigint NOT NULL AUTO_INCREMENT;
ALTER TABLE payment           MODIFY payment_id bigint NOT NULL AUTO_INCREMENT;
ALTER TABLE suit              MODIFY suit_id bigint NOT NULL AUTO_INCREMENT;
ALTER TABLE product           MODIFY product_id bigint NOT NULL AUTO_INCREMENT;
ALTER TABLE design            MODIFY design_id bigint NOT NULL AUTO_INCREMENT;
ALTER TABLE alert             MODIFY alert_id bigint NOT NULL AUTO_INCREMENT;
ALTER TABLE category          MODIFY category_id bigint NOT NULL AUTO_INCREMENT;
ALTER TABLE design_type       MODIFY design_type_id bigint NOT NULL AUTO_INCREMENT;
ALTER TABLE size              MODIFY size_id bigint NOT NULL AUTO_INCREMENT;
ALTER TABLE production_stage  MODIFY stage_id bigint NOT NULL AUTO_INCREMENT;
ALTER TABLE production_module MODIFY module_id bigint NOT NULL AUTO_INCREMENT;
ALTER TABLE payment_method    MODIFY payment_method_id bigint NOT NULL AUTO_INCREMENT;
ALTER TABLE supervisor        MODIFY supervisor_id bigint NOT NULL AUTO_INCREMENT;
ALTER TABLE `user`            MODIFY user_id bigint NOT NULL AUTO_INCREMENT;
ALTER TABLE role              MODIFY role_id bigint NOT NULL AUTO_INCREMENT;
ALTER TABLE inventory         MODIFY inventory_id bigint NOT NULL AUTO_INCREMENT;
ALTER TABLE design_required_stage MODIFY design_stage_id bigint NOT NULL AUTO_INCREMENT;
ALTER TABLE permission        MODIFY permission_id bigint NOT NULL AUTO_INCREMENT;
ALTER TABLE role_permission   MODIFY role_permission_id bigint NOT NULL AUTO_INCREMENT;

-- =============================================================================
-- STEP 3: Set next AUTO_INCREMENT values if you already have data
-- (adjust numbers if your max ids are higher)
-- =============================================================================

ALTER TABLE production_batch  AUTO_INCREMENT = 2;
ALTER TABLE customer          AUTO_INCREMENT = 3;
ALTER TABLE module_assignment AUTO_INCREMENT = 5;
ALTER TABLE bill              AUTO_INCREMENT = 13;
ALTER TABLE design            AUTO_INCREMENT = 2;
ALTER TABLE category          AUTO_INCREMENT = 3;
ALTER TABLE design_type       AUTO_INCREMENT = 2;
ALTER TABLE size              AUTO_INCREMENT = 20;
ALTER TABLE suit              AUTO_INCREMENT = 8;
ALTER TABLE product           AUTO_INCREMENT = 13;

-- Verify:
-- SHOW CREATE TABLE production_batch;
