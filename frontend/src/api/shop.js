import client from './client'

export const shopAPI = {
  getStatus:       () => client.get('/shop/status'),
  getSettings:     () => client.get('/shop/settings'),
  updateSettings:  (data) => client.put('/shop/settings', data),
  getPublicSettings: () => client.get('/shop/settings/public'),
  getCatalog:      (params) => client.get('/shop/catalog', { params }),
  getDesign:       (designId) => client.get(`/shop/catalog/${designId}`),
  getCart:         () => client.get('/shop/cart'),
  addCartItem:     (data) => client.post('/shop/cart/items', data),
  updateCartItem:  (itemId, quantity) => client.put(`/shop/cart/items/${itemId}`, { quantity }),
  removeCartItem:  (itemId) => client.delete(`/shop/cart/items/${itemId}`),
  clearCart:       () => client.delete('/shop/cart'),
  mergeGuestCart:  (items) => client.post('/shop/cart/merge-guest', items),
}
