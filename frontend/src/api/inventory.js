import client from './client'

export const inventoryAPI = {
  getAll:    ()       => client.get('/inventory'),
  getBySuit: (suitId) => client.get(`/inventory/suit/${suitId}`),
  adjustStock: (suitId, data) => client.post(`/inventory/suit/${suitId}/adjust`, data),
  getAdjustments: () => client.get('/inventory/adjustments'),
}
