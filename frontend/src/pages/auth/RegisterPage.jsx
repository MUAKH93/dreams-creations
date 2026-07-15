import { Form, Input, Button, Card, Typography, message, Alert } from 'antd'
import { UserOutlined, LockOutlined, MailOutlined, PhoneOutlined } from '@ant-design/icons'
import { useNavigate, Link } from 'react-router-dom'
import { useState } from 'react'
import { authAPI } from '../../api/auth'
import AuthScreen from '../../components/AuthScreen'

const { Title, Text } = Typography

export default function RegisterPage() {
  const navigate = useNavigate()
  const [form] = Form.useForm()
  const [done, setDone] = useState(false)
  const [result, setResult] = useState(null)

  const onFinish = async (values) => {
    try {
      const res = await authAPI.register(values)
      setResult(res.data)
      setDone(true)
      message.success('Account created — verify your email to sign in')
    } catch (err) {
      message.error(err.response?.data?.message || 'Registration failed')
    }
  }

  if (done) {
    return (
      <AuthScreen>
        <Card className="auth-card">
          <div style={{ textAlign: 'center' }}>
            <Title level={3} style={{ color: '#1a237e' }}>Check your email</Title>
            <Alert
              type="success"
              showIcon
              message={result?.message || 'Account created'}
              description={`A verification link was prepared for ${result?.email || 'your email'}. If SMTP is not configured, no real email is sent — use the development link below or check the backend console log.`}
              style={{ marginTop: 16, textAlign: 'left' }}
            />
            {result?.verificationLink ? (
              <Alert
                type="info"
                message="Development verification link (use this until SMTP is configured)"
                description={<a href={result.verificationLink}>{result.verificationLink}</a>}
                style={{ marginTop: 16, textAlign: 'left' }}
              />
            ) : (
              <Alert
                type="warning"
                message="No verification link returned"
                description="Restart the backend and register again. Also check the Spring Boot console for a line starting with Verification email for user."
                style={{ marginTop: 16, textAlign: 'left' }}
              />
            )}
            <Button type="primary" style={{ marginTop: 24 }} onClick={() => navigate('/login')}>
              Go to sign in
            </Button>
          </div>
        </Card>
      </AuthScreen>
    )
  }

  return (
    <AuthScreen>
      <Card className="auth-card">
        <div style={{ textAlign: 'center', marginBottom: 32 }}>
          <Title level={3} style={{ color: '#1a237e', marginBottom: 4 }}>
            Create Account
          </Title>
          <Text type="secondary">Dreams Creations Customer Portal</Text>
        </div>

        <Form form={form} onFinish={onFinish} layout="vertical" size="large">
          <Form.Item name="firstName" label="First name" rules={[{ required: true }]}>
            <Input prefix={<UserOutlined />} placeholder="Your first name" />
          </Form.Item>

          <Form.Item name="lastName" label="Last name">
            <Input placeholder="Last name (optional)" />
          </Form.Item>

          <Form.Item name="username" label="Username"
            rules={[{ required: true }, { min: 3, message: 'At least 3 characters' }]}>
            <Input prefix={<UserOutlined />} placeholder="Choose a username" />
          </Form.Item>

          <Form.Item name="email" label="Email"
            rules={[{ required: true }, { type: 'email', message: 'Valid email required' }]}>
            <Input prefix={<MailOutlined />} placeholder="your@email.com" />
          </Form.Item>

          <Form.Item name="phone" label="Phone number" rules={[{ required: true }]}>
            <Input prefix={<PhoneOutlined />} placeholder="03xx-xxxxxxx" />
          </Form.Item>

          <Form.Item name="password" label="Password"
            rules={[{ required: true }, { min: 6, message: 'At least 6 characters' }]}>
            <Input.Password prefix={<LockOutlined />} placeholder="Choose a password" />
          </Form.Item>

          <Form.Item>
            <Button type="primary" htmlType="submit" block>
              Create Account
            </Button>
          </Form.Item>

          <div style={{ textAlign: 'center' }}>
            <Text type="secondary">Already have an account? </Text>
            <Link to="/login">Sign in</Link>
          </div>
        </Form>
      </Card>
    </AuthScreen>
  )
}
