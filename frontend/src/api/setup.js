import client from './client'

export const setupAPI = {
  // Sizes
  getSizes:       ()       => client.get('/sizes'),
  createSize:     (data)   => client.post('/sizes', data),

  // Payment methods
  getPaymentMethods: ()    => client.get('/payment-methods'),
  createPaymentMethod: (d) => client.post('/payment-methods', d),

  // Designing / Filling work types
  getDesigningWorkTypes: () => client.get('/designing-work-types'),
  createDesigningWorkType: (d) => client.post('/designing-work-types', d),
  getFillingWorkTypes: () => client.get('/filling-work-types'),
  createFillingWorkType: (d) => client.post('/filling-work-types', d),

  // Supervisor ↔ module assignments
  getSupervisorModules: () => client.get('/supervisor-modules'),
  assignModule: (supervisorId, moduleId) =>
    client.post('/supervisor-modules', { supervisorId, moduleId }),
  unassignModule: (id) => client.delete(`/supervisor-modules/${id}`),
}
