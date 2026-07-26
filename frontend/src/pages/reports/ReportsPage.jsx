import { useEffect, useMemo, useState } from 'react'
import {
  Tabs, Table, Button, Space, Typography, Select, DatePicker, message, Card, Statistic, Row, Col, Alert
} from 'antd'
import { DownloadOutlined, FileExcelOutlined, PrinterOutlined } from '@ant-design/icons'
import dayjs from 'dayjs'
import { salesAPI } from '../../api/sales'
import { productionAPI } from '../../api/production'
import { inventoryAPI } from '../../api/inventory'
import { dashboardAPI } from '../../api/dashboard'
import { apiErrorMessage } from '../../api/client'
import { downloadCsv, todayStamp } from '../../utils/exportCsv'
import ReportPrint, { printReportDocument } from '../../components/ReportPrint'

const { Title, Text } = Typography
const { RangePicker } = DatePicker

function customerName(c) {
  return `${c?.firstName || ''} ${c?.lastName || ''}`.trim() || '—'
}

export default function ReportsPage() {
  const [loading, setLoading] = useState(true)
  const [bills, setBills] = useState([])
  const [customers, setCustomers] = useState([])
  const [customerBalances, setCustomerBalances] = useState([])
  const [stock, setStock] = useState([])
  const [batches, setBatches] = useState([])
  const [assignments, setAssignments] = useState([])
  const [summary, setSummary] = useState(null)

  const [billStatus, setBillStatus] = useState(null)
  const [billRange, setBillRange] = useState(null)
  const [batchStatus, setBatchStatus] = useState(null)
  const [lowStockOnly, setLowStockOnly] = useState(false)
  const [printReport, setPrintReport] = useState(null)

  const load = async () => {
    setLoading(true)
    try {
      const [billsRes, custRes, stockRes, batchRes, assignRes, summaryRes] = await Promise.all([
        salesAPI.getBills(),
        salesAPI.getCustomers(),
        inventoryAPI.getAll(),
        productionAPI.getBatches(),
        productionAPI.getAssignments(),
        dashboardAPI.getSummary(),
      ])
      setBills(billsRes.data)
      setCustomers(custRes.data)
      setStock(stockRes.data)
      setBatches(batchRes.data)
      setAssignments(assignRes.data)
      setSummary(summaryRes.data)

      const balances = await Promise.allSettled(
        custRes.data.map(c => salesAPI.getBalance(c.customerId))
      )
      setCustomerBalances(
        custRes.data.map((c, i) => ({
          ...c,
          balance: balances[i].status === 'fulfilled' ? balances[i].value.data : null,
        }))
      )
    } catch (err) {
      message.error(apiErrorMessage(err))
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { load() }, [])

  const filteredBills = useMemo(() => {
    return bills.filter(b => {
      if (billStatus && b.status !== billStatus) return false
      if (billRange?.[0] && billRange?.[1]) {
        const d = b.billDate ? dayjs(b.billDate) : null
        if (!d || d.isBefore(billRange[0], 'day') || d.isAfter(billRange[1], 'day')) return false
      }
      return true
    })
  }, [bills, billStatus, billRange])

  const filteredStock = useMemo(() => {
    if (!lowStockOnly) return stock
    return stock.filter(s => (s.quantity ?? 0) <= 5)
  }, [stock, lowStockOnly])

  const filteredBatches = useMemo(() => {
    if (!batchStatus) return batches
    return batches.filter(b => b.status === batchStatus)
  }, [batches, batchStatus])

  const exportBills = () => {
    downloadCsv(`bills-report-${todayStamp()}.csv`, [
      { title: 'Bill #', value: r => r.billNumber },
      { title: 'Customer', value: r => customerName(r.customer) },
      { title: 'Status', value: r => r.status },
      { title: 'Subtotal', value: r => r.totalAmount },
      { title: 'Final Amount', value: r => r.finalAmount },
      { title: 'Bill Date', value: r => r.billDate ? new Date(r.billDate).toLocaleDateString() : '' },
      { title: 'Items', value: r => (r.items || []).map(i =>
        `${i.product?.suit?.design?.designCode || '?'} x${i.quantity}`).join('; ') },
    ], filteredBills)
    message.success('Bills exported')
  }

  const exportCustomers = () => {
    downloadCsv(`customers-report-${todayStamp()}.csv`, [
      { title: 'Name', value: r => customerName(r) },
      { title: 'Phone', value: r => r.phone },
      { title: 'Email', value: r => r.email },
      { title: 'City', value: r => r.city },
      { title: 'Status', value: r => r.status },
      { title: 'Total Sales', value: r => r.balance?.totalSales ?? 0 },
      { title: 'Total Paid', value: r => r.balance?.totalPaid ?? 0 },
      { title: 'Balance Due', value: r => r.balance?.balance ?? 0 },
    ], customerBalances)
    message.success('Customers exported')
  }

  const exportInventory = () => {
    downloadCsv(`inventory-report-${todayStamp()}.csv`, [
      { title: 'Design Code', value: r => r.designCode },
      { title: 'Design Name', value: r => r.designName },
      { title: 'Category', value: r => r.categoryName },
      { title: 'Size', value: r => r.sizeValue },
      { title: 'Color', value: r => r.color },
      { title: 'Quantity', value: r => r.quantity },
      { title: 'Last Updated', value: r => r.lastUpdated ? new Date(r.lastUpdated).toLocaleString() : '' },
    ], filteredStock)
    message.success('Inventory exported')
  }

  const exportProduction = () => {
    downloadCsv(`production-report-${todayStamp()}.csv`, [
      { title: 'Batch #', value: r => r.batchNumber },
      { title: 'Design', value: r => r.suit?.design?.name || r.suit?.design?.designCode },
      { title: 'Planned', value: r => r.totalSuitPlanned },
      { title: 'Produced', value: r => r.totalSuitProduced },
      { title: 'Status', value: r => r.status },
      { title: 'Start Date', value: r => r.startDate ? new Date(r.startDate).toLocaleDateString() : '' },
      { title: 'End Date', value: r => r.endDate ? new Date(r.endDate).toLocaleDateString() : '' },
    ], filteredBatches)
    message.success('Production batches exported')
  }

  const exportDispatches = () => {
    downloadCsv(`dispatches-report-${todayStamp()}.csv`, [
      { title: 'Assignment ID', value: r => r.assignmentId },
      { title: 'Batch', value: r => r.batch?.batchNumber },
      { title: 'Stage', value: r => r.module?.stage?.stageName },
      { title: 'Module', value: r => r.module?.moduleName },
      { title: 'Supervisor', value: r => `${r.supervisor?.firstName || ''} ${r.supervisor?.lastName || ''}`.trim() },
      { title: 'Qty Sent', value: r => r.quantitySent },
      { title: 'Returned OK', value: r => r.quantityReturnedOk },
      { title: 'Damaged', value: r => r.quantityDamaged },
      { title: 'Missing', value: r => r.quantityMissing },
      { title: 'Status', value: r => r.status },
      { title: 'Due Date', value: r => r.dueDate ? new Date(r.dueDate).toLocaleDateString() : '' },
    ], assignments)
    message.success('Dispatches exported')
  }

  const triggerPrint = (report) => {
    setPrintReport(report)
    setTimeout(() => {
      printReportDocument()
      setTimeout(() => setPrintReport(null), 500)
    }, 200)
  }

  const printBills = () => triggerPrint({
    title: 'Sales & Bills Report',
    subtitle: billStatus ? `Status: ${billStatus}` : 'All statuses',
    summary: [
      { label: 'Total bills', value: filteredBills.length },
      { label: 'Total amount', value: `Rs. ${filteredBills.reduce((s, b) => s + Number(b.finalAmount || 0), 0).toLocaleString()}` },
    ],
    columns: [
      { key: 'bill', title: 'Bill #', render: r => r.billNumber },
      { key: 'cust', title: 'Customer', render: r => customerName(r.customer) },
      { key: 'status', title: 'Status', render: r => r.status },
      { key: 'amt', title: 'Amount', render: r => `Rs. ${Number(r.finalAmount || 0).toLocaleString()}` },
      { key: 'date', title: 'Date', render: r => r.billDate ? new Date(r.billDate).toLocaleDateString() : '—' },
    ],
    rows: filteredBills.map(b => ({ ...b, id: b.billId })),
  })

  const printCustomers = () => triggerPrint({
    title: 'Customer Balances Report',
    columns: [
      { key: 'name', title: 'Name', render: r => customerName(r) },
      { key: 'phone', title: 'Phone', render: r => r.phone || '—' },
      { key: 'sales', title: 'Total Sales', render: r => `Rs. ${Number(r.balance?.totalSales || 0).toLocaleString()}` },
      { key: 'paid', title: 'Paid', render: r => `Rs. ${Number(r.balance?.totalPaid || 0).toLocaleString()}` },
      { key: 'due', title: 'Due', render: r => `Rs. ${Number(r.balance?.balance || 0).toLocaleString()}` },
    ],
    rows: customerBalances.map(c => ({ ...c, id: c.customerId })),
  })

  const printInventory = () => triggerPrint({
    title: 'Inventory Stock Report',
    subtitle: lowStockOnly ? 'Low stock only (≤ 5)' : 'All stock',
    columns: [
      { key: 'code', title: 'Code', render: r => r.designCode },
      { key: 'name', title: 'Name', render: r => r.designName },
      { key: 'size', title: 'Size', render: r => r.sizeValue },
      { key: 'color', title: 'Color', render: r => r.color },
      { key: 'qty', title: 'Qty', render: r => r.quantity },
    ],
    rows: filteredStock.map(s => ({ ...s, id: s.inventoryId })),
  })

  const printProduction = () => triggerPrint({
    title: 'Production Batches Report',
    subtitle: batchStatus ? `Status: ${batchStatus}` : 'All batches',
    columns: [
      { key: 'batch', title: 'Batch #', render: r => r.batchNumber },
      { key: 'design', title: 'Design', render: r => r.suit?.design?.name },
      { key: 'plan', title: 'Planned', render: r => r.totalSuitPlanned },
      { key: 'prod', title: 'Produced', render: r => r.totalSuitProduced },
      { key: 'status', title: 'Status', render: r => r.status },
    ],
    rows: filteredBatches.map(b => ({ ...b, id: b.batchId })),
  })

  const s = summary || {}

  const tabItems = [
    {
      key: 'sales',
      label: 'Sales & Bills',
      children: (
        <>
          <Space wrap style={{ marginBottom: 16 }}>
            <Select placeholder="Bill status" allowClear style={{ width: 140 }}
              value={billStatus} onChange={setBillStatus}
              options={['unpaid', 'partial', 'paid', 'cancelled'].map(v => ({ value: v, label: v }))} />
            <RangePicker value={billRange} onChange={setBillRange} />
            <Button type="primary" icon={<DownloadOutlined />} onClick={exportBills}>
              Export CSV
            </Button>
            <Button icon={<PrinterOutlined />} onClick={printBills}>
              Print PDF
            </Button>
          </Space>
          <Table
            dataSource={filteredBills}
            rowKey="billId"
            loading={loading}
            size="small"
            pagination={{ pageSize: 10 }}
            columns={[
              { title: 'Bill #', dataIndex: 'billNumber' },
              { title: 'Customer', key: 'cust', render: (_, r) => customerName(r.customer) },
              { title: 'Status', dataIndex: 'status' },
              { title: 'Amount', dataIndex: 'finalAmount', render: v => `Rs. ${Number(v || 0).toLocaleString()}` },
              { title: 'Date', dataIndex: 'billDate', render: d => d ? new Date(d).toLocaleDateString() : '—' },
            ]}
          />
        </>
      ),
    },
    {
      key: 'customers',
      label: 'Customers',
      children: (
        <>
          <Space style={{ marginBottom: 16 }}>
            <Button type="primary" icon={<DownloadOutlined />} onClick={exportCustomers}>
              Export CSV
            </Button>
            <Button icon={<PrinterOutlined />} onClick={printCustomers}>
              Print PDF
            </Button>
          </Space>
          <Table
            dataSource={customerBalances}
            rowKey="customerId"
            loading={loading}
            size="small"
            pagination={{ pageSize: 10 }}
            columns={[
              { title: 'Name', key: 'n', render: (_, r) => customerName(r) },
              { title: 'Phone', dataIndex: 'phone' },
              { title: 'Total Sales', key: 'ts', render: (_, r) => `Rs. ${Number(r.balance?.totalSales || 0).toLocaleString()}` },
              { title: 'Paid', key: 'tp', render: (_, r) => `Rs. ${Number(r.balance?.totalPaid || 0).toLocaleString()}` },
              { title: 'Due', key: 'bd', render: (_, r) => (
                <Text type={r.balance?.balance > 0 ? 'danger' : undefined}>
                  Rs. {Number(r.balance?.balance || 0).toLocaleString()}
                </Text>
              ) },
            ]}
          />
        </>
      ),
    },
    {
      key: 'inventory',
      label: 'Inventory',
      children: (
        <>
          <Space wrap style={{ marginBottom: 16 }}>
            <Button
              type={lowStockOnly ? 'primary' : 'default'}
              onClick={() => setLowStockOnly(v => !v)}
            >
              {lowStockOnly ? 'Showing low stock only' : 'Show all stock'}
            </Button>
            <Button type="primary" icon={<DownloadOutlined />} onClick={exportInventory}>
              Export CSV
            </Button>
            <Button icon={<PrinterOutlined />} onClick={printInventory}>
              Print PDF
            </Button>
          </Space>
          <Table
            dataSource={filteredStock}
            rowKey="inventoryId"
            loading={loading}
            size="small"
            pagination={{ pageSize: 10 }}
            columns={[
              { title: 'Design', dataIndex: 'designCode' },
              { title: 'Name', dataIndex: 'designName' },
              { title: 'Size', dataIndex: 'sizeValue' },
              { title: 'Color', dataIndex: 'color' },
              { title: 'Qty', dataIndex: 'quantity' },
            ]}
          />
        </>
      ),
    },
    {
      key: 'production',
      label: 'Production',
      children: (
        <>
          <Space wrap style={{ marginBottom: 16 }}>
            <Select placeholder="Batch status" allowClear style={{ width: 160 }}
              value={batchStatus} onChange={setBatchStatus}
              options={['planned', 'in_progress', 'completed', 'cancelled'].map(v => ({ value: v, label: v }))} />
            <Button type="primary" icon={<DownloadOutlined />} onClick={exportProduction}>
              Export batches CSV
            </Button>
            <Button icon={<PrinterOutlined />} onClick={printProduction}>
              Print PDF
            </Button>
            <Button icon={<DownloadOutlined />} onClick={exportDispatches}>
              Export dispatches CSV
            </Button>
          </Space>
          <Table
            dataSource={filteredBatches}
            rowKey="batchId"
            loading={loading}
            size="small"
            pagination={{ pageSize: 10 }}
            columns={[
              { title: 'Batch #', dataIndex: 'batchNumber' },
              { title: 'Design', key: 'd', render: (_, r) => r.suit?.design?.name },
              { title: 'Planned', dataIndex: 'totalSuitPlanned' },
              { title: 'Produced', dataIndex: 'totalSuitProduced' },
              { title: 'Status', dataIndex: 'status' },
            ]}
          />
        </>
      ),
    },
  ]

  return (
    <div>
      <Title level={4} className="page-title">Reports & Export</Title>
      <Alert
        type="info"
        showIcon
        icon={<FileExcelOutlined />}
        style={{ marginBottom: 16 }}
        message="Export CSV or print PDF reports for sales, customers, inventory, and production."
        description="CSV opens in Excel. Print PDF uses your browser print dialog — choose Save as PDF."
      />

      {summary && (
        <Row gutter={16} style={{ marginBottom: 24 }}>
          <Col xs={12} sm={6}>
            <Card size="small"><Statistic title="Unpaid Bills" value={s.unpaidBills ?? 0} /></Card>
          </Col>
          <Col xs={12} sm={6}>
            <Card size="small"><Statistic title="Stock Units" value={s.totalStockUnits ?? 0} /></Card>
          </Col>
          <Col xs={12} sm={6}>
            <Card size="small"><Statistic title="Batches Active" value={s.batchesInProgress ?? 0} /></Card>
          </Col>
          <Col xs={12} sm={6}>
            <Card size="small">
              <Statistic title="Outstanding" prefix="Rs." value={Number(s.totalOutstandingBalance || 0).toLocaleString()} />
            </Card>
          </Col>
        </Row>
      )}

      <Tabs items={tabItems} />
      <ReportPrint report={printReport} />
    </div>
  )
}
