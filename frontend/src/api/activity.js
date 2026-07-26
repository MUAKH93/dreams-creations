import client from './client'

export const activityAPI = {
  getRecent: () => client.get('/activity-log'),
}
