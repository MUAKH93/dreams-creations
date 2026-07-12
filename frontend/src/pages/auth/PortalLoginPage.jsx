import { Form, Input, Button, Card, Typography, message } from 'antd'
import { UserOutlined, LockOutlined, ArrowLeftOutlined } from '@ant-design/icons'
import { useNavigate, Link } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import { authAPI } from '../../api/auth'
import { PORTALS, roleMatchesPortal } from '../../utils/roles'

const { Title, Text } = Typography

export default function PortalLoginPage({ portalKey }) {
  const portal = PORTALS[portalKey]
  const { login } = useAuth()
  const navigate = useNavigate()
  const [form] = Form.useForm()

  if (!portal) {
    return null
  }

  const onFinish = async (values) => {
    try {
      const res = await authAPI.login(values)
      const role = res.data.role

      if (!roleMatchesPortal(role, portalKey)) {
        const expected = portal.roles.join(' or ')
        message.error(`This account is not for ${portal.title}. Expected role: ${expected}.`)
        return
      }

      login(res.data)
      navigate(portal.home)
    } catch (err) {
      message.error(err.response?.data?.message || 'Login failed')
    }
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
      <Card style={{ width: 420, borderRadius: 12, boxShadow: '0 8px 32px rgba(0,0,0,0.2)' }}>
        <div style={{ textAlign: 'center', marginBottom: 24 }}>
          <Title level={3} style={{ color: '#1a237e', marginBottom: 4 }}>
            {portal.title}
          </Title>
          <Text type="secondary">{portal.subtitle}</Text>
        </div>

        <Form form={form} onFinish={onFinish} layout="vertical" size="large">
          <Form.Item
            name="username"
            rules={[{ required: true, message: 'Please enter your username' }]}
          >
            <Input prefix={<UserOutlined />} placeholder="Username" />
          </Form.Item>

          <Form.Item
            name="password"
            rules={[{ required: true, message: 'Please enter your password' }]}
          >
            <Input.Password prefix={<LockOutlined />} placeholder="Password" />
          </Form.Item>

          <Form.Item>
            <Button type="primary" htmlType="submit" block>
              Sign In
            </Button>
          </Form.Item>

          {portalKey === 'customer' && (
            <div style={{ textAlign: 'center', marginBottom: 12 }}>
              <Text type="secondary">New customer? </Text>
              <Link to="/register">Create an account</Link>
            </div>
          )}

          <div style={{ textAlign: 'center' }}>
            <Link to="/login">
              <ArrowLeftOutlined /> Choose a different portal
            </Link>
          </div>
        </Form>
      </Card>
    </div>
  )
}
