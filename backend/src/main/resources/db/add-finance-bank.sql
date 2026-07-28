-- Finance module Phase F6 — Bank accounts & reconciliation
-- Run after add-finance-module.sql and add-finance-payables.sql

USE dreams_creations_db;

SET @OLD_SQL_SAFE_UPDATES = @@SQL_SAFE_UPDATES;
SET SQL_SAFE_UPDATES = 0;

CREATE TABLE IF NOT EXISTS finance_bank_account (
    bank_account_id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    account_name          VARCHAR(120) NOT NULL,
    bank_name             VARCHAR(120) NULL,
    account_number        VARCHAR(40)  NULL,
    gl_account_id         BIGINT         NOT NULL,
    opening_balance       DECIMAL(14,2)  NOT NULL DEFAULT 0.00,
    opening_balance_date  DATE           NULL,
    is_active             TINYINT(1)     NOT NULL DEFAULT 1,
    notes                 VARCHAR(255)   NULL,
    created_at            DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_finance_bank_account_gl
        FOREIGN KEY (gl_account_id) REFERENCES finance_account (account_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS finance_bank_transaction (
    transaction_id    BIGINT AUTO_INCREMENT PRIMARY KEY,
    bank_account_id   BIGINT         NOT NULL,
    transaction_date  DATE           NOT NULL,
    description       VARCHAR(255)   NULL,
    amount            DECIMAL(14,2)  NOT NULL COMMENT 'Positive = deposit, negative = withdrawal',
    reference_no      VARCHAR(60)    NULL,
    is_reconciled     TINYINT(1)     NOT NULL DEFAULT 0,
    reconciled_at     DATETIME       NULL,
    matched_entry_id  BIGINT         NULL,
    created_at        DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_finance_bank_tx_account
        FOREIGN KEY (bank_account_id) REFERENCES finance_bank_account (bank_account_id),
    CONSTRAINT fk_finance_bank_tx_entry
        FOREIGN KEY (matched_entry_id) REFERENCES finance_journal_entry (entry_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

SET SQL_SAFE_UPDATES = @OLD_SQL_SAFE_UPDATES;
