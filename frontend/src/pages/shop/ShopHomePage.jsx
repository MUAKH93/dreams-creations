import { useEffect, useState } from 'react'
import { Typography, Card, Tag, Spin, Button, Row, Col, Space } from 'antd'
import { useNavigate } from 'react-router-dom'
import {
  SettingOutlined, ShoppingOutlined, GlobalOutlined, CheckCircleOutlined,
} from '@ant-design/icons'
import { shopAPI } from '../../api/shop'
import { apiErrorMessage } from '../../api/client'

const { Title, Text, Paragraph } = Typography

const QUICK_ACTIONS = [
  {
    title: 'Shop Settings',
    description: 'Store name, tagline, and storefront toggle',
    path: '/shop/settings',
    icon: <SettingOutlined className="shop-action-card__icon" />,
  },
  {
    title: 'Orders',
    description: 'Review, confirm, and convert shop orders',
    path: '/shop/orders',
    icon: <ShoppingOutlined className="shop-action-card__icon" />,
  },
  {
    title: 'Analytics',
    description: 'Revenue, orders, and top designs',
    path: '/shop/analytics',
    icon: <ShoppingOutlined className="shop-action-card__icon" />,
  },
  {
    title: 'Catalog Preview',
    description: 'See what customers see on the storefront',
    path: '/shop/catalog',
    icon: <ShoppingOutlined className="shop-action-card__icon" />,
  },
  {
    title: 'Public Storefront',
    description: 'Open /store in a new tab',
    path: '/store',
    external: true,
    icon: <GlobalOutlined className="shop-action-card__icon" />,
  },
]

export default function ShopHomePage() {
  const navigate = useNavigate()
  const [status, setStatus] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  useEffect(() => {
    shopAPI.getStatus()
      .then(r => setStatus(r.data))
      .catch(err => setError(apiErrorMessage(err)))
      .finally(() => setLoading(false))
  }, [])

  if (loading) {
    return <div style={{ textAlign: 'center', padding: 48 }}><Spin size="large" /></div>
  }

  return (
    <div>
      <Title level={3} className="shop-page-title">Shop overview</Title>
      <Text className="shop-page-subtitle">
        Configure and preview your online storefront — separate from factory operations.
      </Text>

      {error && (
        <Card style={{ marginBottom: 24, borderColor: '#fecaca' }}>
          <Text type="danger">{error}</Text>
        </Card>
      )}

      <Card className="shop-welcome-card" style={{ marginBottom: 24 }}>
        <Space direction="vertical" size="small" style={{ width: '100%' }}>
          <Title level={5} style={{ margin: 0, color: '#9a3412' }}>
            <CheckCircleOutlined style={{ marginRight: 8 }} />
            Welcome to the Shop Portal
          </Title>
          <Paragraph style={{ marginBottom: 0, color: '#57534e' }}>
            Manage your online storefront, orders, payments, and analytics.
          </Paragraph>
          {status && (
            <Space wrap style={{ marginTop: 8 }}>
              <Tag color="orange">Phase {status.currentPhase}</Tag>
              {status.storefrontEnabled
                ? <Tag color="green">Storefront open</Tag>
                : <Tag color="red">Storefront closed</Tag>}
              {status.allowGuestBrowse && <Tag>Guest browse</Tag>}
            </Space>
          )}
        </Space>
      </Card>

      <Row gutter={[16, 16]}>
        {QUICK_ACTIONS.map(action => (
          <Col xs={24} sm={12} lg={8} key={action.title}>
            <Card
              className="shop-action-card"
              hoverable
              onClick={() => {
                if (action.external) {
                  window.open(action.path, '_blank')
                } else {
                  navigate(action.path)
                }
              }}
            >
              {action.icon}
              <Title level={5} style={{ margin: '0 0 4px' }}>{action.title}</Title>
              <Text type="secondary">{action.description}</Text>
            </Card>
          </Col>
        ))}
      </Row>

      {status?.upcomingPhases?.length > 0 && (
        <Card title="Roadmap" style={{ marginTop: 24 }}>
          <ul style={{ margin: 0, paddingLeft: 20, color: '#57534e' }}>
            {status.upcomingPhases.map(phase => (
              <li key={phase}>{phase}</li>
            ))}
          </ul>
        </Card>
      )}
    </div>
  )
}
