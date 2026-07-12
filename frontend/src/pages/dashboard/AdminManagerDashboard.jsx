import { useNavigate } from 'react-router-dom'
import { Row, Col, Card, Statistic, Table, Tag, Alert, Typography, Badge } from 'antd'
import {
  AlertOutlined, ShoppingOutlined, TeamOutlined, CheckCircleOutlined,
  WarningOutlined, FileExclamationOutlined, InboxOutlined, SafetyCertificateOutlined,
} from '@ant-design/icons'

const { Text } = Typography

export default function AdminManagerDashboard({
  role, summary, alerts, batches, loading, fmtMoney,
}) {
  const navigate = useNavigate()
  const s = summary || {}
  const openAlerts = alerts.filter(a => a.status === 'open')
  const cardStyle = { cursor: 'pointer' }
  const isAdmin = role === 'ADMIN'

  const alertColumns = [
    { title: 'Type', dataIndex: 'alertType', key: 'type',
      render: (t) => <Tag color={t === 'OVERDUE' ? 'red' : t === 'LOW_STOCK' ? 'orange' : t === 'PAYMENT_OVERDUE' ? 'magenta' : 'gold'}>{t}</Tag> },
    { title: 'Message', dataIndex: 'message', key: 'message', ellipsis: true },
    { title: 'Date', dataIndex: 'createdDate', key: 'date',
      render: (d) => d ? new Date(d).toLocaleDateString() : '-' },
  ]

  const batchColumns = [
    { title: 'Batch #', dataIndex: 'batchNumber', key: 'batch' },
    { title: 'Planned', dataIndex: 'totalSuitPlanned', key: 'planned' },
    { title: 'Produced', dataIndex: 'totalSuitProduced', key: 'produced' },
    { title: 'Status', dataIndex: 'status', key: 'status',
      render: (st) => (
        <Tag color={st === 'completed' ? 'green' : st === 'in_progress' ? 'blue' : 'default'}>
          {st?.replace('_', ' ').toUpperCase()}
        </Tag>
      ) },
  ]

  return (
    <>
      <Alert
        type="info"
        showIcon
        style={{ marginBottom: 16 }}
        message={isAdmin ? 'Admin overview — factory, sales, inventory & staff' : 'Manager overview — daily operations (production, sales & inventory)'}
        action={isAdmin ? (
          <a onClick={() => navigate('/staff')}>Staff & Supervisors</a>
        ) : (
          <a onClick={() => navigate('/reports')}>Reports</a>
        )}
      />

      {(s.overduePaymentCustomers > 0) && (
        <Alert
          message={`${s.overduePaymentCustomers} customer(s) with payments overdue (30+ days)`}
          type="error"
          showIcon
          style={{ marginBottom: 24, cursor: 'pointer' }}
          onClick={() => navigate('/customers')}
        />
      )}

      {(s.openAlerts > 0 || s.lowStockItems > 0) && (
        <Alert
          message={`${s.openAlerts || 0} open alert(s)${s.lowStockItems > 0 ? `, ${s.lowStockItems} low-stock item(s)` : ''}`}
          type="warning"
          showIcon
          style={{ marginBottom: 24, cursor: 'pointer' }}
          onClick={() => navigate('/alerts')}
        />
      )}

      <Row gutter={[16, 16]} style={{ marginBottom: 16 }}>
        <Col xs={24} sm={12} lg={6}>
          <Card hoverable style={cardStyle} onClick={() => navigate('/alerts')}>
            <Statistic title="Open Alerts" value={s.openAlerts ?? 0} prefix={<AlertOutlined />}
              valueStyle={{ color: (s.openAlerts || 0) > 0 ? '#cf1322' : '#3f8600' }} />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card hoverable style={cardStyle} onClick={() => navigate('/inventory')}>
            <Statistic title="Low Stock" value={s.lowStockItems ?? 0} prefix={<WarningOutlined />}
              valueStyle={{ color: (s.lowStockItems || 0) > 0 ? '#fa8c16' : '#3f8600' }} />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card hoverable style={cardStyle} onClick={() => navigate('/batches')}>
            <Statistic title="Batches In Progress" value={s.batchesInProgress ?? 0} prefix={<ShoppingOutlined />} />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card hoverable style={cardStyle} onClick={() => navigate('/dispatch')}>
            <Statistic title="Overdue Dispatches" value={s.overdueDispatches ?? 0} prefix={<TeamOutlined />}
              valueStyle={{ color: (s.overdueDispatches || 0) > 0 ? '#cf1322' : '#3f8600' }} />
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]} style={{ marginBottom: 16 }}>
        <Col xs={24} sm={12} lg={6}>
          <Card hoverable style={cardStyle} onClick={() => navigate('/bills')}>
            <Statistic title="Unpaid Bills" value={s.unpaidBills ?? 0} prefix={<FileExclamationOutlined />}
              valueStyle={{ color: (s.unpaidBills || 0) > 0 ? '#cf1322' : '#3f8600' }} />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card hoverable style={cardStyle} onClick={() => navigate('/customers')}>
            <Statistic title="Outstanding" value={fmtMoney(s.totalOutstandingBalance)} prefix="Rs."
              valueStyle={{ color: (s.totalOutstandingBalance || 0) > 0 ? '#cf1322' : '#3f8600' }} />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card hoverable style={cardStyle} onClick={() => navigate('/customers')}>
            <Statistic title="Payment Overdue (30d+)" value={s.overduePaymentCustomers ?? 0}
              prefix={<FileExclamationOutlined />}
              valueStyle={{ color: (s.overduePaymentCustomers || 0) > 0 ? '#cf1322' : '#3f8600' }} />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card hoverable style={cardStyle} onClick={() => navigate('/inventory')}>
            <Statistic title="Stock Units" value={s.totalStockUnits ?? 0} prefix={<InboxOutlined />} />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card hoverable style={cardStyle} onClick={() => navigate('/inventory')}>
            <Statistic title="Est. Stock Value" value={fmtMoney(s.estimatedStockValue)} prefix="Rs." />
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]} style={{ marginBottom: 16 }}>
        <Col xs={24} sm={12} lg={6}>
          <Card hoverable style={cardStyle} onClick={() => navigate('/activity')}>
            <Statistic title="Activity Log" value="→" prefix={<SafetyCertificateOutlined />}
              valueStyle={{ fontSize: 20 }} />
          </Card>
        </Col>
        {isAdmin && (
          <>
            <Col xs={24} sm={12} lg={6}>
              <Card hoverable style={cardStyle} onClick={() => navigate('/staff')}>
                <Statistic title="Staff & Setup" value="→" prefix={<SafetyCertificateOutlined />}
                  valueStyle={{ fontSize: 20 }} />
              </Card>
            </Col>
            <Col xs={24} sm={12} lg={6}>
              <Card hoverable style={cardStyle} onClick={() => navigate('/reports')}>
                <Statistic title="Reports" value="→" prefix={<FileExclamationOutlined />}
                  valueStyle={{ fontSize: 20 }} />
              </Card>
            </Col>
          </>
        )}
      </Row>

      <Row gutter={[16, 16]}>
        <Col xs={24} lg={12}>
          <Card title={<><Badge count={openAlerts.length} /> Open Alerts</>}
            extra={<a onClick={() => navigate('/alerts')}>View all</a>}>
            <Table dataSource={openAlerts} columns={alertColumns} rowKey="alertId"
              loading={loading} pagination={{ pageSize: 5 }} size="small" />
          </Card>
        </Col>
        <Col xs={24} lg={12}>
          <Card title="Production Batches" extra={<a onClick={() => navigate('/batches')}>View all</a>}>
            <Table dataSource={batches} columns={batchColumns} rowKey="batchId"
              loading={loading} pagination={{ pageSize: 5 }} size="small" />
          </Card>
        </Col>
      </Row>
    </>
  )
}
