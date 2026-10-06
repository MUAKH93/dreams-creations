import { useEffect, useMemo, useState } from 'react'
import {
  Table, Tag, Typography, message, Alert, InputNumber, Button, Space,
  Input, Select, Checkbox, Modal, Form, Collapse, Image, Card, Row, Col, Statistic, Tabs
} from 'antd'
import {
  EditOutlined, SaveOutlined, WarningOutlined, ToolOutlined,
  PrinterOutlined, BarcodeOutlined, InboxOutlined, AppstoreOutlined,
} from '@ant-design/icons'
import InventoryLabelPrint, { printLabelDocument } from '../../components/InventoryLabelPrint'
import { inventoryAPI } from '../../api/inventory'
import { salesAPI } from '../../api/sales'
import { productionAPI } from '../../api/production'
import { apiErrorMessage } from '../../api/client'
import { designImageUrl } from '../../utils/designImage'
import '../../styles/inventory-page.css'

const { Title, Text } = Typography

const LOW_STOCK_THRESHOLD = 5

function qtyTagColor(record) {
  if (record.designStatus === 'inactive') return 'default'
  if (record.quantity === 0) return 'red'
  if (record.quantity <= LOW_STOCK_THRESHOLD) return 'orange'
  return 'green'
}

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
  const [lowStockOnly, setLowStockOnly] = useState(false)
  const [viewMode, setViewMode] = useState('by-design')
  const [selectedDesignCode, setSelectedDesignCode] = useState(null)
  const [sizeFilter, setSizeFilter] = useState(null)
  const [colorFilter, setColorFilter] = useState(null)
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
      if (inv.status === 'rejected') setLoadError(apiErrorMessage(inv.reason))
    }).finally(() => setLoading(false))
  }

  useEffect(() => { load() }, [])

  const designByCode = (code) => designs.find(d => d.designCode === code)

  const categories = useMemo(() =>
    [...new Set(stock.map(s => s.categoryName).filter(Boolean))].sort(),
    [stock]
  )

  const designsInStock = useMemo(() => {
    const map = new Map()
    stock.forEach(item => {
      if (categoryFilter && item.categoryName !== categoryFilter) return
      const q = search.trim().toLowerCase()
      if (q && !(
        (item.designCode || '').toLowerCase().includes(q) ||
        (item.designName || '').toLowerCase().includes(q)
      )) return

      if (!map.has(item.designCode)) {
        map.set(item.designCode, {
          designCode: item.designCode,
          designName: item.designName,
          categoryName: item.categoryName,
          designStatus: item.designStatus,
          totalQty: 0,
          variantCount: 0,
          lowCount: 0,
        })
      }
      const row = map.get(item.designCode)
      row.totalQty += item.quantity || 0
      row.variantCount += 1
      if (item.designStatus !== 'inactive' && item.quantity <= LOW_STOCK_THRESHOLD) {
        row.lowCount += 1
      }
    })
    return [...map.values()].sort((a, b) => a.designCode.localeCompare(b.designCode))
  }, [stock, search, categoryFilter])

  useEffect(() => {
    if (!designsInStock.length) {
      setSelectedDesignCode(null)
      return
    }
    if (!selectedDesignCode || !designsInStock.some(d => d.designCode === selectedDesignCode)) {
      setSelectedDesignCode(designsInStock[0].designCode)
    }
  }, [designsInStock, selectedDesignCode])

  const selectedDesignMeta = designsInStock.find(d => d.designCode === selectedDesignCode)

  const designVariants = useMemo(() =>
    stock.filter(item => item.designCode === selectedDesignCode),
    [stock, selectedDesignCode]
  )

  const sizesForDesign = useMemo(() =>
    [...new Set(designVariants.map(v => v.sizeValue).filter(Boolean))].sort(),
    [designVariants]
  )

  const colorsForDesign = useMemo(() => {
    const pool = sizeFilter
      ? designVariants.filter(v => v.sizeValue === sizeFilter)
      : designVariants
    return [...new Set(pool.map(v => v.color).filter(Boolean))].sort()
  }, [designVariants, sizeFilter])

  useEffect(() => {
    if (sizeFilter && !sizesForDesign.includes(sizeFilter)) setSizeFilter(null)
  }, [sizeFilter, sizesForDesign])

  useEffect(() => {
    if (colorFilter && !colorsForDesign.includes(colorFilter)) setColorFilter(null)
  }, [colorFilter, colorsForDesign])

  const filteredByDesign = useMemo(() => {
    return designVariants.filter(item => {
      if (lowStockOnly && (item.quantity > LOW_STOCK_THRESHOLD || item.designStatus === 'inactive')) {
        return false
      }
      if (sizeFilter && item.sizeValue !== sizeFilter) return false
      if (colorFilter && item.color !== colorFilter) return false
      return true
    }).sort((a, b) => {
      const sizeCmp = (a.sizeValue || '').localeCompare(b.sizeValue || '')
      if (sizeCmp !== 0) return sizeCmp
      return (a.color || '').localeCompare(b.color || '')
    })
  }, [designVariants, sizeFilter, colorFilter, lowStockOnly])

  const filteredAllStock = useMemo(() => {
    const q = search.trim().toLowerCase()
    return stock.filter(item => {
      if (lowStockOnly && (item.quantity > LOW_STOCK_THRESHOLD || item.designStatus === 'inactive')) return false
      if (categoryFilter && item.categoryName !== categoryFilter) return false
      if (!q) return true
      return (
        (item.designCode || '').toLowerCase().includes(q) ||
        (item.designName || '').toLowerCase().includes(q) ||
        (item.color || '').toLowerCase().includes(q) ||
        (item.sizeValue || '').toLowerCase().includes(q)
      )
    })
  }, [stock, search, categoryFilter, lowStockOnly])

  const tableRows = viewMode === 'by-design' ? filteredByDesign : filteredAllStock

  const lowStockItems = stock.filter(s =>
    s.quantity <= LOW_STOCK_THRESHOLD && s.designStatus !== 'inactive')

  const totalUnits = stock.reduce((sum, s) => sum + (s.quantity || 0), 0)
  const skuCount = stock.length

  const priceForSuit = (suitId) => {
    const p = products.find(x => x.suitId === suitId)
    return p ? { productId: p.productId, price: Number(p.sellingPrice) } : null
  }

  const priceMap = useMemo(() => {
    const m = {}
    products.forEach(p => { m[p.suitId] = Number(p.sellingPrice) || 0 })
    return m
  }, [products])

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

  const onDesignChange = (code) => {
    setSelectedDesignCode(code)
    setSizeFilter(null)
    setColorFilter(null)
    setSelectedRowKeys([])
  }

  const clearVariantFilters = () => {
    setSizeFilter(null)
    setColorFilter(null)
  }

  const variantColumns = [
    ...(viewMode === 'all' ? [
      { title: 'Design', key: 'design', width: 160, ellipsis: true,
        render: (_, r) => (
          <div>
            <Text strong>{r.designCode}</Text>
            <div><Text type="secondary" style={{ fontSize: 12 }}>{r.designName}</Text></div>
          </div>
        ) },
      { title: 'Category', dataIndex: 'categoryName', key: 'category', width: 100,
        render: c => <Tag color={c === 'Kids' ? 'purple' : 'magenta'}>{c}</Tag> },
    ] : []),
    { title: 'Size', dataIndex: 'sizeValue', key: 'size', width: 100,
      render: v => <Text strong>{v || '—'}</Text> },
    { title: 'Color', dataIndex: 'color', key: 'color', width: 120,
      render: v => <Tag>{v || '—'}</Tag> },
    { title: 'Quantity', dataIndex: 'quantity', key: 'qty', width: 110, align: 'center',
      render: (q, r) => (
        <Tag className="inventory-qty-tag" color={qtyTagColor(r)}>
          {q}
          {r.designStatus !== 'inactive' && q <= LOW_STOCK_THRESHOLD && q > 0 ? ' ⚠' : ''}
        </Tag>
      ) },
    { title: 'Price (Rs.)', key: 'price', width: 180,
      render: (_, r) => {
        const p = priceForSuit(r.suitId)
        if (!p) return <Tag>Not set</Tag>
        if (editingId === r.suitId) {
          return (
            <Space>
              <InputNumber min={0} value={editPrice}
                onChange={v => setEditPrice(v || 0)} style={{ width: 110 }} />
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
    { title: 'Actions', key: 'actions', width: 150, fixed: 'right',
      render: (_, r) => (
        <Space size="small">
          <Button size="small" icon={<ToolOutlined />} onClick={() => openAdjust(r)}>Adjust</Button>
          <Button size="small" icon={<BarcodeOutlined />} onClick={() => printOneLabel(r)} title="Print label" />
        </Space>
      ) },
    { title: 'Updated', dataIndex: 'lastUpdated', key: 'updated', width: 150,
      render: d => d ? new Date(d).toLocaleDateString() : '—' },
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

  const selectedDesign = designByCode(selectedDesignCode)
  const previewUrl = selectedDesign ? designImageUrl(selectedDesign) : null

  const designPanel = viewMode === 'by-design' && (
    <div className="inventory-design-panel">
      <div className="inventory-design-panel__header">
        <div className="inventory-design-panel__preview">
          {previewUrl ? (
            <Image src={previewUrl} alt={selectedDesignMeta?.designName} preview={false} />
          ) : (
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', height: '100%', color: '#94a3b8', fontSize: 12 }}>
              No image
            </div>
          )}
        </div>
        <div className="inventory-design-panel__meta">
          <p className="inventory-design-panel__title">{selectedDesignMeta?.designName || 'Select a design'}</p>
          <p className="inventory-design-panel__code">{selectedDesignCode || '—'}</p>
          <div className="inventory-design-panel__stats">
            {selectedDesignMeta?.categoryName && (
              <Tag color={selectedDesignMeta.categoryName === 'Kids' ? 'purple' : 'magenta'}>
                {selectedDesignMeta.categoryName}
              </Tag>
            )}
            {selectedDesignMeta && (
              <>
                <Tag icon={<InboxOutlined />}>{selectedDesignMeta.totalQty} units</Tag>
                <Tag>{selectedDesignMeta.variantCount} variants</Tag>
                {selectedDesignMeta.lowCount > 0 && (
                  <Tag color="orange">{selectedDesignMeta.lowCount} low stock</Tag>
                )}
              </>
            )}
          </div>
        </div>
        <Select
          showSearch
          placeholder="Choose design"
          optionFilterProp="label"
          value={selectedDesignCode}
          onChange={onDesignChange}
          style={{ minWidth: 280, maxWidth: 360 }}
          options={designsInStock.map(d => ({
            value: d.designCode,
            label: `${d.designCode} — ${d.designName}`,
          }))}
        />
      </div>

      <div className="inventory-variant-filters">
        <span className="inventory-variant-filters__label">Size</span>
        <Select
          allowClear
          placeholder="All sizes"
          style={{ width: 130 }}
          value={sizeFilter}
          onChange={setSizeFilter}
          options={sizesForDesign.map(s => ({ value: s, label: s }))}
        />
        <span className="inventory-variant-filters__label">Color</span>
        <Select
          allowClear
          placeholder="All colors"
          style={{ width: 150 }}
          value={colorFilter}
          onChange={setColorFilter}
          options={colorsForDesign.map(c => ({ value: c, label: c }))}
        />
        <Button type="link" onClick={clearVariantFilters} disabled={!sizeFilter && !colorFilter}>
          Clear filters
        </Button>
        <Checkbox checked={lowStockOnly} onChange={e => setLowStockOnly(e.target.checked)}>
          Low stock only (≤ {LOW_STOCK_THRESHOLD})
        </Checkbox>
      </div>
    </div>
  )

  return (
    <div>
      <Title level={4} className="page-title">Inventory Stock</Title>

      <Row gutter={[16, 16]} className="inventory-page__summary">
        <Col xs={24} sm={8}>
          <Card size="small">
            <Statistic title="Total SKUs" value={skuCount} prefix={<AppstoreOutlined />} />
          </Card>
        </Col>
        <Col xs={24} sm={8}>
          <Card size="small">
            <Statistic title="Total units in stock" value={totalUnits} prefix={<InboxOutlined />} />
          </Card>
        </Col>
        <Col xs={24} sm={8}>
          <Card size="small">
            <Statistic
              title="Low stock items"
              value={lowStockItems.length}
              prefix={<WarningOutlined />}
              valueStyle={{ color: lowStockItems.length > 0 ? '#fa8c16' : '#3f8600' }}
            />
          </Card>
        </Col>
      </Row>

      {lowStockItems.length > 0 && (
        <Alert
          type="warning"
          showIcon
          icon={<WarningOutlined />}
          style={{ marginBottom: 16 }}
          message={`${lowStockItems.length} item(s) at or below ${LOW_STOCK_THRESHOLD} units`}
          description={
            <Text type="secondary">
              Use <strong>By design</strong> view, pick the design, then filter by size and color to adjust stock quickly.
            </Text>
          }
        />
      )}

      <div className="inventory-toolbar">
        <Input
          placeholder="Search design code or name"
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
        {viewMode === 'all' && (
          <Checkbox checked={lowStockOnly} onChange={e => setLowStockOnly(e.target.checked)}>
            Low stock only
          </Checkbox>
        )}
        <Button
          icon={<PrinterOutlined />}
          disabled={selectedRowKeys.length === 0}
          onClick={() => printLabels(tableRows.filter(r => selectedRowKeys.includes(r.inventoryId)))}
        >
          Print labels ({selectedRowKeys.length})
        </Button>
      </div>

      <Tabs
        className="inventory-view-tabs"
        activeKey={viewMode}
        onChange={setViewMode}
        items={[
          { key: 'by-design', label: 'By design' },
          { key: 'all', label: 'All items' },
        ]}
      />

      {designPanel}

      {loadError && (
        <Alert type="error" message={loadError} style={{ marginBottom: 16 }} showIcon />
      )}

      <div className="inventory-table-wrap">
        <Table
          className="inventory-table"
          dataSource={tableRows}
          columns={variantColumns}
          rowKey="inventoryId"
          loading={loading}
          bordered
          size="middle"
          scroll={{ x: viewMode === 'all' ? 1100 : 900 }}
          rowSelection={{
            selectedRowKeys,
            onChange: setSelectedRowKeys,
          }}
          rowClassName={(r) =>
            r.designStatus !== 'inactive' && r.quantity <= LOW_STOCK_THRESHOLD
              ? 'inventory-low-stock-row' : ''}
          pagination={{
            pageSize: 12,
            showSizeChanger: true,
            pageSizeOptions: ['12', '24', '48'],
            showTotal: (t) => `${t} variant${t === 1 ? '' : 's'}`,
          }}
          locale={{
            emptyText: viewMode === 'by-design'
              ? 'No variants for this design — try another size/color or design'
              : 'No stock yet — record a return at Press and Packing',
          }}
        />
      </div>

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
        title={adjustTarget
          ? `Adjust — ${adjustTarget.designCode} / ${adjustTarget.sizeValue} / ${adjustTarget.color}`
          : 'Adjust stock'}
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
