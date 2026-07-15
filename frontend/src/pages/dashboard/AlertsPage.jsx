import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  Table, Button, Tag, Typography, message, Alert, Space, Modal, List
} from 'antd'
import { EyeOutlined, SendOutlined, AppstoreOutlined } from '@ant-design/icons'
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
    ],
    action: { label: 'Go to Dispatch', path: '/dispatch' },
  },
  STUCK_PIECES: {
    summary: 'Pieces were returned from a module but not forwarded to the next stage.',
    steps: [
      'Open Production Batches and locate the batch mentioned in the message.',
      'Check which module has stuck quantity in the flow.',
      'Create a new dispatch to forward those pieces to the next stage.',
    ],
    action: { label: 'Go to Batches', path: '/batches' },
  },
  LOW_STOCK: {
    summary: 'A design variant (size/color) is running low or out of stock.',
    steps: [
      'Open Inventory and review the low-stock item.',
      'Complete production batches or adjust selling plans.',
      'Restock via production returns at Press and Packing.',
    ],
    action: { label: 'Go to Inventory', path: '/inventory' },
  },
  PAYMENT_OVERDUE: {
    summary: 'Customer has overdue unpaid bills.',
    steps: [
      'Open Customers and review the customer account.',
      'Follow up for payment or record payment received.',
    ],
    action: { label: 'Go to Customers', path: '/customers' },
  },
}

function resolutionFor(alertType) {
  return RESOLUTION_GUIDE[alertType] || {
    summary: 'Review the related production or sales record.',
    steps: ['Investigate the issue described in the message.', 'Take corrective action.'],
    action: null,
  }
}

export default function AlertsPage() {
  const navigate = useNavigate()
  const [alerts, setAlerts] = useState([])
  const [loading, setLoading] = useState(true)
  const [loadError, setLoadError] = useState(null)
  const [viewingAlert, setViewingAlert] = useState(null)

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

  const dismissAlert = async (id) => {
    try {
      await salesAPI.resolveAlert(id)
      message.success('Alert dismissed')
      setViewingAlert(null)
      load()
    } catch {
      message.error('Failed to dismiss alert')
    }
  }

  const viewAlert = (alert) => {
    setViewingAlert(alert)
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
    { title: 'Created', dataIndex: 'createdDate', key: 'created', width: 160,
      render: d => d ? new Date(d).toLocaleString() : '-' },
    { title: 'Action', key: 'action', width: 200,
      render: (_, r) => {
        if (r.status !== 'open') return '—'
        return (
          <Button size="small" type="primary" icon={<EyeOutlined />}
            onClick={() => viewAlert(r)}>
            View
          </Button>
        )
      }
    },
  ]

  const guide = viewingAlert ? resolutionFor(viewingAlert.alertType) : null

  return (
    <div>
      <Title level={4} className="page-title">Alerts</Title>

      <Alert
        type="info"
        showIcon
        style={{ marginBottom: 16 }}
        message="View alerts to dismiss them"
        description={
          <Paragraph style={{ marginBottom: 0 }}>
            Click <strong>View</strong> to read the alert details and resolution steps.
            When you close the view, the alert is removed from the list.
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
        scroll={{ x: 800 }}
        rowClassName={r => r.status === 'open' ? 'alert-row-open' : ''}
      />

      <Modal
        title={viewingAlert ? `Alert — ${viewingAlert.alertType}` : 'Alert'}
        open={!!viewingAlert}
        onCancel={() => viewingAlert && dismissAlert(viewingAlert.alertId)}
        footer={
          viewingAlert ? (
            <Space>
              {guide?.action && (
                <Button icon={
                  viewingAlert.alertType === 'OVERDUE' ? <SendOutlined /> : <AppstoreOutlined />
                } onClick={() => navigate(guide.action.path)}>
                  {guide.action.label}
                </Button>
              )}
              <Button type="primary" onClick={() => dismissAlert(viewingAlert.alertId)}>
                Done — Dismiss Alert
              </Button>
            </Space>
          ) : null
        }
        width={560}
      >
        {viewingAlert && guide && (
          <>
            <Paragraph><Text strong>{guide.summary}</Text></Paragraph>
            <Paragraph type="secondary">{viewingAlert.message}</Paragraph>
            <Title level={5}>What to do</Title>
            <List
              size="small"
              dataSource={guide.steps}
              renderItem={(item, i) => <List.Item>{i + 1}. {item}</List.Item>}
            />
          </>
        )}
      </Modal>
    </div>
  )
}
