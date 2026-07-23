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
}
