import { useEffect, useState } from 'react'
import { Card, Row, Col, Spin } from 'antd'
import {
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer,
  PieChart, Pie, Cell, Legend,
} from 'recharts'
import { dashboardAPI } from '../../api/dashboard'

const PIPELINE_COLORS = {
  planned: '#8c8c8c',
  in_progress: '#1890ff',
  completed: '#52c41a',
  cancelled: '#ff4d4f',
}

const fmtRs = (v) => `Rs. ${Number(v || 0).toLocaleString()}`

export default function DashboardCharts() {
  const [charts, setCharts] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    dashboardAPI.getCharts()
      .then(r => setCharts(r.data))
      .catch(() => setCharts(null))
      .finally(() => setLoading(false))
  }, [])

  if (loading) {
    return <div style={{ textAlign: 'center', padding: 24 }}><Spin /></div>
  }

  if (!charts) return null

  const salesData = (charts.salesByMonth || []).map(p => ({
    month: p.month,
    amount: Number(p.amount || 0),
  }))

  const pipelineData = Object.entries(charts.productionPipeline || {}).map(([name, value]) => ({
    name: name.replace('_', ' '),
    key: name,
    value: Number(value || 0),
  }))

  return (
    <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
      <Col xs={24} lg={14}>
        <Card title="Sales Trend (last 6 months)">
          <ResponsiveContainer width="100%" height={260}>
            <BarChart data={salesData} margin={{ top: 8, right: 8, left: 0, bottom: 0 }}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="month" tick={{ fontSize: 11 }} />
              <YAxis tickFormatter={v => `${(v / 1000).toFixed(0)}k`} />
              <Tooltip formatter={(v) => fmtRs(v)} />
              <Bar dataKey="amount" name="Sales" fill="#1a237e" radius={[4, 4, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </Card>
      </Col>
      <Col xs={24} lg={10}>
        <Card title="Production Pipeline">
          <ResponsiveContainer width="100%" height={260}>
            <PieChart>
              <Pie
                data={pipelineData}
                dataKey="value"
                nameKey="name"
                cx="50%"
                cy="50%"
                outerRadius={90}
                label={({ name, value }) => (value > 0 ? `${name}: ${value}` : '')}
              >
                {pipelineData.map(entry => (
                  <Cell key={entry.key} fill={PIPELINE_COLORS[entry.key] || '#8884d8'} />
                ))}
              </Pie>
              <Tooltip />
              <Legend />
            </PieChart>
          </ResponsiveContainer>
        </Card>
      </Col>
    </Row>
  )
}
