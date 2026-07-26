import { useEffect, useMemo, useState } from 'react'
import {
  Table, Tag, Typography, message, Alert, InputNumber, Button, Space,
  Input, Select, Checkbox, Modal, Form, Collapse, Image
} from 'antd'
import { EditOutlined, SaveOutlined, WarningOutlined, ToolOutlined, PrinterOutlined, BarcodeOutlined } from '@ant-design/icons'
import InventoryLabelPrint, { printLabelDocument } from '../../components/InventoryLabelPrint'
import { inventoryAPI } from '../../api/inventory'
import { salesAPI } from '../../api/sales'
import { productionAPI } from '../../api/production'
import { apiErrorMessage } from '../../api/client'
import { designImageUrl } from '../../utils/designImage'

const { Title, Text } = Typography

const LOW_STOCK_THRESHOLD = 5

export default function InventoryPage() {
  const [stock, setStock] = useState([])
  const [products, setProducts] = useState([])
  const [designs, setDesigns] = useState([])
  const [adjustments, setAdjustments] = useState([])
  const [loading, setLoading] = useState(true)
  const [loadError, setLoadError] = useState(null)
  const [editingId, setEditingId] = useState(null)
  const [editPrice, setEditPrice] = useState(0)
  const [search, setSearch] = useState('')
  const [categoryFilter, setCategoryFilter] = useState(null)
  const [sizeFilter, setSizeFilter] = useState(null)
  const [lowStockOnly, setLowStockOnly] = useState(false)
  const [adjustOpen, setAdjustOpen] = useState(false)
  const [adjustTarget, setAdjustTarget] = useState(null)
  const [adjustForm] = Form.useForm()
  const [selectedRowKeys, setSelectedRowKeys] = useState([])
  const [labelItems, setLabelItems] = useState([])

  const load = () => {
    setLoading(true)
    setLoadError(null)
    Promise.allSettled([
      inventoryAPI.getAll(),
      salesAPI.getProductsWithStock(),
      inventoryAPI.getAdjustments(),
      productionAPI.getDesigns(),
    ]).then(([inv, prod, adj, des]) => {
      if (inv.status === 'fulfilled') setStock(inv.value.data)
      if (prod.status === 'fulfilled') setProducts(prod.value.data)
      if (adj.status === 'fulfilled') setAdjustments(adj.value.data)
      if (des.status === 'fulfilled') setDesigns(des.value.data)
      if (inv.status === 'rejected') {
        setLoadError(apiErrorMessage(inv.reason))
      }
    }).finally(() => setLoading(false))
  }

  useEffect(() => { load() }, [])

  const categories = useMemo(() =>
    [...new Set(stock.map(s => s.categoryName).filter(Boolean))].sort(),
    [stock]
  )

  const sizes = useMemo(() =>
    [...new Set(stock.map(s => s.sizeValue).filter(Boolean))].sort(),
    [stock]
  )

  const filteredStock = useMemo(() => {
    const q = search.trim().toLowerCase()
    return stock.filter(item => {
      if (lowStockOnly && (item.quantity > LOW_STOCK_THRESHOLD || item.designStatus === 'inactive')) return false
      if (categoryFilter && item.categoryName !== categoryFilter) return false
      if (sizeFilter && item.sizeValue !== sizeFilter) return false
      if (!q) return true
      return (
        (item.designCode || '').toLowerCase().includes(q) ||
        (item.designName || '').toLowerCase().includes(q) ||
        (item.color || '').toLowerCase().includes(q)
      )
    })
  }, [stock, search, categoryFilter, sizeFilter, lowStockOnly])

  const lowStockItems = stock.filter(s =>
    s.quantity <= LOW_STOCK_THRESHOLD && s.designStatus !== 'inactive')

  const designByCode = (code) => designs.find(d => d.designCode === code)

  const priceForSuit = (suitId) => {
    const p = products.find(x => x.suitId === suitId)
    return p ? { productId: p.productId, price: Number(p.sellingPrice) } : null
  }

  const startEdit = (suitId) => {
    const p = priceForSuit(suitId)
    if (!p) {
      message.warning('No product listing for this item yet')
      return
    }
    setEditingId(suitId)
    setEditPrice(p.price)
  }

  const savePrice = async (suitId) => {
    const p = priceForSuit(suitId)
    if (!p) return
    try {
      await salesAPI.updateProduct(p.productId, { sellingPrice: editPrice, status: 'active' })
      message.success('Selling price updated')
      setEditingId(null)
      load()
    } catch (err) {
      message.error(err.response?.data?.message || 'Failed to update price')
    }
  }

  const openAdjust = (record) => {
    setAdjustTarget(record)
    adjustForm.setFieldsValue({ newQuantity: record.quantity, reason: '' })
    setAdjustOpen(true)
  }

  const priceMap = useMemo(() => {
    const m = {}
    products.forEach(p => { m[p.suitId] = Number(p.sellingPrice) || 0 })
    return m
  }, [products])

  const printLabels = (items) => {
    if (!items.length) {
      message.warning('Select at least one item')
      return
    }
    setLabelItems(items)
    setTimeout(() => {
      printLabelDocument()
      setTimeout(() => setLabelItems([]), 500)
    }, 300)
  }

  const printOneLabel = (record) => printLabels([record])

  const submitAdjust = async (values) => {
    if (!adjustTarget) return
    try {
      await inventoryAPI.adjustStock(adjustTarget.suitId, {
        newQuantity: values.newQuantity,
        reason: values.reason,
      })
      message.success('Stock adjusted')
      setAdjustOpen(false)
      setAdjustTarget(null)
      adjustForm.resetFields()
      load()
    } catch (err) {
      message.error(err.response?.data?.message || 'Failed to adjust stock')
    }
  }

  const columns = [
    { title: 'Image', key: 'image', width: 72,
      render: (_, r) => {
        const url = designImageUrl(designByCode(r.designCode))
        return url ? (
          <Image src={url} width={48} height={48}
            style={{ objectFit: 'cover', borderRadius: 4 }} alt={r.designName} />
        ) : <Tag>No image</Tag>
      } },
    { title: 'Design Code', dataIndex: 'designCode', key: 'code' },
    { title: 'Design Name', dataIndex: 'designName', key: 'name' },
    { title: 'Category', dataIndex: 'categoryName', key: 'category',
      render: c => <Tag color={c === 'Kids' ? 'purple' : 'magenta'}>{c}</Tag> },
    { title: 'Size', dataIndex: 'sizeValue', key: 'size' },
    { title: 'Color', dataIndex: 'color', key: 'color' },
    { title: 'Quantity', dataIndex: 'quantity', key: 'qty',
      render: (q, r) => (
        <Tag color={
          r.designStatus === 'inactive' ? 'default'
            : q <= LOW_STOCK_THRESHOLD ? (q === 0 ? 'red' : 'orange') : 'green'
        }>
          {q}{r.designStatus !== 'inactive' && q <= LOW_STOCK_THRESHOLD && q > 0 ? ' ⚠' : ''}
        </Tag>
      ) },
    { title: 'Selling Price (Rs.)', key: 'price',
      render: (_, r) => {
        const p = priceForSuit(r.suitId)
        if (!p) return <Tag>Not set</Tag>
        if (editingId === r.suitId) {
          return (
            <Space>
              <InputNumber min={0} value={editPrice}
                onChange={v => setEditPrice(v || 0)} style={{ width: 120 }} />
              <Button type="primary" size="small" icon={<SaveOutlined />}
                onClick={() => savePrice(r.suitId)} />
              <Button size="small" onClick={() => setEditingId(null)}>Cancel</Button>
            </Space>
          )
        }
        return (
          <Space>
            <span>{p.price > 0 ? p.price.toLocaleString() : '—'}</span>
            <Button type="link" size="small" icon={<EditOutlined />}
              onClick={() => startEdit(r.suitId)} />
          </Space>
        )
      }},
    { title: 'Actions', key: 'actions', width: 140,
      render: (_, r) => (
        <Space>
          <Button size="small" icon={<ToolOutlined />} onClick={() => openAdjust(r)}>Adjust</Button>
          <Button size="small" icon={<BarcodeOutlined />} onClick={() => printOneLabel(r)} title="Print label" />
        </Space>
      ) },
    { title: 'Last Updated', dataIndex: 'lastUpdated', key: 'updated',
      render: d => d ? new Date(d).toLocaleString() : '-' },
  ]

  const adjustmentColumns = [
    { title: 'Date', dataIndex: 'createdAt', key: 'date',
      render: d => d ? new Date(d).toLocaleString() : '-' },
    { title: 'Item', key: 'item',
      render: (_, r) => `${r.designCode} — ${r.sizeValue} / ${r.color}` },
    { title: 'Before', dataIndex: 'previousQuantity', key: 'before' },
    { title: 'After', dataIndex: 'newQuantity', key: 'after' },
    { title: 'Reason', dataIndex: 'reason', key: 'reason', ellipsis: true },
    { title: 'By', dataIndex: 'adjustedByUsername', key: 'by', render: u => u || '—' },
  ]

  return (
    <div>
      <Title level={4} className="page-title">Inventory Stock</Title>

      {lowStockItems.length > 0 && (
        <Alert
          type="warning"
          showIcon
          icon={<WarningOutlined />}
          style={{ marginBottom: 16 }}
          message={`Low stock alert — ${lowStockItems.length} item(s) at or below ${LOW_STOCK_THRESHOLD} units`}
          description={
            <ul style={{ margin: '8px 0 0', paddingLeft: 20 }}>
              {lowStockItems.map(item => (
                <li key={item.inventoryId}>
                  <Text strong>{item.designName}</Text> ({item.designCode}) —{' '}
                  {item.sizeValue} / {item.color}:{' '}
                  <Text type={item.quantity === 0 ? 'danger' : 'warning'}>
                    {item.quantity} in stock
                  </Text>
                </li>
              ))}
            </ul>
          }
        />
      )}

      <Space wrap style={{ marginBottom: 16 }} size="middle">
        <Input
          placeholder="Search design code, name, or color"
          value={search}
          onChange={e => setSearch(e.target.value)}
          allowClear
          style={{ width: 240 }}
        />
        <Select
          placeholder="Category"
          allowClear
          style={{ width: 140 }}
          value={categoryFilter}
          onChange={setCategoryFilter}
          options={categories.map(c => ({ value: c, label: c }))}
        />
        <Select
          placeholder="Size"
          allowClear
          style={{ width: 120 }}
          value={sizeFilter}
          onChange={setSizeFilter}
          options={sizes.map(s => ({ value: s, label: s }))}
        />
        <Checkbox checked={lowStockOnly} onChange={e => setLowStockOnly(e.target.checked)}>
          Low stock only (≤ {LOW_STOCK_THRESHOLD})
        </Checkbox>
        <Button
          icon={<PrinterOutlined />}
          disabled={selectedRowKeys.length === 0}
          onClick={() => printLabels(filteredStock.filter(r => selectedRowKeys.includes(r.inventoryId)))}
        >
          Print labels ({selectedRowKeys.length})
        </Button>
      </Space>

      {loadError && (
        <Alert type="error" message={loadError} style={{ marginBottom: 16 }} showIcon />
      )}

      <Table
        dataSource={filteredStock}
        columns={columns}
        rowKey="inventoryId"
        loading={loading}
        rowSelection={{
          selectedRowKeys,
          onChange: setSelectedRowKeys,
        }}
        rowClassName={(r) =>
          r.designStatus !== 'inactive' && r.quantity <= LOW_STOCK_THRESHOLD
            ? 'inventory-low-stock-row' : ''}
        locale={{ emptyText: 'No stock yet — record a return at Press and Packing (final stage) with OK pieces' }}
      />

      <InventoryLabelPrint items={labelItems} prices={priceMap} />

      <Collapse
        style={{ marginTop: 24 }}
        items={[{
          key: 'history',
          label: `Adjustment history (${adjustments.length})`,
          children: (
            <Table
              dataSource={adjustments}
              columns={adjustmentColumns}
              rowKey="adjustmentId"
              size="small"
              pagination={{ pageSize: 10 }}
              locale={{ emptyText: 'No manual adjustments yet' }}
            />
          ),
        }]}
      />

      <Modal
        title={adjustTarget ? `Adjust stock — ${adjustTarget.designCode} / ${adjustTarget.sizeValue} / ${adjustTarget.color}` : 'Adjust stock'}
        open={adjustOpen}
        onCancel={() => { setAdjustOpen(false); setAdjustTarget(null) }}
        footer={null}
      >
        <Form form={adjustForm} layout="vertical" onFinish={submitAdjust}>
          <Form.Item label="Current quantity">
            <Text strong>{adjustTarget?.quantity ?? 0}</Text>
          </Form.Item>
          <Form.Item
            name="newQuantity"
            label="New quantity"
            rules={[{ required: true, message: 'Enter new quantity' }]}
          >
            <InputNumber min={0} style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item
            name="reason"
            label="Reason"
            rules={[{ required: true, message: 'Reason is required' }]}
          >
            <Input.TextArea rows={3} placeholder="e.g. Physical count correction, damaged goods write-off" />
          </Form.Item>
          <Form.Item>
            <Space>
              <Button type="primary" htmlType="submit">Save adjustment</Button>
              <Button onClick={() => { setAdjustOpen(false); setAdjustTarget(null) }}>Cancel</Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )
}
