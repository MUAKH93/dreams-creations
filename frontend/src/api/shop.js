import client from './client'

export const shopAPI = {
  getStatus:       () => client.get('/shop/status'),
  getSettings:     () => client.get('/shop/settings'),
  updateSettings:  (data) => client.put('/shop/settings', data),
  getPublicSettings: () => client.get('/shop/settings/public'),
  getCatalog:      (params) => client.get('/shop/catalog', { params }),
  getDesign:       (designId) => client.get(`/shop/catalog/${designId}`),
}
