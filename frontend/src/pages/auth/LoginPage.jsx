import { Form, Input, Button, Card, Typography, message } from 'antd'
import { UserOutlined, LockOutlined } from '@ant-design/icons'
import { useNavigate, Link } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import { authAPI } from '../../api/auth'

const { Title, Text } = Typography

export default function LoginPage() {
  const { login } = useAuth()
  const navigate   = useNavigate()
  const [form]     = Form.useForm()

  const onFinish = async (values) => {
    try {
      const res = await authAPI.login(values)
      login(res.data)

      // Redirect based on role
      const role = res.data.role
      if (role === 'ADMIN' || role === 'MANAGER') {
        navigate('/dashboard')
      } else if (role === 'SUPERVISOR') {
        navigate('/assignments')
      } else {
        navigate('/my-orders')
      }
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
    }}>
      <Card style={{ width: 400, borderRadius: 12, boxShadow: '0 8px 32px rgba(0,0,0,0.2)' }}>
        <div style={{ textAlign: 'center', marginBottom: 32 }}>
          <Title level={3} style={{ color: '#1a237e', marginBottom: 4 }}>
            Dreams Creations
          </Title>
          <Text type="secondary">Inventory & Production Management</Text>
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

          <div style={{ textAlign: 'center' }}>
            <Text type="secondary">New customer? </Text>
            <Link to="/register">Create an account</Link>
          </div>
        </Form>
      </Card>
    </div>
  )
}
