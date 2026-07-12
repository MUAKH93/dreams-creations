import { Card, Typography, Row, Col } from 'antd'
import { Link, Navigate } from 'react-router-dom'
import {
  SafetyCertificateOutlined, TeamOutlined, ShoppingOutlined,
} from '@ant-design/icons'
import { useAuth } from '../../context/AuthContext'
import { homeForRole } from '../../utils/roles'

const { Title, Text } = Typography

const PORTAL_CARDS = [
  {
    key: 'management',
    path: '/login/management',
    icon: <SafetyCertificateOutlined style={{ fontSize: 36, color: '#1a237e' }} />,
    title: 'Management',
    description: 'Admin & Manager — production, inventory, sales, reports',
    note: 'Manager uses the same portal as Admin',
  },
  {
    key: 'supervisor',
    path: '/login/supervisor',
    icon: <TeamOutlined style={{ fontSize: 36, color: '#1565c0' }} />,
    title: 'Supervisor',
    description: 'View assignments, record returns, track due dates',
  },
  {
    key: 'customer',
    path: '/login/customer',
    icon: <ShoppingOutlined style={{ fontSize: 36, color: '#2e7d32' }} />,
    title: 'Customer',
    description: 'Browse designs, view bills, and check your balance',
  },
]

export default function LoginPage() {
  const { auth } = useAuth()

  if (auth) {
    return <Navigate to={homeForRole(auth.role)} replace />
  }

  return (
    <div style={{
      minHeight: '100vh',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      background: 'linear-gradient(135deg, #1a237e 0%, #283593 100%)',
      padding: 24,
    }}>
      <div style={{ maxWidth: 900, width: '100%' }}>
        <div style={{ textAlign: 'center', marginBottom: 32 }}>
          <Title level={2} style={{ color: '#fff', marginBottom: 8 }}>
            Dreams Creations
          </Title>
          <Text style={{ color: 'rgba(255,255,255,0.75)' }}>
            Choose your portal to sign in
          </Text>
        </div>

        <Row gutter={[16, 16]}>
          {PORTAL_CARDS.map(card => (
            <Col key={card.key} xs={24} md={8}>
              <Link to={card.path} style={{ textDecoration: 'none' }}>
                <Card
                  hoverable
                  style={{ height: '100%', borderRadius: 12, textAlign: 'center' }}
                  styles={{ body: { padding: 28 } }}
                >
                  <div style={{ marginBottom: 16 }}>{card.icon}</div>
                  <Title level={4} style={{ marginBottom: 8 }}>{card.title}</Title>
                  <Text type="secondary">{card.description}</Text>
                  {card.note && (
                    <div style={{ marginTop: 12 }}>
                      <Text type="secondary" style={{ fontSize: 12 }}>{card.note}</Text>
                    </div>
                  )}
                </Card>
              </Link>
            </Col>
          ))}
        </Row>
      </div>
    </div>
  )
}
