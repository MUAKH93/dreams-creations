-- Remove orphaned customer login + related data
-- Run in MySQL Workbench against dreams_creations_db
-- Change @username below if needed (default: MUAKH93)

SET @username = 'MUAKH93';

-- Preview what will be removed
SELECT u.user_id, u.username, u.email, r.role_name, c.customer_id, c.first_name
FROM `user` u
JOIN role r ON u.role_id = r.role_id
LEFT JOIN customer c ON LOWER(c.email) = LOWER(u.email)
WHERE u.username = @username;

-- Delete quotations for this customer's profile (if customer row still exists)
DELETE qi FROM quotation_item qi
INNER JOIN quotation q ON qi.quotation_id = q.quotation_id
INNER JOIN customer c ON q.customer_id = c.customer_id
INNER JOIN `user` u ON LOWER(u.email) = LOWER(c.email)
WHERE u.username = @username;

DELETE q FROM quotation q
INNER JOIN customer c ON q.customer_id = c.customer_id
INNER JOIN `user` u ON LOWER(u.email) = LOWER(c.email)
WHERE u.username = @username;

-- Delete customer balance + customer row (matched by user email)
DELETE cb FROM customer_balance cb
INNER JOIN customer c ON cb.customer_id = c.customer_id
INNER JOIN `user` u ON LOWER(u.email) = LOWER(c.email)
WHERE u.username = @username;

DELETE c FROM customer c
INNER JOIN `user` u ON LOWER(u.email) = LOWER(c.email)
WHERE u.username = @username;

-- Delete auth tokens + login user
DELETE evt FROM email_verification_token evt
INNER JOIN `user` u ON evt.user_id = u.user_id
WHERE u.username = @username;

DELETE prt FROM password_reset_token prt
INNER JOIN `user` u ON prt.user_id = u.user_id
WHERE u.username = @username;

DELETE u FROM `user` u
JOIN role r ON u.role_id = r.role_id
WHERE u.username = @username AND r.role_name = 'CUSTOMER';

SELECT CONCAT('Cleanup complete for ', @username) AS status;
