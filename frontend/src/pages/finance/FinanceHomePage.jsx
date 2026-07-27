import { useEffect, useState } from 'react'
import { Typography, Card, Tag, Spin, Button, Row, Col, Space } from 'antd'
import { useNavigate } from 'react-router-dom'
import {
  UnorderedListOutlined, FileTextOutlined, BarChartOutlined,
  QuestionCircleOutlined, CheckCircleOutlined, ShopOutlined,
} from '@ant-design/icons'
import { financeAPI } from '../../api/finance'
import { apiErrorMessage } from '../../api/client'

const { Title, Text, Paragraph } = Typography

const QUICK_ACTIONS = [
  {
    title: 'Chart of Accounts',
    description: 'View and manage your account list',
    path: '/finance/accounts',
    icon: <UnorderedListOutlined className="finance-action-card__icon" />,
  },
  {
    title: 'Journal Entries',
    description: 'Post manual or review auto entries',
    path: '/finance/journals',
    icon: <FileTextOutlined className="finance-action-card__icon" />,
  },
  {
    title: 'Payables',
    description: 'Vendor bills, payments & AP aging',
    path: '/finance/payables',
    icon: <ShopOutlined className="finance-action-card__icon" />,
  },
  {
    title: 'Reports',
    description: 'Trial balance, P&L, balance sheet & more',
    path: '/finance/reports',
    icon: <BarChartOutlined className="finance-action-card__icon" />,
  },
]

export default function FinanceHomePage() {
  const navigate = useNavigate()
  const [status, setStatus] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  useEffect(() => {
    financeAPI.getStatus()
      .then(r => setStatus(r.data))
      .catch(err => setError(apiErrorMessage(err)))
      .finally(() => setLoading(false))
  }, [])

  if (loading) {
    return <div style={{ textAlign: 'center', padding: 48 }}><Spin size="large" /></div>
  }

  return (
    <div>
      <Title level={3} className="finance-page-title">Finance overview</Title>
      <Text className="finance-page-subtitle">
        A simple workspace for accounting — separate from daily factory operations.
      </Text>

      <Card className="finance-welcome-card" style={{ marginBottom: 24 }}>
        <Space direction="vertical" size="small" style={{ width: '100%' }}>
          <Title level={5} style={{ margin: 0, color: '#0f766e' }}>
            <CheckCircleOutlined style={{ marginRight: 8 }} />
            Welcome to the Finance Portal
          </Title>
          <Paragraph style={{ marginBottom: 0, color: '#475569' }}>
            Use the menu on the left for accounts, journals, and reports.
            Click <Text strong>Help</Text> in the header anytime for a step-by-step guide.
          </Paragraph>
          {status && (
            <Space wrap style={{ marginTop: 8 }}>
              <Tag color="cyan">Phase {status.currentPhase}</Tag>
              {status.autoPostAr && <Tag color="green">Auto-post bills &amp; payments</Tag>}
              {status.autoPostInventory && <Tag color="green">Auto-post inventory &amp; COGS</Tag>}
            </Space>
          )}
        </Space>
      </Card>

      {error && (
        <Card style={{ marginBottom: 16, borderColor: '#fecaca' }}>
          <Text type="danger">{error}</Text>
        </Card>
      )}

      <Title level={5} style={{ marginBottom: 16 }}>Quick actions</Title>
      <Row gutter={[16, 16]}>
        {QUICK_ACTIONS.map(action => (
          <Col xs={24} sm={12} lg={8} key={action.path}>
            <Card
              className="finance-action-card"
              hoverable
              onClick={() => navigate(action.path)}
            >
              {action.icon}
              <Title level={5} style={{ marginBottom: 4 }}>{action.title}</Title>
              <Text type="secondary">{action.description}</Text>
            </Card>
          </Col>
        ))}
      </Row>

      <Card style={{ marginTop: 24, borderRadius: 12 }}>
        <Title level={5} style={{ marginBottom: 8 }}>
          <QuestionCircleOutlined style={{ color: '#0d9488', marginRight: 8 }} />
          New to finance?
        </Title>
        <Paragraph type="secondary" style={{ marginBottom: 12 }}>
          {status?.message || 'Open Help from the top bar for a short tutorial on accounts, journals, and reports.'}
        </Paragraph>
        <Button type="primary" onClick={() => navigate('/finance/accounts')}>
          Start with Chart of Accounts
        </Button>
      </Card>
    </div>
  )
}
