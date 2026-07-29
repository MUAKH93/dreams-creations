import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import {
  Typography, Row, Col, Card, Tag, Spin, Empty, Button, Alert,
} from 'antd'
import { LoginOutlined, ShoppingOutlined } from '@ant-design/icons'
import { shopAPI } from '../../api/shop'
import { apiErrorMessage } from '../../api/client'
import { shopModuleEnabled } from '../../config/modules'
import { modulesAPI } from '../../api/modules'
import '../../styles/shop-portal.css'

const { Title, Text, Paragraph } = Typography

export default function ShopStorefrontPage() {
  const [settings, setSettings] = useState(null)
  const [catalog, setCatalog] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [shopEnabled, setShopEnabled] = useState(shopModuleEnabled)

  useEffect(() => {
    modulesAPI.getFlags()
      .then(r => {
        if (r.data?.shop?.enabled) setShopEnabled(true)
      })
      .catch(() => {})
  }, [])

  useEffect(() => {
    if (!shopEnabled) {
      setLoading(false)
      return
    }
    Promise.all([
      shopAPI.getPublicSettings(),
      shopAPI.getCatalog(),
    ])
      .then(([settingsRes, catalogRes]) => {
        setSettings(settingsRes.data)
        setCatalog(catalogRes.data || [])
      })
      .catch(err => setError(apiErrorMessage(err)))
      .finally(() => setLoading(false))
  }, [shopEnabled])

  if (!shopEnabled) {
    return (
      <div className="shop-storefront" style={{ padding: 48, textAlign: 'center' }}>
        <Title level={3}>Online shop is not available</Title>
        <Paragraph type="secondary">
          Enable <Text code>modules.shop.enabled=true</Text> in backend{' '}
          <Text code>application.properties</Text>, run{' '}
          <Text code>add-shop-module.sql</Text>, then restart the backend.
        </Paragraph>
        <Link to="/login"><Button type="primary">Go to login</Button></Link>
      </div>
    )
  }

  if (loading) {
    return (
      <div className="shop-storefront" style={{ textAlign: 'center', padding: 80 }}>
        <Spin size="large" />
      </div>
    )
  }

  if (settings && !settings.storefrontEnabled) {
    return (
      <div className="shop-storefront" style={{ padding: 48, textAlign: 'center' }}>
        <Title level={3}>{settings.storeName || 'Dreams Creations Shop'}</Title>
        <Paragraph>Our online storefront is temporarily closed. Please check back soon.</Paragraph>
        <Link to="/login"><Button>Customer login</Button></Link>
      </div>
    )
  }

  return (
    <div className="shop-storefront">
      <header className="shop-storefront-header">
        <Title level={2}>{settings?.storeName || 'Dreams Creations Shop'}</Title>
        {settings?.tagline && <p>{settings.tagline}</p>}
        <div style={{ marginTop: 16 }}>
          <Link to="/login">
            <Button type="default" icon={<LoginOutlined />}>Login to order</Button>
          </Link>
        </div>
      </header>

      <div className="shop-storefront-body">
        {error && (
          <Alert type="error" message={error} style={{ marginBottom: 24 }} showIcon />
        )}

        <Alert
          type="info"
          showIcon
          style={{ marginBottom: 24 }}
          message="Browse only — cart & checkout coming in Phase S3–S4"
          description="Login as a customer to view quotes and bills in the operations portal."
        />

        {catalog.length === 0 ? (
          <Empty description="No products listed yet" />
        ) : (
          <Row gutter={[20, 20]}>
            {catalog.map(item => (
              <Col xs={24} sm={12} md={8} lg={6} key={item.designId}>
                <Card
                  className="shop-product-card"
                  hoverable
                  cover={
                    item.primaryImageUrl ? (
                      <img
                        src={item.primaryImageUrl}
                        alt={item.name}
                        style={{ height: 220, width: '100%', objectFit: 'cover', display: 'block' }}
                      />
                    ) : (
                      <div className="shop-product-card--placeholder">
                        <ShoppingOutlined />
                      </div>
                    )
                  }
                >
                  <Text type="secondary" style={{ fontSize: 12 }}>{item.designCode}</Text>
                  <Title level={5} style={{ margin: '4px 0 8px' }}>{item.name}</Title>
                  {item.categoryName && <Tag style={{ marginBottom: 8 }}>{item.categoryName}</Tag>}
                  {item.featured && <Tag color="gold">Featured</Tag>}
                  <div>
                    <Text strong>From Rs. {Number(item.basePrice || 0).toLocaleString()}</Text>
                  </div>
                  <Text
                    type={item.totalStock > 0 ? 'success' : 'secondary'}
                    style={{ display: 'block', marginTop: 4 }}
                  >
                    {item.totalStock > 0 ? `${item.totalStock} available` : 'Out of stock'}
                  </Text>
                </Card>
              </Col>
            ))}
          </Row>
        )}
      </div>
    </div>
  )
}
