import { useEffect, useState } from 'react'
import { useSearchParams, Link, useNavigate } from 'react-router-dom'
import { Card, Typography, Alert, Spin, Button } from 'antd'
import { CheckCircleOutlined, CloseCircleOutlined } from '@ant-design/icons'
import { authAPI } from '../../api/auth'
import { apiErrorMessage } from '../../api/client'
import AuthScreen from '../../components/AuthScreen'

const { Title, Text } = Typography

export default function VerifyEmailPage() {
  const navigate = useNavigate()
  const [params] = useSearchParams()
  const token = params.get('token')
  const [status, setStatus] = useState('loading')
  const [message, setMessage] = useState('')

  useEffect(() => {
    if (!token) {
      setStatus('error')
      setMessage('Verification link is missing or invalid.')
      return
    }
    authAPI.verifyEmail(token)
      .then(res => {
        setStatus('success')
        setMessage(res.data.message)
      })
      .catch(err => {
        setStatus('error')
        setMessage(apiErrorMessage(err))
      })
  }, [token])

  return (
    <AuthScreen>
      <Card className="auth-card">
        <div style={{ textAlign: 'center' }}>
          {status === 'loading' && (
            <>
              <Spin size="large" />
              <Title level={4} style={{ marginTop: 24 }}>Verifying your email…</Title>
            </>
          )}
          {status === 'success' && (
            <>
              <CheckCircleOutlined style={{ fontSize: 48, color: '#52c41a' }} />
              <Title level={4} style={{ marginTop: 16 }}>Email verified</Title>
              <Alert type="success" message={message} style={{ marginTop: 16, textAlign: 'left' }} />
              <Button type="primary" style={{ marginTop: 24 }} onClick={() => navigate('/login/customer')}>
                Sign in
              </Button>
            </>
          )}
          {status === 'error' && (
            <>
              <CloseCircleOutlined style={{ fontSize: 48, color: '#ff4d4f' }} />
              <Title level={4} style={{ marginTop: 16 }}>Verification failed</Title>
              <Alert type="error" message={message} style={{ marginTop: 16, textAlign: 'left' }} />
              <Text type="secondary" style={{ display: 'block', marginTop: 16 }}>
                <Link to="/login/customer">Back to sign in</Link>
              </Text>
            </>
          )}
        </div>
      </Card>
    </AuthScreen>
  )
}
