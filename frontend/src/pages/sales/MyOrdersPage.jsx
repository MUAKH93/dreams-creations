import { useEffect, useState } from 'react'
import {
  Table, Card, Tag, Typography, Tabs, Row, Col, Statistic, Empty, Alert, Modal, Descriptions
} from 'antd'
import { salesAPI } from '../../api/sales'
import { apiErrorMessage } from '../../api/client'
import { useAuth } from '../../context/AuthContext'

const { Title, Text } = Typography

export default function MyOrdersPage() {
  const { auth } = useAuth()
  const [bills,    setBills]    = useState([])
  const [balance,  setBalance]  = useState(null)
  const [loading,  setLoading]  = useState(true)
  const [loadError, setLoadError] = useState(null)
  const [detail,   setDetail]   = useState(null)

  useEffect(() => {
    setLoading(true)
    setLoadError(null)

    const requests = [salesAPI.getMyBills()]
    if (auth?.customerId) {
      requests.push(salesAPI.getMyBalance())
    }

    Promise.allSettled(requests).then(([billsRes, balanceRes]) => {
      if (billsRes.status === 'fulfilled') setBills(billsRes.value.data)
      else {
        setLoadError(apiErrorMessage(billsRes.reason))
      }
      if (balanceRes?.status === 'fulfilled') setBalance(balanceRes.value.data)
    }).finally(() => setLoading(false))
  }, [auth?.customerId])

  const statusColor = s =>
    ({ paid: 'green', partial: 'orange', unpaid: 'red' }[s] || 'default')

  const billColumns = [
    { title: 'Bill #', dataIndex: 'billNumber', key: 'bill' },
    { title: 'Date', dataIndex: 'billDate', key: 'date',
      render: d => d ? new Date(d).toLocaleDateString() : '-' },
    { title: 'Total', dataIndex: 'finalAmount', key: 'total',
      render: v => `Rs. ${Number(v).toLocaleString()}` },
    { title: 'Status', dataIndex: 'status', key: 'status',
      render: s => <Tag color={statusColor(s)}>{s?.toUpperCase()}</Tag> },
    { title: 'Items', key: 'items',
      render: (_, r) => `${r.items?.length || 0} item(s)` },
    { title: '', key: 'view',
      render: (_, r) => <a onClick={() => setDetail(r)}>View</a> },
  ]

  return (
    <div>
      <Title level={4} className="page-title">
        Welcome, {auth?.username}
      </Title>

      {!auth?.customerId && !loading && (
        <Alert type="warning" showIcon style={{ marginBottom: 16 }}
          message="Customer profile not linked"
          description="Your account email must match a customer record. Contact the factory to link your account, or register a new account." />
      )}

      {loadError && (
        <Alert type="error" message={loadError} style={{ marginBottom: 16 }} showIcon />
      )}

      <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
        <Col xs={24} sm={8}>
          <Card>
            <Statistic title="Total Bills" value={bills.length}
              valueStyle={{ color: '#1a237e' }} />
          </Card>
        </Col>
        <Col xs={24} sm={8}>
          <Card>
            <Statistic title="Unpaid Bills"
              value={bills.filter(b => b.status === 'unpaid').length}
              valueStyle={{ color: '#cf1322' }} />
          </Card>
        </Col>
        <Col xs={24} sm={8}>
          <Card>
            <Statistic title="Outstanding Balance" prefix="Rs."
              value={balance ? Number(balance.balance).toLocaleString() : '0'}
              valueStyle={{ color: balance?.balance > 0 ? '#cf1322' : '#3f8600' }} />
          </Card>
        </Col>
      </Row>

      <Card title="My Bills">
        {bills.length === 0 && !loading ? (
          <Empty description="No bills yet for your account" />
        ) : (
          <Table dataSource={bills} columns={billColumns}
            rowKey="billId" loading={loading} />
        )}
      </Card>

      <Modal title={`Bill ${detail?.billNumber}`} open={!!detail}
        onCancel={() => setDetail(null)} footer={null} width={560}>
        {detail && (
          <>
            <Descriptions bordered size="small" column={1} style={{ marginBottom: 16 }}>
              <Descriptions.Item label="Date">
                {detail.billDate ? new Date(detail.billDate).toLocaleString() : '-'}
              </Descriptions.Item>
              <Descriptions.Item label="Status">
                <Tag color={statusColor(detail.status)}>{detail.status?.toUpperCase()}</Tag>
              </Descriptions.Item>
              <Descriptions.Item label="Total">
                Rs. {Number(detail.totalAmount).toLocaleString()}
              </Descriptions.Item>
              <Descriptions.Item label="Discount">
                Rs. {Number(detail.discount).toLocaleString()}
              </Descriptions.Item>
              <Descriptions.Item label="Final">
                <Text strong>Rs. {Number(detail.finalAmount).toLocaleString()}</Text>
              </Descriptions.Item>
            </Descriptions>
            <Table
              dataSource={detail.items || []}
              rowKey="billItemId"
              size="small"
              pagination={false}
              columns={[
                { title: 'Product', key: 'p',
                  render: (_, r) => r.product?.suit?.design?.name || 'Item' },
                { title: 'Qty', dataIndex: 'quantity' },
                { title: 'Price', dataIndex: 'unitPrice',
                  render: v => `Rs. ${Number(v).toLocaleString()}` },
                { title: 'Total', dataIndex: 'totalPrice',
                  render: v => `Rs. ${Number(v).toLocaleString()}` },
              ]}
            />
          </>
        )}
      </Modal>
    </div>
  )
}
