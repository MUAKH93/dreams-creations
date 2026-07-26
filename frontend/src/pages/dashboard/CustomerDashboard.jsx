import { useNavigate } from 'react-router-dom'
import { Row, Col, Card, Statistic, Table, Tag, Alert, Typography } from 'antd'
import { WalletOutlined, FileTextOutlined, PictureOutlined, ShoppingOutlined } from '@ant-design/icons'

const { Text } = Typography

export default function CustomerDashboard({ balance, bills, designs, loading }) {
  const navigate = useNavigate()
  const openBills = bills.filter(b => b.status !== 'cancelled' && b.status !== 'paid')
  const paidBills = bills.filter(b => b.status === 'paid')

  const columns = [
    { title: 'Bill #', dataIndex: 'billNumber' },
    { title: 'Amount', dataIndex: 'finalAmount', render: v => `Rs. ${Number(v || 0).toLocaleString()}` },
    { title: 'Status', dataIndex: 'status',
      render: s => <Tag color={s === 'paid' ? 'green' : s === 'partial' ? 'orange' : 'red'}>{s?.toUpperCase()}</Tag> },
    { title: 'Date', dataIndex: 'billDate', render: d => d ? new Date(d).toLocaleDateString() : '—' },
  ]

  return (
    <>
      <Alert
        type="info"
        showIcon
        style={{ marginBottom: 16 }}
        message="Welcome to Dreams Creations — browse designs and track your orders"
        action={<a onClick={() => navigate('/designs')}>Browse catalog</a>}
      />

      <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
        <Col xs={12} sm={6}>
          <Card hoverable onClick={() => navigate('/my-orders')}>
            <Statistic title="Balance Due" prefix="Rs."
              value={Number(balance?.balance || 0).toLocaleString()}
              valueStyle={{ color: (balance?.balance || 0) > 0 ? '#cf1322' : '#3f8600' }} />
          </Card>
        </Col>
        <Col xs={12} sm={6}>
          <Card hoverable onClick={() => navigate('/my-orders')}>
            <Statistic title="Total Purchases" prefix="Rs."
              value={Number(balance?.totalSales || 0).toLocaleString()} />
          </Card>
        </Col>
        <Col xs={12} sm={6}>
          <Card hoverable onClick={() => navigate('/my-orders')}>
            <Statistic title="Open Bills" value={openBills.length} prefix={<FileTextOutlined />} />
          </Card>
        </Col>
        <Col xs={12} sm={6}>
          <Card hoverable onClick={() => navigate('/designs')}>
            <Statistic title="Designs" value={designs.length} prefix={<PictureOutlined />} />
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]}>
        <Col xs={24} lg={14}>
          <Card title="Recent Bills" extra={<a onClick={() => navigate('/my-orders')}>All bills</a>}>
            <Table dataSource={bills.slice(0, 8)} columns={columns} rowKey="billId"
              loading={loading} pagination={false} size="small" />
          </Card>
        </Col>
        <Col xs={24} lg={10}>
          <Card title="Quick links">
            <Row gutter={[8, 8]}>
              <Col span={12}>
                <Card size="small" hoverable onClick={() => navigate('/designs')}>
                  <ShoppingOutlined /> Browse designs
                </Card>
              </Col>
              <Col span={12}>
                <Card size="small" hoverable onClick={() => navigate('/my-orders')}>
                  <WalletOutlined /> My balance
                </Card>
              </Col>
            </Row>
            {paidBills.length > 0 && (
              <Text type="secondary" style={{ display: 'block', marginTop: 12 }}>
                {paidBills.length} paid bill(s) on record
              </Text>
            )}
          </Card>
        </Col>
      </Row>
    </>
  )
}
