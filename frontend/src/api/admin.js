import client from './client'

export const adminAPI = {
  // Supervisors
  getSupervisorAccounts: () => client.get('/admin/supervisor-accounts'),
  createSupervisorAccount: (data) => client.post('/admin/supervisor-accounts', data),
  createSupervisorLogin: (supervisorId, data) =>
    client.post(`/admin/supervisor-accounts/${supervisorId}/login`, data),
  updateSupervisorAccount: (supervisorId, data) =>
    client.put(`/admin/supervisor-accounts/${supervisorId}`, data),
  updateSupervisorLogin: (supervisorId, data) =>
    client.put(`/admin/supervisor-accounts/${supervisorId}/login`, data),
  deleteSupervisorAccount: (supervisorId) =>
    client.delete(`/admin/supervisor-accounts/${supervisorId}`),

  // Managers
  getManagerAccounts: () => client.get('/admin/manager-accounts'),
  createManagerAccount: (data) => client.post('/admin/manager-accounts', data),
  updateManagerAccount: (userId, data) => client.put(`/admin/manager-accounts/${userId}`, data),
  deleteManagerAccount: (userId) => client.delete(`/admin/manager-accounts/${userId}`),

  getProductionSettings: () => client.get('/admin/production-settings'),
  updateProductionSettings: (data) => client.put('/admin/production-settings', data),
  resetStaffPassword: (userId, data) => client.post(`/admin/staff/${userId}/reset-password`, data),
}
