import { Form, Input, Button, Card, Typography, Alert, message } from 'antd'
import { UserOutlined, LockOutlined, ArrowLeftOutlined, MailOutlined } from '@ant-design/icons'
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
  const [resendEmail, setResendEmail] = useState('')
  const [resendLoading, setResendLoading] = useState(false)
  const [resendMsg, setResendMsg] = useState(null)
  const [emailNotVerified, setEmailNotVerified] = useState(false)

  if (!portal) {
    return null
  }

  const onFinish = async (values) => {
    setLoginError(null)
    setResendMsg(null)
    setEmailNotVerified(false)
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
      if (errorCode === 'EMAIL_NOT_VERIFIED') {
        setEmailNotVerified(true)
        setLoginError(msg)
        const identifier = values.username || ''
        if (identifier.includes('@')) setResendEmail(identifier)
      } else if (errorCode === 'ACCOUNT_DISABLED') {
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

  const handleResend = async () => {
    if (!resendEmail) {
      message.error('Enter your email in the username field first')
      return
    }
    setResendLoading(true)
    try {
      const res = await authAPI.resendVerification(resendEmail)
      setResendMsg(res.data.message)
      if (res.data.verificationLink) {
        setResendMsg(`${res.data.message} Dev link: ${res.data.verificationLink}`)
      }
    } catch (err) {
      setResendMsg(apiErrorMessage(err))
    } finally {
      setResendLoading(false)
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

        {resendMsg && (
          <Alert type="info" message={resendMsg} style={{ marginBottom: 16 }} closable onClose={() => setResendMsg(null)} />
        )}

        {emailNotVerified && portalKey === 'customer' && (
          <div style={{ marginBottom: 16 }}>
            <Input
              prefix={<MailOutlined />}
              placeholder="Your email for resend"
              value={resendEmail}
              onChange={e => setResendEmail(e.target.value)}
              style={{ marginBottom: 8 }}
            />
            <Button block loading={resendLoading} onClick={handleResend}>
              Resend verification email
            </Button>
          </div>
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
