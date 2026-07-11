import { useEffect, useState } from 'react'
import {
  Table, Button, Modal, Form, Select, InputNumber, Input,
  Tag, Typography, Space, DatePicker, message, Tabs, Alert, Row, Col, Divider
} from 'antd'
import { PlusOutlined, RollbackOutlined, DeleteOutlined } from '@ant-design/icons'
import { productionAPI } from '../../api/production'
import { apiErrorMessage } from '../../api/client'
import dayjs from 'dayjs'

const { Title, Text } = Typography

const FINAL_STAGE_NAMES = ['Cutting & Stitching', 'Cutting', 'Stitching']

export default function DispatchPage() {
  const [assignments, setAssignments] = useState([])
  const [batches, setBatches] = useState([])
  const [modules, setModules] = useState([])
  const [supervisors, setSupervisors] = useState([])
  const [overdue, setOverdue] = useState([])
  const [designingTypes, setDesigningTypes] = useState([])
  const [fillingTypes, setFillingTypes] = useState([])
  const [sizes, setSizes] = useState([])
  const [stagePath, setStagePath] = useState([])
  const [skuLines, setSkuLines] = useState([{ sizeId: null, color: '', quantity: 1 }])
  const [loading, setLoading] = useState(true)
  const [loadError, setLoadError] = useState(null)
  const [dispatchModal, setDispatchModal] = useState(false)
  const [returnModal, setReturnModal] = useState(false)
  const [selectedAssignment, setSelectedAssignment] = useState(null)
  const [dispatchForm] = Form.useForm()
  const [returnForm] = Form.useForm()

  const load = async () => {
    setLoading(true)
    setLoadError(null)
    const errors = []
    try {
      const results = await Promise.allSettled([
        productionAPI.getAssignments(),
        productionAPI.getBatches(),
        productionAPI.getModules(),
        productionAPI.getSupervisors(),
        productionAPI.getOverdue(),
        productionAPI.getDesigningWorkTypes(),
        productionAPI.getFillingWorkTypes(),
        productionAPI.getSizes(),
      ])
      const [a, b, m, s, o, dt, ft, sz] = results
      if (a.status === 'fulfilled') setAssignments(a.value.data)
      else errors.push('assignments')
      if (b.status === 'fulfilled') setBatches(b.value.data)
      else errors.push('batches')
      if (m.status === 'fulfilled') setModules(m.value.data)
      else errors.push('modules')
      if (s.status === 'fulfilled') setSupervisors(s.value.data)
      else errors.push('supervisors')
      if (o.status === 'fulfilled') setOverdue(o.value.data)
      if (dt.status === 'fulfilled') setDesigningTypes(dt.value.data)
      if (ft.status === 'fulfilled') setFillingTypes(ft.value.data)
      if (sz.status === 'fulfilled') setSizes(sz.value.data)
      if (errors.length >= 5) {
        const msg = apiErrorMessage(results.find(r => r.status === 'rejected')?.reason)
        setLoadError(msg)
        message.error(msg)
      }
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { load() }, [])

  const sortedModules = [...modules].sort((a, b) =>
    (a.stage?.stageOrder || 0) - (b.stage?.stageOrder || 0)
  )

  const selectedBatch = (batchId) => batches.find(b => b.batchId === batchId)
  const selectedModule = (moduleId) => modules.find(m => m.moduleId === moduleId)

  const onBatchChange = async (batchId) => {
    dispatchForm.setFieldValue('moduleId', undefined)
    setStagePath([])
    const batch = selectedBatch(batchId)
    const designId = batch?.suit?.design?.designId
    if (!designId) return
    try {
      const res = await productionAPI.getRequiredStages(designId)
      setStagePath(res.data)
    } catch {
      setStagePath([])
    }
  }

  const isFinalStageModule = (module) => {
    if (!module) return false
    if (FINAL_STAGE_NAMES.includes(module.stage?.stageName)) return true
    if (!stagePath.length) return false
    const last = stagePath[stagePath.length - 1]
    return module.stage?.stageId === last.stageId
  }

  const filteredSizes = (batchId) => {
    const batch = selectedBatch(batchId)
    const categoryId = batch?.suit?.design?.category?.categoryId
    return sizes.filter(s => !categoryId || s.category?.categoryId === categoryId)
  }

  const skuTotal = skuLines.reduce((sum, l) => sum + (Number(l.quantity) || 0), 0)

  const onDispatch = async (values) => {
    const module = selectedModule(values.moduleId)
    const isDesigning = module?.stage?.stageName === 'Designing'
    const isFilling = module?.stage?.stageName === 'Filling'
    const isFinal = isFinalStageModule(module)

    const payload = {
      batchId: values.batchId,
      moduleId: values.moduleId,
      supervisorId: values.supervisorId,
      dueDate: values.dueDate.format('YYYY-MM-DDTHH:mm:ss'),
    }

    if (isDesigning) payload.designingWorkTypeId = values.designingWorkTypeId
    if (isFilling) payload.fillingWorkTypeId = values.fillingWorkTypeId

    if (isFinal) {
      if (!skuLines.every(l => l.sizeId && l.color?.trim() && l.quantity > 0)) {
        message.error('Each size/color line needs size, color, and quantity')
        return
      }
      payload.skuLines = skuLines.map(l => ({
        sizeId: l.sizeId,
        color: l.color.trim(),
        quantity: l.quantity,
      }))
    } else {
      payload.quantitySent = values.quantitySent
    }

    try {
      await productionAPI.dispatch(payload)
      message.success('Dispatched successfully')
      setDispatchModal(false)
      dispatchForm.resetFields()
      setSkuLines([{ sizeId: null, color: '', quantity: 1 }])
      setStagePath([])
      load()
    } catch (err) {
      message.error(err.response?.data?.message || 'Dispatch failed')
    }
  }

  const onReturn = async (values) => {
    try {
      const hasLines = selectedAssignment?.skuLines?.length > 0
      const body = hasLines
        ? {
            skuLines: selectedAssignment.skuLines.map((line, i) => ({
              lineId: line.lineId,
              returnedOk: values[`ok_${i}`] ?? 0,
              damaged: values[`damaged_${i}`] ?? 0,
              missing: values[`missing_${i}`] ?? 0,
            })),
          }
        : {
            returnedOk: values.returnedOk,
            damaged: values.damaged,
            missing: values.missing,
          }
      await productionAPI.returnAssignment(selectedAssignment.assignmentId, body)
      message.success('Return recorded successfully')
      setReturnModal(false)
      returnForm.resetFields()
      setSelectedAssignment(null)
      load()
    } catch (err) {
      message.error(err.response?.data?.message || 'Return failed')
    }
  }

  const openReturn = (record) => {
    setSelectedAssignment(record)
    returnForm.resetFields()
    setReturnModal(true)
  }

  const statusColor = (s) => ({
    sent: 'blue', in_progress: 'processing',
    returned: 'green', overdue: 'red',
  }[s] || 'default')

  const columns = [
    { title: 'Dispatch #', dataIndex: 'assignmentId', key: 'id', width: 90 },
    { title: 'Batch', key: 'batch',
      render: (_, r) => r.batch?.batchNumber || `#${r.batch?.batchId}` },
    { title: 'Stage', key: 'stage',
      render: (_, r) => r.module?.stage?.stageName || '-' },
    { title: 'Work Type', key: 'workType',
      render: (_, r) => r.designingWorkType?.typeName
        || r.fillingWorkType?.typeName
        || (r.skuLines?.length
          ? `${r.skuLines.length} size/color line(s)`
          : '—') },
    { title: 'Module', key: 'module',
      render: (_, r) => r.module?.moduleName || '-' },
    { title: 'Supervisor', key: 'supervisor',
      render: (_, r) => r.supervisor
        ? `${r.supervisor.firstName} ${r.supervisor.lastName || ''}`
        : '-' },
    { title: 'Sent', dataIndex: 'quantitySent', key: 'sent' },
    { title: 'OK', dataIndex: 'quantityReturnedOk', key: 'ok' },
    { title: 'Due', dataIndex: 'dueDate', key: 'due',
      render: d => d ? dayjs(d).format('DD MMM YYYY') : '-' },
    { title: 'Status', dataIndex: 'status', key: 'status',
      render: s => <Tag color={statusColor(s)}>{s?.toUpperCase()}</Tag> },
    { title: 'Action', key: 'action',
      render: (_, r) => r.status !== 'returned' ? (
        <Button icon={<RollbackOutlined />} size="small" type="primary"
          onClick={() => openReturn(r)}>
          Record Return
        </Button>
      ) : <Text type="secondary">Completed</Text> },
  ]

  const tabItems = [
    {
      key: 'all',
      label: `All Dispatches (${assignments.length})`,
      children: (
        <Table dataSource={assignments} columns={columns}
          rowKey="assignmentId" loading={loading} scroll={{ x: 1200 }} />
      )
    },
    {
      key: 'overdue',
      label: <span style={{ color: overdue.length > 0 ? '#cf1322' : undefined }}>
        Overdue ({overdue.length})
      </span>,
      children: (
        <Table dataSource={overdue} columns={columns}
          rowKey="assignmentId" loading={loading} scroll={{ x: 1200 }} />
      )
    },
  ]

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 24 }}>
        <Title level={4} className="page-title" style={{ margin: 0 }}>
          Dispatch Management
        </Title>
        <Button type="primary" icon={<PlusOutlined />}
          onClick={() => setDispatchModal(true)}>
          Dispatch to Stage
        </Button>
      </div>

      {loadError && (
        <Alert type="error" message={loadError} style={{ marginBottom: 16 }} showIcon />
      )}

      <Alert
        type="info"
        showIcon
        style={{ marginBottom: 16 }}
        message="Stage-specific dispatch details"
        description="Designing and Filling require a work type. Cutting & Stitching requires size/color quantity breakdown from pieces received at the previous stage."
      />

      <Tabs items={tabItems} />

      <Modal title="Dispatch to Production Stage" open={dispatchModal}
        onCancel={() => setDispatchModal(false)} footer={null} width={640}>
        <Form form={dispatchForm} onFinish={onDispatch} layout="vertical">
          <Form.Item name="batchId" label="Production Batch" rules={[{ required: true }]}>
            <Select placeholder="Select batch" onChange={onBatchChange}>
              {batches.filter(b => b.status !== 'completed').map(b => (
                <Select.Option key={b.batchId} value={b.batchId}>
                  {b.batchNumber} — {b.suit?.design?.name || 'Design'} ({b.status})
                </Select.Option>
              ))}
            </Select>
          </Form.Item>

          <Form.Item name="moduleId" label="Production Stage / Module" rules={[{ required: true }]}>
            <Select placeholder="Select stage">
              {sortedModules.map(m => (
                <Select.Option key={m.moduleId} value={m.moduleId}>
                  {m.stage?.stageOrder}. {m.stage?.stageName} — {m.moduleName}
                </Select.Option>
              ))}
            </Select>
          </Form.Item>

          <Form.Item noStyle shouldUpdate>
            {({ getFieldValue }) => {
              const module = selectedModule(getFieldValue('moduleId'))
              const isDesigning = module?.stage?.stageName === 'Designing'
              const isFilling = module?.stage?.stageName === 'Filling'
              const isFinal = isFinalStageModule(module)
              const batchId = getFieldValue('batchId')

              return (
                <>
                  {isDesigning && (
                    <Form.Item name="designingWorkTypeId" label="Designing Type"
                      rules={[{ required: true, message: 'Select designing type' }]}>
                      <Select placeholder="Select type of designing work">
                        {designingTypes.map(t => (
                          <Select.Option key={t.designingWorkTypeId} value={t.designingWorkTypeId}>
                            {t.typeName}
                          </Select.Option>
                        ))}
                      </Select>
                    </Form.Item>
                  )}

                  {isFilling && (
                    <Form.Item name="fillingWorkTypeId" label="Filling Type"
                      rules={[{ required: true, message: 'Select filling type' }]}>
                      <Select placeholder="Select type of filling work">
                        {fillingTypes.map(t => (
                          <Select.Option key={t.fillingWorkTypeId} value={t.fillingWorkTypeId}>
                            {t.typeName}
                          </Select.Option>
                        ))}
                      </Select>
                    </Form.Item>
                  )}

                  {isFinal && (
                    <>
                      <Alert type="warning" showIcon style={{ marginBottom: 12 }}
                        message="Assign size and color before Cutting & Stitching"
                        description="Split the quantity received from the previous stage into size/color lines. Total must not exceed available pieces." />
                      <Divider orientation="left">Size / Color Breakdown</Divider>
                      {skuLines.map((line, i) => (
                        <Row gutter={8} key={i} style={{ marginBottom: 8 }}>
                          <Col span={8}>
                            <Select placeholder="Size" style={{ width: '100%' }}
                              value={line.sizeId}
                              onChange={v => {
                                const next = [...skuLines]
                                next[i] = { ...next[i], sizeId: v }
                                setSkuLines(next)
                              }}>
                              {filteredSizes(batchId).map(s => (
                                <Select.Option key={s.sizeId} value={s.sizeId}>
                                  {s.sizeValue}
                                </Select.Option>
                              ))}
                            </Select>
                          </Col>
                          <Col span={8}>
                            <Input placeholder="Color"
                              value={line.color}
                              onChange={e => {
                                const next = [...skuLines]
                                next[i] = { ...next[i], color: e.target.value }
                                setSkuLines(next)
                              }} />
                          </Col>
                          <Col span={5}>
                            <InputNumber min={1} style={{ width: '100%' }}
                              value={line.quantity}
                              onChange={v => {
                                const next = [...skuLines]
                                next[i] = { ...next[i], quantity: v }
                                setSkuLines(next)
                              }} />
                          </Col>
                          <Col span={3}>
                            {skuLines.length > 1 && (
                              <Button danger size="small" icon={<DeleteOutlined />}
                                onClick={() => setSkuLines(skuLines.filter((_, idx) => idx !== i))} />
                            )}
                          </Col>
                        </Row>
                      ))}
                      <Button type="dashed" block style={{ marginBottom: 12 }}
                        onClick={() => setSkuLines([...skuLines, { sizeId: null, color: '', quantity: 1 }])}>
                        + Add size/color line
                      </Button>
                      <Text strong>Total to dispatch: {skuTotal}</Text>
                    </>
                  )}

                  {!isFinal && (
                    <Form.Item name="quantitySent" label="Quantity to Dispatch"
                      rules={[{ required: true, message: 'Enter quantity' }]}>
                      <InputNumber min={1} style={{ width: '100%' }} />
                    </Form.Item>
                  )}
                </>
              )
            }}
          </Form.Item>

          <Form.Item name="supervisorId" label="Supervisor" rules={[{ required: true }]}>
            <Select placeholder="Select supervisor">
              {supervisors.map(s => (
                <Select.Option key={s.supervisorId} value={s.supervisorId}>
                  {s.firstName} {s.lastName || ''}
                </Select.Option>
              ))}
            </Select>
          </Form.Item>
          <Form.Item name="dueDate" label="Due Date" rules={[{ required: true }]}>
            <DatePicker showTime style={{ width: '100%' }}
              disabledDate={d => d && d < dayjs().startOf('day')} />
          </Form.Item>
          <Form.Item>
            <Space>
              <Button type="primary" htmlType="submit">Dispatch</Button>
              <Button onClick={() => setDispatchModal(false)}>Cancel</Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>

      <Modal title="Record Return from Module" open={returnModal}
        onCancel={() => setReturnModal(false)} footer={null} width={560}>
        {selectedAssignment?.skuLines?.length > 0 ? (
          <>
            <Alert type="info" showIcon style={{ marginBottom: 16 }}
              message="Return per size/color line — each line must reconcile to its sent quantity" />
            <Form form={returnForm} onFinish={onReturn} layout="vertical">
              {selectedAssignment.skuLines.map((line, i) => (
                <div key={line.lineId || i} style={{ marginBottom: 16, padding: 12, background: '#fafafa' }}>
                  <Text strong>
                    {line.size?.sizeValue || 'Size'} / {line.color} — sent {line.quantitySent}
                  </Text>
                  <Row gutter={8} style={{ marginTop: 8 }}>
                    <Col span={8}>
                      <Form.Item name={`ok_${i}`} label="OK" rules={[{ required: true }]} initialValue={0}>
                        <InputNumber min={0} style={{ width: '100%' }} />
                      </Form.Item>
                    </Col>
                    <Col span={8}>
                      <Form.Item name={`damaged_${i}`} label="Damaged" rules={[{ required: true }]} initialValue={0}>
                        <InputNumber min={0} style={{ width: '100%' }} />
                      </Form.Item>
                    </Col>
                    <Col span={8}>
                      <Form.Item name={`missing_${i}`} label="Missing" rules={[{ required: true }]} initialValue={0}>
                        <InputNumber min={0} style={{ width: '100%' }} />
                      </Form.Item>
                    </Col>
                  </Row>
                </div>
              ))}
              <Button type="primary" htmlType="submit">Confirm Return</Button>
            </Form>
          </>
        ) : (
          <Form form={returnForm} onFinish={onReturn} layout="vertical">
            {selectedAssignment && (
              <Alert type="info" showIcon style={{ marginBottom: 16 }}
                message={`Quantity sent: ${selectedAssignment.quantitySent}`} />
            )}
            <Form.Item name="returnedOk" label="Pieces Returned OK" rules={[{ required: true }]} initialValue={0}>
              <InputNumber min={0} style={{ width: '100%' }} />
            </Form.Item>
            <Form.Item name="damaged" label="Damaged Pieces" rules={[{ required: true }]} initialValue={0}>
              <InputNumber min={0} style={{ width: '100%' }} />
            </Form.Item>
            <Form.Item name="missing" label="Missing Pieces" rules={[{ required: true }]} initialValue={0}>
              <InputNumber min={0} style={{ width: '100%' }} />
            </Form.Item>
            <Button type="primary" htmlType="submit">Confirm Return</Button>
          </Form>
        )}
      </Modal>
    </div>
  )
}
