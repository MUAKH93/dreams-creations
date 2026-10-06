import { Row, Col, Card, Statistic, Table, Tag, Alert, Typography, Badge, Tabs, Button } from 'antd'
import {
  AlertOutlined, ShoppingOutlined, TeamOutlined, WarningOutlined,
  FileExclamationOutlined, InboxOutlined, SafetyCertificateOutlined,
  SolutionOutlined, SettingOutlined, RightOutlined,
} from '@ant-design/icons'
import { useNavigate } from 'react-router-dom'
import ProductionBatchesTable from '../../components/production/ProductionBatchesTable'

const { Text } = Typography

function StatCard({ title, value, prefix, color, onClick, suffix }) {
  return (
    <Card hoverable style={{ cursor: 'pointer' }} onClick={onClick}>
      <Statistic
        title={title}
        value={value}
        prefix={prefix}
        suffix={suffix}
        valueStyle={color ? { color } : undefined}
      />
    </Card>
  )
}

export default function AdminManagerDashboard({
  role, summary, alerts, batches, loading, fmtMoney,
}) {
  const navigate = useNavigate()
  const s = summary || {}
  const openAlerts = alerts.filter(a => a.status === 'open')
  const isAdmin = role === 'ADMIN'

  const alertColumns = [
    { title: 'Type', dataIndex: 'alertType', key: 'type',
      render: (t) => <Tag color={t === 'OVERDUE' ? 'red' : t === 'LOW_STOCK' ? 'orange' : t === 'PAYMENT_OVERDUE' ? 'magenta' : 'gold'}>{t}</Tag> },
    { title: 'Message', dataIndex: 'message', key: 'message', ellipsis: true },
    { title: 'Date', dataIndex: 'createdDate', key: 'date',
      render: (d) => d ? new Date(d).toLocaleDateString() : '-' },
  ]

  const notices = []
  if (s.overduePaymentCustomers > 0) {
    notices.push(
      <Alert
        key="overdue"
        type="error"
        showIcon
        message={`${s.overduePaymentCustomers} customer(s) with payments overdue (30+ days)`}
        style={{ marginBottom: 12, cursor: 'pointer' }}
        onClick={() => navigate('/customers')}
      />
    )
  }
  if (s.pendingQuotations > 0) {
    notices.push(
      <Alert
        key="quotes"
        type="warning"
        showIcon
        message={`${s.pendingQuotations} quotation(s) need review`}
        style={{ marginBottom: 12, cursor: 'pointer' }}
        onClick={() => navigate('/quotations')}
      />
    )
  }
  if ((s.openAlerts || 0) > 0 || (s.lowStockItems || 0) > 0) {
    notices.push(
      <Alert
        key="alerts"
        type="warning"
        showIcon
        message={`${s.openAlerts || 0} open alert(s)${s.lowStockItems > 0 ? `, ${s.lowStockItems} low-stock item(s)` : ''}`}
        style={{ marginBottom: 12, cursor: 'pointer' }}
        onClick={() => navigate('/alerts')}
      />
    )
  }

  const overviewTab = (
    <>
      {notices.length > 0 && <div style={{ marginBottom: 8 }}>{notices}</div>}
      <Row gutter={[16, 16]} style={{ marginBottom: 16 }}>
        <Col xs={24} sm={12} lg={6}>
          <StatCard
            title="Open Alerts"
            value={s.openAlerts ?? 0}
            prefix={<AlertOutlined />}
            color={(s.openAlerts || 0) > 0 ? '#cf1322' : '#3f8600'}
            onClick={() => navigate('/alerts')}
          />
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <StatCard
            title="Batches In Progress"
            value={s.batchesInProgress ?? 0}
            prefix={<ShoppingOutlined />}
            onClick={() => navigate('/batches')}
          />
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <StatCard
            title="Unpaid Bills"
            value={s.unpaidBills ?? 0}
            prefix={<FileExclamationOutlined />}
            color={(s.unpaidBills || 0) > 0 ? '#cf1322' : '#3f8600'}
            onClick={() => navigate('/bills')}
          />
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <StatCard
            title="Outstanding"
            value={fmtMoney(s.totalOutstandingBalance)}
            prefix="Rs."
            color={(s.totalOutstandingBalance || 0) > 0 ? '#cf1322' : '#3f8600'}
            onClick={() => navigate('/customers')}
          />
        </Col>
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
            <ProductionBatchesTable
              batches={batches}
              loading={loading}
              pageSize={5}
              compact
              showSizeChanger={false}
              onRowClick={() => navigate('/batches')}
            />
          </Card>
        </Col>
      </Row>
    </>
  )

  const productionTab = (
    <>
      <Card
        className="dashboard-batches-card"
        title="Latest Production Batches"
        extra={
          <Button type="link" icon={<RightOutlined />} onClick={() => navigate('/batches')}>
            All batches
          </Button>
        }
      >
        <ProductionBatchesTable
          batches={batches}
          loading={loading}
          pageSize={8}
          showSizeChanger={false}
          onRowClick={() => navigate('/batches')}
        />
      </Card>

      <Row gutter={[16, 16]} style={{ marginTop: 20 }}>
        <Col xs={24} sm={12} lg={8}>
          <StatCard
            title="Batches In Progress"
            value={s.batchesInProgress ?? 0}
            prefix={<ShoppingOutlined />}
            onClick={() => navigate('/batches')}
          />
        </Col>
        <Col xs={24} sm={12} lg={8}>
          <StatCard
            title="Overdue Dispatches"
            value={s.overdueDispatches ?? 0}
            prefix={<TeamOutlined />}
            color={(s.overdueDispatches || 0) > 0 ? '#cf1322' : '#3f8600'}
            onClick={() => navigate('/dispatch')}
          />
        </Col>
        <Col xs={24} sm={12} lg={8}>
          <StatCard
            title="Low Stock Items"
            value={s.lowStockItems ?? 0}
            prefix={<WarningOutlined />}
            color={(s.lowStockItems || 0) > 0 ? '#fa8c16' : '#3f8600'}
            onClick={() => navigate('/inventory')}
          />
        </Col>
        <Col xs={24} sm={12} lg={8}>
          <StatCard
            title="Stock Units"
            value={s.totalStockUnits ?? 0}
            prefix={<InboxOutlined />}
            onClick={() => navigate('/inventory')}
          />
        </Col>
        <Col xs={24} sm={12} lg={8}>
          <StatCard
            title="Est. Stock Value"
            value={fmtMoney(s.estimatedStockValue)}
            prefix="Rs."
            onClick={() => navigate('/inventory')}
          />
        </Col>
      </Row>
    </>
  )

  const salesTab = (
    <Row gutter={[16, 16]}>
      <Col xs={24} sm={12} lg={8}>
        <StatCard
          title="Pending Quotations"
          value={s.pendingQuotations ?? 0}
          prefix={<SolutionOutlined />}
          color={(s.pendingQuotations || 0) > 0 ? '#fa8c16' : '#3f8600'}
          onClick={() => navigate('/quotations')}
        />
      </Col>
      <Col xs={24} sm={12} lg={8}>
        <StatCard
          title="Unpaid Bills"
          value={s.unpaidBills ?? 0}
          prefix={<FileExclamationOutlined />}
          color={(s.unpaidBills || 0) > 0 ? '#cf1322' : '#3f8600'}
          onClick={() => navigate('/bills')}
        />
      </Col>
      <Col xs={24} sm={12} lg={8}>
        <StatCard
          title="Payment Overdue (30d+)"
          value={s.overduePaymentCustomers ?? 0}
          prefix={<FileExclamationOutlined />}
          color={(s.overduePaymentCustomers || 0) > 0 ? '#cf1322' : '#3f8600'}
          onClick={() => navigate('/customers')}
        />
      </Col>
      <Col xs={24} sm={12} lg={8}>
        <StatCard
          title="Outstanding Balance"
          value={fmtMoney(s.totalOutstandingBalance)}
          prefix="Rs."
          color={(s.totalOutstandingBalance || 0) > 0 ? '#cf1322' : '#3f8600'}
          onClick={() => navigate('/customers')}
        />
      </Col>
    </Row>
  )

  const tabItems = [
    { key: 'overview', label: 'Overview', children: overviewTab },
    { key: 'production', label: 'Production', children: productionTab },
    { key: 'sales', label: 'Sales', children: salesTab },
  ]

  if (isAdmin) {
    tabItems.push({
      key: 'admin',
      label: 'Admin',
      children: (
        <Row gutter={[16, 16]}>
          <Col xs={24} sm={12} lg={8}>
            <StatCard
              title="Staff"
              value="Manage"
              prefix={<SafetyCertificateOutlined />}
              onClick={() => navigate('/staff')}
            />
          </Col>
          <Col xs={24} sm={12} lg={8}>
            <StatCard
              title="Factory Setup"
              value="Configure"
              prefix={<SettingOutlined />}
              onClick={() => navigate('/setup')}
            />
          </Col>
        </Row>
      ),
    })
  }

  return (
    <>
      <Text type="secondary" style={{ display: 'block', marginBottom: 16 }}>
        {isAdmin ? 'Factory, sales, inventory, and staff at a glance.' : 'Daily production, sales, and inventory overview.'}
      </Text>
      <Tabs defaultActiveKey="overview" items={tabItems} />
    </>
  )
}
