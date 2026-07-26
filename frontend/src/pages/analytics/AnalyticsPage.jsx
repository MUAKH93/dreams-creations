import { useEffect, useState } from 'react'
import {
  Typography, Card, Row, Col, Table, Tag, Spin, Statistic, Select, Alert,
} from 'antd'
import {
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer,
  Legend,
} from 'recharts'
import { analyticsAPI } from '../../api/analytics'
import { apiErrorMessage } from '../../api/client'

const { Title, Text } = Typography

const fmtRs = (v) => `Rs. ${Number(v || 0).toLocaleString(undefined, { maximumFractionDigits: 0 })}`

export default function AnalyticsPage() {
  const [data, setData] = useState(null)
  const [loading, setLoading] = useState(true)
  const [months, setMonths] = useState(6)
  const [error, setError] = useState(null)

  useEffect(() => {
    setLoading(true)
    setError(null)
    analyticsAPI.getDashboard(months)
      .then(r => setData(r.data))
      .catch(err => setError(apiErrorMessage(err)))
      .finally(() => setLoading(false))
  }, [months])

  if (loading && !data) {
    return <div style={{ textAlign: 'center', padding: 80 }}><Spin size="large" /></div>
  }

  const sup = data?.supervisorPerformance || []
  const topDesigns = data?.topDesigns || []
  const topCustomers = data?.topCustomers || []
  const production = (data?.productionByMonth || []).map(p => ({
    month: p.month,
    units: Number(p.unitsProduced || 0),
    batches: Number(p.batchesCompleted || 0),
  }))
  const profitability = data?.designProfitability || []

  const supervisorColumns = [
    { title: 'Supervisor', dataIndex: 'supervisorName', key: 'name' },
    { title: 'Completed', dataIndex: 'completedCount', key: 'done' },
    { title: 'On-time %', dataIndex: 'onTimeRate', key: 'ontime',
      render: v => <Tag color={v >= 80 ? 'green' : v >= 50 ? 'orange' : 'red'}>{v}%</Tag> },
    { title: 'Overdue', dataIndex: 'overdueActiveCount', key: 'overdue',
      render: v => v > 0 ? <Tag color="red">{v}</Tag> : '0' },
    { title: 'Yield %', dataIndex: 'yieldRate', key: 'yield', render: v => `${v}%` },
    { title: 'Damage %', dataIndex: 'damageRate', key: 'dmg',
      render: v => <Tag color={v > 5 ? 'red' : 'default'}>{v}%</Tag> },
    { title: 'Missing %', dataIndex: 'missingRate', key: 'miss', render: v => `${v}%` },
    { title: 'Pieces OK', dataIndex: 'piecesReturnedOk', key: 'ok' },
  ]

  const designColumns = [
    { title: 'Design', dataIndex: 'designCode', key: 'code' },
    { title: 'Name', dataIndex: 'name', key: 'name', ellipsis: true },
    { title: 'Units Sold', dataIndex: 'unitsSold', key: 'units' },
    { title: 'Revenue', dataIndex: 'revenue', key: 'rev', render: v => fmtRs(v) },
  ]

  const customerColumns = [
    { title: 'Customer', dataIndex: 'customerName', key: 'name' },
    { title: 'Bills', dataIndex: 'billCount', key: 'bills' },
    { title: 'Revenue', dataIndex: 'revenue', key: 'rev', render: v => fmtRs(v) },
  ]

  const profitColumns = [
    { title: 'Design', dataIndex: 'designCode', key: 'code' },
    { title: 'Units', dataIndex: 'unitsSold', key: 'units' },
    { title: 'Revenue', dataIndex: 'revenue', key: 'rev', render: v => fmtRs(v) },
    { title: 'Cost/Unit', dataIndex: 'productionCostPerUnit', key: 'cpu',
      render: v => Number(v) > 0 ? fmtRs(v) : <Text type="secondary">—</Text> },
    { title: 'Profit', dataIndex: 'profit', key: 'profit',
      render: v => <Text type={v >= 0 ? 'success' : 'danger'}>{fmtRs(v)}</Text> },
    { title: 'Margin', dataIndex: 'profitMarginPercent', key: 'margin',
      render: v => `${v}%` },
  ]

  return (
    <div>
      <div className="page-toolbar">
        <Title level={4} className="page-title">Production Intelligence</Title>
        <Select value={months} onChange={setMonths} style={{ width: 160 }}
          options={[
            { value: 3, label: 'Last 3 months' },
            { value: 6, label: 'Last 6 months' },
            { value: 12, label: 'Last 12 months' },
          ]} />
      </div>

      {error && <Alert type="error" message={error} style={{ marginBottom: 16 }} showIcon />}

      <Row gutter={[16, 16]} style={{ marginBottom: 16 }}>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic title="Stock Turnover" value={data?.stockTurnoverRatio ?? 0}
              suffix="×" precision={2}
              tooltip="Units sold ÷ current stock (higher = faster moving inventory)" />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic title="Supervisors Tracked" value={sup.length} />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic title="Top Design Revenue" value={topDesigns[0]?.revenue ?? 0}
              prefix="Rs." precision={0} />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card>
            <Statistic title="Active Overdue Dispatches"
              value={sup.reduce((s, r) => s + (r.overdueActiveCount || 0), 0)} />
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]} style={{ marginBottom: 16 }}>
        <Col xs={24} lg={12}>
          <Card title="Production Output">
            <ResponsiveContainer width="100%" height={260}>
              <BarChart data={production}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="month" tick={{ fontSize: 11 }} />
                <YAxis />
                <Tooltip />
                <Legend />
                <Bar dataKey="units" name="Units produced" fill="#1565c0" radius={[4, 4, 0, 0]} />
                <Bar dataKey="batches" name="Batches completed" fill="#2e7d32" radius={[4, 4, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </Card>
        </Col>
        <Col xs={24} lg={12}>
          <Card title="Top Designs by Revenue">
            <ResponsiveContainer width="100%" height={260}>
              <BarChart data={topDesigns.slice(0, 6)} layout="vertical" margin={{ left: 20 }}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis type="number" tickFormatter={v => `${(v / 1000).toFixed(0)}k`} />
                <YAxis type="category" dataKey="designCode" width={80} tick={{ fontSize: 11 }} />
                <Tooltip formatter={v => fmtRs(v)} />
                <Bar dataKey="revenue" fill="#1a237e" radius={[0, 4, 4, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </Card>
        </Col>
      </Row>

      <Card title="Supervisor Performance" style={{ marginBottom: 16 }}>
        <Table dataSource={sup} columns={supervisorColumns} rowKey="supervisorId"
          pagination={false} size="small" scroll={{ x: 800 }} />
      </Card>

      <Row gutter={[16, 16]} style={{ marginBottom: 16 }}>
        <Col xs={24} lg={12}>
          <Card title="Top Customers">
            <Table dataSource={topCustomers} columns={customerColumns} rowKey="customerId"
              pagination={false} size="small" />
          </Card>
        </Col>
        <Col xs={24} lg={12}>
          <Card title="Top Designs (detail)">
            <Table dataSource={topDesigns} columns={designColumns} rowKey="designId"
              pagination={false} size="small" />
          </Card>
        </Col>
      </Row>

      <Card title="Design Profitability">
        <Alert type="info" showIcon style={{ marginBottom: 12 }}
          message="Set production cost per design in Designs Catalog → Edit to see margin. Without cost, profit shows revenue only." />
        <Table dataSource={profitability} columns={profitColumns} rowKey="designId"
          pagination={{ pageSize: 10 }} size="small" scroll={{ x: 700 }} />
      </Card>
    </div>
  )
}
