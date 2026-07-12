import { Form, Input, Button, Card, Typography, message } from 'antd'
import { UserOutlined, LockOutlined, MailOutlined } from '@ant-design/icons'
import { useNavigate, Link } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import { authAPI } from '../../api/auth'

const { Title, Text } = Typography

export default function RegisterPage() {
  const { login } = useAuth()
  const navigate   = useNavigate()
  const [form]     = Form.useForm()

  const onFinish = async (values) => {
    try {
      const res = await authAPI.register(values)
      login(res.data)
      navigate('/my-orders')
      message.success('Account created successfully!')
    } catch (err) {
      message.error(err.response?.data?.message || 'Registration failed')
    }
  }

  return (
    <div style={{
      minHeight: '100vh',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      background: 'linear-gradient(135deg, #1a237e 0%, #283593 100%)',
    }}>
      <Card style={{ width: 420, borderRadius: 12, boxShadow: '0 8px 32px rgba(0,0,0,0.2)' }}>
        <div style={{ textAlign: 'center', marginBottom: 32 }}>
          <Title level={3} style={{ color: '#1a237e', marginBottom: 4 }}>
            Create Account
          </Title>
          <Text type="secondary">Dreams Creations Customer Portal</Text>
        </div>

        <Form form={form} onFinish={onFinish} layout="vertical" size="large">
          <Form.Item name="username" label="Username"
            rules={[{ required: true }, { min: 3, message: 'At least 3 characters' }]}>
            <Input prefix={<UserOutlined />} placeholder="Choose a username" />
          </Form.Item>

          <Form.Item name="email" label="Email"
            rules={[{ required: true }, { type: 'email', message: 'Valid email required' }]}>
            <Input prefix={<MailOutlined />} placeholder="your@email.com" />
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
            <Link to="/login/customer">Customer sign in</Link>
          </div>
        </Form>
      </Card>
    </div>
  )
}
