-- Finance module Phase F5 — Accounts payable (vendors & payables)
-- Run after add-finance-module.sql

USE dreams_creations_db;

SET @OLD_SQL_SAFE_UPDATES = @@SQL_SAFE_UPDATES;
SET SQL_SAFE_UPDATES = 0;

CREATE TABLE IF NOT EXISTS finance_vendor (
    vendor_id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    vendor_name     VARCHAR(120) NOT NULL,
    phone           VARCHAR(30)  NULL,
    email           VARCHAR(120) NULL,
    notes           VARCHAR(255) NULL,
    is_active       TINYINT(1)   NOT NULL DEFAULT 1,
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS finance_payable (
    payable_id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    vendor_id           BIGINT         NOT NULL,
    invoice_number      VARCHAR(40)    NOT NULL,
    invoice_date        DATE           NOT NULL,
    due_date            DATE           NOT NULL,
    amount              DECIMAL(14,2)  NOT NULL,
    amount_paid         DECIMAL(14,2)  NOT NULL DEFAULT 0.00,
    expense_account_id  BIGINT         NOT NULL,
    memo                VARCHAR(255)   NULL,
    status              VARCHAR(20)    NOT NULL DEFAULT 'unpaid',
    created_at          DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_finance_payable_invoice (vendor_id, invoice_number),
    CONSTRAINT fk_finance_payable_vendor
        FOREIGN KEY (vendor_id) REFERENCES finance_vendor (vendor_id),
    CONSTRAINT fk_finance_payable_expense_account
        FOREIGN KEY (expense_account_id) REFERENCES finance_account (account_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS finance_payable_payment (
    payment_id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    payable_id      BIGINT         NOT NULL,
    payment_date    DATE           NOT NULL,
    amount          DECIMAL(14,2)  NOT NULL,
    payment_method  VARCHAR(30)    NULL,
    reference_no    VARCHAR(60)    NULL,
    created_at      DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_finance_payable_payment_payable
        FOREIGN KEY (payable_id) REFERENCES finance_payable (payable_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

SET SQL_SAFE_UPDATES = @OLD_SQL_SAFE_UPDATES;
