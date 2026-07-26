/**
 * Frontend module flags. Must align with backend application.properties on each environment.
 *
 * Copy .env.example to .env and set VITE_FINANCE_MODULE_ENABLED=true on feature/finance-v2.
 */

export const financeModuleEnabled =
  import.meta.env.VITE_FINANCE_MODULE_ENABLED === 'true'

export const shopModuleEnabled =
  import.meta.env.VITE_SHOP_MODULE_ENABLED === 'true'
