import client from './client'

export const profileAPI = {
  getMy:       () => client.get('/profile/me'),
  updateMy:    (data) => client.put('/profile/me', data),
  uploadPhoto: (file) => {
    const form = new FormData()
    form.append('file', file)
    return client.post('/profile/me/photo', form)
  },
}
