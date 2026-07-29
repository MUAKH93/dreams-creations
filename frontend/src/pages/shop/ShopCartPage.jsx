import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import {
  Typography, Table, Button, InputNumber, Space, Alert, Spin, Popconfirm, Row, Col, Card,
} from 'antd'
import { DeleteOutlined, LoginOutlined, ShoppingOutlined } from '@ant-design/icons'
import { shopAPI } from '../../api/shop'
import { apiErrorMessage } from '../../api/client'
import { shopModuleEnabled } from '../../config/modules'
import { modulesAPI } from '../../api/modules'
import { useShopCart } from '../../hooks/useShopCart'
import { useAuth } from '../../context/AuthContext'
import ShopStorefrontHeader from '../../components/shop/ShopStorefrontHeader'
import '../../styles/shop-portal.css'

const { Title, Text } = Typography

export default function ShopCartPage() {
  const navigate = useNavigate()
  const { auth } = useAuth()
  const { cart, loading, isGuest, updateQuantity, removeItem, clearCart } = useShopCart()
  const [settings, setSettings] = useState(null)
  const [shopEnabled, setShopEnabled] = useState(shopModuleEnabled)
  const [error, setError] = useState(null)

  useEffect(() => {
    modulesAPI.getFlags()
      .then(r => { if (r.data?.shop?.enabled) setShopEnabled(true) })
      .catch(() => {})
  }, [])

  useEffect(() => {
    if (!shopEnabled) return
    shopAPI.getPublicSettings().then(r => setSettings(r.data)).catch(() => {})
  }, [shopEnabled])

  const handleQtyChange = async (record, value) => {
    if (!value || value < 1) return
    try {
      setError(null)
      await updateQuantity(record.keyId, value, record.isGuest)
    } catch (err) {
      setError(apiErrorMessage(err))
    }
  }

  const handleRemove = async (record) => {
    try {
      setError(null)
      await removeItem(record.keyId, record.isGuest)
    } catch (err) {
      setError(apiErrorMessage(err))
    }
  }

  if (!shopEnabled) {
    return (
      <div className="shop-storefront" style={{ padding: 48, textAlign: 'center' }}>
        <Title level={3}>Shop not available</Title>
      </div>
    )
  }

  const rows = (cart?.items || []).map(item => ({
    ...item,
    keyId: isGuest ? item.productId : item.itemId,
    isGuest,
  }))

  return (
    <div className="shop-storefront">
      <ShopStorefrontHeader settings={settings} showBack />

      <div className="shop-storefront-body shop-detail-page">
        <Title level={3}>Your cart</Title>

        {isGuest && (
          <Alert
            type="info"
            showIcon
            style={{ marginBottom: 16 }}
            message="Guest cart"
            description="Items are saved in this browser. Login as a customer to sync your cart across devices."
            action={
              <Link to="/login">
                <Button type="primary" size="small" icon={<LoginOutlined />}>Login</Button>
              </Link>
            }
          />
        )}

        {error && <Alert type="error" message={error} style={{ marginBottom: 16 }} showIcon />}

        {loading && !cart ? (
          <Spin size="large" />
        ) : rows.length === 0 ? (
          <Card>
            <Space direction="vertical">
              <ShoppingOutlined style={{ fontSize: 32, color: '#ea580c' }} />
              <Text>Your cart is empty.</Text>
              <Button type="primary" onClick={() => navigate('/store')}>Browse shop</Button>
            </Space>
          </Card>
        ) : (
          <>
            <Table
              dataSource={rows}
              rowKey="keyId"
              pagination={false}
              columns={[
                {
                  title: 'Item',
                  key: 'item',
                  render: (_, r) => (
                    <Space>
                      {r.primaryImageUrl && (
                        <img src={r.primaryImageUrl} alt="" style={{ width: 48, height: 48, objectFit: 'cover', borderRadius: 4 }} />
                      )}
                      <div>
                        <Text strong>{r.designName}</Text>
                        <br />
                        <Text type="secondary">{r.designCode} — {r.sizeValue || '—'} / {r.color || '—'}</Text>
                      </div>
                    </Space>
                  ),
                },
                {
                  title: 'Price',
                  dataIndex: 'unitPrice',
                  render: v => `Rs. ${Number(v || 0).toLocaleString()}`,
                },
                {
                  title: 'Qty',
                  key: 'qty',
                  render: (_, r) => (
                    <InputNumber
                      min={1}
                      max={r.stockAvailable || undefined}
                      value={r.quantity}
                      onChange={val => handleQtyChange(r, val)}
                    />
                  ),
                },
                {
                  title: 'Line total',
                  dataIndex: 'lineTotal',
                  render: (v, r) => `Rs. ${Number(v ?? (r.unitPrice * r.quantity) || 0).toLocaleString()}`,
                },
                {
                  title: '',
                  key: 'act',
                  render: (_, r) => (
                    <Popconfirm title="Remove this item?" onConfirm={() => handleRemove(r)}>
                      <Button danger size="small" icon={<DeleteOutlined />} />
                    </Popconfirm>
                  ),
                },
              ]}
            />

            <Row gutter={24} style={{ marginTop: 24 }}>
              <Col xs={24} md={12}>
                <Popconfirm title="Clear entire cart?" onConfirm={clearCart}>
                  <Button danger>Clear cart</Button>
                </Popconfirm>
              </Col>
              <Col xs={24} md={12}>
                <Card className="shop-detail-price">
                  <Row justify="space-between"><Text>Subtotal</Text><Text>Rs. {Number(cart.subtotal || 0).toLocaleString()}</Text></Row>
                  {Number(cart.discountPercent) > 0 && (
                    <Row justify="space-between" style={{ marginTop: 8 }}>
                      <Text>Discount ({cart.discountPercent}%)</Text>
                      <Text type="success">− Rs. {Number(cart.discountAmount || 0).toLocaleString()}</Text>
                    </Row>
                  )}
                  <Row justify="space-between" style={{ marginTop: 12 }}>
                    <Title level={4} style={{ margin: 0 }}>Total</Title>
                    <Title level={4} style={{ margin: 0 }}>Rs. {Number(cart.total ?? cart.subtotal || 0).toLocaleString()}</Title>
                  </Row>
                </Card>
                <Button type="primary" size="large" block style={{ marginTop: 16 }} disabled>
                  Proceed to checkout (Phase S4)
                </Button>
                {!auth && (
                  <Link to="/login" style={{ display: 'block', marginTop: 8, textAlign: 'center' }}>
                    Login to save cart & checkout later
                  </Link>
                )}
              </Col>
            </Row>
          </>
        )}
      </div>
    </div>
  )
}
