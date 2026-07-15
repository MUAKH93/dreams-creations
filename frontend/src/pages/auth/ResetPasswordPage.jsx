import { useEffect, useState } from 'react'
import { Form, Input, Button, Card, Typography, Alert, Spin, message } from 'antd'
import { LockOutlined, ArrowLeftOutlined } from '@ant-design/icons'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import { authAPI } from '../../api/auth'
import AuthScreen from '../../components/AuthScreen'

const { Title, Text } = Typography

export default function ResetPasswordPage() {
  const [searchParams] = useSearchParams()
  const navigate = useNavigate()
  const token = searchParams.get('token')
  const [validating, setValidating] = useState(true)
  const [tokenValid, setTokenValid] = useState(false)
  const [loading, setLoading] = useState(false)
  const [form] = Form.useForm()

  useEffect(() => {
    if (!token) {
      setValidating(false)
      setTokenValid(false)
      return
    }
    authAPI.validateResetToken(token)
      .then(res => setTokenValid(!!res.data.valid))
      .catch(() => setTokenValid(false))
      .finally(() => setValidating(false))
  }, [token])

  const onFinish = async (values) => {
    setLoading(true)
    try {
      await authAPI.resetPassword({ token, newPassword: values.password })
      message.success('Password updated — you can sign in now')
      navigate('/login')
    } catch (err) {
      message.error(err.response?.data?.message || 'Could not reset password')
    } finally {
      setLoading(false)
    }
  }

  if (validating) {
    return (
      <AuthScreen>
        <div style={{ textAlign: 'center', padding: 48 }}><Spin size="large" /></div>
      </AuthScreen>
    )
  }

  return (
    <AuthScreen>
      <Card className="auth-card">
        <div style={{ textAlign: 'center', marginBottom: 24 }}>
          <Title level={3} style={{ color: '#1a237e', marginBottom: 4 }}>Reset Password</Title>
          <Text type="secondary">Choose a new password for your account</Text>
        </div>

        {!tokenValid ? (
          <>
            <Alert
              type="error"
              showIcon
              message="Invalid or expired link"
              description="Request a new password reset from the sign-in page."
              style={{ marginBottom: 16 }}
            />
            <div style={{ textAlign: 'center' }}>
              <Link to="/login"><ArrowLeftOutlined /> Back to sign in</Link>
            </div>
          </>
        ) : (
          <Form form={form} onFinish={onFinish} layout="vertical" size="large">
            <Form.Item
              name="password"
              label="New password"
              rules={[
                { required: true, message: 'Password is required' },
                { min: 6, message: 'At least 6 characters' },
              ]}
              hasFeedback
            >
              <Input.Password prefix={<LockOutlined />} placeholder="New password" />
            </Form.Item>
            <Form.Item
              name="confirm"
              label="Confirm password"
              dependencies={['password']}
              hasFeedback
              rules={[
                { required: true, message: 'Confirm your password' },
                ({ getFieldValue }) => ({
                  validator(_, value) {
                    if (!value || getFieldValue('password') === value) return Promise.resolve()
                    return Promise.reject(new Error('Passwords do not match'))
                  },
                }),
              ]}
            >
              <Input.Password prefix={<LockOutlined />} placeholder="Confirm password" />
            </Form.Item>
            <Form.Item>
              <Button type="primary" htmlType="submit" block loading={loading}>
                Update Password
              </Button>
            </Form.Item>
          </Form>
        )}
      </Card>
    </AuthScreen>
  )
}
