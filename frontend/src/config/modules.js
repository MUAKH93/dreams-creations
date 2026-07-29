/**
 * Frontend module flags. Must align with backend application.properties on each environment.
 *
 * Copy .env.example to .env and set flags per branch:
 *   feature/finance-v2 → VITE_FINANCE_MODULE_ENABLED=true
 *   feature/shop-v1    → VITE_SHOP_MODULE_ENABLED=true (and finance if needed)
 */

export const financeModuleEnabled =
  import.meta.env.VITE_FINANCE_MODULE_ENABLED === 'true'

export const shopModuleEnabled =
  import.meta.env.VITE_SHOP_MODULE_ENABLED === 'true'
