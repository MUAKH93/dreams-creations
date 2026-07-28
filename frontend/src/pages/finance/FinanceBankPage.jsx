import { useEffect, useState } from 'react'
import {
  Table, Button, Modal, Form, Input, Typography, Space, message, Tag,
  DatePicker, Select, InputNumber, Tabs, Row, Col, Card, Statistic, Alert
} from 'antd'
import { PlusOutlined, CheckOutlined } from '@ant-design/icons'
import { financeAPI } from '../../api/finance'
import { apiErrorMessage } from '../../api/client'
import dayjs from 'dayjs'

const { Title, Text } = Typography

export default function FinanceBankPage() {
  const [bankAccounts, setBankAccounts] = useState([])
  const [glAccounts, setGlAccounts] = useState([])
  const [transactions, setTransactions] = useState([])
  const [reconciliation, setReconciliation] = useState(null)
  const [selectedBankId, setSelectedBankId] = useState(null)
  const [asOfDate, setAsOfDate] = useState(dayjs())
  const [loading, setLoading] = useState(true)
  const [loadingTx, setLoadingTx] = useState(false)
  const [loadingRecon, setLoadingRecon] = useState(false)
  const [accountModalOpen, setAccountModalOpen] = useState(false)
  const [txModalOpen, setTxModalOpen] = useState(false)
  const [accountForm] = Form.useForm()
  const [txForm] = Form.useForm()

  const loadAccounts = () => {
    setLoading(true)
    Promise.all([
      financeAPI.getBankAccounts(false),
      financeAPI.getAccounts(true),
    ])
      .then(([b, a]) => {
        setBankAccounts(b.data)
        setGlAccounts(a.data.filter(acc => acc.accountType === 'ASSET'))
        if (!selectedBankId && b.data.length > 0) {
          setSelectedBankId(b.data[0].bankAccountId)
        }
      })
      .catch(err => message.error(apiErrorMessage(err)))
      .finally(() => setLoading(false))
  }

  const loadTransactions = (bankId = selectedBankId) => {
    if (!bankId) return
    setLoadingTx(true)
    financeAPI.getBankTransactions(bankId)
      .then(r => setTransactions(r.data))
      .catch(err => message.error(apiErrorMessage(err)))
      .finally(() => setLoadingTx(false))
  }

  const loadReconciliation = (bankId = selectedBankId, date = asOfDate) => {
    if (!bankId || !date) return
    setLoadingRecon(true)
    financeAPI.getBankReconciliation(bankId, { asOfDate: date.format('YYYY-MM-DD') })
      .then(r => setReconciliation(r.data))
      .catch(err => message.error(apiErrorMessage(err)))
      .finally(() => setLoadingRecon(false))
  }

  useEffect(() => { loadAccounts() }, [])
  useEffect(() => {
    if (selectedBankId) {
      loadTransactions(selectedBankId)
      loadReconciliation(selectedBankId, asOfDate)
    }
  }, [selectedBankId])

  const onCreateAccount = async (values) => {
    try {
      await financeAPI.createBankAccount({
        accountName: values.accountName,
        bankName: values.bankName,
        accountNumber: values.accountNumber,
        glAccountId: values.glAccountId,
        openingBalance: values.openingBalance,
        openingBalanceDate: values.openingBalanceDate?.format('YYYY-MM-DD'),
        notes: values.notes,
      })
      message.success('Bank account created')
      setAccountModalOpen(false)
      accountForm.resetFields()
      loadAccounts()
    } catch (err) {
      message.error(apiErrorMessage(err))
    }
  }

  const onCreateTransaction = async (values) => {
    try {
      await financeAPI.createBankTransaction({
        bankAccountId: selectedBankId,
        transactionDate: values.transactionDate.format('YYYY-MM-DD'),
        description: values.description,
        amount: values.amount,
        referenceNo: values.referenceNo,
      })
      message.success('Statement line added')
      setTxModalOpen(false)
      txForm.resetFields()
      loadTransactions()
      loadReconciliation()
    } catch (err) {
      message.error(apiErrorMessage(err))
    }
  }

  const reconcile = async (transactionId) => {
    try {
      await financeAPI.reconcileBankTransaction(transactionId, {})
      message.success('Marked reconciled')
      loadTransactions()
      loadReconciliation()
    } catch (err) {
      message.error(apiErrorMessage(err))
    }
  }

  const accountColumns = [
    { title: 'Name', dataIndex: 'accountName', key: 'name' },
    { title: 'Bank', dataIndex: 'bankName', width: 120, render: v => v || '—' },
    { title: 'Account #', dataIndex: 'accountNumber', width: 110, render: v => v || '—' },
    { title: 'GL account', key: 'gl', render: (_, r) => `${r.glAccountCode} — ${r.glAccountName}` },
    { title: 'Opening', dataIndex: 'openingBalance', width: 100, render: v => Number(v || 0).toLocaleString() },
    { title: 'Status', dataIndex: 'isActive', width: 80,
      render: v => v ? <Tag color="green">Active</Tag> : <Tag>Inactive</Tag> },
  ]

  const txColumns = [
    { title: 'Date', dataIndex: 'transactionDate', width: 110 },
    { title: 'Description', dataIndex: 'description', render: v => v || '—' },
    { title: 'Reference', dataIndex: 'referenceNo', width: 100, render: v => v || '—' },
    { title: 'Amount', dataIndex: 'amount', width: 110,
      render: v => {
        const n = Number(v || 0)
        return <span style={{ color: n >= 0 ? '#0d9488' : '#dc2626' }}>{n.toLocaleString()}</span>
      },
    },
    { title: 'Status', dataIndex: 'isReconciled', width: 100,
      render: v => v ? <Tag color="green">Reconciled</Tag> : <Tag color="orange">Open</Tag> },
    { title: '', key: 'action', width: 110,
      render: (_, r) => !r.isReconciled && (
        <Button size="small" icon={<CheckOutlined />} onClick={() => reconcile(r.transactionId)}>
          Reconcile
        </Button>
      ),
    },
  ]

  const bookColumns = [
    { title: 'Date', dataIndex: 'entryDate', width: 110 },
    { title: 'Entry #', dataIndex: 'entryNumber', width: 120 },
    { title: 'Memo', dataIndex: 'memo', render: v => v || '—' },
    { title: 'Debit', dataIndex: 'debitAmount', width: 100, render: v => Number(v || 0).toLocaleString() },
    { title: 'Credit', dataIndex: 'creditAmount', width: 100, render: v => Number(v || 0).toLocaleString() },
    { title: 'Net', dataIndex: 'netAmount', width: 100, render: v => Number(v || 0).toLocaleString() },
  ]

  const tabItems = [
    {
      key: 'reconciliation',
      label: 'Reconciliation',
      children: (
        <>
          <Space wrap style={{ marginBottom: 16 }}>
            <Select
              style={{ width: 260 }}
              placeholder="Select bank account"
              value={selectedBankId}
              onChange={setSelectedBankId}
              options={bankAccounts.map(b => ({
                value: b.bankAccountId,
                label: b.accountName,
              }))}
            />
            <DatePicker value={asOfDate} onChange={setAsOfDate} />
            <Button type="primary" onClick={() => loadReconciliation()} loading={loadingRecon}>
              Run Reconciliation
            </Button>
            {reconciliation && (
              reconciliation.reconciled
                ? <Tag color="green">Reconciled</Tag>
                : <Tag color="orange">Difference: {Number(reconciliation.difference).toLocaleString()}</Tag>
            )}
          </Space>
          {reconciliation && (
            <>
              <Alert type={reconciliation.reconciled ? 'success' : 'warning'} showIcon style={{ marginBottom: 16 }}
                message={`Ledger: ${Number(reconciliation.ledgerBalance).toLocaleString()} · Statement: ${Number(reconciliation.statementBalance).toLocaleString()} · GL ${reconciliation.glAccountCode}`}
                description={reconciliation.message} />
              <Row gutter={16} style={{ marginBottom: 16 }}>
                <Col span={6}><Card><Statistic title="Opening" value={reconciliation.openingBalance} precision={2} /></Card></Col>
                <Col span={6}><Card><Statistic title="Statement balance" value={reconciliation.statementBalance} precision={2} /></Card></Col>
                <Col span={6}><Card><Statistic title="Ledger balance" value={reconciliation.ledgerBalance} precision={2} /></Card></Col>
                <Col span={6}><Card><Statistic title="Unreconciled lines" value={reconciliation.unreconciledStatementCount} /></Card></Col>
              </Row>
            </>
          )}
          <Title level={5}>Statement lines</Title>
          <Table dataSource={reconciliation?.statementLines || []}
            columns={txColumns.filter(c => c.key !== 'action')}
            rowKey="transactionId" loading={loadingRecon} pagination={{ pageSize: 15 }} style={{ marginBottom: 24 }} />
          <Title level={5}>Book entries (last 3 months)</Title>
          <Table dataSource={reconciliation?.bookLines || []}
            columns={bookColumns} rowKey={(_, i) => i} loading={loadingRecon} pagination={{ pageSize: 15 }} />
        </>
      ),
    },
    {
      key: 'accounts',
      label: 'Bank Accounts',
      children: (
        <>
          <Space style={{ marginBottom: 16 }}>
            <Button type="primary" icon={<PlusOutlined />} onClick={() => setAccountModalOpen(true)}>
              New Bank Account
            </Button>
          </Space>
          <Table dataSource={bankAccounts} columns={accountColumns} rowKey="bankAccountId"
            loading={loading} pagination={{ pageSize: 10 }} />
        </>
      ),
    },
    {
      key: 'statement',
      label: 'Statement Lines',
      children: (
        <>
          <Space wrap style={{ marginBottom: 16 }}>
            <Select
              style={{ width: 260 }}
              value={selectedBankId}
              onChange={id => { setSelectedBankId(id); loadTransactions(id) }}
              options={bankAccounts.map(b => ({
                value: b.bankAccountId,
                label: b.accountName,
              }))}
            />
            <Button type="primary" icon={<PlusOutlined />} onClick={() => setTxModalOpen(true)}
              disabled={!selectedBankId}>
              Add Line
            </Button>
            <Button onClick={() => loadTransactions()} loading={loadingTx}>Refresh</Button>
          </Space>
          <Table dataSource={transactions} columns={txColumns} rowKey="transactionId"
            loading={loadingTx} pagination={{ pageSize: 15 }} />
        </>
      ),
    },
  ]

  return (
    <div>
      <Title level={4} className="finance-page-title">Bank Reconciliation</Title>
      <Text className="finance-page-subtitle">
        Link bank accounts to GL cash, enter statement lines, and reconcile book vs bank balances
      </Text>

      <Tabs items={tabItems} style={{ marginTop: 16 }} />

      <Modal title="New Bank Account" open={accountModalOpen} onCancel={() => setAccountModalOpen(false)}
        onOk={() => accountForm.submit()} okText="Create">
        <Form form={accountForm} layout="vertical" onFinish={onCreateAccount}
          initialValues={{ openingBalance: 0, openingBalanceDate: dayjs() }}>
          <Form.Item name="accountName" label="Account name" rules={[{ required: true }]}>
            <Input placeholder="e.g. HBL Current Account" />
          </Form.Item>
          <Form.Item name="bankName" label="Bank name"><Input /></Form.Item>
          <Form.Item name="accountNumber" label="Account number"><Input /></Form.Item>
          <Form.Item name="glAccountId" label="GL cash account" rules={[{ required: true }]}>
            <Select showSearch optionFilterProp="label" placeholder="Select asset account"
              options={glAccounts.map(a => ({
                value: a.accountId,
                label: `${a.accountCode} — ${a.accountName}`,
              }))} />
          </Form.Item>
          <Form.Item name="openingBalance" label="Opening balance">
            <InputNumber precision={2} style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="openingBalanceDate" label="Opening balance date">
            <DatePicker style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="notes" label="Notes"><Input.TextArea rows={2} /></Form.Item>
        </Form>
      </Modal>

      <Modal title="Add Statement Line" open={txModalOpen} onCancel={() => setTxModalOpen(false)}
        onOk={() => txForm.submit()} okText="Add">
        <Form form={txForm} layout="vertical" onFinish={onCreateTransaction}
          initialValues={{ transactionDate: dayjs() }}>
          <Form.Item name="transactionDate" label="Date" rules={[{ required: true }]}>
            <DatePicker style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="amount" label="Amount (+ deposit, − withdrawal)" rules={[{ required: true }]}>
            <InputNumber precision={2} style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="description" label="Description"><Input /></Form.Item>
          <Form.Item name="referenceNo" label="Reference #"><Input /></Form.Item>
        </Form>
      </Modal>
    </div>
  )
}
