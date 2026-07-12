import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  Table, Button, Modal, Form, Select, InputNumber, Input, Tag, Typography,
  Space, message, Descriptions, Divider, Alert,
} from 'antd'
import { PlusOutlined, EyeOutlined, SendOutlined, PictureOutlined } from '@ant-design/icons'
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

export default function MyQuotesPage() {
  const navigate = useNavigate()
  const [quotations, setQuotations] = useState([])
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
      quotationsAPI.getMy(),
      salesAPI.getDesigns(),
      salesAPI.getSizes(),
    ]).then(([q, d, sz]) => {
      if (q.status === 'fulfilled') setQuotations(q.value.data)
      else message.error(apiErrorMessage(q.reason))
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

  const openCreate = () => {
    form.resetFields()
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

  const onSave = async (values) => {
    if (!items.every(it => it.designId && it.quantity > 0)) {
      message.error('Each line needs a design and quantity')
      return
    }
    try {
      await quotationsAPI.create({
        notes: values.notes || null,
        items: items.map(it => ({
          design: { designId: it.designId },
          size: it.sizeId ? { sizeId: it.sizeId } : null,
          color: it.color || null,
          quantity: it.quantity,
          unitPrice: it.unitPrice,
          notes: it.notes || null,
        })),
      })
      message.success('Quote saved as draft')
      setModalOpen(false)
      form.resetFields()
      setItems([emptyLine()])
      load()
    } catch (err) {
      message.error(err.response?.data?.message || 'Failed to save quote')
    }
  }

  const handleSubmit = async (id) => {
    try {
      await quotationsAPI.submit(id)
      message.success('Quote submitted for review')
      setDetailOpen(false)
      load()
    } catch (err) {
      message.error(err.response?.data?.message || 'Submit failed')
    }
  }

  const columns = [
    { title: 'Quote #', dataIndex: 'quotationNumber', key: 'num' },
    { title: 'Status', dataIndex: 'status', key: 'status',
      render: s => <Tag color={statusColor[s] || 'default'}>{s?.toUpperCase()}</Tag> },
    { title: 'Estimated Total', dataIndex: 'finalAmount', key: 'total',
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
        <Title level={4} className="page-title" style={{ margin: 0 }}>My Quote Requests</Title>
        <Space>
          <Button icon={<PictureOutlined />} onClick={() => navigate('/designs')}>Browse Designs</Button>
          <Button type="primary" icon={<PlusOutlined />} onClick={openCreate}>Request Quote</Button>
        </Space>
      </div>

      <Alert
        type="info"
        showIcon
        style={{ marginBottom: 16 }}
        message="Browse designs, build a quote request, and submit it. Our team will review and convert approved quotes to bills."
      />

      <Table dataSource={quotations} columns={columns} rowKey="quotationId" loading={loading} />

      <Modal title="Request a Quote" open={modalOpen} onCancel={() => setModalOpen(false)} footer={null} width={720}>
        <Form form={form} layout="vertical" onFinish={onSave}>
          <Form.Item name="notes" label="Notes / special requests">
            <Input.TextArea rows={2} placeholder="Preferred delivery, custom sizing, etc." />
          </Form.Item>

          <Divider>Items</Divider>
          {items.map((line, i) => (
            <div key={i} style={{ marginBottom: 12, padding: 12, background: '#fafafa', borderRadius: 8 }}>
              <Space wrap style={{ width: '100%' }}>
                <Select
                  placeholder="Design"
                  style={{ width: 180 }}
                  value={line.designId}
                  onChange={v => onDesignChange(i, v)}
                  options={designs.filter(d => d.status !== 'inactive').map(d => ({
                    value: d.designId,
                    label: `${d.designCode} — Rs. ${Number(d.basePrice || 0).toLocaleString()}`,
                  }))}
                />
                <Select
                  placeholder="Size (optional)"
                  style={{ width: 110 }}
                  value={line.sizeId}
                  disabled={!line.designId}
                  allowClear
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
                  style={{ width: 110 }} addonBefore="Rs" />
                {items.length > 1 && (
                  <Button danger size="small" onClick={() => setItems(items.filter((_, idx) => idx !== i))}>
                    Remove
                  </Button>
                )}
              </Space>
            </div>
          ))}
          <Button type="dashed" onClick={() => setItems([...items, emptyLine()])} style={{ marginBottom: 16 }}>
            Add another design
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
        title={selected ? `Quote ${selected.quotationNumber}` : 'Quote'}
        open={detailOpen}
        onCancel={() => setDetailOpen(false)}
        footer={selected ? (
          <Space>
            {selected.status === 'draft' && (
              <Button type="primary" icon={<SendOutlined />} onClick={() => handleSubmit(selected.quotationId)}>
                Submit for Review
              </Button>
            )}
            <Button onClick={() => setDetailOpen(false)}>Close</Button>
          </Space>
        ) : null}
        width={600}
      >
        {selected && (
          <>
            <Descriptions column={1} size="small" bordered>
              <Descriptions.Item label="Status">
                <Tag color={statusColor[selected.status]}>{selected.status?.toUpperCase()}</Tag>
              </Descriptions.Item>
              <Descriptions.Item label="Estimated Total">
                <Text strong>Rs. {Number(selected.finalAmount || 0).toLocaleString()}</Text>
              </Descriptions.Item>
              {selected.discount > 0 && (
                <Descriptions.Item label="Discount applied">
                  Rs. {Number(selected.discount || 0).toLocaleString()}
                </Descriptions.Item>
              )}
              {selected.bill && (
                <Descriptions.Item label="Bill created">{selected.bill.billNumber}</Descriptions.Item>
              )}
              {selected.notes && <Descriptions.Item label="Your notes">{selected.notes}</Descriptions.Item>}
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
                { title: 'Line Total', dataIndex: 'totalPrice', key: 't',
                  render: v => `Rs. ${Number(v || 0).toLocaleString()}` },
              ]}
            />
          </>
        )}
      </Modal>
    </div>
  )
}
