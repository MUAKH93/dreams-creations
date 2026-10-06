import {
  ToolOutlined,
  DollarOutlined,
  ArrowLeftOutlined,
} from '@ant-design/icons'
import { ROLES } from '../utils/roles'

/** Production ERP modules — shop is a separate project, not listed here. */
export const ERP_MODULES = [
  {
    key: 'production',
    title: 'Production & Operations',
    description: 'Batches, dispatch, inventory, sales, customers, and factory setup.',
    route: '/dashboard',
    icon: ToolOutlined,
    color: '#1a237e',
    accent: '#eff6ff',
    roles: [ROLES.ADMIN, ROLES.MANAGER],
  },
  {
    key: 'finance',
    title: 'Finance Portal',
    description: 'Chart of accounts, journals, payables, bank reconciliation, and reports.',
    route: '/finance',
    icon: DollarOutlined,
    color: '#0d9488',
    accent: '#ecfdf5',
    roles: [ROLES.ADMIN, ROLES.MANAGER],
    requiresFinance: true,
  },
]

export function getErpModules(role, { showFinance = false } = {}) {
  return ERP_MODULES.filter(m => {
    if (!m.roles.includes(role)) return false
    if (m.requiresFinance && !showFinance) return false
    return true
  })
}

export { ArrowLeftOutlined }
