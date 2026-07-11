import { useNavigate } from 'react-router-dom'
import { Row, Col, Card, Statistic, Table, Tag, Alert, Typography } from 'antd'
import { CheckSquareOutlined, ClockCircleOutlined, RollbackOutlined, WarningOutlined } from '@ant-design/icons'
import dayjs from 'dayjs'

const { Text } = Typography

export default function SupervisorDashboard({ assignments, loading }) {
  const navigate = useNavigate()
  const now = dayjs()

  const pending = assignments.filter(a => a.status !== 'returned')
  const overdue = pending.filter(a => a.dueDate && dayjs(a.dueDate).isBefore(now, 'day'))
  const dueSoon = pending.filter(a => {
    if (!a.dueDate) return false
    const d = dayjs(a.dueDate)
    return !d.isBefore(now, 'day') && d.diff(now, 'day') <= 2
  })
  const completed = assignments.filter(a => a.status === 'returned')

  const columns = [
    { title: 'Batch', key: 'batch', render: (_, r) => r.batch?.batchNumber },
    { title: 'Stage', key: 'stage', render: (_, r) => r.module?.stage?.stageName },
    { title: 'Sent', dataIndex: 'quantitySent' },
    { title: 'Due', dataIndex: 'dueDate',
      render: d => d ? dayjs(d).format('DD MMM') : '—' },
    { title: 'Status', dataIndex: 'status',
      render: (s, r) => {
        const isOver = r.dueDate && dayjs(r.dueDate).isBefore(now, 'day') && s !== 'returned'
        return <Tag color={isOver ? 'red' : s === 'returned' ? 'green' : 'blue'}>{s?.toUpperCase()}</Tag>
      } },
  ]

  return (
    <>
      <Alert
        type="info"
        showIcon
        style={{ marginBottom: 16 }}
        message="Supervisor dashboard — your active dispatches and returns"
        action={<a onClick={() => navigate('/assignments')}>Go to assignments</a>}
      />

      {overdue.length > 0 && (
        <Alert type="error" showIcon style={{ marginBottom: 16 }}
          message={`${overdue.length} overdue assignment(s) — record returns or contact manager`} />
      )}

      <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
        <Col xs={12} sm={6}>
          <Card hoverable onClick={() => navigate('/assignments')}>
            <Statistic title="Active" value={pending.length} prefix={<CheckSquareOutlined />} />
          </Card>
        </Col>
        <Col xs={12} sm={6}>
          <Card hoverable onClick={() => navigate('/assignments')}>
            <Statistic title="Overdue" value={overdue.length} prefix={<WarningOutlined />}
              valueStyle={{ color: overdue.length > 0 ? '#cf1322' : '#3f8600' }} />
          </Card>
        </Col>
        <Col xs={12} sm={6}>
          <Card hoverable onClick={() => navigate('/assignments')}>
            <Statistic title="Due in 2 days" value={dueSoon.length} prefix={<ClockCircleOutlined />} />
          </Card>
        </Col>
        <Col xs={12} sm={6}>
          <Card hoverable onClick={() => navigate('/assignments')}>
            <Statistic title="Completed" value={completed.length} prefix={<RollbackOutlined />}
              valueStyle={{ color: '#3f8600' }} />
          </Card>
        </Col>
      </Row>

      <Card title="My Assignments" extra={<a onClick={() => navigate('/assignments')}>View all</a>}>
        <Table
          dataSource={pending}
          columns={columns}
          rowKey="assignmentId"
          loading={loading}
          pagination={{ pageSize: 8 }}
          size="small"
          locale={{ emptyText: 'No active assignments' }}
        />
      </Card>
    </>
  )
}
