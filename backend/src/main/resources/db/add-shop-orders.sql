-- Shop module schema (Phase S4 — checkout & shop orders)
-- Branch: feature/shop-v1
-- Run after add-shop-cart.sql
--
-- Usage (MySQL Workbench):
--   USE dreams_creations_db;
--   SOURCE add-shop-orders.sql;

USE dreams_creations_db;

SET @OLD_SQL_SAFE_UPDATES = @@SQL_SAFE_UPDATES;
SET SQL_SAFE_UPDATES = 0;

CREATE TABLE IF NOT EXISTS shop_order (
    order_id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_number      VARCHAR(30)    NOT NULL UNIQUE,
    customer_id       BIGINT         NOT NULL,
    status            VARCHAR(20)    NOT NULL DEFAULT 'pending',
    subtotal          DECIMAL(12, 2) NOT NULL,
    discount_amount   DECIMAL(10, 2) NOT NULL DEFAULT 0,
    total_amount      DECIMAL(12, 2) NOT NULL,
    shipping_notes    VARCHAR(500)   NULL,
    customer_notes    VARCHAR(500)   NULL,
    quotation_id      BIGINT         NULL,
    bill_id           BIGINT         NULL,
    confirmed_by      BIGINT         NULL,
    created_at        DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_shop_order_customer
        FOREIGN KEY (customer_id) REFERENCES customer (customer_id),
    CONSTRAINT fk_shop_order_quotation
        FOREIGN KEY (quotation_id) REFERENCES quotation (quotation_id),
    CONSTRAINT fk_shop_order_bill
        FOREIGN KEY (bill_id) REFERENCES bill (bill_id),
    CONSTRAINT fk_shop_order_confirmed_by
        FOREIGN KEY (confirmed_by) REFERENCES user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS shop_order_item (
    item_id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id      BIGINT         NOT NULL,
    product_id    BIGINT         NOT NULL,
    quantity      INT            NOT NULL,
    unit_price    DECIMAL(10, 2) NOT NULL,
    total_price   DECIMAL(12, 2) NOT NULL,
    CONSTRAINT fk_shop_order_item_order
        FOREIGN KEY (order_id) REFERENCES shop_order (order_id) ON DELETE CASCADE,
    CONSTRAINT fk_shop_order_item_product
        FOREIGN KEY (product_id) REFERENCES product (product_id),
    CONSTRAINT uk_shop_order_product UNIQUE (order_id, product_id),
    CONSTRAINT chk_shop_order_qty CHECK (quantity > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

SET SQL_SAFE_UPDATES = @OLD_SQL_SAFE_UPDATES;
