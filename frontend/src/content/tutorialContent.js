/**
 * In-app tutorial content for Operations and Finance portals.
 * Used by Help drawers, nav tours, and the /guide page.
 */

export const FINANCE_TUTORIAL = {
  storageKey: 'dc-finance-tutorial-done',
  portalName: 'Finance Portal',
  themeColor: '#0d9488',
  welcomeTitle: 'Welcome to Finance',
  guideTitle: 'Finance module guide',
  intro:
    'The Finance Portal is a separate accounting workspace. It handles ledgers, journals, payables, bank reconciliation, and financial reports — without mixing in factory production screens.',
  sections: [
    {
      id: 'overview',
      title: 'Overview & two portals',
      path: '/finance',
      summary: 'Finance is separate from daily operations but shares the same database.',
      details: [
        'Operations = production, sales, inventory, customers.',
        'Finance = chart of accounts, journals, payables, bank rec, P&L, balance sheet.',
        'Use the green “Open Finance Portal” button in operations, or “Back to Operations” to return.',
      ],
      tips: ['Only Admin and Manager roles can access Finance.'],
    },
    {
      id: 'accounts',
      title: 'Chart of Accounts',
      path: '/finance/accounts',
      summary: 'Every transaction posts to an account code (Asset, Liability, Equity, Income, Expense).',
      details: [
        'Pre-seeded accounts: 1000 Cash, 1100 AR, 1200 Inventory, 2000 AP, 4000 Revenue, 5000 COGS.',
        'System accounts cannot be deleted; add custom accounts for your business.',
        'Run add-finance-module.sql once on MySQL before first use.',
      ],
      tips: ['Start here on first setup to confirm account codes match your accountant’s chart.'],
    },
    {
      id: 'journals',
      title: 'Journal Entries',
      path: '/finance/journals',
      summary: 'Double-entry bookkeeping — total debits must equal total credits.',
      details: [
        'Manual entries: opening balances, adjustments, corrections.',
        'Auto-posted entries (when enabled): customer bills, payments, payables, COGS.',
        'Example opening: Dr Cash 1000 / Cr Owner’s Equity 3000.',
      ],
      tips: ['Click any entry to view debit/credit lines and source type (manual, bill, payment, etc.).'],
    },
    {
      id: 'payables',
      title: 'Accounts Payable',
      path: '/finance/payables',
      summary: 'Vendor bills and payments with automatic AP journals.',
      details: [
        'Create vendors, then record supplier invoices with an expense account.',
        'On save: Dr Expense / Cr Accounts Payable (2000).',
        'On payment: Dr AP / Cr Cash (1000).',
        'AP Aging tab compares open payables to ledger AP balance.',
      ],
      tips: ['Run add-finance-payables.sql on MySQL before using payables.'],
    },
    {
      id: 'bank',
      title: 'Bank Reconciliation',
      path: '/finance/bank',
      summary: 'Match bank statement lines to ledger cash.',
      details: [
        'Link a bank account to GL cash account (1000).',
        'Enter statement lines: positive = deposit, negative = withdrawal.',
        'Run reconciliation to compare statement balance vs ledger balance.',
        'Mark lines Reconciled when matched to book entries.',
      ],
      tips: ['Run add-finance-bank.sql on MySQL before using bank reconciliation.'],
    },
    {
      id: 'reports',
      title: 'Financial Reports',
      path: '/finance/reports',
      summary: 'Trial balance, GL, AR aging, inventory valuation, P&L, balance sheet.',
      details: [
        'Trial Balance — verify debits equal credits.',
        'General Ledger — running balance for any account.',
        'AR Aging — unpaid customer bills by age bucket.',
        'P&L — income minus expenses for a date range.',
        'Balance Sheet — assets vs liabilities + equity as of a date.',
      ],
      tips: ['Use date filters on P&L and balance sheet for month-end close.'],
    },
  ],
  postingTable: [
    { event: 'Customer bill (auto AR)', debit: '1100 AR', credit: '4000 Revenue' },
    { event: 'Customer payment', debit: '1000 Cash', credit: '1100 AR' },
    { event: 'Vendor invoice', debit: 'Expense account', credit: '2000 AP' },
    { event: 'Vendor payment', debit: '2000 AP', credit: '1000 Cash' },
  ],
}

