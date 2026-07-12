import client from './client'

export const salesAPI = {
  // Customers
  getCustomers:   ()       => client.get('/customers'),
  getMyBalance:   ()       => client.get('/customers/me/balance'),
  getCustomer:    (id)     => client.get(`/customers/${id}`),
  createCustomer: (data)   => client.post('/customers', data),
  updateCustomer: (id, data) => client.put(`/customers/${id}`, data),
  deleteCustomer: (id)     => client.delete(`/customers/${id}`),
  getBalance:     (id)     => client.get(`/customers/${id}/balance`),
  getPaymentReminders: (days = 30) => client.get('/customers/payment-reminders', { params: { days } }),

  // Bills
  getBills:       ()       => client.get('/bills'),
  getMyBills:     ()       => client.get('/bills/my'),
  getBill:        (id)     => client.get(`/bills/${id}`),
  createBill:     (data)   => client.post('/bills', data),
  getNextBillNumber: ()    => client.get('/bills/next-number'),
  getBillsByCustomer: (id) => client.get(`/bills/customer/${id}`),
  getByStatus:    (status) => client.get(`/bills/status/${status}`),
  updateBillStatus: (id, status) => client.patch(`/bills/${id}/status`, { status }),

  // Payments
  getPayments:    ()       => client.get('/payments'),
  getByBill:      (billId) => client.get(`/payments/bill/${billId}`),
  recordPayment:  (data)   => client.post('/payments', data),

  // Products
  getProducts:    ()       => client.get('/products'),
  getProductsWithStock: () => client.get('/products/with-stock'),
  getDesigns:           () => client.get('/designs'),
  getSizes:             () => client.get('/sizes'),
  updateProduct:  (id, data) => client.put(`/products/${id}`, data),
  createProduct:  (data)   => client.post('/products', data),

  // Payment methods
  getPaymentMethods: ()    => client.get('/payment-methods'),

  // Alerts
  getAlerts:      ()       => client.get('/alerts'),
  resolveAlert:   (id)     => client.put(`/alerts/${id}/resolve`),
}
