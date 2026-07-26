import client from './client'

export const quotationsAPI = {
  getAll:           ()       => client.get('/quotations'),
  getMy:            ()       => client.get('/quotations/my'),
  getById:          (id)     => client.get(`/quotations/${id}`),
  getNextNumber:    ()       => client.get('/quotations/next-number'),
  create:           (data)   => client.post('/quotations', data),
  update:           (id, data) => client.put(`/quotations/${id}`, data),
  submit:           (id)     => client.post(`/quotations/${id}/submit`),
  updateStatus:     (id, status) => client.patch(`/quotations/${id}/status`, { status }),
  convertToBill:    (id)     => client.post(`/quotations/${id}/convert-to-bill`),
}
