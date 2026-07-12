-- Activity log for key operations (Phase 5)
-- Run in MySQL Workbench against dreams_creations_db

CREATE TABLE IF NOT EXISTS activity_log (
    activity_id BIGINT NOT NULL AUTO_INCREMENT,
    action_type VARCHAR(40) NOT NULL,
    entity_type VARCHAR(40) NOT NULL,
    entity_id BIGINT NULL,
    summary VARCHAR(500) NOT NULL,
    performed_by_user_id BIGINT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (activity_id),
    CONSTRAINT fk_activity_user FOREIGN KEY (performed_by_user_id) REFERENCES `user` (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