export const OPERATIONS_TUTORIAL_MANAGER = {
  storageKey: 'dc-ops-tutorial-done-manager',
  portalName: 'Operations Portal',
  themeColor: '#1a237e',
  welcomeTitle: 'Welcome to Operations',
  guideTitle: 'Operations module guide',
  intro:
    'The Operations Portal runs your factory day-to-day: production batches, dispatch, inventory, sales, customers, and operational reports. Managers and Admins also open the Finance Portal for accounting.',
  sections: [
    {
      id: 'dashboard',
      title: 'Dashboard',
      path: '/dashboard',
      summary: 'Role-specific home with KPIs and quick links.',
      details: [
        'Review production status, sales summary, and alerts at a glance.',
        'Use the sidebar to jump to any module.',
      ],
      tips: ['Check Alerts regularly for low stock and overdue items.'],
    },
    {
      id: 'batches',
      title: 'Production Batches',
      path: '/batches',
      summary: 'Create and track production runs from cutting through packing.',
      details: [
        'Create a batch linked to a design and quantity.',
        'Move batches through stages: Cutting → Stitching → Press → Packing.',
        'Completed packing adds finished goods to inventory.',
      ],
      tips: ['Supervisors complete stage work from My Assignments.'],
    },
    {
      id: 'dispatch',
      title: 'Dispatch Management',
      path: '/dispatch',
      summary: 'Ship finished orders and track outbound delivery.',
      details: [
        'View orders ready for dispatch.',
        'Record dispatch date and carrier details.',
      ],
      tips: ['Ensure inventory is available before dispatching.'],
    },
    {
      id: 'inventory',
      title: 'Inventory',
      path: '/inventory',
      summary: 'Stock levels by design, size, and color.',
      details: [
        'Quantities update when production completes and when bills sell stock.',
        'Finance inventory valuation report compares stock at cost to ledger 1200.',
      ],
      tips: ['Set production cost on each design for accurate valuation.'],
    },
    {
      id: 'designs',
      title: 'Designs Catalog',
      path: '/designs',
      summary: 'Product designs with images, sizes, colors, and pricing.',
      details: [
        'Add designs with production cost for COGS posting.',
        'Customers browse the same catalog in their portal.',
      ],
      tips: ['Production cost feeds finance COGS when auto-post inventory is on.'],
    },
    {
      id: 'customers',
      title: 'Customers',
      path: '/customers',
      summary: 'Customer records, balances, and contact details.',
      details: [
        'Register walk-in and account customers.',
        'Customer balance tracks operational credit; AR aging tracks unpaid bills in Finance.',
      ],
      tips: ['Link every bill to a customer for clean AR reporting.'],
    },
    {
      id: 'quotations',
      title: 'Quotations',
      path: '/quotations',
      summary: 'Price quotes before converting to bills.',
      details: [
        'Create quotes with line items from the catalog.',
        'Convert approved quotes to bills in one step.',
      ],
      tips: ['Customers see quotes under My Quotes in their portal.'],
    },
    {
      id: 'bills',
      title: 'Bills & Payments',
      path: '/bills',
      summary: 'Sales invoices, discounts, and customer payments.',
      details: [
        'Create bills with line items; apply discounts.',
        'Record payments (cash, bank, cheque) against bills.',
        'When finance auto-post AR is enabled, bills and payments create journal entries.',
      ],
      tips: ['Void bills carefully — finance may post reversal entries if already posted.'],
    },
    {
      id: 'reports',
      title: 'Operations Reports',
      path: '/reports',
      summary: 'Factory and sales reports (not the same as Finance P&L).',
      details: [
        'Production, sales, and inventory operational reports.',
        'Use Finance Portal for trial balance, P&L, and balance sheet.',
      ],
      tips: ['Operational reports = factory metrics; Finance reports = accounting.'],
    },
    {
      id: 'analytics',
      title: 'Analytics',
      path: '/analytics',
      summary: 'Trends and charts for management decisions.',
      details: ['Sales trends, production throughput, and related KPIs.'],
      tips: [],
    },
    {
      id: 'finance-portal',
      title: 'Finance Portal',
      path: '/finance',
      summary: 'Separate accounting workspace (sidebar green button).',
      details: [
        'Chart of accounts, journals, payables, bank reconciliation, financial statements.',
        'Click Help in Finance header anytime to replay the finance tutorial.',
      ],
      tips: ['Complete SQL migrations before first finance use.'],
    },
  ],
}

