-- Phase 7: Design production cost for profitability analytics
-- Run in MySQL Workbench against dreams_creations_db

ALTER TABLE design
    ADD COLUMN production_cost DECIMAL(10,2) NULL;
