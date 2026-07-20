-- Finance module schema (Phase F1 — core ledger)
-- Branch: feature/finance-v2
-- Run on STAGING database first. Do NOT run on production until UAT sign-off.
--
-- Usage (MySQL Workbench):
--   USE dreams_creations_staging;
--   SOURCE add-finance-module.sql;

USE dreams_creations_db;

SET @OLD_SQL_SAFE_UPDATES = @@SQL_SAFE_UPDATES;
SET SQL_SAFE_UPDATES = 0;

-- ── Phase F1: Chart of accounts ─────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS finance_account (
    account_id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    account_code    VARCHAR(20)  NOT NULL UNIQUE,
    account_name    VARCHAR(120) NOT NULL,
    account_type    VARCHAR(20)  NOT NULL COMMENT 'ASSET, LIABILITY, EQUITY, INCOME, EXPENSE',
    parent_id       BIGINT       NULL,
    is_active       TINYINT(1)   NOT NULL DEFAULT 1,
    is_system       TINYINT(1)   NOT NULL DEFAULT 0 COMMENT 'System accounts cannot be deleted',
    description     VARCHAR(255) NULL,
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_finance_account_parent
        FOREIGN KEY (parent_id) REFERENCES finance_account (account_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS finance_fiscal_period (
    period_id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    period_name     VARCHAR(40)  NOT NULL,
    start_date      DATE         NOT NULL,
    end_date        DATE         NOT NULL,
    status          VARCHAR(20)  NOT NULL DEFAULT 'open' COMMENT 'open, closed',
    closed_at       DATETIME     NULL,
    closed_by       BIGINT       NULL,
    UNIQUE KEY uk_finance_period_range (start_date, end_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS finance_journal_entry (
    entry_id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    entry_number    VARCHAR(30)  NOT NULL UNIQUE,
    entry_date      DATE         NOT NULL,
    fiscal_period_id BIGINT      NULL,
    memo            VARCHAR(255) NULL,
    source_type     VARCHAR(30)  NOT NULL DEFAULT 'manual' COMMENT 'manual, bill, payment, inventory, opening',
    source_id       BIGINT       NULL COMMENT 'bill_id, payment_id, etc.',
    status          VARCHAR(20)  NOT NULL DEFAULT 'posted' COMMENT 'draft, posted, void',
    created_by      BIGINT       NULL,
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    posted_at       DATETIME     NULL,
    CONSTRAINT fk_finance_journal_period
        FOREIGN KEY (fiscal_period_id) REFERENCES finance_fiscal_period (period_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS finance_journal_line (
    line_id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    entry_id        BIGINT         NOT NULL,
    account_id      BIGINT         NOT NULL,
    debit_amount    DECIMAL(14,2)  NOT NULL DEFAULT 0.00,
    credit_amount   DECIMAL(14,2)  NOT NULL DEFAULT 0.00,
    line_memo       VARCHAR(255)   NULL,
    line_order      INT            NOT NULL DEFAULT 1,
    CONSTRAINT fk_finance_line_entry
        FOREIGN KEY (entry_id) REFERENCES finance_journal_entry (entry_id) ON DELETE CASCADE,
    CONSTRAINT fk_finance_line_account
        FOREIGN KEY (account_id) REFERENCES finance_account (account_id),
    CONSTRAINT chk_finance_line_debit_credit
        CHECK (debit_amount >= 0 AND credit_amount >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS finance_posting_link (
    link_id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    entry_id        BIGINT       NOT NULL,
    entity_type     VARCHAR(30)  NOT NULL COMMENT 'bill, payment, inventory_adjustment',
    entity_id       BIGINT       NOT NULL,
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_finance_posting_entity (entity_type, entity_id),
    CONSTRAINT fk_finance_posting_entry
        FOREIGN KEY (entry_id) REFERENCES finance_journal_entry (entry_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ── Default chart of accounts (seed once) ───────────────────────────────────

INSERT INTO finance_account (account_code, account_name, account_type, is_system, description)
SELECT * FROM (
    SELECT '1000' AS account_code, 'Cash & Bank' AS account_name, 'ASSET' AS account_type, 1 AS is_system, 'Cash and bank balances' AS description
    UNION ALL SELECT '1100', 'Accounts Receivable', 'ASSET', 1, 'Customer outstanding balances'
    UNION ALL SELECT '1200', 'Inventory — Finished Goods', 'ASSET', 1, 'Stock on hand at cost'
    UNION ALL SELECT '1300', 'Work in Progress', 'ASSET', 1, 'Production in progress'
    UNION ALL SELECT '2000', 'Accounts Payable', 'LIABILITY', 1, 'Supplier balances'
    UNION ALL SELECT '2100', 'Customer Deposits', 'LIABILITY', 1, 'Advance payments from customers'
    UNION ALL SELECT '3000', 'Owner''s Equity', 'EQUITY', 1, 'Capital / owner investment'
    UNION ALL SELECT '3100', 'Retained Earnings', 'EQUITY', 1, 'Accumulated profit'
    UNION ALL SELECT '4000', 'Sales Revenue', 'INCOME', 1, 'Revenue from bill sales'
    UNION ALL SELECT '4100', 'Sales Discounts', 'INCOME', 1, 'Contra revenue — discounts given'
    UNION ALL SELECT '5000', 'Cost of Goods Sold', 'EXPENSE', 1, 'Cost of inventory sold'
    UNION ALL SELECT '5100', 'Production Expenses', 'EXPENSE', 1, 'Direct production costs'
    UNION ALL SELECT '5200', 'Operating Expenses', 'EXPENSE', 1, 'General factory overhead'
) AS seed
WHERE NOT EXISTS (SELECT 1 FROM finance_account LIMIT 1);

-- ── Phase F5+ (future — uncomment when implementing) ──────────────────────────
-- CREATE TABLE finance_vendor ( ... );
-- CREATE TABLE finance_bank_account ( ... );
-- CREATE TABLE finance_bank_transaction ( ... );

SET SQL_SAFE_UPDATES = @OLD_SQL_SAFE_UPDATES;

-- After running this script, set modules.finance.enabled=true and add JPA entities in Phase F1.
