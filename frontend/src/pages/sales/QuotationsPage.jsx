import { useEffect, useState } from 'react'
import {
  Table, Button, Modal, Form, Select, InputNumber, Input, Tag, Typography,
  Space, message, Descriptions, Divider, Popconfirm, Alert,
} from 'antd'
import { PlusOutlined, EyeOutlined, CheckOutlined, CloseOutlined, FileTextOutlined } from '@ant-design/icons'
import { quotationsAPI } from '../../api/quotations'
import { salesAPI } from '../../api/sales'
import { apiErrorMessage } from '../../api/client'

const { Title, Text } = Typography

const emptyLine = () => ({
  designId: null, sizeId: null, color: '', quantity: 1, unitPrice: 0, notes: '',
})

const statusColor = {
  draft: 'default',
  submitted: 'blue',
  approved: 'green',
  rejected: 'red',
  converted: 'purple',
}

function customerName(c) {
  if (!c) return '—'
  return `${c.firstName || ''} ${c.lastName || ''}`.trim()
}

export default function QuotationsPage() {
  const [quotations, setQuotations] = useState([])
  const [customers, setCustomers] = useState([])
  const [designs, setDesigns] = useState([])
  const [sizes, setSizes] = useState([])
  const [loading, setLoading] = useState(true)
  const [modalOpen, setModalOpen] = useState(false)
  const [detailOpen, setDetailOpen] = useState(false)
  const [selected, setSelected] = useState(null)
  const [items, setItems] = useState([emptyLine()])
  const [form] = Form.useForm()

  const load = () => {
    setLoading(true)
    Promise.allSettled([
      quotationsAPI.getAll(),
      salesAPI.getCustomers(),
      salesAPI.getDesigns(),
      salesAPI.getSizes(),
    ]).then(([q, c, d, sz]) => {
      if (q.status === 'fulfilled') setQuotations(q.value.data)
      else message.error(apiErrorMessage(q.reason))
      if (c.status === 'fulfilled') setCustomers(c.value.data)
      if (d.status === 'fulfilled') setDesigns(d.value.data)
      if (sz.status === 'fulfilled') setSizes(sz.value.data)
    }).finally(() => setLoading(false))
  }

  useEffect(() => { load() }, [])

  const getDesign = (id) => designs.find(d => d.designId === id)

  const sizesForDesign = (designId) => {
    const design = getDesign(designId)
    if (!design) return []
    return sizes.filter(s => s.category?.categoryId === design.category?.categoryId)
  }

  const updateLine = (i, patch) => {
    setItems(prev => {
      const updated = [...prev]
      updated[i] = { ...updated[i], ...patch }
      return updated
    })
  }

  const onDesignChange = (i, designId) => {
    const design = getDesign(designId)
    updateLine(i, {
      designId,
      sizeId: null,
      unitPrice: design?.basePrice ? Number(design.basePrice) : 0,
    })
  }

  const openCreate = async () => {
    try {
      const res = await quotationsAPI.getNextNumber()
      form.setFieldsValue({ discount: 0 })
      form.setFieldValue('quotationNumber', res.data.quotationNumber)
    } catch {
      form.setFieldsValue({ discount: 0 })
    }
    setItems([emptyLine()])
    setModalOpen(true)
  }

  const viewDetail = async (record) => {
    try {
      const res = await quotationsAPI.getById(record.quotationId)
      setSelected(res.data)
      setDetailOpen(true)
    } catch {
      message.error('Could not load quotation')
    }
  }

  const onCustomerChange = (customerId) => {
    const cust = customers.find(c => c.customerId === customerId)
    if (cust?.discountPercent > 0) {
      message.info(`Customer has ${cust.discountPercent}% standing discount (applied automatically if discount left at 0)`)
    }
  }

  const buildPayload = (values) => ({
    quotationNumber: values.quotationNumber,
    customer: { customerId: values.customerId },
    discount: values.discount || 0,
    notes: values.notes || null,
    status: 'draft',
    items: items.map(it => ({
      design: { designId: it.designId },
      size: it.sizeId ? { sizeId: it.sizeId } : null,
      color: it.color || null,
      quantity: it.quantity,
      unitPrice: it.unitPrice,
      notes: it.notes || null,
    })),
  })

  const onCreate = async (values) => {
    if (!items.every(it => it.designId && it.quantity > 0)) {
      message.error('Each line needs a design and quantity')
      return
    }
    try {
      await quotationsAPI.create(buildPayload(values))
      message.success('Quotation created')
      setModalOpen(false)
      form.resetFields()
      setItems([emptyLine()])
      load()
    } catch (err) {
      message.error(err.response?.data?.message || 'Failed to create quotation')
    }
  }

  const handleStatus = async (id, status) => {
    try {
      await quotationsAPI.updateStatus(id, status)
      message.success(`Quotation ${status}`)
      setDetailOpen(false)
      load()
    } catch (err) {
      message.error(err.response?.data?.message || 'Status update failed')
    }
  }

  const handleConvert = async (id) => {
    try {
      const res = await quotationsAPI.convertToBill(id)
      message.success(`Converted to bill ${res.data.bill?.billNumber || ''}`)
      setDetailOpen(false)
      load()
    } catch (err) {
      message.error(err.response?.data?.message || 'Conversion failed — ensure size, color & stock exist')
    }
  }

  const columns = [
    { title: 'Quote #', dataIndex: 'quotationNumber', key: 'num' },
    { title: 'Customer', key: 'cust', render: (_, r) => customerName(r.customer) },
    { title: 'Status', dataIndex: 'status', key: 'status',
      render: s => <Tag color={statusColor[s] || 'default'}>{s?.toUpperCase()}</Tag> },
    { title: 'Total', dataIndex: 'finalAmount', key: 'total',
      render: v => `Rs. ${Number(v || 0).toLocaleString()}` },
    { title: 'Date', dataIndex: 'createdAt', key: 'date',
      render: d => d ? new Date(d).toLocaleDateString() : '—' },
    { title: 'Actions', key: 'actions',
      render: (_, r) => (
        <Button size="small" icon={<EyeOutlined />} onClick={() => viewDetail(r)}>View</Button>
      ) },
  ]

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 24 }}>
        <Title level={4} className="page-title" style={{ margin: 0 }}>Quotations</Title>
        <Button type="primary" icon={<PlusOutlined />} onClick={openCreate}>New Quotation</Button>
      </div>

      <Alert
        type="info"
        showIcon
        style={{ marginBottom: 16 }}
        message="Approve submitted quotes, then convert to a bill when stock is available. Customer discount % applies automatically when discount is left at 0."
      />

      <Table dataSource={quotations} columns={columns} rowKey="quotationId" loading={loading} />

      <Modal title="New Quotation" open={modalOpen} onCancel={() => setModalOpen(false)} footer={null} width={720}>
        <Form form={form} layout="vertical" onFinish={onCreate}>
          <Form.Item name="quotationNumber" label="Quote Number">
            <Input disabled />
          </Form.Item>
          <Form.Item name="customerId" label="Customer" rules={[{ required: true }]}>
            <Select showSearch optionFilterProp="label" onChange={onCustomerChange}
              options={customers.map(c => ({
                value: c.customerId,
                label: `${customerName(c)}${c.discountPercent ? ` (${c.discountPercent}% off)` : ''}`,
              }))} />
          </Form.Item>
          <Form.Item name="discount" label="Discount (Rs.)" tooltip="Leave 0 to apply customer standing discount %">
            <InputNumber min={0} style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="notes" label="Notes">
            <Input.TextArea rows={2} />
          </Form.Item>

          <Divider>Line Items</Divider>
          {items.map((line, i) => (
            <div key={i} style={{ marginBottom: 12, padding: 12, background: '#fafafa', borderRadius: 8 }}>
              <RowLine
                line={line} i={i} designs={designs}
                sizesForDesign={sizesForDesign}
                onDesignChange={onDesignChange}
                updateLine={updateLine}
                onRemove={() => setItems(items.filter((_, idx) => idx !== i))}
                canRemove={items.length > 1}
              />
            </div>
          ))}
          <Button type="dashed" onClick={() => setItems([...items, emptyLine()])} style={{ marginBottom: 16 }}>
            Add line
          </Button>

          <Form.Item>
            <Space>
              <Button type="primary" htmlType="submit">Save Draft</Button>
              <Button onClick={() => setModalOpen(false)}>Cancel</Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title={selected ? `Quotation ${selected.quotationNumber}` : 'Quotation'}
        open={detailOpen}
        onCancel={() => setDetailOpen(false)}
        footer={selected ? (
          <Space>
            {selected.status === 'submitted' && (
              <>
                <Popconfirm title="Approve this quotation?" onConfirm={() => handleStatus(selected.quotationId, 'approved')}>
                  <Button type="primary" icon={<CheckOutlined />}>Approve</Button>
                </Popconfirm>
                <Popconfirm title="Reject this quotation?" onConfirm={() => handleStatus(selected.quotationId, 'rejected')}>
                  <Button danger icon={<CloseOutlined />}>Reject</Button>
                </Popconfirm>
              </>
            )}
            {['submitted', 'approved'].includes(selected.status) && (
              <Popconfirm
                title="Convert to bill?"
                description="Stock will be deducted. Each line needs size, color & available stock."
                onConfirm={() => handleConvert(selected.quotationId)}
              >
                <Button type="primary" icon={<FileTextOutlined />}>Convert to Bill</Button>
              </Popconfirm>
            )}
            <Button onClick={() => setDetailOpen(false)}>Close</Button>
          </Space>
        ) : null}
        width={640}
      >
        {selected && (
          <>
            <Descriptions column={2} size="small" bordered>
              <Descriptions.Item label="Customer">{customerName(selected.customer)}</Descriptions.Item>
              <Descriptions.Item label="Status">
                <Tag color={statusColor[selected.status]}>{selected.status?.toUpperCase()}</Tag>
              </Descriptions.Item>
              <Descriptions.Item label="Subtotal">Rs. {Number(selected.totalAmount || 0).toLocaleString()}</Descriptions.Item>
              <Descriptions.Item label="Discount">Rs. {Number(selected.discount || 0).toLocaleString()}</Descriptions.Item>
              <Descriptions.Item label="Final" span={2}>
                <Text strong>Rs. {Number(selected.finalAmount || 0).toLocaleString()}</Text>
              </Descriptions.Item>
              {selected.bill && (
                <Descriptions.Item label="Bill" span={2}>{selected.bill.billNumber}</Descriptions.Item>
              )}
              {selected.notes && (
                <Descriptions.Item label="Notes" span={2}>{selected.notes}</Descriptions.Item>
              )}
            </Descriptions>
            <Divider />
            <Table
              size="small"
              pagination={false}
              rowKey={(r, idx) => r.quotationItemId || idx}
              dataSource={selected.items || []}
              columns={[
                { title: 'Design', key: 'd', render: (_, r) => r.design?.designCode || '—' },
                { title: 'Size', key: 's', render: (_, r) => r.size?.sizeValue || '—' },
                { title: 'Color', dataIndex: 'color', key: 'c', render: c => c || '—' },
                { title: 'Qty', dataIndex: 'quantity', key: 'q' },
                { title: 'Unit', dataIndex: 'unitPrice', key: 'u',
                  render: v => `Rs. ${Number(v || 0).toLocaleString()}` },
                { title: 'Total', dataIndex: 'totalPrice', key: 't',
                  render: v => `Rs. ${Number(v || 0).toLocaleString()}` },
              ]}
            />
          </>
        )}
      </Modal>
    </div>
  )
}

