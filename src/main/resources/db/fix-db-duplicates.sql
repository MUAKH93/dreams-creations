-- Dreams Creations — find and fix duplicate rows that cause
-- "Query did not return a unique result: 2 results were returned"
--
-- Run STEP 1 (all SELECTs) first. Only run STEP 2 DELETEs for tables that show duplicates.

USE dreams_creations_db;

-- =============================================================================
-- STEP 1 — Inspect duplicates (safe, read-only)
-- =============================================================================

-- Duplicate roles (very common cause of supervisor login failure)
SELECT role_name, COUNT(*) AS cnt, GROUP_CONCAT(role_id ORDER BY role_id) AS ids
FROM role
GROUP BY role_name
HAVING cnt > 1;

-- Duplicate user emails
SELECT email, COUNT(*) AS cnt, GROUP_CONCAT(user_id ORDER BY user_id) AS ids,
       GROUP_CONCAT(username ORDER BY user_id) AS usernames
FROM `user`
WHERE email IS NOT NULL AND TRIM(email) != ''
GROUP BY email
HAVING cnt > 1;

-- Duplicate usernames
SELECT username, COUNT(*) AS cnt, GROUP_CONCAT(user_id ORDER BY user_id) AS ids
FROM `user`
GROUP BY username
HAVING cnt > 1;

-- Duplicate supervisor emails
SELECT email, COUNT(*) AS cnt, GROUP_CONCAT(supervisor_id ORDER BY supervisor_id) AS ids
FROM supervisor
WHERE email IS NOT NULL AND TRIM(email) != ''
GROUP BY email
HAVING cnt > 1;

-- =============================================================================
-- STEP 2 — Remove duplicates (only if STEP 1 found rows)
-- Keep the LOWEST id in each group. Edit the id lists before running.
-- =============================================================================

SET @OLD_SQL_SAFE_UPDATES = @@SQL_SAFE_UPDATES;
SET SQL_SAFE_UPDATES = 0;

-- 2a) Duplicate roles — keep lowest role_id per role_name
DELETE r FROM role r
INNER JOIN (
    SELECT role_name, MIN(role_id) AS keep_id
    FROM role
    GROUP BY role_name
    HAVING COUNT(*) > 1
) d ON r.role_name = d.role_name AND r.role_id != d.keep_id;

-- 2b) Duplicate users by email — keep lowest user_id
-- WARNING: verify no important data on duplicate rows before deleting
DELETE u FROM `user` u
INNER JOIN (
    SELECT email, MIN(user_id) AS keep_id
    FROM `user`
    WHERE email IS NOT NULL AND TRIM(email) != ''
    GROUP BY email
    HAVING COUNT(*) > 1
) d ON u.email = d.email AND u.user_id != d.keep_id;

-- 2c) Duplicate supervisors by email — keep lowest supervisor_id
DELETE s FROM supervisor s
INNER JOIN (
    SELECT email, MIN(supervisor_id) AS keep_id
    FROM supervisor
    WHERE email IS NOT NULL AND TRIM(email) != ''
    GROUP BY email
    HAVING COUNT(*) > 1
) d ON s.email = d.email AND s.supervisor_id != d.keep_id;

SET SQL_SAFE_UPDATES = @OLD_SQL_SAFE_UPDATES;

-- =============================================================================
-- STEP 3 — Verify (all should return 0 rows)
-- =============================================================================
SELECT role_name, COUNT(*) AS cnt FROM role GROUP BY role_name HAVING cnt > 1;
SELECT email, COUNT(*) AS cnt FROM `user` WHERE email IS NOT NULL GROUP BY email HAVING cnt > 1;
SELECT email, COUNT(*) AS cnt FROM supervisor WHERE email IS NOT NULL GROUP BY email HAVING cnt > 1;
