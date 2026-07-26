import { useEffect, useState } from 'react'
import {
  Table, Button, Modal, Form, Input, InputNumber, Tag, Typography, Space, message,
  Statistic, Card, Row, Col, Alert, Select, Popconfirm, Divider
} from 'antd'
import { PlusOutlined, WalletOutlined, EditOutlined, DeleteOutlined, DollarOutlined } from '@ant-design/icons'
import { salesAPI } from '../../api/sales'
import { apiErrorMessage } from '../../api/client'

const { Title, Text } = Typography

const COUNTRY_CODES = ['+92', '+971', '+1', '+44']

function splitPhone(phone) {
  if (!phone) return { countryCode: '+92', phoneNumber: '' }
  const match = COUNTRY_CODES.find(code => phone.startsWith(code))
  if (match) {
    return { countryCode: match, phoneNumber: phone.slice(match.length) }
  }
  return { countryCode: '+92', phoneNumber: phone.replace(/\D/g, '') }
}

export default function CustomersPage() {
  const [customers, setCustomers] = useState([])
  const [reminders, setReminders] = useState([])
  const [loading, setLoading] = useState(true)
  const [loadError, setLoadError] = useState(null)
  const [modalOpen, setModalOpen] = useState(false)
  const [editingId, setEditingId] = useState(null)
  const [accountOpen, setAccountOpen] = useState(false)
  const [payOpen, setPayOpen] = useState(false)
  const [selectedCustomer, setSelectedCustomer] = useState(null)
  const [balance, setBalance] = useState(null)
  const [payments, setPayments] = useState([])
  const [customerBills, setCustomerBills] = useState([])
  const [paymentMethods, setPaymentMethods] = useState([])
  const [form] = Form.useForm()
  const [payForm] = Form.useForm()

  const load = () => {
    setLoading(true)
    setLoadError(null)
    Promise.allSettled([
      salesAPI.getCustomers(),
      salesAPI.getPaymentReminders(30),
    ]).then(([cust, rem]) => {
      if (cust.status === 'fulfilled') setCustomers(cust.value.data)
      else {
        setLoadError(apiErrorMessage(cust.reason))
        message.error(apiErrorMessage(cust.reason))
      }
      if (rem.status === 'fulfilled') setReminders(rem.value.data)
    }).finally(() => setLoading(false))
  }

  useEffect(() => { load() }, [])

  const openAdd = () => {
    setEditingId(null)
    form.resetFields()
    form.setFieldsValue({ countryCode: '+92' })
    setModalOpen(true)
  }

  const openEdit = (record) => {
    const { countryCode, phoneNumber } = splitPhone(record.phone)
    setEditingId(record.customerId)
    form.setFieldsValue({
      firstName: record.firstName,
      lastName: record.lastName,
      countryCode,
      phoneNumber,
      email: record.email,
      city: record.city,
      address: record.address,
      status: record.status || 'active',
      discountPercent: record.discountPercent || 0,
    })
    setModalOpen(true)
  }

  const openCustomerAccount = async (customer) => {
    setSelectedCustomer(customer)
    try {
      const [balRes, payRes, billsRes, pmRes] = await Promise.all([
        salesAPI.getBalance(customer.customerId),
        salesAPI.getByCustomer(customer.customerId),
        salesAPI.getBillsByCustomer(customer.customerId),
        salesAPI.getPaymentMethods(),
      ])
      setBalance(balRes.data)
      setPayments(payRes.data)
      setCustomerBills(billsRes.data)
      setPaymentMethods(pmRes.data)
      setAccountOpen(true)
    } catch {
      message.error('Could not load customer account')
    }
  }

  const openRecordPayment = (bill) => {
    setPayOpen(true)
    payForm.resetFields()
    payForm.setFieldsValue({ billId: bill.billId })
  }

  const onRecordPayment = async (values) => {
    try {
      await salesAPI.recordPayment({
        bill: { billId: values.billId },
        paymentMethod: { paymentMethodId: values.paymentMethodId },
        amount: values.amount,
        notes: values.notes,
        referenceNo: values.referenceNo,
      })
      message.success('Payment recorded — bill closed')
      setPayOpen(false)
      payForm.resetFields()
      if (selectedCustomer) openCustomerAccount(selectedCustomer)
      load()
    } catch (err) {
      message.error(err.response?.data?.message || 'Payment failed')
    }
  }

  const handleDelete = async (customerId) => {
    try {
      await salesAPI.deleteCustomer(customerId)
      message.success('Customer deleted')
      load()
    } catch (err) {
      message.error(err.response?.data?.message || 'Failed to delete customer')
    }
  }

  const onFinish = async (values) => {
    try {
      const phone = values.countryCode && values.phoneNumber
        ? `${values.countryCode}${values.phoneNumber}`.replace(/\s+/g, '')
        : values.phoneNumber || null

      const payload = {
        firstName: values.firstName,
        lastName: values.lastName || null,
        phone,
        email: values.email || null,
        city: values.city || null,
        address: values.address || null,
        status: values.status || 'active',
        discountPercent: values.discountPercent || 0,
      }

      if (editingId) {
        await salesAPI.updateCustomer(editingId, payload)
        message.success('Customer updated')
      } else {
        await salesAPI.createCustomer(payload)
        message.success('Customer created')
      }
      setModalOpen(false)
      form.resetFields()
      setEditingId(null)
      load()
    } catch (err) {
      message.error(err.response?.data?.message || 'Failed to save customer')
    }
  }

  const unpaidBills = customerBills.filter(b => b.status === 'unpaid')

  const columns = [
    { title: 'Name', key: 'name',
      render: (_, r) => `${r.firstName} ${r.lastName || ''}`.trim() },
    { title: 'Phone', dataIndex: 'phone', key: 'phone', render: p => p || '—' },
    { title: 'Email', dataIndex: 'email', key: 'email', render: e => e || '—' },
    { title: 'City', dataIndex: 'city', key: 'city', render: c => c || '—' },
    { title: 'Discount', dataIndex: 'discountPercent', key: 'discount',
      render: v => (v > 0 ? <Tag color="blue">{v}%</Tag> : '—') },
    { title: 'Status', dataIndex: 'status', key: 'status',
      render: s => (
        <Tag color={s === 'active' ? 'green' : 'red'}>{s?.toUpperCase() || 'ACTIVE'}</Tag>
      ) },
    { title: 'Actions', key: 'actions',
      render: (_, r) => (
        <Space>
          <Button icon={<WalletOutlined />} size="small" onClick={() => openCustomerAccount(r)}>
            Records
          </Button>
          <Button icon={<EditOutlined />} size="small" onClick={() => openEdit(r)}>
            Edit
          </Button>
          <Popconfirm
            title="Delete this customer?"
            description="Customers with bills cannot be deleted."
            onConfirm={() => handleDelete(r.customerId)}
            okText="Delete"
            okButtonProps={{ danger: true }}
          >
            <Button icon={<DeleteOutlined />} size="small" danger />
          </Popconfirm>
        </Space>
      ) },
  ]

  const reminderColumns = [
    { title: 'Customer', dataIndex: 'customerName', key: 'name' },
    { title: 'Phone', dataIndex: 'phone', key: 'phone', render: p => p || '—' },
    { title: 'Balance Due', dataIndex: 'balanceDue', key: 'balance',
      render: (v) => <Text type="danger">Rs. {Number(v || 0).toLocaleString()}</Text> },
    { title: 'Overdue Bills', dataIndex: 'overdueBillCount', key: 'bills' },
    { title: 'Days Overdue', dataIndex: 'daysOverdue', key: 'days',
      render: (d) => <Tag color="red">{d} days</Tag> },
  ]

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 24 }}>
        <Title level={4} className="page-title" style={{ margin: 0 }}>Customers</Title>
        <Button type="primary" icon={<PlusOutlined />} onClick={openAdd}>
          Add Customer
        </Button>
      </div>

      {loadError && (
        <Alert type="error" message={loadError} style={{ marginBottom: 16 }} showIcon />
      )}

      <Alert type="info" showIcon style={{ marginBottom: 16 }}
        message="Customer account model"
        description="Bills show invoice totals only. All payments are recorded here. Partial payment closes a bill; remaining amount stays as customer balance due." />

      {reminders.length > 0 && (
        <Card title="Payment Reminders (30+ days overdue)" style={{ marginBottom: 24 }}>
          <Table
            dataSource={reminders}
            columns={reminderColumns}
            rowKey="customerId"
            pagination={false}
            size="small"
          />
        </Card>
      )}

      <Table
        dataSource={customers}
        columns={columns}
        rowKey="customerId"
        loading={loading}
        locale={{ emptyText: loading ? 'Loading...' : 'No customers yet — click Add Customer' }}
      />

      {/* Add/Edit customer modal — unchanged structure */}
      <Modal
        title={editingId ? 'Edit Customer' : 'Add Customer'}
        open={modalOpen}
        onCancel={() => { setModalOpen(false); setEditingId(null) }}
        footer={null}
        width={480}
      >
        <Form form={form} onFinish={onFinish} layout="vertical" initialValues={{ countryCode: '+92', status: 'active' }}>
          <Form.Item name="firstName" label="First Name" rules={[{ required: true, message: 'First name is required' }]}>
            <Input placeholder="Ahmed" />
          </Form.Item>
          <Form.Item name="lastName" label="Last Name">
            <Input placeholder="Raza" />
          </Form.Item>
          <Form.Item label="Phone Number">
            <Space.Compact style={{ width: '100%' }}>
              <Form.Item name="countryCode" noStyle rules={[{ required: true }]}>
                <Select style={{ width: 100 }}>
                  <Select.Option value="+92">+92 PK</Select.Option>
                  <Select.Option value="+971">+971 UAE</Select.Option>
                  <Select.Option value="+1">+1 US</Select.Option>
                  <Select.Option value="+44">+44 UK</Select.Option>
                </Select>
              </Form.Item>
              <Form.Item name="phoneNumber" noStyle
                rules={[
                  { required: true, message: 'Phone number is required' },
                  { pattern: /^[0-9]{7,15}$/, message: 'Enter digits only (7–15)' },
                ]}>
                <Input placeholder="3211234567" style={{ width: 'calc(100% - 100px)' }} />
              </Form.Item>
            </Space.Compact>
          </Form.Item>
          <Form.Item name="email" label="Email" rules={[{ type: 'email', message: 'Invalid email' }]}>
            <Input placeholder="customer@email.com" />
          </Form.Item>
          <Form.Item name="city" label="City">
            <Input placeholder="Lahore" />
          </Form.Item>
          <Form.Item name="address" label="Address">
            <Input.TextArea rows={2} placeholder="Optional address" />
          </Form.Item>
          <Form.Item name="discountPercent" label="Standing Discount %">
            <InputNumber min={0} max={100} step={0.5} style={{ width: '100%' }} addonAfter="%" />
          </Form.Item>
          {editingId && (
            <Form.Item name="status" label="Status">
              <Select>
                <Select.Option value="active">Active</Select.Option>
                <Select.Option value="inactive">Inactive</Select.Option>
              </Select>
            </Form.Item>
          )}
          <Form.Item>
            <Space>
              <Button type="primary" htmlType="submit">{editingId ? 'Update' : 'Save'} Customer</Button>
              <Button onClick={() => { setModalOpen(false); setEditingId(null) }}>Cancel</Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title={selectedCustomer ? `Customer Records — ${selectedCustomer.firstName} ${selectedCustomer.lastName || ''}` : 'Customer Records'}
        open={accountOpen}
        onCancel={() => { setAccountOpen(false); setSelectedCustomer(null) }}
        footer={null}
        width={900}
      >
        {balance && (
          <>
            <Row gutter={16} style={{ marginBottom: 16 }}>
              <Col span={8}>
                <Card size="small"><Statistic title="Total Sales" value={balance.totalSales} prefix="Rs." precision={2} /></Card>
              </Col>
              <Col span={8}>
                <Card size="small"><Statistic title="Total Paid" value={balance.totalPaid} prefix="Rs." precision={2} valueStyle={{ color: '#3f8600' }} /></Card>
              </Col>
              <Col span={8}>
                <Card size="small"><Statistic title="Balance Due" value={balance.balance} prefix="Rs." precision={2}
                  valueStyle={{ color: balance.balance > 0 ? '#cf1322' : '#3f8600' }} /></Card>
              </Col>
            </Row>

            <Divider orientation="left">Open Bills (unpaid)</Divider>
            <Table
              dataSource={unpaidBills}
              rowKey="billId"
              size="small"
              pagination={false}
              locale={{ emptyText: 'No open bills' }}
              columns={[
                { title: 'Bill #', dataIndex: 'billNumber' },
                { title: 'Bill Total', dataIndex: 'finalAmount',
                  render: v => `Rs. ${Number(v).toLocaleString()}` },
                { title: 'Grand Total', key: 'grand',
                  render: (_, r) => `Rs. ${Number(r.grandTotal || r.finalAmount).toLocaleString()}` },
                { title: '', key: 'act',
                  render: (_, r) => (
                    <Button size="small" type="primary" icon={<DollarOutlined />}
                      onClick={() => openRecordPayment(r)}>Receive Payment</Button>
                  ) },
              ]}
              style={{ marginBottom: 16 }}
            />

            <Divider orientation="left">Payment History</Divider>
            <Table
              dataSource={payments}
              rowKey="paymentId"
              size="small"
              pagination={{ pageSize: 8 }}
              locale={{ emptyText: 'No payments recorded yet' }}
              columns={[
                { title: 'Date', dataIndex: 'paymentDate',
                  render: d => d ? new Date(d).toLocaleString() : '—' },
                { title: 'Bill #', key: 'bill',
                  render: (_, r) => r.bill?.billNumber || '—' },
                { title: 'Amount', dataIndex: 'amount',
                  render: v => <Text strong>Rs. {Number(v).toLocaleString()}</Text> },
                { title: 'Method', key: 'method',
                  render: (_, r) => r.paymentMethod?.methodName || '—' },
                { title: 'Reference', dataIndex: 'referenceNo', render: v => v || '—' },
                { title: 'Notes', dataIndex: 'notes', render: v => v || '—' },
              ]}
            />
          </>
        )}
      </Modal>

      <Modal title="Receive Payment" open={payOpen} onCancel={() => setPayOpen(false)} footer={null} width={480}>
        <Alert type="info" showIcon style={{ marginBottom: 16 }}
          message="Partial payment is allowed"
          description="Bill closes on any payment. Remaining due stays on customer balance." />
        <Form form={payForm} onFinish={onRecordPayment} layout="vertical">
          <Form.Item name="billId" label="Bill" rules={[{ required: true }]}>
            <Select placeholder="Select open bill">
              {unpaidBills.map(b => (
                <Select.Option key={b.billId} value={b.billId}>
                  {b.billNumber} — Grand Rs. {Number(b.grandTotal || b.finalAmount).toLocaleString()}
                </Select.Option>
              ))}
            </Select>
          </Form.Item>
          <Form.Item name="paymentMethodId" label="Payment Method" rules={[{ required: true }]}>
            <Select placeholder="Select method">
              {paymentMethods.map(pm => (
                <Select.Option key={pm.paymentMethodId} value={pm.paymentMethodId}>{pm.methodName}</Select.Option>
              ))}
            </Select>
          </Form.Item>
          <Form.Item name="amount" label="Amount Received (Rs.)" rules={[{ required: true }]}>
            <InputNumber min={1} style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="referenceNo" label="Reference / Transaction No.">
            <Input />
          </Form.Item>
          <Form.Item name="notes" label="Notes">
            <Input.TextArea rows={2} />
          </Form.Item>
          <Form.Item>
            <Button type="primary" htmlType="submit">Record Payment</Button>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )
}
