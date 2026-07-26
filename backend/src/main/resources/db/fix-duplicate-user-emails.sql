-- Find duplicate user emails (can cause "Query did not return a unique result: 2 results")
USE dreams_creations_db;

SELECT email, COUNT(*) AS cnt, GROUP_CONCAT(user_id ORDER BY user_id) AS user_ids,
       GROUP_CONCAT(username ORDER BY user_id) AS usernames
FROM `user`
WHERE email IS NOT NULL AND TRIM(email) != ''
GROUP BY email
HAVING cnt > 1;

-- >>> If duplicates found, keep the lowest user_id and delete the rest manually, e.g.:
-- DELETE FROM `user` WHERE user_id IN (/* duplicate ids except the one to keep */);
