import client from './client'

export const adminAPI = {
  getSupervisorAccounts: () => client.get('/admin/supervisor-accounts'),
  createSupervisorAccount: (data) => client.post('/admin/supervisor-accounts', data),
  createSupervisorLogin: (supervisorId, data) =>
    client.post(`/admin/supervisor-accounts/${supervisorId}/login`, data),
}
