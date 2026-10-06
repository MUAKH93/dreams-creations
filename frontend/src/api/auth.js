import client from './client'

export const authAPI = {
  listTenants: () => client.get('/auth/tenants'),
  login:    (data) => client.post('/auth/login', data),
  register: (data) => client.post('/auth/register', data),
  verifyEmail: (token, tenant) => client.get('/auth/verify-email', { params: { token, tenant } }),
  validateResetToken: (token, tenant) => client.get('/auth/validate-reset-token', { params: { token, tenant } }),
  resendVerification: (email) => client.post('/auth/resend-verification', { email }),
  forgotPassword: (email) => client.post('/auth/forgot-password', { email }),
  resetPassword: (data) => client.post('/auth/reset-password', data),
}
