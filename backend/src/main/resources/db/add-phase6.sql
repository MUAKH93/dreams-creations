-- Phase 6: Quotations & customer discount rules
-- Run in MySQL Workbench against dreams_creations_db

ALTER TABLE customer
    ADD COLUMN discount_percent DECIMAL(5,2) NOT NULL DEFAULT 0;

CREATE TABLE IF NOT EXISTS quotation (
    quotation_id BIGINT NOT NULL AUTO_INCREMENT,
    quotation_number VARCHAR(30) NOT NULL,
    customer_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'draft',
    total_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
    discount DECIMAL(10,2) NOT NULL DEFAULT 0,
    final_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
    notes VARCHAR(500) NULL,
    bill_id BIGINT NULL,
    created_by BIGINT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (quotation_id),
    UNIQUE KEY uk_quotation_number (quotation_number),
    CONSTRAINT fk_quotation_customer FOREIGN KEY (customer_id) REFERENCES customer (customer_id),
    CONSTRAINT fk_quotation_bill FOREIGN KEY (bill_id) REFERENCES bill (bill_id),
    CONSTRAINT fk_quotation_created_by FOREIGN KEY (created_by) REFERENCES `user` (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS quotation_item (
    quotation_item_id BIGINT NOT NULL AUTO_INCREMENT,
    quotation_id BIGINT NOT NULL,
    design_id BIGINT NOT NULL,
    size_id BIGINT NULL,
    color VARCHAR(50) NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    total_price DECIMAL(12,2) NOT NULL,
    notes VARCHAR(255) NULL,
    PRIMARY KEY (quotation_item_id),
    CONSTRAINT fk_qitem_quotation FOREIGN KEY (quotation_id) REFERENCES quotation (quotation_id) ON DELETE CASCADE,
    CONSTRAINT fk_qitem_design FOREIGN KEY (design_id) REFERENCES design (design_id),
    CONSTRAINT fk_qitem_size FOREIGN KEY (size_id) REFERENCES size (size_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