export const OPERATIONS_TUTORIAL_ADMIN = {
  ...OPERATIONS_TUTORIAL_MANAGER,
  storageKey: 'dc-ops-tutorial-done-admin',
  sections: [
    ...OPERATIONS_TUTORIAL_MANAGER.sections,
    {
      id: 'staff',
      title: 'Staff & Supervisors',
      path: '/staff',
      summary: 'Admin-only: manage user accounts and roles.',
      details: ['Create supervisor and manager logins.', 'Reset access and assign roles.'],
      tips: [],
    },
    {
      id: 'setup',
      title: 'Factory Setup',
      path: '/setup',
      summary: 'Admin-only: sizes, payment methods, packing defaults.',
      details: ['Configure factory master data used across production and sales.'],
      tips: [],
    },
  ],
}

export const OPERATIONS_TUTORIAL_SUPERVISOR = {
  storageKey: 'dc-ops-tutorial-done-supervisor',
  portalName: 'Supervisor Portal',
  themeColor: '#1a237e',
  welcomeTitle: 'Welcome, Supervisor',
  guideTitle: 'Supervisor guide',
  intro: 'Supervisors focus on production assignments and the designs catalog.',
  sections: [
    {
      id: 'dashboard',
      title: 'Dashboard',
      path: '/dashboard',
      summary: 'Your home page with assignment summary.',
      details: ['See tasks assigned to you and factory updates.'],
      tips: [],
    },
    {
      id: 'assignments',
      title: 'My Assignments',
      path: '/assignments',
      summary: 'Complete production stage work on assigned batches.',
      details: [
        'Open an assignment, record quantities completed.',
        'Final stage completion updates inventory.',
      ],
      tips: ['Mark work complete promptly so managers see accurate batch status.'],
    },
    {
      id: 'designs',
      title: 'Designs Catalog',
      path: '/designs',
      summary: 'Browse product designs (read-only for supervisors).',
      details: ['Reference design codes and specs while working batches.'],
      tips: [],
    },
  ],
}

export const OPERATIONS_TUTORIAL_CUSTOMER = {
  storageKey: 'dc-ops-tutorial-done-customer',
  portalName: 'Customer Portal',
  themeColor: '#1a237e',
  welcomeTitle: 'Welcome',
  guideTitle: 'Customer portal guide',
  intro: 'Browse designs, request quotes, and view your bills and orders.',
  sections: [
    {
      id: 'dashboard',
      title: 'Dashboard',
      path: '/dashboard',
      summary: 'Your account home.',
      details: ['Quick overview of recent quotes and orders.'],
      tips: [],
    },
    {
      id: 'designs',
      title: 'Designs Catalog',
      path: '/designs',
      summary: 'Browse available products.',
      details: ['View designs, sizes, and colors before requesting a quote.'],
      tips: [],
    },
    {
      id: 'quotes',
      title: 'My Quotes',
      path: '/my-quotes',
      summary: 'Quotations sent to you by the factory.',
      details: ['Review pricing and status of your quote requests.'],
      tips: [],
    },
    {
      id: 'orders',
      title: 'My Bills',
      path: '/my-orders',
      summary: 'Your invoices and payment status.',
      details: ['View bill details and amounts due.'],
      tips: ['Contact the factory for payment instructions.'],
    },
  ],
}

export function getOperationsTutorialForRole(role) {
  if (role === 'ADMIN') return OPERATIONS_TUTORIAL_ADMIN
  if (role === 'MANAGER') return OPERATIONS_TUTORIAL_MANAGER
  if (role === 'SUPERVISOR') return OPERATIONS_TUTORIAL_SUPERVISOR
  if (role === 'CUSTOMER') return OPERATIONS_TUTORIAL_CUSTOMER
  return OPERATIONS_TUTORIAL_MANAGER
}
