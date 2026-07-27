import { useEffect, useState } from 'react'
import {
  Tabs, Table, Typography, Alert, Card, Statistic, Row, Col, Select,
  DatePicker, Button, Space, message, Tag
} from 'antd'
import { financeAPI } from '../../api/finance'
import { apiErrorMessage } from '../../api/client'
import dayjs from 'dayjs'

const { Title, Text } = Typography
const { RangePicker } = DatePicker

export default function FinanceReportsPage() {
  const [accounts, setAccounts] = useState([])
  const [trialBalance, setTrialBalance] = useState(null)
  const [ledger, setLedger] = useState(null)
  const [arAging, setArAging] = useState(null)
  const [arRecon, setArRecon] = useState(null)
  const [inventoryValuation, setInventoryValuation] = useState(null)
  const [profitLoss, setProfitLoss] = useState(null)
  const [balanceSheet, setBalanceSheet] = useState(null)
  const [loadingTb, setLoadingTb] = useState(false)
  const [loadingGl, setLoadingGl] = useState(false)
  const [loadingAr, setLoadingAr] = useState(false)
  const [loadingInv, setLoadingInv] = useState(false)
  const [loadingPl, setLoadingPl] = useState(false)
  const [loadingBs, setLoadingBs] = useState(false)
  const [accountId, setAccountId] = useState(null)
  const [dateRange, setDateRange] = useState([dayjs().startOf('month'), dayjs()])
  const [plRange, setPlRange] = useState([dayjs().startOf('year'), dayjs()])
  const [bsDate, setBsDate] = useState(dayjs())

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

  const loadArAging = () => {
    setLoadingAr(true)
    Promise.all([financeAPI.getArAging(), financeAPI.getArReconciliation()])
      .then(([agingRes, reconRes]) => {
        setArAging(agingRes.data)
        setArRecon(reconRes.data)
      })
      .catch(err => message.error(apiErrorMessage(err)))
      .finally(() => setLoadingAr(false))
  }

  useEffect(() => { loadArAging() }, [])

  const loadInventoryValuation = () => {
    setLoadingInv(true)
    financeAPI.getInventoryValuation()
      .then(r => setInventoryValuation(r.data))
      .catch(err => message.error(apiErrorMessage(err)))
      .finally(() => setLoadingInv(false))
  }

  useEffect(() => { loadInventoryValuation() }, [])

  const loadProfitLoss = () => {
    if (!plRange?.[0] || !plRange?.[1]) {
      message.warning('Select a date range')
      return
    }
    setLoadingPl(true)
    financeAPI.getProfitLoss({
      fromDate: plRange[0].format('YYYY-MM-DD'),
      toDate: plRange[1].format('YYYY-MM-DD'),
    })
      .then(r => setProfitLoss(r.data))
      .catch(err => message.error(apiErrorMessage(err)))
      .finally(() => setLoadingPl(false))
  }

  const loadBalanceSheet = () => {
    if (!bsDate) {
      message.warning('Select an as-of date')
      return
    }
    setLoadingBs(true)
    financeAPI.getBalanceSheet({ asOfDate: bsDate.format('YYYY-MM-DD') })
      .then(r => setBalanceSheet(r.data))
      .catch(err => message.error(apiErrorMessage(err)))
      .finally(() => setLoadingBs(false))
  }

  useEffect(() => { loadProfitLoss() }, [])
  useEffect(() => { loadBalanceSheet() }, [])

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

  const arColumns = [
    { title: 'Customer', dataIndex: 'customerName', key: 'name' },
    { title: 'Phone', dataIndex: 'phone', key: 'phone', width: 120, render: v => v || '—' },
    { title: 'Current (0–30d)', dataIndex: 'current', width: 120, render: v => Number(v || 0).toLocaleString() },
    { title: '31–60d', dataIndex: 'days31to60', width: 100, render: v => Number(v || 0).toLocaleString() },
    { title: '61–90d', dataIndex: 'days61to90', width: 100, render: v => Number(v || 0).toLocaleString() },
    { title: '90+ days', dataIndex: 'over90', width: 100, render: v => Number(v || 0).toLocaleString() },
    { title: 'Unpaid bills', dataIndex: 'totalOutstanding', width: 110, render: v => Number(v || 0).toLocaleString() },
    { title: 'Ops balance', dataIndex: 'operationalBalance', width: 110, render: v => Number(v || 0).toLocaleString() },
  ]

  const invColumns = [
    { title: 'Design', dataIndex: 'designCode', key: 'code', width: 100 },
    { title: 'Name', dataIndex: 'designName', key: 'name' },
    { title: 'Size', dataIndex: 'sizeValue', key: 'size', width: 80 },
    { title: 'Color', dataIndex: 'color', key: 'color', width: 90 },
    { title: 'Qty', dataIndex: 'quantity', key: 'qty', width: 70 },
    { title: 'Unit cost', dataIndex: 'unitCost', width: 100, render: v => Number(v || 0).toLocaleString() },
    { title: 'Value', dataIndex: 'lineValue', width: 110, render: v => Number(v || 0).toLocaleString() },
  ]

  const plColumns = [
    { title: 'Code', dataIndex: 'accountCode', width: 80 },
    { title: 'Account', dataIndex: 'accountName' },
    { title: 'Amount', dataIndex: 'amount', width: 120, render: v => Number(v || 0).toLocaleString() },
  ]

  const bsColumns = [
    { title: 'Code', dataIndex: 'accountCode', width: 80 },
    { title: 'Account', dataIndex: 'accountName' },
    { title: 'Balance', dataIndex: 'balance', width: 120, render: v => Number(v || 0).toLocaleString() },
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
    {
      key: 'ar-aging',
      label: 'AR Aging',
      children: (
        <>
          <Space style={{ marginBottom: 16 }}>
            <Button onClick={loadArAging} loading={loadingAr}>Refresh</Button>
            {arRecon && (
              arRecon.reconciled
                ? <Tag color="green">AR reconciled</Tag>
                : <Tag color="orange">AR difference: {Number(arRecon.difference).toLocaleString()}</Tag>
            )}
          </Space>
          {arRecon && (
            <Alert type={arRecon.reconciled ? 'success' : 'warning'} showIcon style={{ marginBottom: 16 }}
              message={`Ledger AR: ${Number(arRecon.ledgerArBalance).toLocaleString()} · Operational: ${Number(arRecon.operationalBalanceTotal).toLocaleString()}`}
              description={arRecon.message} />
          )}
          {arAging && (
            <Row gutter={16} style={{ marginBottom: 16 }}>
              <Col span={6}><Card><Statistic title="Current" value={arAging.totalCurrent} precision={2} /></Card></Col>
              <Col span={6}><Card><Statistic title="31–60 days" value={arAging.totalDays31to60} precision={2} /></Card></Col>
              <Col span={6}><Card><Statistic title="61–90 days" value={arAging.totalDays61to90} precision={2} /></Card></Col>
              <Col span={6}><Card><Statistic title="90+ days" value={arAging.totalOver90} precision={2} /></Card></Col>
            </Row>
          )}
          <Table dataSource={arAging?.lines || []} columns={arColumns}
            rowKey="customerId" loading={loadingAr} pagination={{ pageSize: 20 }} />
        </>
      ),
    },
    {
      key: 'inventory-valuation',
      label: 'Inventory Valuation',
      children: (
        <>
          <Space style={{ marginBottom: 16 }}>
            <Button onClick={loadInventoryValuation} loading={loadingInv}>Refresh</Button>
            {inventoryValuation && (
              inventoryValuation.reconciled
                ? <Tag color="green">Inventory reconciled</Tag>
                : <Tag color="orange">Difference: {Number(inventoryValuation.difference).toLocaleString()}</Tag>
            )}
          </Space>
          {inventoryValuation && (
            <>
              <Alert type={inventoryValuation.reconciled ? 'success' : 'warning'} showIcon style={{ marginBottom: 16 }}
                message={`Ledger inventory: ${Number(inventoryValuation.ledgerInventoryBalance).toLocaleString()} · Stock at cost: ${Number(inventoryValuation.operationalStockValue).toLocaleString()}`}
                description={inventoryValuation.message} />
              <Row gutter={16} style={{ marginBottom: 16 }}>
                <Col span={8}><Card><Statistic title="Total units" value={inventoryValuation.totalUnits} /></Card></Col>
                <Col span={8}><Card><Statistic title="Stock value" value={inventoryValuation.operationalStockValue} precision={2} /></Card></Col>
                <Col span={8}><Card><Statistic title="Missing cost SKUs" value={inventoryValuation.linesMissingCost} /></Card></Col>
              </Row>
            </>
          )}
          <Table dataSource={inventoryValuation?.lines || []} columns={invColumns}
            rowKey="suitId" loading={loadingInv} pagination={{ pageSize: 20 }} />
        </>
      ),
    },
    {
      key: 'profit-loss',
      label: 'Profit & Loss',
      children: (
        <>
          <Space wrap style={{ marginBottom: 16 }}>
            <RangePicker value={plRange} onChange={setPlRange} />
            <Button type="primary" onClick={loadProfitLoss} loading={loadingPl}>Run Report</Button>
          </Space>
          {profitLoss && (
            <>
              <Row gutter={16} style={{ marginBottom: 16 }}>
                <Col span={8}><Card><Statistic title="Total income" value={profitLoss.totalIncome} precision={2} /></Card></Col>
                <Col span={8}><Card><Statistic title="Total expenses" value={profitLoss.totalExpenses} precision={2} /></Card></Col>
                <Col span={8}>
                  <Card>
                    <Statistic
                      title="Net income"
                      value={profitLoss.netIncome}
                      precision={2}
                      valueStyle={{ color: Number(profitLoss.netIncome) >= 0 ? '#0d9488' : '#dc2626' }}
                    />
                  </Card>
                </Col>
              </Row>
              <Title level={5}>Income</Title>
              <Table dataSource={profitLoss.incomeLines || []} columns={plColumns}
                rowKey="accountId" loading={loadingPl} pagination={false} size="small" style={{ marginBottom: 24 }} />
              <Title level={5}>Expenses</Title>
              <Table dataSource={profitLoss.expenseLines || []} columns={plColumns}
                rowKey={r => `exp-${r.accountId}`} loading={loadingPl} pagination={false} size="small" />
            </>
          )}
        </>
      ),
    },
    {
      key: 'balance-sheet',
      label: 'Balance Sheet',
      children: (
        <>
          <Space wrap style={{ marginBottom: 16 }}>
            <DatePicker value={bsDate} onChange={setBsDate} />
            <Button type="primary" onClick={loadBalanceSheet} loading={loadingBs}>Run Report</Button>
            {balanceSheet && (
              balanceSheet.balanced
                ? <Tag color="green">Balanced</Tag>
                : <Tag color="orange">Difference: {Number(balanceSheet.difference).toLocaleString()}</Tag>
            )}
          </Space>
          {balanceSheet && (
            <>
              <Alert type={balanceSheet.balanced ? 'success' : 'info'} showIcon style={{ marginBottom: 16 }}
                message={`Assets: ${Number(balanceSheet.totalAssets).toLocaleString()} · Liabilities + Equity: ${Number(balanceSheet.totalLiabilities + balanceSheet.totalEquity).toLocaleString()}`}
                description={balanceSheet.message} />
              <Title level={5}>Assets</Title>
              <Table dataSource={balanceSheet.assetLines || []} columns={bsColumns}
                rowKey={r => `asset-${r.accountId}`} loading={loadingBs} pagination={false} size="small" style={{ marginBottom: 24 }} />
              <Title level={5}>Liabilities</Title>
              <Table dataSource={balanceSheet.liabilityLines || []} columns={bsColumns}
                rowKey={r => `liab-${r.accountId}`} loading={loadingBs} pagination={false} size="small" style={{ marginBottom: 24 }} />
              <Title level={5}>Equity</Title>
              <Table dataSource={balanceSheet.equityLines || []} columns={bsColumns}
                rowKey={r => `eq-${r.accountId}`} loading={loadingBs} pagination={false} size="small" />
            </>
          )}
        </>
      ),
    },
  ]

  return (
    <div>
      <Title level={4} className="finance-page-title">Finance Reports</Title>
      <Text className="finance-page-subtitle">Trial balance, ledger, AR aging, inventory valuation, P&L, and balance sheet</Text>
      <Tabs items={tabItems} />
    </div>
  )
}
