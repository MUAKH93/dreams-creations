-- Optional base price on designs (not required when adding a design)
USE dreams_creations_db;

ALTER TABLE design ADD COLUMN base_price DECIMAL(10,2) NULL AFTER description;

-- Allow production batches without a specific size at design phase
ALTER TABLE suit MODIFY size_id bigint NULL;
