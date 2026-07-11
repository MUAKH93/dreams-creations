-- Dreams Creations — database maintenance script
-- Database: dreams_creations_db
--
-- HOW TO USE:
--   1. Run STEP 1 first.
--   2. If STEP 1 returns 0 rows → SKIP STEP 2 entirely, go to STEP 3.
--   3. If STEP 1 shows duplicates → run STEP 2 (uses safe-updates-off for that session only).
--   4. Always run STEP 3 to fix batch produced counts.

USE dreams_creations_db;

-- =============================================================================
-- STEP 1 — Check for duplicate customer emails (run this first)
-- =============================================================================
SELECT email, COUNT(*) AS cnt, GROUP_CONCAT(customer_id ORDER BY customer_id) AS ids
FROM customer
WHERE email IS NOT NULL AND TRIM(email) != ''
GROUP BY email
HAVING cnt > 1;

-- >>> If 0 rows above: SKIP STEP 2. Your data is clean. Go to STEP 3. <<<

-- =============================================================================
-- STEP 2 — ONLY run when STEP 1 returned duplicate rows
-- Workbench safe-update mode blocks these unless disabled for the session.
-- =============================================================================

SET @OLD_SQL_SAFE_UPDATES = @@SQL_SAFE_UPDATES;
SET SQL_SAFE_UPDATES = 0;

-- 2a) Re-point bills to the lowest customer_id per duplicate email
UPDATE bill b
INNER JOIN customer c ON b.customer_id = c.customer_id
INNER JOIN (
    SELECT email, MIN(customer_id) AS keep_id
    FROM customer
    WHERE email IS NOT NULL AND TRIM(email) != ''
    GROUP BY email
    HAVING COUNT(*) > 1
) d ON c.email = d.email AND c.customer_id != d.keep_id
SET b.customer_id = d.keep_id;

-- 2b) Merge duplicate balances into the kept customer
UPDATE customer_balance cb_keep
INNER JOIN (
    SELECT d.keep_id,
           SUM(cb_dup.total_sales) AS add_sales,
           SUM(cb_dup.total_paid)  AS add_paid,
           SUM(cb_dup.balance)     AS add_balance
    FROM customer c
    INNER JOIN (
        SELECT email, MIN(customer_id) AS keep_id
        FROM customer
        WHERE email IS NOT NULL AND TRIM(email) != ''
        GROUP BY email
        HAVING COUNT(*) > 1
    ) d ON c.email = d.email AND c.customer_id != d.keep_id
    INNER JOIN customer_balance cb_dup ON cb_dup.customer_id = c.customer_id
    GROUP BY d.keep_id
) agg ON cb_keep.customer_id = agg.keep_id
SET cb_keep.total_sales = cb_keep.total_sales + agg.add_sales,
    cb_keep.total_paid  = cb_keep.total_paid  + agg.add_paid,
    cb_keep.balance     = cb_keep.balance     + agg.add_balance;

-- 2c) Remove balance rows for duplicate customers
DELETE cb FROM customer_balance cb
INNER JOIN customer c ON cb.customer_id = c.customer_id
INNER JOIN (
    SELECT email, MIN(customer_id) AS keep_id
    FROM customer
    WHERE email IS NOT NULL AND TRIM(email) != ''
    GROUP BY email
    HAVING COUNT(*) > 1
) d ON c.email = d.email AND c.customer_id != d.keep_id;

-- 2d) Remove duplicate customer rows
DELETE c FROM customer c
INNER JOIN (
    SELECT email, MIN(customer_id) AS keep_id
    FROM customer
    WHERE email IS NOT NULL AND TRIM(email) != ''
    GROUP BY email
    HAVING COUNT(*) > 1
) d ON c.email = d.email AND c.customer_id != d.keep_id;

SET SQL_SAFE_UPDATES = @OLD_SQL_SAFE_UPDATES;

-- 2e) Verify — should return 0 rows after STEP 2
SELECT email, COUNT(*) AS cnt
FROM customer
WHERE email IS NOT NULL AND TRIM(email) != ''
GROUP BY email
HAVING cnt > 1;

-- =============================================================================
-- STEP 3 — Fix batch produced count (always safe to run)
-- Recalculates from Stitching returns and caps at planned quantity.
-- =============================================================================
UPDATE production_batch
SET total_suit_produced = LEAST(
    total_suit_planned,
    COALESCE((
        SELECT SUM(ma.quantity_returned_ok)
        FROM module_assignment ma
        INNER JOIN production_module pm ON ma.module_id = pm.module_id
        INNER JOIN production_stage ps ON pm.stage_id = ps.stage_id
        WHERE ma.batch_id = production_batch.batch_id
          AND ma.status = 'returned'
          AND ps.stage_name IN ('Cutting & Stitching', 'Cutting', 'Stitching')
    ), 0)
)
WHERE batch_id > 0;

-- Check result
SELECT batch_id, batch_number, total_suit_planned, total_suit_produced, status
FROM production_batch;
