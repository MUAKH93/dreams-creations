import { useEffect, useState } from 'react'
import {
  Table, Button, Modal, Form, Select, InputNumber, Input,
  Tag, Typography, Space, message, Descriptions,
  Divider, Row, Col, Alert, Statistic, Popconfirm, Image
} from 'antd'
import { PlusOutlined, DollarOutlined, EyeOutlined, PrinterOutlined, StopOutlined } from '@ant-design/icons'
import { salesAPI } from '../../api/sales'
import { apiErrorMessage } from '../../api/client'
import { useAuth } from '../../context/AuthContext'
import BillPrint, { printBillDocument } from '../../components/BillPrint'
import { designImageUrl } from '../../utils/designImage'

const { Title, Text } = Typography

const emptyLine = () => ({
  designId: null, sizeId: null, color: null, quantity: 1, unitPrice: 0, productId: null,
})

export default function BillsPage() {
  const { auth } = useAuth()
  const [bills,          setBills]          = useState([])
  const [customers,      setCustomers]      = useState([])
  const [designs,        setDesigns]        = useState([])
  const [sizes,          setSizes]          = useState([])
  const [products,       setProducts]       = useState([])
  const [paymentMethods, setPaymentMethods] = useState([])
  const [loading,        setLoading]        = useState(true)
  const [loadError,      setLoadError]      = useState(null)
  const [billModal,      setBillModal]      = useState(false)
  const [payModal,       setPayModal]       = useState(false)
  const [detailModal,    setDetailModal]    = useState(false)
  const [selectedBill,   setSelectedBill]   = useState(null)
  const [customerPrevBalance, setCustomerPrevBalance] = useState(0)
  const [billForm]       = Form.useForm()
  const [payForm]        = Form.useForm()
  const [items,          setItems]          = useState([emptyLine()])

  const load = () => {
    setLoading(true)
    setLoadError(null)
    Promise.allSettled([
      salesAPI.getBills(),
      salesAPI.getCustomers(),
      salesAPI.getDesigns(),
      salesAPI.getSizes(),
      salesAPI.getProductsWithStock(),
      salesAPI.getPaymentMethods(),
    ]).then(([b, c, d, sz, p, pm]) => {
      if (b.status === 'fulfilled') setBills(b.value.data)
      if (c.status === 'fulfilled') setCustomers(c.value.data)
      if (d.status === 'fulfilled') setDesigns(d.value.data.filter(x => x.status !== 'inactive'))
      if (sz.status === 'fulfilled') setSizes(sz.value.data)
      if (p.status === 'fulfilled') setProducts(p.value.data)
      if (pm.status === 'fulfilled') setPaymentMethods(pm.value.data)
      const failed = [b, c, d, sz, p, pm].filter(r => r.status === 'rejected')
      if (failed.length === 6) {
        const msg = apiErrorMessage(failed[0].reason)
        setLoadError(msg)
        message.error(msg)
      }
    }).finally(() => setLoading(false))
  }

  useEffect(() => { load() }, [])

  const getDesign = (id) => designs.find(d => d.designId === id)

  const sizesForDesign = (designId) => {
    const design = getDesign(designId)
    if (!design) return []
    return sizes.filter(s => s.category?.categoryId === design.category?.categoryId)
  }

  const stockProductsForLine = (designId, sizeId) =>
    products.filter(p =>
      p.designId === designId &&
      p.sizeId === sizeId &&
      p.stockQuantity > 0
    )

  const colorsForLine = (designId, sizeId) => {
    const seen = new Set()
    return stockProductsForLine(designId, sizeId)
      .map(p => p.color)
      .filter(c => {
        if (!c || seen.has(c)) return false
        seen.add(c)
        return true
      })
  }

  const stockForDesign = (designId) =>
    products.filter(p => p.designId === designId && p.stockQuantity > 0)
      .sort((a, b) => (a.sizeValue || '').localeCompare(b.sizeValue || ''))

  const resolveProduct = (designId, sizeId, color) => {
    if (!designId || !sizeId || !color) return null
    return products.find(p =>
      p.designId === designId &&
      p.sizeId === sizeId &&
      p.color === color &&
      p.stockQuantity > 0
    ) || null
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
      color: null,
      productId: null,
      unitPrice: design?.basePrice ? Number(design.basePrice) : 0,
    })
  }

  const onSizeChange = (i, sizeId) => {
    updateLine(i, { sizeId, color: null, productId: null })
  }

  const onColorChange = (i, color, line) => {
    const prod = resolveProduct(line.designId, line.sizeId, color)
    updateLine(i, {
      color,
      productId: prod?.productId || null,
      unitPrice: prod?.sellingPrice ? Number(prod.sellingPrice) : line.unitPrice,
    })
  }

  const viewBill = async (bill) => {
    try {
      const billRes = await salesAPI.getBill(bill.billId)
      setSelectedBill(billRes.data)
      setDetailModal(true)
    } catch {
      message.error('Could not load bill details')
    }
  }

  const handlePrint = async (bill) => {
    try {
      const billRes = await salesAPI.getBill(bill.billId)
      setSelectedBill(billRes.data)
      setTimeout(() => printBillDocument(), 150)
    } catch {
      message.error('Could not load bill for printing')
    }
  }

  const onCustomerSelect = async (customerId) => {
    if (!customerId) {
      setCustomerPrevBalance(0)
      return
    }
    try {
      const res = await salesAPI.getBalance(customerId)
      setCustomerPrevBalance(Math.max(0, Number(res.data.balance || 0)))
    } catch {
      setCustomerPrevBalance(0)
    }
  }

  const openPayModal = (bill) => {
    setSelectedBill(bill)
    setPayModal(true)
  }

  const addItem = () => setItems([...items, emptyLine()])
  const removeItem = (i) => setItems(items.filter((_, idx) => idx !== i))

  const openBillModal = async () => {
    try {
      const res = await salesAPI.getNextBillNumber()
      billForm.setFieldsValue({ billNumber: res.data.billNumber, discount: 0 })
    } catch {
      billForm.setFieldsValue({ discount: 0 })
    }
    setItems([emptyLine()])
    setCustomerPrevBalance(0)
    setBillModal(true)
  }

  const onCreateBill = async (values) => {
    const resolved = items.map(it => ({
      ...it,
      product: resolveProduct(it.designId, it.sizeId, it.color),
    }))

    if (!resolved.every(it => it.designId && it.sizeId && it.color && it.quantity > 0)) {
      message.error('Each line needs design, size, color, and quantity')
      return
    }
    if (!resolved.every(it => it.product?.productId)) {
      message.error('No stock found for one or more design / size / color combinations')
      return
    }
    for (const it of resolved) {
      if (it.quantity > it.product.stockQuantity) {
        message.error(
          `Only ${it.product.stockQuantity} in stock for ${it.product.designName} `
          + `${it.product.sizeValue} ${it.product.color}`
        )
        return
      }
    }
    try {
      let discount = values.discount || 0
      if (!discount) {
        const cust = customers.find(c => c.customerId === values.customerId)
        if (cust?.discountPercent > 0) {
          const subtotal = resolved.reduce((s, it) => s + it.quantity * it.unitPrice, 0)
          discount = Math.round(subtotal * Number(cust.discountPercent) / 100)
        }
      }
      await salesAPI.createBill({
        billNumber: values.billNumber,
        customer:   { customerId: values.customerId },
        createdBy:  { userId: auth?.userId || 1 },
        discount,
        items: resolved.map(it => ({
          product:   { productId: it.product.productId },
          quantity:   it.quantity,
          unitPrice:  it.unitPrice,
        }))
      })
      message.success('Bill created — inventory updated')
      setBillModal(false)
      billForm.resetFields()
      setItems([emptyLine()])
      load()
    } catch (err) {
      message.error(err.response?.data?.message || 'Failed to create bill')
    }
  }

  const onRecordPayment = async (values) => {
    try {
      await salesAPI.recordPayment({
        bill:          { billId: selectedBill.billId },
        paymentMethod: { paymentMethodId: values.paymentMethodId },
        amount:         values.amount,
        notes:          values.notes,
        referenceNo:    values.referenceNo,
      })
      message.success('Payment recorded — bill closed. Remaining balance is on customer account.')
      setPayModal(false)
      payForm.resetFields()
      load()
    } catch (err) {
      message.error(err.response?.data?.message || 'Payment failed')
    }
  }

  const onCancelBill = async (bill) => {
    try {
      await salesAPI.updateBillStatus(bill.billId, 'cancelled')
      message.success('Bill cancelled — stock restored')
      if (detailModal && selectedBill?.billId === bill.billId) {
        setDetailModal(false)
        setSelectedBill(null)
      }
      load()
    } catch (err) {
      message.error(err.response?.data?.message || 'Could not cancel bill')
    }
  }

  const statusColor = s =>
    ({ paid: 'green', partial: 'orange', unpaid: 'red', cancelled: 'default' }[s] || 'default')

  const canCancelBill = (bill) =>
    bill.status !== 'cancelled' && bill.status !== 'paid'

  const columns = [
    { title: 'Bill #',    dataIndex: 'billNumber', key: 'bill' },
    { title: 'Customer',  key: 'customer',
      render: (_, r) => r.customer
        ? `${r.customer.firstName} ${r.customer.lastName || ''}`
        : '-' },
    { title: 'Total',     dataIndex: 'totalAmount',  key: 'total',
      render: v => `Rs. ${Number(v).toLocaleString()}` },
    { title: 'Discount',  dataIndex: 'discount',     key: 'discount',
      render: v => `Rs. ${Number(v).toLocaleString()}` },
    { title: 'Final',     dataIndex: 'finalAmount',  key: 'final',
      render: v => <Text strong>Rs. {Number(v).toLocaleString()}</Text> },
    { title: 'Status',    dataIndex: 'status',       key: 'status',
      render: s => <Tag color={statusColor(s)}>{s?.toUpperCase()}</Tag> },
    { title: 'Actions',   key: 'actions',
      render: (_, r) => (
        <Space>
          <Button size="small" icon={<EyeOutlined />}
            onClick={() => viewBill(r)}>View</Button>
          <Button size="small" icon={<PrinterOutlined />}
            onClick={() => handlePrint(r)}>Print</Button>
          {r.status !== 'paid' && r.status !== 'cancelled' && (
            <Button size="small" type="primary" icon={<DollarOutlined />}
              onClick={() => openPayModal(r)}>Pay</Button>
          )}
          {canCancelBill(r) && (
            <Popconfirm
              title="Cancel this bill?"
              description="Stock for all line items will be restored to inventory."
              onConfirm={() => onCancelBill(r)}
              okText="Yes, cancel"
              okButtonProps={{ danger: true }}
            >
              <Button size="small" danger icon={<StopOutlined />}>Cancel</Button>
            </Popconfirm>
          )}
        </Space>
      )
    },
  ]

  const lineTotal = items.reduce((sum, it) => sum + (it.quantity * it.unitPrice), 0)
  const discountPreview = billForm.getFieldValue('discount') || 0
  const billTotalPreview = Math.max(0, lineTotal - discountPreview)
  const grandTotalPreview = billTotalPreview + customerPrevBalance

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 24 }}>
        <Title level={4} className="page-title" style={{ margin: 0 }}>Bills & Payments</Title>
        <Button type="primary" icon={<PlusOutlined />} onClick={openBillModal}>
          Create Bill
        </Button>
      </div>

      {loadError && (
        <Alert type="error" message={loadError} style={{ marginBottom: 16 }} showIcon />
      )}

      <Alert
        type="info"
        showIcon
        style={{ marginBottom: 16 }}
        message="Select design, size, and color for each line — price defaults from design catalog"
        description="Only combinations with stock in inventory can be billed. Cancelled bills restore stock."
      />

      {customers.length === 0 && !loading && (
        <Alert type="warning" showIcon style={{ marginBottom: 16 }}
          message="No customers found — add a customer first before creating a bill." />
      )}

      <Table dataSource={bills} columns={columns}
        rowKey="billId" loading={loading} scroll={{ x: 800 }} />

      <Modal title="Create Bill" open={billModal} onCancel={() => setBillModal(false)}
        footer={null} width={900}>
        <Form form={billForm} onFinish={onCreateBill} layout="vertical">
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item name="billNumber" label="Bill Number (auto-generated)">
                <Input readOnly />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item name="customerId" label="Customer" rules={[{ required: true, message: 'Select a customer' }]}>
                <Select placeholder="Select customer" onChange={onCustomerSelect}>
                  {customers.map(c => (
                    <Select.Option key={c.customerId} value={c.customerId}>
                      {c.firstName} {c.lastName || ''}
                    </Select.Option>
                  ))}
                </Select>
              </Form.Item>
            </Col>
          </Row>

          <Divider orientation="left">Line Items</Divider>
          <Row gutter={8} style={{ marginBottom: 8, fontWeight: 600 }}>
            <Col span={6}>Design</Col>
            <Col span={4}>Size</Col>
            <Col span={4}>Color</Col>
            <Col span={3}>Qty</Col>
            <Col span={4}>Unit Price</Col>
            <Col span={3}></Col>
          </Row>

          {items.map((item, i) => {
            const colorOptions = colorsForLine(item.designId, item.sizeId)
            const matched = resolveProduct(item.designId, item.sizeId, item.color)
            return (
              <div key={i}>
                <Row gutter={8} style={{ marginBottom: 8 }}>
                  <Col span={6}>
                    <Select placeholder="Design" style={{ width: '100%' }}
                      value={item.designId}
                      showSearch optionFilterProp="label"
                      onChange={v => onDesignChange(i, v)}>
                      {designs.map(d => (
                        <Select.Option key={d.designId} value={d.designId}
                          label={`${d.designCode} ${d.name}`}>
                          {d.designCode} — {d.name}
                          {d.basePrice != null ? ` (Rs.${d.basePrice})` : ''}
                        </Select.Option>
                      ))}
                    </Select>
                  </Col>
                  <Col span={4}>
                    <Select placeholder="Size" style={{ width: '100%' }}
                      value={item.sizeId}
                      disabled={!item.designId}
                      onChange={v => onSizeChange(i, v)}>
                      {sizesForDesign(item.designId).map(s => (
                        <Select.Option key={s.sizeId} value={s.sizeId}>{s.sizeValue}</Select.Option>
                      ))}
                    </Select>
                  </Col>
                  <Col span={4}>
                    <Select placeholder="Color" style={{ width: '100%' }}
                      value={item.color}
                      disabled={!item.sizeId}
                      onChange={v => onColorChange(i, v, item)}>
                      {colorOptions.map(c => (
                        <Select.Option key={c} value={c}>{c}</Select.Option>
                      ))}
                    </Select>
                  </Col>
                  <Col span={3}>
                    <InputNumber min={1} style={{ width: '100%' }}
                      max={matched?.stockQuantity || undefined}
                      value={item.quantity}
                      disabled={!matched}
                      onChange={v => updateLine(i, { quantity: v })} />
                  </Col>
                  <Col span={4}>
                    <InputNumber min={0} style={{ width: '100%' }}
                      value={item.unitPrice}
                      onChange={v => updateLine(i, { unitPrice: v })} />
                  </Col>
                  <Col span={3}>
                    {items.length > 1 && (
                      <Button danger size="small" onClick={() => removeItem(i)}>Remove</Button>
                    )}
                  </Col>
                </Row>
                {item.designId && (
                  <div style={{ marginBottom: 12 }}>
                    <Space align="start" style={{ marginBottom: 8 }}>
                      {designImageUrl(getDesign(item.designId)) && (
                        <Image
                          src={designImageUrl(getDesign(item.designId))}
                          width={56}
                          height={56}
                          style={{ objectFit: 'cover', borderRadius: 6 }}
                          alt={getDesign(item.designId)?.name}
                        />
                      )}
                      <div style={{ flex: 1 }}>
                    {stockForDesign(item.designId).length > 0 ? (
                      <Alert
                        type="success"
                        showIcon
                        style={{ marginBottom: 8 }}
                        message={`${getDesign(item.designId)?.name || 'Design'} — stock available`}
                        description={
                          <Space size={[8, 4]} wrap style={{ marginTop: 4 }}>
                            {stockForDesign(item.designId).map(p => (
                              <Tag key={`${p.sizeId}-${p.color}`} color="green">
                                {p.sizeValue} / {p.color}: <strong>{p.stockQuantity}</strong>
                              </Tag>
                            ))}
                          </Space>
                        }
                      />
                    ) : (
                      <Alert type="error" showIcon style={{ marginBottom: 8 }}
                        message="No stock available for this design"
                        description="Complete production or choose another design." />
                    )}
                      </div>
                    </Space>
                  </div>
                )}
                {item.designId && item.sizeId && item.color && !matched && (
                  <Alert type="warning" showIcon style={{ marginBottom: 8 }}
                    message="No stock for this design / size / color" />
                )}
                {matched && (
                  <Text type="secondary" style={{ fontSize: 12, display: 'block', marginBottom: 8 }}>
                    In stock: {matched.stockQuantity}
                  </Text>
                )}
              </div>
            )
          })}

          <Button type="dashed" onClick={addItem} block style={{ marginBottom: 16 }}>
            + Add Line Item
          </Button>

          <Row gutter={16}>
            <Col span={12}>
              <Form.Item name="discount" label="Discount (Rs.)">
                <InputNumber min={0} style={{ width: '100%' }} />
              </Form.Item>
            </Col>
            <Col span={12}>
              <div style={{ paddingTop: 30 }}>
                <div>Subtotal: <Text strong>Rs. {lineTotal.toLocaleString()}</Text></div>
                <div>Bill Total: <Text strong>Rs. {billTotalPreview.toLocaleString()}</Text></div>
                {customerPrevBalance > 0 && (
                  <div>Previous Balance: <Text type="warning">Rs. {customerPrevBalance.toLocaleString()}</Text></div>
                )}
                <div style={{ marginTop: 4 }}>
                  Grand Total Due: <Text strong type="danger">Rs. {grandTotalPreview.toLocaleString()}</Text>
                </div>
              </div>
            </Col>
          </Row>

          <Form.Item>
            <Space>
              <Button type="primary" htmlType="submit">Create Bill</Button>
              <Button onClick={() => setBillModal(false)}>Cancel</Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>

      <Modal title={`Record Payment — ${selectedBill?.billNumber}`}
        open={payModal} onCancel={() => setPayModal(false)} footer={null}>
        {selectedBill && (
          <>
            <Alert type="info" showIcon style={{ marginBottom: 16 }}
              message="Partial payment closes this bill"
              description="Any amount received closes the bill. Remaining due stays on the customer account. Full payment history is in Customer Records." />
            <Row gutter={16} style={{ marginBottom: 16 }}>
              <Col span={12}>
                <Statistic title="Bill Total" prefix="Rs."
                  value={Number(selectedBill.finalAmount).toLocaleString()} />
              </Col>
              <Col span={12}>
                <Statistic title="Grand Total Due" prefix="Rs."
                  value={Number(selectedBill.grandTotal || selectedBill.finalAmount).toLocaleString()} />
              </Col>
            </Row>
          </>
        )}
        <Form form={payForm} onFinish={onRecordPayment} layout="vertical">
          <Form.Item name="paymentMethodId" label="Payment Method" rules={[{ required: true }]}>
            <Select placeholder="Select method">
              {paymentMethods.map(pm => (
                <Select.Option key={pm.paymentMethodId} value={pm.paymentMethodId}>
                  {pm.methodName}
                </Select.Option>
              ))}
            </Select>
          </Form.Item>
          <Form.Item name="amount" label="Amount Received (Rs.)" rules={[{ required: true }]}>
            <InputNumber min={1} style={{ width: '100%' }}
              max={selectedBill ? Number(selectedBill.grandTotal || selectedBill.finalAmount) : undefined} />
          </Form.Item>
          <Form.Item name="referenceNo" label="Reference / Transaction No.">
            <Input placeholder="Cheque no., bank ref, etc." />
          </Form.Item>
          <Form.Item name="notes" label="Notes">
            <Input.TextArea rows={2} placeholder="Optional payment notes" />
          </Form.Item>
          <Form.Item>
            <Space>
              <Button type="primary" htmlType="submit">Record Payment</Button>
              <Button onClick={() => setPayModal(false)}>Cancel</Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>

      <Modal title={`Bill: ${selectedBill?.billNumber}`}
        open={detailModal} onCancel={() => setDetailModal(false)}
        footer={
          selectedBill ? (
            <Space>
              {canCancelBill(selectedBill) && (
                <Popconfirm
                  title="Cancel this bill?"
                  description="Stock for all line items will be restored."
                  onConfirm={() => onCancelBill(selectedBill)}
                  okText="Yes, cancel"
                  okButtonProps={{ danger: true }}
                >
                  <Button danger icon={<StopOutlined />}>Cancel Bill</Button>
                </Popconfirm>
              )}
              <Button icon={<PrinterOutlined />} type="primary"
                onClick={() => printBillDocument()}>
                Print Bill
              </Button>
              <Button onClick={() => setDetailModal(false)}>Close</Button>
            </Space>
          ) : null
        }
        width={700}>
        {selectedBill && (
          <>
            <Descriptions bordered size="small" style={{ marginBottom: 16 }}>
              <Descriptions.Item label="Customer" span={3}>
                {selectedBill.customer?.firstName} {selectedBill.customer?.lastName}
              </Descriptions.Item>
              <Descriptions.Item label="Phone">
                {selectedBill.customer?.phone || '—'}
              </Descriptions.Item>
              <Descriptions.Item label="City">
                {selectedBill.customer?.city || '—'}
              </Descriptions.Item>
              <Descriptions.Item label="Status">
                <Tag color={statusColor(selectedBill.status)}>
                  {selectedBill.status?.toUpperCase()}
                </Tag>
              </Descriptions.Item>
            </Descriptions>

            <Title level={5}>Line Items</Title>
            <Table
              dataSource={selectedBill.items || []}
              rowKey="billItemId"
              size="small"
              pagination={false}
              columns={[
                { title: 'Design', key: 'design',
                  render: (_, r) => r.product?.suit?.design?.name || r.product?.suit?.design?.designCode || '—' },
                { title: 'Size', key: 'size',
                  render: (_, r) => r.product?.suit?.size?.sizeValue || '—' },
                { title: 'Color', key: 'color',
                  render: (_, r) => r.product?.suit?.color || '—' },
                { title: 'Qty', dataIndex: 'quantity', key: 'qty' },
                { title: 'Price', dataIndex: 'unitPrice', key: 'price',
                  render: v => `Rs. ${Number(v).toLocaleString()}` },
                { title: 'Total', dataIndex: 'totalPrice', key: 'total',
                  render: v => `Rs. ${Number(v).toLocaleString()}` },
              ]}
            />

            <div style={{ marginTop: 16, marginBottom: 16, maxWidth: 360, marginLeft: 'auto' }}>
              <Row justify="space-between" style={{ marginBottom: 6 }}>
                <Col><Text>Subtotal</Text></Col>
                <Col>Rs. {Number(selectedBill.totalAmount).toLocaleString()}</Col>
              </Row>
              <Row justify="space-between" style={{ marginBottom: 6 }}>
                <Col><Text>Discount</Text></Col>
                <Col>Rs. {Number(selectedBill.discount).toLocaleString()}</Col>
              </Row>
              <Row justify="space-between" style={{ marginBottom: 6 }}>
                <Col><Text strong>Bill Total</Text></Col>
                <Col><Text strong>Rs. {Number(selectedBill.finalAmount).toLocaleString()}</Text></Col>
              </Row>
              {Number(selectedBill.previousBalance) > 0 && (
                <Row justify="space-between" style={{ marginBottom: 6 }}>
                  <Col><Text>Previous Balance</Text></Col>
                  <Col>Rs. {Number(selectedBill.previousBalance).toLocaleString()}</Col>
                </Row>
              )}
              <Row justify="space-between" style={{ borderTop: '1px solid #ddd', paddingTop: 8 }}>
                <Col><Text strong>Grand Total Due</Text></Col>
                <Col>
                  <Text strong type="danger">
                    Rs. {Number(selectedBill.grandTotal || selectedBill.finalAmount).toLocaleString()}
                  </Text>
                </Col>
              </Row>
            </div>
            <Alert type="info" showIcon
              message="Payment details are recorded under Customer Records, not on the bill." />
          </>
        )}
      </Modal>

      <BillPrint bill={selectedBill} />
    </div>
  )
}
