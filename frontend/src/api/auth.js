import client from './client'

export const authAPI = {
  login:    (data) => client.post('/auth/login', data),
  register: (data) => client.post('/auth/register', data),
  forgotPassword: (email) => client.post('/auth/forgot-password', { email }),
  validateResetToken: (token) => client.get('/auth/validate-reset-token', { params: { token } }),
  resetPassword: (data) => client.post('/auth/reset-password', data),
}
