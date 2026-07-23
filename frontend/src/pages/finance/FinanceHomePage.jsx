import { useEffect, useState } from 'react'
import { Typography, Alert, Card, Tag, Timeline, Spin, Button, Space } from 'antd'
import { useNavigate } from 'react-router-dom'
import { financeAPI } from '../../api/finance'
import { apiErrorMessage } from '../../api/client'

const { Title, Text, Paragraph } = Typography

export default function FinanceHomePage() {
  const navigate = useNavigate()
  const [status, setStatus] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  useEffect(() => {
    financeAPI.getStatus()
      .then(r => setStatus(r.data))
      .catch(err => setError(apiErrorMessage(err)))
      .finally(() => setLoading(false))
  }, [])

  if (loading) {
    return <div style={{ textAlign: 'center', padding: 48 }}><Spin size="large" /></div>
  }

  return (
    <div>
      <Title level={4} className="page-title">Finance Module</Title>

      <Alert
        type="info"
        showIcon
        style={{ marginBottom: 24 }}
        message="Development branch: feature/finance-v2"
        description="Phase F1 is active: chart of accounts, manual journals, trial balance, and general ledger."
      />

      {error && (
        <Alert type="error" message={error} style={{ marginBottom: 16 }} showIcon />
      )}

      {status && (
        <Card style={{ marginBottom: 24 }}>
          <Tag color="blue">{status.version}</Tag>
          <Tag color="processing">Phase {status.currentPhase}</Tag>
          {status.autoPostAr && <Tag color="green">AR auto-post ON</Tag>}
          <Paragraph style={{ marginTop: 16, marginBottom: 16 }}>{status.message}</Paragraph>
          <Space wrap>
            <Button onClick={() => navigate('/finance/accounts')}>Chart of Accounts</Button>
            <Button onClick={() => navigate('/finance/journals')}>Journal Entries</Button>
            <Button onClick={() => navigate('/finance/reports')}>Reports</Button>
          </Space>
        </Card>
      )}

      <Card title="Delivery roadmap">
        <Timeline
          items={[
            { color: 'green', children: 'Scaffold — feature flag, API namespace, SQL stub' },
            ...(status?.upcomingPhases || []).map(phase => ({
              color: 'gray',
              children: phase,
            })),
          ]}
        />
        <Text type="secondary">
          Target MVP (F0–F4): ~12 weeks · Full module (F0–F6): ~16 weeks
        </Text>
      </Card>
    </div>
  )
}
