import client from './client'

export const financeAPI = {
  getStatus: () => client.get('/finance/status'),

  getAccounts: (activeOnly = false) =>
    client.get('/finance/accounts', { params: { activeOnly } }),
  getAccount: (id) => client.get(`/finance/accounts/${id}`),
  createAccount: (data) => client.post('/finance/accounts', data),
  updateAccount: (id, data) => client.put(`/finance/accounts/${id}`, data),
  deleteAccount: (id) => client.delete(`/finance/accounts/${id}`),

  getJournals: () => client.get('/finance/journals'),
  getJournal: (id) => client.get(`/finance/journals/${id}`),
  createJournal: (data) => client.post('/finance/journals', data),

  getTrialBalance: (params = {}) =>
    client.get('/finance/reports/trial-balance', { params }),
  getGeneralLedger: (params) =>
    client.get('/finance/reports/general-ledger', { params }),
  getArAging: () => client.get('/finance/reports/ar-aging'),
  getArReconciliation: () => client.get('/finance/reports/ar-reconciliation'),
  getInventoryValuation: () => client.get('/finance/reports/inventory-valuation'),
  getProfitLoss: (params) => client.get('/finance/reports/profit-loss', { params }),
  getBalanceSheet: (params) => client.get('/finance/reports/balance-sheet', { params }),
  getApAging: () => client.get('/finance/reports/ap-aging'),

  getVendors: (activeOnly = true) =>
    client.get('/finance/vendors', { params: { activeOnly } }),
  createVendor: (data) => client.post('/finance/vendors', data),
  updateVendor: (id, data) => client.put(`/finance/vendors/${id}`, data),
  deactivateVendor: (id) => client.delete(`/finance/vendors/${id}`),

  getPayables: () => client.get('/finance/payables'),
  createPayable: (data) => client.post('/finance/payables', data),
  recordPayablePayment: (id, data) => client.post(`/finance/payables/${id}/payments`, data),

  getBankAccounts: (activeOnly = true) =>
    client.get('/finance/bank/accounts', { params: { activeOnly } }),
  createBankAccount: (data) => client.post('/finance/bank/accounts', data),
  getBankTransactions: (bankAccountId) =>
    client.get(`/finance/bank/accounts/${bankAccountId}/transactions`),
  createBankTransaction: (data) => client.post('/finance/bank/transactions', data),
  reconcileBankTransaction: (id, data) =>
    client.post(`/finance/bank/transactions/${id}/reconcile`, data),
  getBankReconciliation: (bankAccountId, params) =>
    client.get(`/finance/bank/accounts/${bankAccountId}/reconciliation`, { params }),
}
