import { useEffect, useState } from 'react'
import { Table, Tag, Typography, message } from 'antd'
import { activityAPI } from '../../api/activity'
import { apiErrorMessage } from '../../api/client'

const { Title } = Typography

const ACTION_COLORS = {
  DESIGN_UPDATED: 'blue',
  BILL_CANCELLED: 'red',
  STOCK_ADJUSTED: 'orange',
  BATCH_CANCELLED: 'volcano',
  BATCH_UPDATED: 'cyan',
}

export default function ActivityLogPage() {
  const [logs, setLogs] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    activityAPI.getRecent()
      .then(r => setLogs(r.data))
      .catch(err => message.error(apiErrorMessage(err)))
      .finally(() => setLoading(false))
  }, [])

  const columns = [
    {
      title: 'When',
      dataIndex: 'createdAt',
      key: 'when',
      width: 170,
      render: (d) => d ? new Date(d).toLocaleString() : '—',
    },
    {
      title: 'Action',
      dataIndex: 'actionType',
      key: 'action',
      width: 150,
      render: (t) => <Tag color={ACTION_COLORS[t] || 'default'}>{t?.replace(/_/g, ' ')}</Tag>,
    },
    { title: 'Summary', dataIndex: 'summary', key: 'summary', ellipsis: true },
    {
      title: 'Entity',
      key: 'entity',
      width: 140,
      render: (_, r) => `${r.entityType || '—'}${r.entityId ? ` #${r.entityId}` : ''}`,
    },
    {
      title: 'By',
      dataIndex: 'performedByUsername',
      key: 'by',
      width: 120,
      render: (u) => u || 'system',
    },
  ]

  return (
    <div>
      <Title level={4} className="page-title">Activity Log</Title>
      <Table
        dataSource={logs}
        columns={columns}
        rowKey="activityId"
        loading={loading}
        pagination={{ pageSize: 20, showSizeChanger: true }}
      />
    </div>
  )
}
