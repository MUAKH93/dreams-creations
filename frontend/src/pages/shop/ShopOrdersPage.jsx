import { useEffect, useState } from 'react'
import {
  Typography, Table, Tag, Button, Select, Space, message, Modal, Descriptions,
} from 'antd'
import { shopAPI } from '../../api/shop'
import { apiErrorMessage } from '../../api/client'

const { Title, Text } = Typography

const STATUS_COLORS = {
  pending: 'gold',
  confirmed: 'blue',
  fulfilled: 'green',
  cancelled: 'default',
}

export default function ShopOrdersPage() {
  const [orders, setOrders] = useState([])
  const [loading, setLoading] = useState(true)
  const [statusFilter, setStatusFilter] = useState('')
  const [detail, setDetail] = useState(null)
  const [actionLoading, setActionLoading] = useState(false)

  const load = () => {
    setLoading(true)
    shopAPI.getOrders(statusFilter ? { status: statusFilter } : {})
      .then(r => setOrders(r.data || []))
      .catch(err => message.error(apiErrorMessage(err)))
      .finally(() => setLoading(false))
  }

  useEffect(() => {
    load()
  }, [statusFilter])

  const runAction = async (fn, successMsg) => {
    setActionLoading(true)
    try {
      const res = await fn()
      message.success(successMsg)
      setDetail(res.data)
      load()
    } catch (err) {
      message.error(apiErrorMessage(err))
    } finally {
      setActionLoading(false)
    }
  }

  const columns = [
    { title: 'Order #', dataIndex: 'orderNumber', key: 'orderNumber' },
    { title: 'Customer', dataIndex: 'customerName', key: 'customerName' },
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
      render: s => <Tag color={STATUS_COLORS[s] || 'default'}>{s}</Tag>,
    },
    {
      title: 'Actions',
      key: 'actions',
      render: (_, r) => (
        <Button size="small" onClick={() => setDetail(r)}>View</Button>
      ),
    },
  ]

  return (
    <div>
      <Title level={3} className="shop-page-title">Shop orders</Title>
      <Text className="shop-page-subtitle">
        Review customer orders, confirm them, and convert to quotations or bills.
      </Text>

      <Space style={{ marginBottom: 16 }}>
        <Text>Filter:</Text>
        <Select
          allowClear
          placeholder="All statuses"
          style={{ width: 180 }}
          value={statusFilter || undefined}
          onChange={v => setStatusFilter(v || '')}
          options={[
            { value: 'pending', label: 'Pending' },
            { value: 'confirmed', label: 'Confirmed' },
            { value: 'fulfilled', label: 'Fulfilled' },
            { value: 'cancelled', label: 'Cancelled' },
          ]}
        />
        <Button onClick={load}>Refresh</Button>
      </Space>

      <Table
        rowKey="orderId"
        loading={loading}
        dataSource={orders}
        columns={columns}
        pagination={{ pageSize: 10 }}
      />

      <Modal
        title={detail ? `Order ${detail.orderNumber}` : 'Order detail'}
        open={!!detail}
        onCancel={() => setDetail(null)}
        footer={null}
        width={720}
      >
        {detail && (
          <>
            <Descriptions bordered size="small" column={2} style={{ marginBottom: 16 }}>
              <Descriptions.Item label="Customer">{detail.customerName}</Descriptions.Item>
              <Descriptions.Item label="Status">
                <Tag color={STATUS_COLORS[detail.status]}>{detail.status}</Tag>
              </Descriptions.Item>
              <Descriptions.Item label="Total">Rs. {Number(detail.totalAmount || 0).toLocaleString()}</Descriptions.Item>
              <Descriptions.Item label="Items">{detail.itemCount}</Descriptions.Item>
              {detail.shippingNotes && (
                <Descriptions.Item label="Shipping" span={2}>{detail.shippingNotes}</Descriptions.Item>
              )}
              {detail.customerNotes && (
                <Descriptions.Item label="Notes" span={2}>{detail.customerNotes}</Descriptions.Item>
              )}
              {detail.quotationNumber && (
                <Descriptions.Item label="Quotation" span={2}>{detail.quotationNumber}</Descriptions.Item>
              )}
              {detail.stockReserved && (
                <Descriptions.Item label="Stock">Reserved</Descriptions.Item>
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
                  render: (_, i) => `${i.designName} (${i.designCode}) — ${i.sizeValue || '—'} / ${i.color || '—'}`,
                },
                { title: 'Qty', dataIndex: 'quantity' },
                {
                  title: 'Line total',
                  dataIndex: 'totalPrice',
                  render: v => `Rs. ${Number(v || 0).toLocaleString()}`,
                },
              ]}
            />

            <Space wrap style={{ marginTop: 16 }}>
              {detail.status === 'pending' && (
                <>
                  <Button
                    type="primary"
                    loading={actionLoading}
                    onClick={() => runAction(
                      () => shopAPI.updateOrderStatus(detail.orderId, 'confirmed'),
                      'Order confirmed',
                    )}
                  >
                    Confirm
                  </Button>
                  <Button
                    danger
                    loading={actionLoading}
                    onClick={() => runAction(
                      () => shopAPI.updateOrderStatus(detail.orderId, 'cancelled'),
                      'Order cancelled',
                    )}
                  >
                    Cancel
                  </Button>
                </>
              )}
              {detail.status === 'confirmed' && (
                <>
                  <Button
                    loading={actionLoading}
                    onClick={() => runAction(
                      () => shopAPI.convertOrderToQuotation(detail.orderId),
                      'Converted to quotation',
                    )}
                  >
                    Convert to quotation
                  </Button>
                  <Button
                    type="primary"
                    loading={actionLoading}
                    onClick={() => runAction(
                      () => shopAPI.convertOrderToBill(detail.orderId),
                      'Converted to bill',
                    )}
                  >
                    Convert to bill
                  </Button>
                  <Button
                    loading={actionLoading}
                    onClick={() => runAction(
                      () => shopAPI.updateOrderStatus(detail.orderId, 'fulfilled'),
                      'Order fulfilled',
                    )}
                  >
                    Mark fulfilled
                  </Button>
                </>
              )}
            </Space>
          </>
        )}
      </Modal>
    </div>
  )
}