function RowLine({ line, i, designs, sizesForDesign, onDesignChange, updateLine, onRemove, canRemove }) {
  return (
    <Space wrap style={{ width: '100%' }}>
      <Select
        placeholder="Design"
        style={{ width: 160 }}
        value={line.designId}
        onChange={v => onDesignChange(i, v)}
        options={designs.map(d => ({ value: d.designId, label: d.designCode }))}
      />
      <Select
        placeholder="Size"
        style={{ width: 100 }}
        value={line.sizeId}
        disabled={!line.designId}
        onChange={v => updateLine(i, { sizeId: v })}
        options={sizesForDesign(line.designId).map(s => ({ value: s.sizeId, label: s.sizeValue }))}
      />
      <Input
        placeholder="Color"
        style={{ width: 100 }}
        value={line.color}
        onChange={e => updateLine(i, { color: e.target.value })}
      />
      <InputNumber min={1} value={line.quantity} onChange={v => updateLine(i, { quantity: v })}
        style={{ width: 70 }} />
      <InputNumber min={0} value={line.unitPrice} onChange={v => updateLine(i, { unitPrice: v })}
        style={{ width: 100 }} addonBefore="Rs" />
      {canRemove && <Button danger size="small" onClick={onRemove}>Remove</Button>}
    </Space>
  )
}
