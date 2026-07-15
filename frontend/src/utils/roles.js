export const ROLES = {
  ADMIN: 'ADMIN',
  MANAGER: 'MANAGER',
  SUPERVISOR: 'SUPERVISOR',
  CUSTOMER: 'CUSTOMER',
}

/** Admin and Manager share the management portal (Manager has no separate UI). */
export const MANAGEMENT_ROLES = [ROLES.ADMIN, ROLES.MANAGER]

export const PORTALS = {
  management: {
    key: 'management',
    title: 'Management Login',
    subtitle: 'Admin & Manager — factory operations',
    roles: MANAGEMENT_ROLES,
    home: '/dashboard',
    loginPath: '/login',
  },
  supervisor: {
    key: 'supervisor',
    title: 'Supervisor Login',
    subtitle: 'Production assignments & returns',
    roles: [ROLES.SUPERVISOR],
    home: '/dashboard',
    loginPath: '/login',
  },
  customer: {
    key: 'customer',
    title: 'Customer Login',
    subtitle: 'Browse designs & track your orders',
    roles: [ROLES.CUSTOMER],
    home: '/dashboard',
    loginPath: '/login',
  },
}

export function portalForRole(role) {
  if (MANAGEMENT_ROLES.includes(role)) return PORTALS.management
  if (role === ROLES.SUPERVISOR) return PORTALS.supervisor
  if (role === ROLES.CUSTOMER) return PORTALS.customer
  return null
}

export function homeForRole(role) {
  return portalForRole(role)?.home || '/dashboard'
}

export function portalLabel(role) {
  if (role === ROLES.ADMIN) return 'Admin Portal'
  if (role === ROLES.MANAGER) return 'Management System'
  if (role === ROLES.SUPERVISOR) return 'Supervisor Portal'
  if (role === ROLES.CUSTOMER) return 'Customer Portal'
  return 'Dreams Creations'
}

export function roleMatchesPortal(role, portalKey) {
  const portal = PORTALS[portalKey]
  return portal ? portal.roles.includes(role) : false
}
