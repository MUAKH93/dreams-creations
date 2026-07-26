-- Run this if add-user-profile-verification.sql failed with "Duplicate column name"
-- Columns already exist — only finishes the remaining steps.

-- Verify staff can log in (no email verification required for them)
UPDATE `user` u
JOIN role r ON u.role_id = r.role_id
SET u.email_verified = TRUE
WHERE r.role_name IN ('ADMIN', 'MANAGER', 'SUPERVISOR');

CREATE TABLE IF NOT EXISTS email_verification_token (
    token_id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    token VARCHAR(64) NOT NULL,
    expires_at DATETIME NOT NULL,
    used_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (token_id),
    UNIQUE KEY uk_email_verify_token (token),
    CONSTRAINT fk_email_verify_user FOREIGN KEY (user_id) REFERENCES `user` (user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

SELECT 'User profile migration complete' AS status;
