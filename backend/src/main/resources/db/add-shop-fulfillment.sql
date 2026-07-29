-- Shop module schema (Phase S5 — fulfillment)
-- Branch: feature/shop-v1
-- Run after add-shop-orders.sql
--
-- Usage (MySQL Workbench):
--   USE dreams_creations_db;
--   SOURCE add-shop-fulfillment.sql;

USE dreams_creations_db;

SET @OLD_SQL_SAFE_UPDATES = @@SQL_SAFE_UPDATES;
SET SQL_SAFE_UPDATES = 0;

ALTER TABLE shop_order
    ADD COLUMN stock_reserved BOOLEAN NOT NULL DEFAULT FALSE
        COMMENT 'True when inventory was deducted on order confirm';

SET SQL_SAFE_UPDATES = @OLD_SQL_SAFE_UPDATES;
