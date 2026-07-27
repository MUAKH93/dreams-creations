import { useEffect, useState } from 'react'
import {
  Table, Button, Modal, Form, Input, Typography, Space, message, Tag,
  DatePicker, Select, InputNumber, Tabs, Row, Col, Card, Statistic, Alert
} from 'antd'
import { PlusOutlined, DollarOutlined } from '@ant-design/icons'
import { financeAPI } from '../../api/finance'
import { apiErrorMessage } from '../../api/client'
import dayjs from 'dayjs'

const { Title, Text } = Typography

const statusColor = { unpaid: 'orange', partial: 'blue', paid: 'green' }

export default function FinancePayablesPage() {
  const [vendors, setVendors] = useState([])
  const [payables, setPayables] = useState([])
  const [expenseAccounts, setExpenseAccounts] = useState([])
  const [apAging, setApAging] = useState(null)
  const [loading, setLoading] = useState(true)
  const [loadingAging, setLoadingAging] = useState(false)
  const [vendorModalOpen, setVendorModalOpen] = useState(false)
  const [payableModalOpen, setPayableModalOpen] = useState(false)
  const [paymentModalOpen, setPaymentModalOpen] = useState(false)
  const [selectedPayable, setSelectedPayable] = useState(null)
  const [vendorForm] = Form.useForm()
  const [payableForm] = Form.useForm()
  const [paymentForm] = Form.useForm()

  const load = () => {
    setLoading(true)
    Promise.all([
      financeAPI.getVendors(false),
      financeAPI.getPayables(),
      financeAPI.getAccounts(true),
    ])
      .then(([v, p, a]) => {
        setVendors(v.data)
        setPayables(p.data)
        setExpenseAccounts(a.data.filter(acc => acc.accountType === 'EXPENSE'))
      })
      .catch(err => message.error(apiErrorMessage(err)))
      .finally(() => setLoading(false))
  }

  const loadApAging = () => {
    setLoadingAging(true)
    financeAPI.getApAging()
      .then(r => setApAging(r.data))
      .catch(err => message.error(apiErrorMessage(err)))
      .finally(() => setLoadingAging(false))
  }

  useEffect(() => {
    load()
    loadApAging()
  }, [])

  const onCreateVendor = async (values) => {
    try {
      await financeAPI.createVendor({
        vendorName: values.vendorName,
        phone: values.phone,
        email: values.email,
        notes: values.notes,
      })
      message.success('Vendor created')
      setVendorModalOpen(false)
      vendorForm.resetFields()
      load()
    } catch (err) {
      message.error(apiErrorMessage(err))
    }
  }

  const onCreatePayable = async (values) => {
    try {
      await financeAPI.createPayable({
        vendorId: values.vendorId,
        invoiceNumber: values.invoiceNumber,
        invoiceDate: values.invoiceDate.format('YYYY-MM-DD'),
        dueDate: values.dueDate.format('YYYY-MM-DD'),
        amount: values.amount,
        expenseAccountId: values.expenseAccountId,
        memo: values.memo,
      })
      message.success('Payable recorded and journal posted')
      setPayableModalOpen(false)
      payableForm.resetFields()
      load()
      loadApAging()
    } catch (err) {
      message.error(apiErrorMessage(err))
    }
  }

  const openPayment = (record) => {
    setSelectedPayable(record)
    paymentForm.resetFields()
    paymentForm.setFieldsValue({
      paymentDate: dayjs(),
      amount: Number(record.balanceDue || 0),
    })
    setPaymentModalOpen(true)
  }

  const onRecordPayment = async (values) => {
    try {
      await financeAPI.recordPayablePayment(selectedPayable.payableId, {
        amount: values.amount,
        paymentDate: values.paymentDate.format('YYYY-MM-DD'),
        paymentMethod: values.paymentMethod,
        referenceNo: values.referenceNo,
      })
      message.success('Payment recorded')
      setPaymentModalOpen(false)
      load()
      loadApAging()
    } catch (err) {
      message.error(apiErrorMessage(err))
    }
  }

  const vendorColumns = [
    { title: 'Vendor', dataIndex: 'vendorName', key: 'name' },
    { title: 'Phone', dataIndex: 'phone', width: 120, render: v => v || '—' },
    { title: 'Email', dataIndex: 'email', width: 180, render: v => v || '—' },
    { title: 'Status', dataIndex: 'isActive', width: 90,
      render: v => v ? <Tag color="green">Active</Tag> : <Tag>Inactive</Tag> },
  ]

  const payableColumns = [
    { title: 'Vendor', dataIndex: 'vendorName', key: 'vendor' },
    { title: 'Invoice #', dataIndex: 'invoiceNumber', width: 110 },
    { title: 'Invoice date', dataIndex: 'invoiceDate', width: 110 },
    { title: 'Due date', dataIndex: 'dueDate', width: 110 },
    { title: 'Amount', dataIndex: 'amount', width: 100, render: v => Number(v || 0).toLocaleString() },
    { title: 'Paid', dataIndex: 'amountPaid', width: 90, render: v => Number(v || 0).toLocaleString() },
    { title: 'Balance', dataIndex: 'balanceDue', width: 100, render: v => Number(v || 0).toLocaleString() },
    { title: 'Expense acct', key: 'expense', render: (_, r) => `${r.expenseAccountCode} — ${r.expenseAccountName}` },
    { title: 'Status', dataIndex: 'status', width: 90,
      render: v => <Tag color={statusColor[v] || 'default'}>{v}</Tag> },
    { title: '', key: 'action', width: 100,
      render: (_, r) => r.status !== 'paid' && (
        <Button size="small" icon={<DollarOutlined />} onClick={() => openPayment(r)}>
          Pay
        </Button>
      ),
    },
  ]

  const apColumns = [
    { title: 'Vendor', dataIndex: 'vendorName', key: 'name' },
    { title: 'Phone', dataIndex: 'phone', width: 120, render: v => v || '—' },
    { title: 'Current (0–30d)', dataIndex: 'current', width: 120, render: v => Number(v || 0).toLocaleString() },
    { title: '31–60d', dataIndex: 'days31to60', width: 100, render: v => Number(v || 0).toLocaleString() },
    { title: '61–90d', dataIndex: 'days61to90', width: 100, render: v => Number(v || 0).toLocaleString() },
    { title: '90+ days', dataIndex: 'over90', width: 100, render: v => Number(v || 0).toLocaleString() },
    { title: 'Outstanding', dataIndex: 'totalOutstanding', width: 110, render: v => Number(v || 0).toLocaleString() },
  ]

  const tabItems = [
    {
      key: 'payables',
      label: 'Open Payables',
      children: (
        <>
          <Space style={{ marginBottom: 16 }}>
            <Button type="primary" icon={<PlusOutlined />} onClick={() => setPayableModalOpen(true)}>
              New Payable
            </Button>
            <Button onClick={load} loading={loading}>Refresh</Button>
          </Space>
          <Table dataSource={payables} columns={payableColumns} rowKey="payableId"
            loading={loading} pagination={{ pageSize: 15 }} />
        </>
      ),
    },
    {
      key: 'vendors',
      label: 'Vendors',
      children: (
        <>
          <Space style={{ marginBottom: 16 }}>
            <Button type="primary" icon={<PlusOutlined />} onClick={() => setVendorModalOpen(true)}>
              New Vendor
            </Button>
          </Space>
          <Table dataSource={vendors} columns={vendorColumns} rowKey="vendorId"
            loading={loading} pagination={{ pageSize: 15 }} />
        </>
      ),
    },
    {
      key: 'ap-aging',
      label: 'AP Aging',
      children: (
        <>
          <Space style={{ marginBottom: 16 }}>
            <Button onClick={loadApAging} loading={loadingAging}>Refresh</Button>
            {apAging && (
              apAging.reconciled
                ? <Tag color="green">AP reconciled</Tag>
                : <Tag color="orange">AP difference: {Number(apAging.difference).toLocaleString()}</Tag>
            )}
          </Space>
          {apAging && (
            <>
              <Alert type={apAging.reconciled ? 'success' : 'warning'} showIcon style={{ marginBottom: 16 }}
                message={`Ledger AP: ${Number(apAging.ledgerApBalance).toLocaleString()} · Open payables: ${Number(apAging.grandTotal).toLocaleString()}`}
                description={apAging.message} />
              <Row gutter={16} style={{ marginBottom: 16 }}>
                <Col span={6}><Card><Statistic title="Current" value={apAging.totalCurrent} precision={2} /></Card></Col>
                <Col span={6}><Card><Statistic title="31–60 days" value={apAging.totalDays31to60} precision={2} /></Card></Col>
                <Col span={6}><Card><Statistic title="61–90 days" value={apAging.totalDays61to90} precision={2} /></Card></Col>
                <Col span={6}><Card><Statistic title="90+ days" value={apAging.totalOver90} precision={2} /></Card></Col>
              </Row>
            </>
          )}
          <Table dataSource={apAging?.lines || []} columns={apColumns}
            rowKey="vendorId" loading={loadingAging} pagination={{ pageSize: 20 }} />
        </>
      ),
    },
  ]

  return (
    <div>
      <Title level={4} className="finance-page-title">Accounts Payable</Title>
      <Text className="finance-page-subtitle">
        Vendor bills, payments, and AP aging — journals post automatically (Dr expense / Cr AP; payment Dr AP / Cr cash)
      </Text>

      <Tabs items={tabItems} style={{ marginTop: 16 }} />

      <Modal title="New Vendor" open={vendorModalOpen} onCancel={() => setVendorModalOpen(false)}
        onOk={() => vendorForm.submit()} okText="Create">
        <Form form={vendorForm} layout="vertical" onFinish={onCreateVendor}>
          <Form.Item name="vendorName" label="Vendor name" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="phone" label="Phone"><Input /></Form.Item>
          <Form.Item name="email" label="Email"><Input type="email" /></Form.Item>
          <Form.Item name="notes" label="Notes"><Input.TextArea rows={2} /></Form.Item>
        </Form>
      </Modal>

      <Modal title="New Payable" open={payableModalOpen} onCancel={() => setPayableModalOpen(false)}
        onOk={() => payableForm.submit()} okText="Record & Post" width={520}>
        <Form form={payableForm} layout="vertical" onFinish={onCreatePayable}
          initialValues={{ invoiceDate: dayjs(), dueDate: dayjs().add(30, 'day') }}>
          <Form.Item name="vendorId" label="Vendor" rules={[{ required: true }]}>
            <Select placeholder="Select vendor" showSearch optionFilterProp="label"
              options={vendors.filter(v => v.isActive).map(v => ({
                value: v.vendorId,
                label: v.vendorName,
              }))} />
          </Form.Item>
          <Form.Item name="invoiceNumber" label="Invoice number" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Space style={{ width: '100%' }} size="middle">
            <Form.Item name="invoiceDate" label="Invoice date" rules={[{ required: true }]}>
              <DatePicker />
            </Form.Item>
            <Form.Item name="dueDate" label="Due date" rules={[{ required: true }]}>
              <DatePicker />
            </Form.Item>
          </Space>
          <Form.Item name="amount" label="Amount" rules={[{ required: true }]}>
            <InputNumber min={0.01} precision={2} style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="expenseAccountId" label="Expense account" rules={[{ required: true }]}>
            <Select placeholder="Select expense account" showSearch optionFilterProp="label"
              options={expenseAccounts.map(a => ({
                value: a.accountId,
                label: `${a.accountCode} — ${a.accountName}`,
              }))} />
          </Form.Item>
          <Form.Item name="memo" label="Memo"><Input.TextArea rows={2} /></Form.Item>
        </Form>
      </Modal>

      <Modal title={`Payment — ${selectedPayable?.invoiceNumber || ''}`}
        open={paymentModalOpen} onCancel={() => setPaymentModalOpen(false)}
        onOk={() => paymentForm.submit()} okText="Record Payment">
        <Form form={paymentForm} layout="vertical" onFinish={onRecordPayment}>
          <Alert type="info" showIcon style={{ marginBottom: 16 }}
            message={`Balance due: ${Number(selectedPayable?.balanceDue || 0).toLocaleString()}`} />
          <Form.Item name="amount" label="Amount" rules={[{ required: true }]}>
            <InputNumber min={0.01} precision={2} style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="paymentDate" label="Payment date" rules={[{ required: true }]}>
            <DatePicker style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="paymentMethod" label="Method"><Input placeholder="Cash, bank transfer, etc." /></Form.Item>
          <Form.Item name="referenceNo" label="Reference #"><Input /></Form.Item>
        </Form>
      </Modal>
    </div>
  )
}
