import { useEffect, useState } from 'react'
import { Typography, Card, Row, Col, Statistic, Table, Spin, Alert } from 'antd'
import { shopAPI } from '../../api/shop'
import { apiErrorMessage } from '../../api/client'

const { Title, Text } = Typography

export default function ShopAnalyticsPage() {
  const [data, setData] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  useEffect(() => {
    shopAPI.getAnalytics()
      .then(r => setData(r.data))
      .catch(err => setError(apiErrorMessage(err)))
      .finally(() => setLoading(false))
  }, [])

  if (loading) {
    return <div style={{ textAlign: 'center', padding: 48 }}><Spin size="large" /></div>
  }

  return (
    <div>
      <Title level={3} className="shop-page-title">Shop analytics</Title>
      <Text className="shop-page-subtitle">Orders, revenue, and top-selling designs from the online shop.</Text>

      {error && <Alert type="error" message={error} style={{ marginBottom: 16 }} showIcon />}

      {data && (
        <>
          <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
            <Col xs={12} md={6}>
              <Card><Statistic title="Total orders" value={data.totalOrders} /></Card>
            </Col>
            <Col xs={12} md={6}>
              <Card><Statistic title="Pending" value={data.pendingOrders} valueStyle={{ color: '#ca8a04' }} /></Card>
            </Col>
            <Col xs={12} md={6}>
              <Card><Statistic title="Fulfilled" value={data.fulfilledOrders} valueStyle={{ color: '#16a34a' }} /></Card>
            </Col>
            <Col xs={12} md={6}>
              <Card><Statistic title="Cancelled" value={data.cancelledOrders} /></Card>
            </Col>
          </Row>

          <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
            <Col xs={24} md={8}>
              <Card><Statistic title="Revenue" prefix="Rs." value={Number(data.totalRevenue || 0).toLocaleString()} /></Card>
            </Col>
            <Col xs={24} md={8}>
              <Card><Statistic title="Collected" prefix="Rs." value={Number(data.totalPaid || 0).toLocaleString()} valueStyle={{ color: '#16a34a' }} /></Card>
            </Col>
            <Col xs={24} md={8}>
              <Card><Statistic title="Outstanding" prefix="Rs." value={Number(data.outstanding || 0).toLocaleString()} valueStyle={{ color: '#dc2626' }} /></Card>
            </Col>
          </Row>

          <Card title="Top designs">
            <Table
              rowKey="designId"
              pagination={false}
              dataSource={data.topDesigns || []}
              locale={{ emptyText: 'No sales data yet' }}
              columns={[
                { title: 'Code', dataIndex: 'designCode' },
                { title: 'Design', dataIndex: 'designName' },
                { title: 'Qty sold', dataIndex: 'quantitySold' },
                {
                  title: 'Revenue',
                  dataIndex: 'revenue',
                  render: v => `Rs. ${Number(v || 0).toLocaleString()}`,
                },
              ]}
            />
          </Card>
        </>
      )}
    </div>
  )
}
