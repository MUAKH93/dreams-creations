import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  Table, Button, Tag, Typography, message, Popconfirm, Alert, Space, Tooltip
} from 'antd'
import { CheckOutlined, SendOutlined, AppstoreOutlined, QuestionCircleOutlined } from '@ant-design/icons'
import { salesAPI } from '../../api/sales'
import { apiErrorMessage } from '../../api/client'

const { Title, Text, Paragraph } = Typography

const RESOLUTION_GUIDE = {
  OVERDUE: {
    summary: 'A dispatch passed its due date without a return.',
    steps: [
      'Open Dispatch Management and find the overdue assignment.',
      'Contact the assigned supervisor to complete the work.',
      'Record the return (OK + damaged + missing) on My Assignments or Dispatch.',
      'Mark this alert resolved once the return is recorded or the issue is handled.',
    ],
    action: { label: 'Go to Dispatch', path: '/dispatch' },
  },
  STUCK_PIECES: {
    summary: 'Pieces were returned from a module but not forwarded to the next stage.',
    steps: [
      'Open Production Batches and locate the batch mentioned in the message.',
      'Check which module has stuck quantity in the flow.',
      'Create a new dispatch to forward those pieces to the next stage.',
      'Mark resolved after pieces are forwarded or accounted for.',
    ],
    action: { label: 'Go to Batches', path: '/batches' },
  },
  LOW_STOCK: {
    summary: 'A design variant (size/color) is running low or out of stock.',
    steps: [
      'Open Inventory and review the low-stock item.',
      'Complete production batches or adjust selling plans.',
      'Restock via production returns at Cutting & Stitching.',
      'Mark resolved once stock is replenished above the threshold.',
    ],
    action: { label: 'Go to Inventory', path: '/inventory' },
  },
}

function resolutionFor(alertType) {
  return RESOLUTION_GUIDE[alertType] || {
    summary: 'Review the related production or sales record.',
    steps: ['Investigate the issue described in the message.', 'Take corrective action.', 'Mark resolved when done.'],
    action: null,
  }
}

export default function AlertsPage() {
  const navigate = useNavigate()
  const [alerts,  setAlerts]  = useState([])
  const [loading, setLoading] = useState(true)
  const [loadError, setLoadError] = useState(null)

  const load = () => {
    setLoading(true)
    setLoadError(null)
    salesAPI.getAlerts()
      .then(r => setAlerts(r.data))
      .catch(err => {
        setLoadError(apiErrorMessage(err))
        message.error(apiErrorMessage(err))
      })
      .finally(() => setLoading(false))
  }

  useEffect(() => { load() }, [])

  const resolve = async (id) => {
    try {
      await salesAPI.resolveAlert(id)
      message.success('Alert marked as resolved')
      load()
    } catch {
      message.error('Failed to resolve alert')
    }
  }

  const openCount = alerts.filter(a => a.status === 'open').length

  const columns = [
    { title: 'Type', dataIndex: 'alertType', key: 'type', width: 130,
      render: t => (
        <Tag color={t === 'OVERDUE' ? 'red' : t === 'STUCK_PIECES' ? 'orange' : t === 'LOW_STOCK' ? 'gold' : 'blue'}>
          {t}
        </Tag>
      )
    },
    { title: 'Message', dataIndex: 'message', key: 'message' },
    { title: 'How to resolve', key: 'guide', width: 280,
      render: (_, r) => {
        const guide = resolutionFor(r.alertType)
        return (
          <Tooltip title={
            <div>
              <div><strong>{guide.summary}</strong></div>
              <ol style={{ paddingLeft: 16, margin: '8px 0 0' }}>
                {guide.steps.map((s, i) => <li key={i}>{s}</li>)}
              </ol>
            </div>
          }>
            <Text type="secondary" style={{ cursor: 'help' }}>
              <QuestionCircleOutlined /> {guide.summary}
            </Text>
          </Tooltip>
        )
      }
    },
    { title: 'Created', dataIndex: 'createdDate', key: 'created', width: 160,
      render: d => d ? new Date(d).toLocaleString() : '-' },
    { title: 'Status', dataIndex: 'status', key: 'status', width: 100,
      render: s => <Tag color={s === 'open' ? 'red' : 'green'}>{s?.toUpperCase()}</Tag> },
    { title: 'Action', key: 'action', width: 220,
      render: (_, r) => {
        if (r.status !== 'open') return '—'
        const guide = resolutionFor(r.alertType)
        return (
          <Space wrap>
            {guide.action && (
              <Button size="small" icon={
                r.alertType === 'OVERDUE' ? <SendOutlined /> : <AppstoreOutlined />
              } onClick={() => navigate(guide.action.path)}>
                {guide.action.label}
              </Button>
            )}
            <Popconfirm
              title="Mark as resolved?"
              description="Only resolve after you've taken the steps listed above."
              onConfirm={() => resolve(r.alertId)}
            >
              <Button icon={<CheckOutlined />} size="small" type="primary" ghost>
                Resolve
              </Button>
            </Popconfirm>
          </Space>
        )
      }
    },
  ]

  return (
    <div>
      <Title level={4} className="page-title">Alerts</Title>

      <Alert
        type="info"
        showIcon
        style={{ marginBottom: 16 }}
        message="How alerts work"
        description={
          <Paragraph style={{ marginBottom: 0 }}>
            Alerts are warnings — they do not fix themselves. Use the <strong>How to resolve</strong> column
            (hover for full steps), go to the linked page, fix the underlying issue, then click <strong>Resolve</strong>.
            {openCount > 0 && ` You have ${openCount} open alert${openCount > 1 ? 's' : ''}.`}
          </Paragraph>
        }
      />

      {loadError && (
        <Alert type="error" message={loadError} style={{ marginBottom: 16 }} showIcon />
      )}

      <Table
        dataSource={alerts}
        columns={columns}
        rowKey="alertId"
        loading={loading}
        scroll={{ x: 1100 }}
        rowClassName={r => r.status === 'open' ? 'alert-row-open' : ''}
      />
    </div>
  )
}
