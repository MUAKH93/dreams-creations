import { useMemo } from 'react'
import { Table, Tag, Progress, Typography } from 'antd'
import dayjs from 'dayjs'

const { Text } = Typography

export const BATCH_STATUS_COLORS = {
  completed: 'green',
  in_progress: 'blue',
  planned: 'default',
  cancelled: 'red',
}

export function batchStatusTag(status) {
  const key = status || 'planned'
  return (
    <Tag color={BATCH_STATUS_COLORS[key] || 'default'} style={{ margin: 0 }}>
      {key.replace('_', ' ').toUpperCase()}
    </Tag>
  )
}

export function sortBatchesLatestFirst(list) {
  return [...(list || [])].sort((a, b) => {
    const dateA = a.startDate || a.expectedCompletionDate || ''
    const dateB = b.startDate || b.expectedCompletionDate || ''
    if (dateA !== dateB) return dateB.localeCompare(dateA)
    return (b.batchId ?? 0) - (a.batchId ?? 0)
  })
}

export function buildBatchTableColumns({
  compact = false,
  includeSuit = false,
  includeDue = true,
  renderActions,
} = {}) {
  const columns = [
    {
      title: 'Batch #',
      key: 'batchNumber',
      width: compact ? 140 : 168,
      render: (_, r) => (
        <div className="dashboard-batch-cell dashboard-batch-cell--primary">
          <Text strong>{r.batchNumber || '—'}</Text>
          {r.articleName && (
            <Text type="secondary" className="dashboard-batch-cell__sub">{r.articleName}</Text>
          )}
        </div>
      ),
    },
    {
      title: 'Design',
      key: 'design',
      width: compact ? 120 : 150,
      ellipsis: true,
      render: (_, r) => (
        <span className="dashboard-batch-cell">
          {r.designLabel || r.suit?.design?.name || '—'}
        </span>
      ),
    },
  ]

  if (includeSuit) {
    columns.push({
      title: 'Suit',
      key: 'suit',
      width: 180,
      ellipsis: true,
      render: (_, r) => (
        <span className="dashboard-batch-cell">
          {r.suit
            ? `${r.suit.design?.designCode || ''} — ${r.suit.size?.sizeValue || 'Size TBD'} — ${r.suit.color || ''}`
            : '—'}
        </span>
      ),
    })
  }

  columns.push(
    {
      title: 'Planned',
      dataIndex: 'totalSuitPlanned',
      key: 'planned',
      width: 88,
      align: 'center',
      render: (v) => <span className="dashboard-batch-cell dashboard-batch-cell--num">{v ?? 0}</span>,
    },
    {
      title: 'Produced',
      key: 'produced',
      width: compact ? 120 : 168,
      render: (_, r) => {
        const planned = r.totalSuitPlanned || 0
        const produced = r.totalSuitProduced || 0
        const pct = planned ? Math.round((produced / planned) * 100) : 0
        return (
          <div className="dashboard-batch-cell dashboard-batch-cell--progress">
            <Text className="dashboard-batch-cell--num">{produced}</Text>
            <Progress
              percent={pct}
              size="small"
              showInfo={false}
              status={r.status === 'completed' ? 'success' : 'active'}
              strokeColor={r.status === 'completed' ? '#52c41a' : '#1a237e'}
            />
            <Text type="secondary" style={{ fontSize: 11 }}>{pct}%</Text>
          </div>
        )
      },
    },
  )

  if (includeDue && !compact) {
    columns.push({
      title: 'Due',
      dataIndex: 'expectedCompletionDate',
      key: 'due',
      width: 120,
      render: (d) => (
        <span className="dashboard-batch-cell">
          {d ? dayjs(d).format('DD MMM YYYY') : '—'}
        </span>
      ),
    })
  }

  columns.push({
    title: 'Status',
    dataIndex: 'status',
    key: 'status',
    width: 120,
    align: 'center',
    render: (s) => batchStatusTag(s),
  })

  if (renderActions) {
    columns.push({
      title: 'Actions',
      key: 'actions',
      width: 220,
      fixed: 'right',
      render: renderActions,
    })
  }

  return columns
}

export default function ProductionBatchesTable({
  batches,
  loading,
  compact = false,
  includeSuit = false,
  includeDue = true,
  pageSize = 10,
  showSizeChanger = true,
  onRowClick,
  renderActions,
  pagination,
}) {
  const sorted = useMemo(() => sortBatchesLatestFirst(batches), [batches])
  const columns = useMemo(
    () => buildBatchTableColumns({ compact, includeSuit, includeDue, renderActions }),
    [compact, includeSuit, includeDue, renderActions],
  )

  const defaultPagination = {
    pageSize,
    showSizeChanger,
    pageSizeOptions: ['10', '20', '50'],
    showTotal: (total) => `${total} batch${total === 1 ? '' : 'es'}`,
  }

  return (
    <div className="dashboard-batches-table-wrap">
      <Table
        className="dashboard-batches-table"
        dataSource={sorted}
        columns={columns}
        rowKey="batchId"
        loading={loading}
        bordered
        size="middle"
        tableLayout="fixed"
        scroll={renderActions ? { x: 1100 } : undefined}
        pagination={pagination ?? defaultPagination}
        onRow={(record) => ({
          onClick: (e) => {
            if (e.target.closest('button, a, .ant-popover, .ant-popconfirm')) return
            onRowClick?.(record)
          },
          style: { cursor: onRowClick ? 'pointer' : undefined },
        })}
        locale={{ emptyText: 'No production batches yet' }}
      />
    </div>
  )
}
