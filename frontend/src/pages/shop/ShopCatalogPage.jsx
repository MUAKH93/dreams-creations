import { useEffect, useState } from 'react'
import {
  Typography, Card, Row, Col, Tag, Spin, Empty, Statistic, Table, Button, Space,
} from 'antd'
import { GlobalOutlined } from '@ant-design/icons'
import { shopAPI } from '../../api/shop'
import { apiErrorMessage } from '../../api/client'

const { Title, Text } = Typography

export default function ShopCatalogPage() {
  const [catalog, setCatalog] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  useEffect(() => {
    shopAPI.getCatalog()
      .then(r => setCatalog(r.data || []))
      .catch(err => setError(apiErrorMessage(err)))
      .finally(() => setLoading(false))
  }, [])

  if (loading) {
    return <div style={{ textAlign: 'center', padding: 48 }}><Spin size="large" /></div>
  }

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 16 }}>
        <div>
          <Title level={3} className="shop-page-title">Catalog preview</Title>
          <Text className="shop-page-subtitle" style={{ marginBottom: 0 }}>
            Active designs with listed products — same data as the public storefront.
          </Text>
        </div>
        <Button icon={<GlobalOutlined />} onClick={() => window.open('/store', '_blank')}>
          Open storefront
        </Button>
      </div>

      {error && (
        <Card style={{ marginBottom: 16, borderColor: '#fecaca' }}>
          <Text type="danger">{error}</Text>
        </Card>
      )}

      {catalog.length === 0 ? (
        <Empty description="No shop listings yet — add active products in Operations." />
      ) : (
        <Row gutter={[16, 16]}>
          {catalog.map(item => (
            <Col xs={24} sm={12} lg={8} key={item.designId}>
              <Card
                className="shop-product-card"
                cover={
                  item.primaryImageUrl ? (
                    <img
                      src={item.primaryImageUrl}
                      alt={item.name}
                      style={{ height: 220, width: '100%', objectFit: 'cover', display: 'block' }}
                    />
                  ) : (
                    <div className="shop-product-card--placeholder">✦</div>
                  )
                }
              >
                <Space direction="vertical" size={4} style={{ width: '100%' }}>
                  <Text type="secondary">{item.designCode}</Text>
                  <Title level={5} style={{ margin: 0 }}>{item.name}</Title>
                  {item.categoryName && <Tag>{item.categoryName}</Tag>}
                  {item.featured && <Tag color="gold">Featured</Tag>}
                  <Statistic
                    title="From"
                    value={Number(item.basePrice || 0)}
                    prefix="Rs."
                    valueStyle={{ fontSize: 18 }}
                  />
                  <Text type={item.totalStock > 0 ? 'success' : 'secondary'}>
                    {item.totalStock > 0 ? `${item.totalStock} in stock` : 'Out of stock'}
                  </Text>
                  <Text type="secondary">{item.variants?.length || 0} variant(s)</Text>
                </Space>
              </Card>
            </Col>
          ))}
        </Row>
      )}

      {catalog.length > 0 && (
        <Card title="Variant detail" style={{ marginTop: 24 }}>
          <Table
            size="small"
            pagination={false}
            rowKey={r => `${r.designCode}-${r.sizeValue}-${r.color}-${r.productId}`}
            dataSource={catalog.flatMap(d =>
              (d.variants || []).map(v => ({
                ...v,
                designCode: d.designCode,
                designName: d.name,
              }))
            )}
            columns={[
              { title: 'Design', dataIndex: 'designName' },
              { title: 'Size', dataIndex: 'sizeValue', render: v => v || '—' },
              { title: 'Color', dataIndex: 'color', render: v => v || '—' },
              { title: 'Price', dataIndex: 'sellingPrice',
                render: v => `Rs. ${Number(v || 0).toLocaleString()}` },
              { title: 'Stock', dataIndex: 'stockQty' },
              { title: 'Status', key: 'stock',
                render: (_, r) => (
                  <Tag color={r.inStock ? 'green' : 'default'}>
                    {r.inStock ? 'In stock' : 'Out of stock'}
                  </Tag>
                ) },
            ]}
          />
        </Card>
      )}
    </div>
  )
}