import { useEffect, useState } from 'react'
import {
  Table, Button, Modal, Form, Input, InputNumber, Tag, Typography, Space, message,
  Statistic, Card, Row, Col, Alert, Select, Popconfirm
} from 'antd'
import { PlusOutlined, WalletOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons'
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
  const [balance, setBalance] = useState(null)
  const [balDrawer, setBalDrawer] = useState(false)
  const [form] = Form.useForm()

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

  const viewBalance = async (customerId) => {
    try {
      const res = await salesAPI.getBalance(customerId)
      setBalance(res.data)
      setBalDrawer(true)
    } catch {
      message.error('Could not load balance')
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
          <Button icon={<WalletOutlined />} size="small" onClick={() => viewBalance(r.customerId)}>
            Balance
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
          <Form.Item name="discountPercent" label="Standing Discount %" tooltip="Auto-applied on quotes and bills when discount is left at 0">
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

      <Modal title="Customer Balance" open={balDrawer} onCancel={() => setBalDrawer(false)} footer={null}>
        {balance && (
          <Row gutter={16}>
            <Col span={8}>
              <Card><Statistic title="Total Sales" value={balance.totalSales} prefix="Rs." precision={2} /></Card>
            </Col>
            <Col span={8}>
              <Card><Statistic title="Total Paid" value={balance.totalPaid} prefix="Rs." precision={2} valueStyle={{ color: '#3f8600' }} /></Card>
            </Col>
            <Col span={8}>
              <Card><Statistic title="Balance Due" value={balance.balance} prefix="Rs." precision={2}
                valueStyle={{ color: balance.balance > 0 ? '#cf1322' : '#3f8600' }} /></Card>
            </Col>
          </Row>
        )}
      </Modal>
    </div>
  )
}
