import { useEffect, useState } from 'react'
import {
  Tabs, Table, Typography, Alert, Card, Statistic, Row, Col, Select,
  DatePicker, Button, Space, message, Tag
} from 'antd'
import { financeAPI } from '../../api/finance'
import { apiErrorMessage } from '../../api/client'
import dayjs from 'dayjs'

const { Title } = Typography
const { RangePicker } = DatePicker

export default function FinanceReportsPage() {
  const [accounts, setAccounts] = useState([])
  const [trialBalance, setTrialBalance] = useState(null)
  const [ledger, setLedger] = useState(null)
  const [loadingTb, setLoadingTb] = useState(false)
  const [loadingGl, setLoadingGl] = useState(false)
  const [accountId, setAccountId] = useState(null)
  const [dateRange, setDateRange] = useState([dayjs().startOf('month'), dayjs()])

  useEffect(() => {
    financeAPI.getAccounts(true)
      .then(r => setAccounts(r.data))
      .catch(err => message.error(apiErrorMessage(err)))
  }, [])

  const loadTrialBalance = () => {
    setLoadingTb(true)
    financeAPI.getTrialBalance({ activeOnly: true, includeZero: false })
      .then(r => setTrialBalance(r.data))
      .catch(err => message.error(apiErrorMessage(err)))
      .finally(() => setLoadingTb(false))
  }

  const loadGeneralLedger = () => {
    if (!accountId || !dateRange?.[0] || !dateRange?.[1]) {
      message.warning('Select an account and date range')
      return
    }
    setLoadingGl(true)
    financeAPI.getGeneralLedger({
      accountId,
      fromDate: dateRange[0].format('YYYY-MM-DD'),
      toDate: dateRange[1].format('YYYY-MM-DD'),
    })
      .then(r => setLedger(r.data))
      .catch(err => message.error(apiErrorMessage(err)))
      .finally(() => setLoadingGl(false))
  }

  useEffect(() => { loadTrialBalance() }, [])

  const tbColumns = [
    { title: 'Code', dataIndex: 'accountCode', key: 'code', width: 80 },
    { title: 'Account', dataIndex: 'accountName', key: 'name' },
    { title: 'Type', dataIndex: 'accountType', key: 'type', width: 100 },
    { title: 'Debit', dataIndex: 'totalDebit', key: 'debit', width: 110,
      render: v => Number(v || 0).toLocaleString() },
    { title: 'Credit', dataIndex: 'totalCredit', key: 'credit', width: 110,
      render: v => Number(v || 0).toLocaleString() },
    { title: 'Balance', dataIndex: 'balance', key: 'balance', width: 110,
      render: v => Number(v || 0).toLocaleString() },
  ]

  const glColumns = [
    { title: 'Date', dataIndex: 'entryDate', key: 'date', width: 110 },
    { title: 'Entry #', dataIndex: 'entryNumber', key: 'num', width: 120 },
    { title: 'Memo', key: 'memo', render: (_, r) => r.lineMemo || r.entryMemo || '—' },
    { title: 'Debit', dataIndex: 'debitAmount', width: 100,
      render: v => Number(v || 0).toLocaleString() },
    { title: 'Credit', dataIndex: 'creditAmount', width: 100,
      render: v => Number(v || 0).toLocaleString() },
    { title: 'Balance', dataIndex: 'runningBalance', width: 110,
      render: v => Number(v || 0).toLocaleString() },
  ]

  const tabItems = [
    {
      key: 'trial-balance',
      label: 'Trial Balance',
      children: (
        <>
          <Space style={{ marginBottom: 16 }}>
            <Button onClick={loadTrialBalance} loading={loadingTb}>Refresh</Button>
            {trialBalance && (
              trialBalance.balanced
                ? <Tag color="green">Balanced</Tag>
                : <Tag color="red">Out of balance</Tag>
            )}
          </Space>
          {trialBalance && (
            <Row gutter={16} style={{ marginBottom: 16 }}>
              <Col span={8}>
                <Card><Statistic title="Total Debit" value={trialBalance.totalDebit} precision={2} /></Card>
              </Col>
              <Col span={8}>
                <Card><Statistic title="Total Credit" value={trialBalance.totalCredit} precision={2} /></Card>
              </Col>
            </Row>
          )}
          <Table dataSource={trialBalance?.lines || []} columns={tbColumns}
            rowKey="accountId" loading={loadingTb} pagination={{ pageSize: 20 }} />
        </>
      ),
    },
    {
      key: 'general-ledger',
      label: 'General Ledger',
      children: (
        <>
          <Space wrap style={{ marginBottom: 16 }}>
            <Select
              style={{ width: 280 }}
              placeholder="Select account"
              value={accountId}
              onChange={setAccountId}
              showSearch
              optionFilterProp="label"
              options={accounts.map(a => ({
                value: a.accountId,
                label: `${a.accountCode} — ${a.accountName}`,
              }))}
            />
            <RangePicker value={dateRange} onChange={setDateRange} />
            <Button type="primary" onClick={loadGeneralLedger} loading={loadingGl}>Run Report</Button>
          </Space>
          {ledger && (
            <>
              <Alert type="info" showIcon style={{ marginBottom: 16 }}
                message={`${ledger.accountCode} — ${ledger.accountName}`}
                description={`Opening: ${Number(ledger.openingBalance).toLocaleString()} · Closing: ${Number(ledger.closingBalance).toLocaleString()}`} />
              <Table dataSource={ledger.lines || []} columns={glColumns}
                rowKey={(_, i) => i} loading={loadingGl} pagination={{ pageSize: 25 }} />
            </>
          )}
        </>
      ),
    },
  ]

  return (
    <div>
      <Title level={4} className="page-title">Finance Reports</Title>
      <Tabs items={tabItems} />
    </div>
  )
}
