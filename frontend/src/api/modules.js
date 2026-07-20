import client from './client'

export const modulesAPI = {
  getFlags: () => client.get('/modules'),
}
