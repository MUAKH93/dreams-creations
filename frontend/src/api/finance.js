import client from './client'

export const financeAPI = {
  getStatus: () => client.get('/finance/status'),
}
