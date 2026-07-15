import { useState } from 'react'
import { Form, Input, Button, Card, Typography, Alert, message } from 'antd'
import { MailOutlined, ArrowLeftOutlined } from '@ant-design/icons'
import { Link } from 'react-router-dom'
import { authAPI } from '../../api/auth'
import AuthScreen from '../../components/AuthScreen'

const { Title, Text } = Typography

export default function ForgotPasswordPage() {
  const [loading, setLoading] = useState(false)
  const [submitted, setSubmitted] = useState(false)
  const [resetLink, setResetLink] = useState(null)
  const [form] = Form.useForm()

  const onFinish = async (values) => {
    setLoading(true)
    try {
      const res = await authAPI.forgotPassword(values.email)
      setSubmitted(true)
      if (res.data.resetLink) {
        setResetLink(res.data.resetLink)
      }
      message.success('Check your email for reset instructions.')
    } catch (err) {
      message.error(err.response?.data?.message || 'Could not process request')
    } finally {
      setLoading(false)
    }
  }

  return (
    <AuthScreen>
      <Card className="auth-card">
        <div style={{ textAlign: 'center', marginBottom: 24 }}>
          <Title level={3} style={{ color: '#1a237e', marginBottom: 4 }}>Forgot Password</Title>
          <Text type="secondary">We will email you a reset link if the account exists</Text>
        </div>

        {submitted ? (
          <>
            <Alert
              type="success"
              showIcon
              message="Request received"
              description="If an account exists with that email, you will receive reset instructions. Check your inbox (and spam folder)."
              style={{ marginBottom: 16 }}
            />
            {resetLink && (
              <Alert
                type="info"
                showIcon
                message="Development reset link"
                description={
                  <a href={resetLink} style={{ wordBreak: 'break-all' }}>{resetLink}</a>
                }
                style={{ marginBottom: 16 }}
              />
            )}
            <div style={{ textAlign: 'center' }}>
              <Link to="/login"><ArrowLeftOutlined /> Back to sign in</Link>
            </div>
          </>
        ) : (
          <Form form={form} onFinish={onFinish} layout="vertical" size="large">
            <Alert
              type="info"
              showIcon
              message="Enter the email linked to your account"
              style={{ marginBottom: 16 }}
            />
            <Form.Item
              name="email"
              label="Email"
              rules={[
                { required: true, message: 'Email is required' },
                { type: 'email', message: 'Enter a valid email address' },
              ]}
            >
              <Input prefix={<MailOutlined />} placeholder="your@email.com" />
            </Form.Item>
            <Form.Item>
              <Button type="primary" htmlType="submit" block loading={loading}>
                Send Reset Link
              </Button>
            </Form.Item>
            <div style={{ textAlign: 'center' }}>
              <Link to="/login"><ArrowLeftOutlined /> Back to sign in</Link>
            </div>
          </Form>
        )}
      </Card>
    </AuthScreen>
  )
}
