import { useEffect, useState } from 'react'
import {
  Table, Button, Modal, Form, Select, InputNumber,
  Tag, Typography, Space, DatePicker, message, Drawer, Descriptions, Progress, Card, Alert, Popconfirm
} from 'antd'
import { PlusOutlined, EyeOutlined, EditOutlined, StopOutlined } from '@ant-design/icons'
import { productionAPI } from '../../api/production'
import { apiErrorMessage } from '../../api/client'
import dayjs from 'dayjs'

const { Title } = Typography

export default function BatchesPage() {
  const [batches,    setBatches]    = useState([])
  const [designs,    setDesigns]    = useState([])
  const [categories, setCategories] = useState([])
  const [supervisors,setSupervisors]= useState([])
  const [designingTypes, setDesigningTypes] = useState([])
  const [stagePath,  setStagePath]  = useState([])
  const [loading,    setLoading]    = useState(true)
  const [modalOpen,  setModalOpen]  = useState(false)
  const [editOpen,   setEditOpen]   = useState(false)
  const [editingBatch, setEditingBatch] = useState(null)
  const [flowDrawer, setFlowDrawer] = useState(false)
  const [flowData,   setFlowData]   = useState(null)
  const [form]                      = Form.useForm()
  const [editForm]                  = Form.useForm()

  const load = () => {
    setLoading(true)
    Promise.all([
      productionAPI.getBatches(),
      productionAPI.getDesigns(),
      productionAPI.getCategories(),
      productionAPI.getSupervisors(),
      productionAPI.getDesigningWorkTypes(),
    ]).then(([b, d, c, sup, dwt]) => {
      setBatches(b.data)
      setDesigns(d.data)
      setCategories(c.data)
      setSupervisors(sup.data)
      setDesigningTypes(dwt.data)
    }).catch((err) => message.error(apiErrorMessage(err)))
      .finally(() => setLoading(false))
  }

  useEffect(() => { load() }, [])

  const viewFlow = async (batchId) => {
    try {
      const res = await productionAPI.getBatchFlow(batchId)
      setFlowData(res.data)
      setFlowDrawer(true)
    } catch {
      message.error('Could not load flow data')
    }
  }

  const selectedDesign = (designId) => designs.find(d => d.designId === designId)

  const onDesignChange = async (designId) => {
    if (!designId) { setStagePath([]); return }
    try {
      const res = await productionAPI.getRequiredStages(designId)
      setStagePath(res.data)
    } catch { setStagePath([]) }
  }

  const onFinish = async (values) => {
    try {
      const res = await productionAPI.startProductionOrder({
        designId: values.designId,
        categoryId: values.categoryId,
        quantity: values.quantity,
        supervisorId: values.supervisorId,
        designingWorkTypeId: values.designingWorkTypeId,
        expectedCompletionDate: values.endDate?.format('YYYY-MM-DD'),
        dueDate: values.endDate?.format('YYYY-MM-DDTHH:mm:ss'),
      })
      message.success(res.data.message || 'Production order created')
      setModalOpen(false)
      form.resetFields()
      load()
    } catch (err) {
      message.error(err.response?.data?.message || 'Failed to create production order')
    }
  }

  const openEditBatch = (batch) => {
    setEditingBatch(batch)
    editForm.setFieldsValue({
      totalSuitPlanned: batch.totalSuitPlanned,
      expectedCompletionDate: batch.expectedCompletionDate ? dayjs(batch.expectedCompletionDate) : null,
    })
    setEditOpen(true)
  }

  const onEditBatch = async (values) => {
    try {
      await productionAPI.updateBatch(editingBatch.batchId, {
        totalSuitPlanned: values.totalSuitPlanned,
        expectedCompletionDate: values.expectedCompletionDate?.format('YYYY-MM-DD'),
      })
      message.success('Batch updated')
      setEditOpen(false)
      setEditingBatch(null)
      load()
    } catch (err) {
      message.error(err.response?.data?.message || 'Failed to update batch')
    }
  }

  const cancelBatch = async (batchId) => {
    try {
      await productionAPI.cancelBatch(batchId)
      message.success('Batch cancelled')
      load()
    } catch (err) {
      message.error(err.response?.data?.message || 'Failed to cancel batch')
    }
  }

  const canModifyBatch = (batch) =>
    batch.status !== 'completed' && batch.status !== 'cancelled'

  const columns = [
    { title: 'Batch #',   dataIndex: 'batchNumber', key: 'batch' },
    { title: 'Suit',      key: 'suit',
      render: (_, r) => r.suit
        ? `${r.suit.design?.name || 'Design'} — ${r.suit.size?.sizeValue || 'Size TBD'} — ${r.suit.color || ''}`
        : '-' },
    { title: 'Planned',   dataIndex: 'totalSuitPlanned',  key: 'planned' },
    { title: 'Produced',  dataIndex: 'totalSuitProduced', key: 'produced',
      render: (v, r) => (
        <Progress
          percent={r.totalSuitPlanned ? Math.round((v / r.totalSuitPlanned) * 100) : 0}
          size="small"
          status={r.status === 'completed' ? 'success' : 'active'}
        />
      )
    },
    { title: 'Due',       dataIndex: 'expectedCompletionDate', key: 'due',
      render: (d) => d ? dayjs(d).format('DD MMM YYYY') : '-' },
    { title: 'Status',    dataIndex: 'status', key: 'status',
      render: (s) => (
        <Tag color={s === 'completed' ? 'green' : s === 'in_progress' ? 'blue' : s === 'cancelled' ? 'red' : 'default'}>
          {s?.replace('_', ' ').toUpperCase()}
        </Tag>
      )
    },
    {
      title: 'Actions', key: 'actions',
      render: (_, r) => (
        <Space>
          <Button icon={<EyeOutlined />} size="small"
            onClick={() => viewFlow(r.batchId)}>Flow</Button>
          {canModifyBatch(r) && (
            <>
              <Button icon={<EditOutlined />} size="small"
                onClick={() => openEditBatch(r)}>Edit</Button>
              <Popconfirm
                title="Cancel this batch?"
                description="Only batches without active dispatches can be cancelled."
                onConfirm={() => cancelBatch(r.batchId)}
                okText="Cancel batch"
                okButtonProps={{ danger: true }}
              >
                <Button danger icon={<StopOutlined />} size="small">Cancel</Button>
              </Popconfirm>
            </>
          )}
        </Space>
      )
    },
  ]

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 24 }}>
        <Title level={4} className="page-title" style={{ margin: 0 }}>Production Batches</Title>
        <Button type="primary" icon={<PlusOutlined />} onClick={() => setModalOpen(true)}>
          New Production Order
        </Button>
      </div>

      <Alert
        type="info"
        showIcon
        style={{ marginBottom: 16 }}
        message="Production flow: Designing → (optional Filling) → Cutting & Stitching"
        description="Create a batch — suits go to Designing with a work type. Size and color are assigned when dispatching to Cutting & Stitching."
      />

      <Table dataSource={batches} columns={columns} rowKey="batchId" loading={loading} />

      <Modal title="New Production Order (Design Phase)" open={modalOpen}
        onCancel={() => setModalOpen(false)} footer={null} width={560}>
        <Form form={form} onFinish={onFinish} layout="vertical">
          <Form.Item name="categoryId" label="Category" rules={[{ required: true, message: 'Select category' }]}>
            <Select placeholder="Women, Kids, etc." onChange={() => form.setFieldValue('sizeId', undefined)}>
              {categories.map(c => (
                <Select.Option key={c.categoryId} value={c.categoryId}>{c.categoryName}</Select.Option>
              ))}
            </Select>
          </Form.Item>
          <Form.Item name="designId" label="Design Number / Code" rules={[{ required: true, message: 'Select design' }]}>
            <Select placeholder="Select design" showSearch optionFilterProp="label"
              onChange={onDesignChange}>
              {designs.map(d => (
                <Select.Option key={d.designId} value={d.designId} label={d.designCode}>
                  {d.designCode} — {d.name}
                </Select.Option>
              ))}
            </Select>
          </Form.Item>
          <Form.Item noStyle shouldUpdate={(prev, cur) => prev.designId !== cur.designId}>
            {({ getFieldValue }) => {
              const d = selectedDesign(getFieldValue('designId'))
              return d ? (
                <Alert type="info" showIcon style={{ marginBottom: 16 }}
                  message={`Embroidery: ${d.embroideryType?.name || 'None'}`}
                  description={stagePath.length
                    ? `Stage path: ${stagePath.map(s => s.stageName).join(' → ')}`
                    : 'Stage path will load when design is selected'}
                />
              ) : null
            }}
          </Form.Item>
          <Form.Item name="designingWorkTypeId" label="Designing Type"
            rules={[{ required: true, message: 'Select designing type' }]}>
            <Select placeholder="Type of designing work">
              {designingTypes.map(t => (
                <Select.Option key={t.designingWorkTypeId} value={t.designingWorkTypeId}>
                  {t.typeName}
                </Select.Option>
              ))}
            </Select>
          </Form.Item>
          <Alert type="info" showIcon style={{ marginBottom: 16 }}
            message="Size and color are assigned later"
            description="When forwarding to Cutting & Stitching, you will split quantities by size and color in Dispatch Management." />
          <Form.Item name="quantity" label="Quantity" rules={[{ required: true, message: 'Enter quantity' }]}>
            <InputNumber min={1} style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="supervisorId" label="Supervisor" rules={[{ required: true, message: 'Select supervisor' }]}>
            <Select placeholder="Select supervisor">
              {supervisors.map(s => (
                <Select.Option key={s.supervisorId} value={s.supervisorId}>
                  {s.firstName} {s.lastName || ''}
                </Select.Option>
              ))}
            </Select>
          </Form.Item>
          <Form.Item name="endDate" label="End Date (Due)" rules={[{ required: true, message: 'Select end date' }]}>
            <DatePicker showTime style={{ width: '100%' }} disabledDate={d => d && d < dayjs().startOf('day')} />
          </Form.Item>
          <Form.Item>
            <Space>
              <Button type="primary" htmlType="submit">Create Order</Button>
              <Button onClick={() => setModalOpen(false)}>Cancel</Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>

      <Modal title={`Edit Batch ${editingBatch?.batchNumber || ''}`} open={editOpen}
        onCancel={() => { setEditOpen(false); setEditingBatch(null) }} footer={null}>
        <Form form={editForm} onFinish={onEditBatch} layout="vertical">
          <Alert type="info" showIcon style={{ marginBottom: 16 }}
            message="You can adjust planned quantity and due date. Planned cannot be less than already produced." />
          <Form.Item name="totalSuitPlanned" label="Planned Quantity"
            rules={[{ required: true, message: 'Enter planned quantity' }]}>
            <InputNumber min={editingBatch?.totalSuitProduced || 1} style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="expectedCompletionDate" label="Expected Completion Date"
            rules={[{ required: true, message: 'Select due date' }]}>
            <DatePicker style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item>
            <Space>
              <Button type="primary" htmlType="submit">Save</Button>
              <Button onClick={() => { setEditOpen(false); setEditingBatch(null) }}>Cancel</Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>

      <Drawer title={`Flow: ${flowData?.batchNumber || ''}`}
        open={flowDrawer} onClose={() => setFlowDrawer(false)} width={640}>
        {flowData && (
          <>
            <Descriptions bordered size="small" style={{ marginBottom: 24 }}>
              <Descriptions.Item label="Planned">{flowData.totalPlanned}</Descriptions.Item>
              <Descriptions.Item label="Produced">{flowData.totalProduced}</Descriptions.Item>
              <Descriptions.Item label="In Progress">{flowData.totalInProgress}</Descriptions.Item>
              <Descriptions.Item label="Stuck">{flowData.totalStuck}</Descriptions.Item>
              <Descriptions.Item label="Lost">{flowData.totalLost}</Descriptions.Item>
              <Descriptions.Item label="Status">
                <Tag color={flowData.overallStatus === 'on_track' ? 'green' :
                  flowData.overallStatus === 'has_bottleneck' ? 'orange' : 'blue'}>
                  {flowData.overallStatus?.replace('_', ' ').toUpperCase()}
                </Tag>
              </Descriptions.Item>
            </Descriptions>

            {flowData.moduleFlows?.length ? flowData.moduleFlows.map(mf => (
              <Card key={mf.moduleId} size="small" style={{ marginBottom: 12 }}
                title={<>{mf.moduleName} <Tag>{mf.stageName}</Tag></>}
                extra={<Tag color={mf.flowStatus === 'completed' ? 'green' :
                  mf.flowStatus === 'stuck' ? 'red' :
                  mf.flowStatus === 'in_progress' ? 'blue' : 'default'}>
                  {mf.flowStatus?.toUpperCase()}
                </Tag>}>
                <Descriptions size="small" column={2}>
                  <Descriptions.Item label="Sent">{mf.quantitySent}</Descriptions.Item>
                  <Descriptions.Item label="Returned OK">{mf.quantityReturnedOk}</Descriptions.Item>
                  <Descriptions.Item label="Pending">{mf.quantityPending}</Descriptions.Item>
                  <Descriptions.Item label="Forwarded">{mf.quantityForwarded}</Descriptions.Item>
                  <Descriptions.Item label="Stuck">{mf.quantityStuck}</Descriptions.Item>
                  <Descriptions.Item label="Lost">{mf.quantityDamaged + mf.quantityMissing}</Descriptions.Item>
                </Descriptions>
              </Card>
            )) : (
              <Alert message="No dispatches yet. Batch will auto-dispatch to Designing on creation." type="warning" />
            )}
          </>
        )}
      </Drawer>
    </div>
  )
}
