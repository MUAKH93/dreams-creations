-- Shop module schema (Phase S1 — foundation)
-- Branch: feature/shop-v1
-- Run on STAGING database first. Do NOT run on production until operations go-live.
--
-- Usage (MySQL Workbench):
--   USE dreams_creations_db;
--   SOURCE add-shop-module.sql;

USE dreams_creations_db;

SET @OLD_SQL_SAFE_UPDATES = @@SQL_SAFE_UPDATES;
SET SQL_SAFE_UPDATES = 0;

-- ── Phase S1: Shop settings (singleton row) ───────────────────────────────────

CREATE TABLE IF NOT EXISTS shop_settings (
    settings_id         BIGINT       NOT NULL PRIMARY KEY DEFAULT 1,
    store_name          VARCHAR(120) NOT NULL DEFAULT 'Dreams Creations Shop',
    tagline             VARCHAR(255) NULL,
    storefront_enabled  TINYINT(1)   NOT NULL DEFAULT 1 COMMENT 'When false, /store shows closed message',
    allow_guest_browse  TINYINT(1)   NOT NULL DEFAULT 1 COMMENT 'Allow browsing without login',
    updated_at          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT chk_shop_settings_singleton CHECK (settings_id = 1)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO shop_settings (settings_id, store_name, tagline, storefront_enabled, allow_guest_browse)
SELECT 1, 'Dreams Creations Shop', 'Premium suits — order online', 1, 1
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM shop_settings WHERE settings_id = 1);

SET SQL_SAFE_UPDATES = @OLD_SQL_SAFE_UPDATES;
