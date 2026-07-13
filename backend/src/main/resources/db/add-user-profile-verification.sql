-- User profiles + email verification (safe to re-run)
-- Run in MySQL Workbench against dreams_creations_db

DELIMITER //

DROP PROCEDURE IF EXISTS migrate_user_profile_columns //

CREATE PROCEDURE migrate_user_profile_columns()
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'user'
          AND COLUMN_NAME = 'first_name'
    ) THEN
        ALTER TABLE `user` ADD COLUMN first_name VARCHAR(50) NULL;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'user'
          AND COLUMN_NAME = 'last_name'
    ) THEN
        ALTER TABLE `user` ADD COLUMN last_name VARCHAR(50) NULL;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'user'
          AND COLUMN_NAME = 'phone'
    ) THEN
        ALTER TABLE `user` ADD COLUMN phone VARCHAR(20) NULL;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'user'
          AND COLUMN_NAME = 'profile_photo'
    ) THEN
        ALTER TABLE `user` ADD COLUMN profile_photo VARCHAR(255) NULL;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'user'
          AND COLUMN_NAME = 'email_verified'
    ) THEN
        ALTER TABLE `user`
            ADD COLUMN email_verified BOOLEAN NOT NULL DEFAULT FALSE;
    END IF;
END //

DELIMITER ;

CALL migrate_user_profile_columns();
DROP PROCEDURE IF EXISTS migrate_user_profile_columns;

-- Staff/admin/manager accounts: keep login working without re-verification
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
