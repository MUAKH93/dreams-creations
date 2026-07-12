import { Form, Input, Button, Card, Typography, Alert } from 'antd'
import { UserOutlined, LockOutlined, ArrowLeftOutlined } from '@ant-design/icons'
import { useNavigate, Link } from 'react-router-dom'
import { useState } from 'react'
import { useAuth } from '../../context/AuthContext'
import { authAPI } from '../../api/auth'
import { apiErrorMessage } from '../../api/client'
import { PORTALS, roleMatchesPortal } from '../../utils/roles'
import AuthScreen from '../../components/AuthScreen'

const { Title, Text } = Typography

export default function PortalLoginPage({ portalKey }) {
  const portal = PORTALS[portalKey]
  const { login } = useAuth()
  const navigate = useNavigate()
  const [form] = Form.useForm()
  const [loginError, setLoginError] = useState(null)
  const [loading, setLoading] = useState(false)

  if (!portal) {
    return null
  }

  const onFinish = async (values) => {
    setLoginError(null)
    setLoading(true)
    try {
      const res = await authAPI.login(values)
      const role = res.data.role

      if (!roleMatchesPortal(role, portalKey)) {
        const expected = portal.roles.join(' or ')
        setLoginError(`This account is not for ${portal.title}. Expected role: ${expected}.`)
        return
      }

      login(res.data)
      navigate(portal.home)
    } catch (err) {
      const msg = apiErrorMessage(err)
      const errorCode = err.response?.data?.error
      if (errorCode === 'ACCOUNT_DISABLED') {
        setLoginError(msg)
      } else if (err.response?.status === 401) {
        setLoginError('Incorrect email/username or password. Please check your credentials and try again.')
        form.setFields([
          { name: 'username', errors: [' '] },
          { name: 'password', errors: [' '] },
        ])
      } else {
        setLoginError(msg)
      }
    } finally {
      setLoading(false)
    }
  }

  const clearError = () => {
    if (loginError) setLoginError(null)
    form.setFields([
      { name: 'username', errors: [] },
      { name: 'password', errors: [] },
    ])
  }

  return (
    <AuthScreen>
      <Card className="auth-card">
        <div style={{ textAlign: 'center', marginBottom: 24 }}>
          <Title level={3} style={{ color: '#1a237e', marginBottom: 4 }}>
            {portal.title}
          </Title>
          <Text type="secondary">{portal.subtitle}</Text>
        </div>

        {loginError && (
          <Alert
            type="error"
            showIcon
            message="Sign in failed"
            description={loginError}
            style={{ marginBottom: 16 }}
            closable
            onClose={() => setLoginError(null)}
          />
        )}

        <Form form={form} onFinish={onFinish} layout="vertical" size="large">
          <Form.Item
            name="username"
            label="Username or email"
            rules={[{ required: true, message: 'Enter your username or email' }]}
          >
            <Input
              prefix={<UserOutlined />}
              placeholder="Username or email"
              onChange={clearError}
              autoComplete="username"
            />
          </Form.Item>

          <Form.Item
            name="password"
            label="Password"
            rules={[{ required: true, message: 'Enter your password' }]}
          >
            <Input.Password
              prefix={<LockOutlined />}
              placeholder="Password"
              onChange={clearError}
              autoComplete="current-password"
            />
          </Form.Item>

          <div style={{ textAlign: 'right', marginBottom: 16 }}>
            <Link to={`/forgot-password?portal=${portalKey}`}>Forgot password?</Link>
          </div>

          <Form.Item>
            <Button type="primary" htmlType="submit" block loading={loading}>
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
    </AuthScreen>
  )
}
