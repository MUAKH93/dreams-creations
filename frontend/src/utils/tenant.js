const STORAGE_KEY = 'tenantId'
const DEFAULT_TENANT = 'default'

export function getStoredTenantId() {
  return localStorage.getItem(STORAGE_KEY) || DEFAULT_TENANT
}

export function setStoredTenantId(tenantId) {
  if (!tenantId) {
    localStorage.removeItem(STORAGE_KEY)
    return
  }
  localStorage.setItem(STORAGE_KEY, tenantId)
}

export function clearStoredTenantId() {
  localStorage.removeItem(STORAGE_KEY)
}

export const TENANT_HEADER = 'X-Tenant-ID'
