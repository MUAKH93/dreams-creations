import client from './client'

export const analyticsAPI = {
  getDashboard: (months = 6) => client.get('/analytics/dashboard', { params: { months } }),
}
