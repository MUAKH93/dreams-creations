-- Shop module schema (Phase S3 — shopping cart)
-- Branch: feature/shop-v1
-- Run after add-shop-module.sql
--
-- Usage (MySQL Workbench):
--   USE dreams_creations_db;
--   SOURCE add-shop-cart.sql;

USE dreams_creations_db;

SET @OLD_SQL_SAFE_UPDATES = @@SQL_SAFE_UPDATES;
SET SQL_SAFE_UPDATES = 0;

CREATE TABLE IF NOT EXISTS shop_cart (
    cart_id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id   BIGINT   NOT NULL UNIQUE,
    updated_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_shop_cart_customer
        FOREIGN KEY (customer_id) REFERENCES customer (customer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS shop_cart_item (
    item_id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    cart_id       BIGINT         NOT NULL,
    product_id    BIGINT         NOT NULL,
    quantity      INT            NOT NULL DEFAULT 1,
    unit_price    DECIMAL(10, 2) NOT NULL COMMENT 'Price snapshot at add time',
    created_at    DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_shop_cart_item_cart
        FOREIGN KEY (cart_id) REFERENCES shop_cart (cart_id) ON DELETE CASCADE,
    CONSTRAINT fk_shop_cart_item_product
        FOREIGN KEY (product_id) REFERENCES product (product_id),
    CONSTRAINT uk_shop_cart_product UNIQUE (cart_id, product_id),
    CONSTRAINT chk_shop_cart_qty CHECK (quantity > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

SET SQL_SAFE_UPDATES = @OLD_SQL_SAFE_UPDATES;
