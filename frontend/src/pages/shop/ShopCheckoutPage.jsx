import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import {
  Typography, Form, Input, Button, Card, Alert, Spin, Row, Col, Divider, message,
} from 'antd'
import { CheckCircleOutlined, LoginOutlined } from '@ant-design/icons'
import { shopAPI } from '../../api/shop'
import { apiErrorMessage } from '../../api/client'
import { shopModuleEnabled } from '../../config/modules'
import { modulesAPI } from '../../api/modules'
import { useAuth } from '../../context/AuthContext'
import ShopStorefrontHeader from '../../components/shop/ShopStorefrontHeader'
import '../../styles/shop-portal.css'

const { Title, Text } = Typography
const { TextArea } = Input

export default function ShopCheckoutPage() {
  const navigate = useNavigate()
  const { auth } = useAuth()
  const [form] = Form.useForm()
  const [settings, setSettings] = useState(null)
  const [cart, setCart] = useState(null)
  const [loading, setLoading] = useState(true)
  const [submitting, setSubmitting] = useState(false)
  const [order, setOrder] = useState(null)
  const [error, setError] = useState(null)
  const [shopEnabled, setShopEnabled] = useState(shopModuleEnabled)

  const isCustomer = auth?.role === 'CUSTOMER' && auth?.token

  useEffect(() => {
    modulesAPI.getFlags()
      .then(r => { if (r.data?.shop?.enabled) setShopEnabled(true) })
      .catch(() => {})
  }, [])

  useEffect(() => {
    if (!shopEnabled) {
      setLoading(false)
      return
    }
    shopAPI.getPublicSettings().then(r => setSettings(r.data)).catch(() => {})
  }, [shopEnabled])

  useEffect(() => {
    if (!shopEnabled || !isCustomer) {
      setLoading(false)
      return
    }
    setLoading(true)
    shopAPI.getCart()
      .then(r => {
        setCart(r.data)
        if (!r.data?.items?.length) {
          setError('Your cart is empty.')
        }
      })
      .catch(err => setError(apiErrorMessage(err)))
      .finally(() => setLoading(false))
  }, [shopEnabled, isCustomer])

  const onSubmit = async (values) => {
    setSubmitting(true)
    setError(null)
    try {
      const res = await shopAPI.checkout({
        shippingNotes: values.shippingNotes,
        customerNotes: values.customerNotes,
      })
      setOrder(res.data)
      message.success('Order placed successfully')
    } catch (err) {
      setError(apiErrorMessage(err))
    } finally {
      setSubmitting(false)
    }
  }

  if (!shopEnabled) {
    return (
      <div className="shop-storefront" style={{ padding: 48, textAlign: 'center' }}>
        <Title level={3}>Shop not available</Title>
      </div>
    )
  }

  if (!isCustomer) {
    return (
      <div className="shop-storefront">
        <ShopStorefrontHeader settings={settings} showBack backTo="/store/cart" />
        <div className="shop-storefront-body shop-detail-page">
          <Alert
            type="info"
            showIcon
            message="Login required"
            description="Checkout is available for registered customer accounts. Your guest cart will sync after login."
            action={
              <Link to="/login">
                <Button type="primary" icon={<LoginOutlined />}>Login</Button>
              </Link>
            }
          />
        </div>
      </div>
    )
  }

  if (order) {
    return (
      <div className="shop-storefront">
        <ShopStorefrontHeader settings={settings} />
        <div className="shop-storefront-body shop-detail-page" style={{ textAlign: 'center', maxWidth: 520, margin: '0 auto' }}>
          <CheckCircleOutlined style={{ fontSize: 48, color: '#16a34a', marginBottom: 16 }} />
          <Title level={3}>Order placed</Title>
          <Text>Order number: <Text strong>{order.orderNumber}</Text></Text>
          <Divider />
          <Text>Total: Rs. {Number(order.totalAmount || 0).toLocaleString()}</Text>
          <p style={{ marginTop: 16 }}>
            <Text type="secondary">We will review your order and confirm it shortly.</Text>
          </p>
          <Button type="primary" onClick={() => navigate('/store')} style={{ marginTop: 24 }}>
            Continue shopping
          </Button>
        </div>
      </div>
    )
  }

  return (
    <div className="shop-storefront">
      <ShopStorefrontHeader settings={settings} showBack backTo="/store/cart" />

      <div className="shop-storefront-body shop-detail-page">
        <Title level={3}>Checkout</Title>

        {error && <Alert type="error" message={error} style={{ marginBottom: 16 }} showIcon />}

        {loading ? (
          <Spin size="large" />
        ) : cart?.items?.length ? (
          <Row gutter={[32, 24]}>
            <Col xs={24} md={14}>
              <Form form={form} layout="vertical" onFinish={onSubmit}>
                <Form.Item
                  name="shippingNotes"
                  label="Shipping / delivery notes"
                  extra="Address, contact phone, or delivery instructions."
                >
                  <TextArea rows={3} maxLength={500} placeholder="Delivery address and contact details" />
                </Form.Item>
                <Form.Item name="customerNotes" label="Order notes (optional)">
                  <TextArea rows={2} maxLength={500} placeholder="Any special requests" />
                </Form.Item>
                <Button type="primary" size="large" htmlType="submit" loading={submitting}>
                  Place order
                </Button>
              </Form>
            </Col>
            <Col xs={24} md={10}>
              <Card title="Order summary" className="shop-detail-price">
                {cart.items.map(item => (
                  <Row key={item.itemId || item.productId} justify="space-between" style={{ marginBottom: 8 }}>
                    <Text>{item.designName} × {item.quantity}</Text>
                    <Text>Rs. {Number(item.lineTotal || 0).toLocaleString()}</Text>
                  </Row>
                ))}
                <Divider style={{ margin: '12px 0' }} />
                <Row justify="space-between"><Text>Subtotal</Text><Text>Rs. {Number(cart.subtotal || 0).toLocaleString()}</Text></Row>
                {Number(cart.discountPercent) > 0 && (
                  <Row justify="space-between" style={{ marginTop: 8 }}>
                    <Text>Discount ({cart.discountPercent}%)</Text>
                    <Text type="success">− Rs. {Number(cart.discountAmount || 0).toLocaleString()}</Text>
                  </Row>
                )}
                <Row justify="space-between" style={{ marginTop: 12 }}>
                  <Title level={5} style={{ margin: 0 }}>Total</Title>
                  <Title level={5} style={{ margin: 0 }}>Rs. {Number(cart.total || 0).toLocaleString()}</Title>
                </Row>
              </Card>
            </Col>
          </Row>
        ) : null}
      </div>
    </div>
  )
}
