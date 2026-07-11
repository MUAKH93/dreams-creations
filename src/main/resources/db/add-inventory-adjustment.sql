-- Manual stock adjustment audit log (Phase 3)
-- Run in MySQL Workbench against dreams_creations_db

CREATE TABLE IF NOT EXISTS inventory_adjustment (
    adjustment_id BIGINT NOT NULL AUTO_INCREMENT,
    suit_id BIGINT NOT NULL,
    previous_quantity INT NOT NULL,
    new_quantity INT NOT NULL,
    reason VARCHAR(255) NOT NULL,
    adjusted_by_user_id BIGINT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (adjustment_id),
    CONSTRAINT fk_inv_adj_suit FOREIGN KEY (suit_id) REFERENCES suit (suit_id),
    CONSTRAINT fk_inv_adj_user FOREIGN KEY (adjusted_by_user_id) REFERENCES `user` (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
