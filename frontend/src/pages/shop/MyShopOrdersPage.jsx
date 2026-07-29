import { useEffect, useState } from 'react'
import {
  Typography, Table, Tag, Button, Alert, Modal, Descriptions, Space, message, Popconfirm,
} from 'antd'
import { shopAPI } from '../../api/shop'
import { apiErrorMessage } from '../../api/client'
import { useAuth } from '../../context/AuthContext'
import { Link } from 'react-router-dom'

const { Title, Text } = Typography

const STATUS_COLORS = {
  pending: 'gold',
  confirmed: 'blue',
  fulfilled: 'green',
  cancelled: 'default',
}

export default function MyShopOrdersPage() {
  const { auth } = useAuth()
  const [orders, setOrders] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [detail, setDetail] = useState(null)
  const [cancelling, setCancelling] = useState(false)

  const load = () => {
    setLoading(true)
    setError(null)
    shopAPI.getMyOrders()
      .then(r => setOrders(r.data || []))
      .catch(err => setError(apiErrorMessage(err)))
      .finally(() => setLoading(false))
  }

  useEffect(() => {
    load()
  }, [])

  const handleCancel = async (orderId) => {
    setCancelling(true)
    try {
      await shopAPI.cancelMyOrder(orderId)
      message.success('Order cancelled')
      setDetail(null)
      load()
    } catch (err) {
      message.error(apiErrorMessage(err))
    } finally {
      setCancelling(false)
    }
  }

  const columns = [
    { title: 'Order #', dataIndex: 'orderNumber', key: 'orderNumber' },
    {
      title: 'Date',
      dataIndex: 'createdAt',
      key: 'createdAt',
      render: v => v ? new Date(v).toLocaleString() : '—',
    },
    {
      title: 'Total',
      dataIndex: 'totalAmount',
      key: 'totalAmount',
      render: v => `Rs. ${Number(v || 0).toLocaleString()}`,
    },
    {
      title: 'Status',
      dataIndex: 'status',
      key: 'status',
      render: s => <Tag color={STATUS_COLORS[s] || 'default'}>{s?.toUpperCase()}</Tag>,
    },
    {
      title: '',
      key: 'view',
      render: (_, r) => <Button size="small" onClick={() => setDetail(r)}>View</Button>,
    },
  ]

  return (
    <div>
      <Title level={4} className="page-title">My shop orders</Title>
      <Text type="secondary" style={{ display: 'block', marginBottom: 16 }}>
        Online orders placed through the storefront. Bills from the factory are under{' '}
        <Link to="/my-orders">My Bills</Link>.
      </Text>

      {!auth?.customerId && !loading && (
        <Alert type="warning" showIcon style={{ marginBottom: 16 }}
          message="Customer profile not linked"
          description="Your account email must match a customer record to view shop orders." />
      )}

      {error && <Alert type="error" message={error} style={{ marginBottom: 16 }} showIcon />}

      <Space style={{ marginBottom: 16 }}>
        <Link to="/store">
          <Button type="primary">Browse shop</Button>
        </Link>
        <Button onClick={load}>Refresh</Button>
      </Space>

      <Table
        rowKey="orderId"
        loading={loading}
        dataSource={orders}
        columns={columns}
        pagination={{ pageSize: 10 }}
        locale={{ emptyText: 'No shop orders yet' }}
      />

      <Modal
        title={detail ? `Order ${detail.orderNumber}` : 'Order detail'}
        open={!!detail}
        onCancel={() => setDetail(null)}
        footer={null}
        width={640}
      >
        {detail && (
          <>
            <Descriptions bordered size="small" column={2} style={{ marginBottom: 16 }}>
              <Descriptions.Item label="Status">
                <Tag color={STATUS_COLORS[detail.status]}>{detail.status}</Tag>
              </Descriptions.Item>
              <Descriptions.Item label="Total">
                Rs. {Number(detail.totalAmount || 0).toLocaleString()}
              </Descriptions.Item>
              {detail.shippingNotes && (
                <Descriptions.Item label="Shipping" span={2}>{detail.shippingNotes}</Descriptions.Item>
              )}
              {detail.billNumber && (
                <Descriptions.Item label="Bill" span={2}>{detail.billNumber}</Descriptions.Item>
              )}
            </Descriptions>

            <Table
              size="small"
              rowKey="itemId"
              pagination={false}
              dataSource={detail.items || []}
              columns={[
                {
                  title: 'Item',
                  key: 'item',
                  render: (_, i) => `${i.designName} — ${i.sizeValue || '—'} / ${i.color || '—'}`,
                },
                { title: 'Qty', dataIndex: 'quantity' },
                {
                  title: 'Line total',
                  dataIndex: 'totalPrice',
                  render: v => `Rs. ${Number(v || 0).toLocaleString()}`,
                },
              ]}
            />

            {detail.status === 'pending' && (
              <Popconfirm
                title="Cancel this order?"
                onConfirm={() => handleCancel(detail.orderId)}
              >
                <Button danger style={{ marginTop: 16 }} loading={cancelling}>
                  Cancel order
                </Button>
              </Popconfirm>
            )}
          </>
        )}
      </Modal>
    </div>
  )
}
