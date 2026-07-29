-- Shop module schema (Phase S6 — payments)
-- Branch: feature/shop-v1
-- Run after add-shop-fulfillment.sql

USE dreams_creations_db;

SET @OLD_SQL_SAFE_UPDATES = @@SQL_SAFE_UPDATES;
SET SQL_SAFE_UPDATES = 0;

ALTER TABLE shop_order
    ADD COLUMN payment_method VARCHAR(30) NULL COMMENT 'cod | bank_transfer | online_gateway',
    ADD COLUMN payment_status VARCHAR(20) NOT NULL DEFAULT 'unpaid'
        COMMENT 'unpaid | pending | partial | paid',
    ADD COLUMN amount_paid DECIMAL(12, 2) NOT NULL DEFAULT 0,
    ADD COLUMN payment_reference VARCHAR(100) NULL;

CREATE TABLE IF NOT EXISTS shop_order_payment (
    payment_id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id        BIGINT         NOT NULL,
    amount          DECIMAL(12, 2) NOT NULL,
    payment_method  VARCHAR(30)    NOT NULL,
    reference_no    VARCHAR(100)   NULL,
    notes           VARCHAR(500)   NULL,
    recorded_by     BIGINT         NULL,
    created_at      DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_shop_order_payment_order
        FOREIGN KEY (order_id) REFERENCES shop_order (order_id) ON DELETE CASCADE,
    CONSTRAINT fk_shop_order_payment_user
        FOREIGN KEY (recorded_by) REFERENCES user (user_id),
    CONSTRAINT chk_shop_order_payment_amount CHECK (amount > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

SET SQL_SAFE_UPDATES = @OLD_SQL_SAFE_UPDATES;
