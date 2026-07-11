import client from './client'

export const dashboardAPI = {
  getSummary: () => client.get('/dashboard/summary'),
}
