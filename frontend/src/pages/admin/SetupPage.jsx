import { useEffect, useState } from 'react'
import {
  Tabs, Table, Button, Modal, Form, Input, Select, Tag, Typography,
  Space, message, Alert, Popconfirm
} from 'antd'
import { PlusOutlined, DeleteOutlined } from '@ant-design/icons'
import { setupAPI } from '../../api/setup'
import { productionAPI } from '../../api/production'
import { adminAPI } from '../../api/admin'
import { apiErrorMessage } from '../../api/client'

const { Title, Text } = Typography

function SizesTab() {
  const [sizes, setSizes] = useState([])
  const [categories, setCategories] = useState([])
  const [loading, setLoading] = useState(true)
  const [open, setOpen] = useState(false)
  const [form] = Form.useForm()

  const load = () => {
    setLoading(true)
    Promise.all([setupAPI.getSizes(), productionAPI.getCategories()])
      .then(([s, c]) => { setSizes(s.data); setCategories(c.data) })
      .catch(err => message.error(apiErrorMessage(err)))
      .finally(() => setLoading(false))
  }

  useEffect(() => { load() }, [])

  const onAdd = async (values) => {
    try {
      await setupAPI.createSize({
        sizeValue: values.sizeValue,
        description: values.description,
        category: { categoryId: values.categoryId },
      })
      message.success('Size added')
      setOpen(false)
      form.resetFields()
      load()
    } catch (err) {
      message.error(err.response?.data?.message || 'Failed to add size')
    }
  }

  const columns = [
    { title: 'Category', key: 'cat',
      render: (_, r) => r.category?.categoryName || '—' },
    { title: 'Size', dataIndex: 'sizeValue', key: 'size' },
    { title: 'Description', dataIndex: 'description', key: 'desc',
      render: d => d || '—' },
  ]

  return (
    <div>
      <Alert type="info" showIcon style={{ marginBottom: 16 }}
        message="Ladies: XS–XL · Kids: 18–40 (or age sizes like 2Y, 4Y)"
        description="Each size belongs to one category. Used when creating suits and inventory." />
      <Button type="primary" icon={<PlusOutlined />} style={{ marginBottom: 16 }}
        onClick={() => setOpen(true)}>Add Size</Button>
      <Table dataSource={sizes} columns={columns} rowKey="sizeId" loading={loading} />

      <Modal title="Add Size" open={open} onCancel={() => setOpen(false)} footer={null}>
        <Form form={form} layout="vertical" onFinish={onAdd}>
          <Form.Item name="categoryId" label="Category" rules={[{ required: true }]}>
            <Select placeholder="Ladies or Kids">
              {categories.map(c => (
                <Select.Option key={c.categoryId} value={c.categoryId}>{c.categoryName}</Select.Option>
              ))}
            </Select>
          </Form.Item>
          <Form.Item name="sizeValue" label="Size value" rules={[{ required: true }]}>
            <Input placeholder="e.g. M, XL, 24, 2Y" />
          </Form.Item>
          <Form.Item name="description" label="Description">
            <Input placeholder="Optional" />
          </Form.Item>
          <Button type="primary" htmlType="submit">Save</Button>
        </Form>
      </Modal>
    </div>
  )
}

function PaymentMethodsTab() {
  const [methods, setMethods] = useState([])
  const [loading, setLoading] = useState(true)
  const [open, setOpen] = useState(false)
  const [form] = Form.useForm()

  const load = () => {
    setLoading(true)
    setupAPI.getPaymentMethods()
      .then(r => setMethods(r.data))
      .catch(err => message.error(apiErrorMessage(err)))
      .finally(() => setLoading(false))
  }

  useEffect(() => { load() }, [])

  const onAdd = async (values) => {
    try {
      await setupAPI.createPaymentMethod({
        methodName: values.methodName,
        description: values.description,
        status: 'active',
      })
      message.success('Payment method added')
      setOpen(false)
      form.resetFields()
      load()
    } catch (err) {
      message.error(err.response?.data?.message || 'Failed to add payment method')
    }
  }

  const columns = [
    { title: 'Method', dataIndex: 'methodName', key: 'name' },
    { title: 'Description', dataIndex: 'description', key: 'desc', render: d => d || '—' },
    { title: 'Status', dataIndex: 'status', key: 'status',
      render: s => <Tag color={s === 'active' ? 'green' : 'default'}>{s?.toUpperCase()}</Tag> },
  ]

  return (
    <div>
      <Button type="primary" icon={<PlusOutlined />} style={{ marginBottom: 16 }}
        onClick={() => setOpen(true)}>Add Payment Method</Button>
      <Table dataSource={methods} columns={columns} rowKey="paymentMethodId" loading={loading} />

      <Modal title="Add Payment Method" open={open} onCancel={() => setOpen(false)} footer={null}>
        <Form form={form} layout="vertical" onFinish={onAdd}>
          <Form.Item name="methodName" label="Name" rules={[{ required: true }]}>
            <Input placeholder="e.g. Cash, Bank Transfer, JazzCash" />
          </Form.Item>
          <Form.Item name="description" label="Description">
            <Input placeholder="Optional" />
          </Form.Item>
          <Button type="primary" htmlType="submit">Save</Button>
        </Form>
      </Modal>
    </div>
  )
}

function DesigningTypesTab() {
  const [types, setTypes] = useState([])
  const [loading, setLoading] = useState(true)
  const [open, setOpen] = useState(false)
  const [form] = Form.useForm()

  const load = () => {
    setLoading(true)
    setupAPI.getDesigningWorkTypes()
      .then(r => setTypes(r.data))
      .catch(err => message.error(apiErrorMessage(err)))
      .finally(() => setLoading(false))
  }

  useEffect(() => { load() }, [])

  const onAdd = async (values) => {
    try {
      await setupAPI.createDesigningWorkType({
        typeName: values.typeName,
        description: values.description,
        status: 'active',
      })
      message.success('Designing type added')
      setOpen(false)
      form.resetFields()
      load()
    } catch (err) {
      message.error(err.response?.data?.message || 'Failed to add type')
    }
  }

  return (
    <div>
      <Alert type="info" showIcon style={{ marginBottom: 16 }}
        message="Types of designing work"
        description="Required when dispatching to the Designing stage (new orders and forwards)." />
      <Button type="primary" icon={<PlusOutlined />} style={{ marginBottom: 16 }}
        onClick={() => setOpen(true)}>Add Designing Type</Button>
      <Table dataSource={types} rowKey="designingWorkTypeId" loading={loading}
        columns={[
          { title: 'Type', dataIndex: 'typeName', key: 'name' },
          { title: 'Description', dataIndex: 'description', key: 'desc', render: d => d || '—' },
          { title: 'Status', dataIndex: 'status', key: 'status',
            render: s => <Tag color="green">{s?.toUpperCase()}</Tag> },
        ]} />
      <Modal title="Add Designing Type" open={open} onCancel={() => setOpen(false)} footer={null}>
        <Form form={form} layout="vertical" onFinish={onAdd}>
          <Form.Item name="typeName" label="Type name" rules={[{ required: true }]}>
            <Input placeholder="e.g. Pattern Drafting, Embroidery Layout" />
          </Form.Item>
          <Form.Item name="description" label="Description">
            <Input placeholder="Optional" />
          </Form.Item>
          <Button type="primary" htmlType="submit">Save</Button>
        </Form>
      </Modal>
    </div>
  )
}

function FillingTypesTab() {
  const [types, setTypes] = useState([])
  const [loading, setLoading] = useState(true)
  const [open, setOpen] = useState(false)
  const [form] = Form.useForm()

  const load = () => {
    setLoading(true)
    setupAPI.getFillingWorkTypes()
      .then(r => setTypes(r.data))
      .catch(err => message.error(apiErrorMessage(err)))
      .finally(() => setLoading(false))
  }

  useEffect(() => { load() }, [])

  const onAdd = async (values) => {
    try {
      await setupAPI.createFillingWorkType({
        typeName: values.typeName,
        description: values.description,
        status: 'active',
      })
      message.success('Filling type added')
      setOpen(false)
      form.resetFields()
      load()
    } catch (err) {
      message.error(err.response?.data?.message || 'Failed to add type')
    }
  }

  return (
    <div>
      <Alert type="info" showIcon style={{ marginBottom: 16 }}
        message="Types of filling / padding work"
        description="Required when dispatching to the optional Filling stage." />
      <Button type="primary" icon={<PlusOutlined />} style={{ marginBottom: 16 }}
        onClick={() => setOpen(true)}>Add Filling Type</Button>
      <Table dataSource={types} rowKey="fillingWorkTypeId" loading={loading}
        columns={[
          { title: 'Type', dataIndex: 'typeName', key: 'name' },
          { title: 'Description', dataIndex: 'description', key: 'desc', render: d => d || '—' },
          { title: 'Status', dataIndex: 'status', key: 'status',
            render: s => <Tag color="green">{s?.toUpperCase()}</Tag> },
        ]} />
      <Modal title="Add Filling Type" open={open} onCancel={() => setOpen(false)} footer={null}>
        <Form form={form} layout="vertical" onFinish={onAdd}>
          <Form.Item name="typeName" label="Type name" rules={[{ required: true }]}>
            <Input placeholder="e.g. Standard Padding, Heavy Padding" />
          </Form.Item>
          <Form.Item name="description" label="Description">
            <Input placeholder="Optional" />
          </Form.Item>
          <Button type="primary" htmlType="submit">Save</Button>
        </Form>
      </Modal>
    </div>
  )
}

function ModuleAssignmentsTab() {
  const [assignments, setAssignments] = useState([])
  const [supervisors, setSupervisors] = useState([])
  const [modules, setModules] = useState([])
  const [loading, setLoading] = useState(true)
  const [open, setOpen] = useState(false)
  const [form] = Form.useForm()

  const load = () => {
    setLoading(true)
    Promise.all([
      setupAPI.getSupervisorModules(),
      adminAPI.getSupervisorAccounts(),
      productionAPI.getModules(),
    ]).then(([a, s, m]) => {
      setAssignments(a.data)
      setSupervisors(s.data)
      setModules(m.data)
    }).catch(err => message.error(apiErrorMessage(err)))
      .finally(() => setLoading(false))
  }

  useEffect(() => { load() }, [])

  const onAssign = async (values) => {
    try {
      await setupAPI.assignModule(values.supervisorId, values.moduleId)
      message.success('Module assigned to supervisor')
      setOpen(false)
      form.resetFields()
      load()
    } catch (err) {
      message.error(err.response?.data?.message || 'Assignment failed')
    }
  }

  const unassign = async (id) => {
    try {
      await setupAPI.unassignModule(id)
      message.success('Assignment removed')
      load()
    } catch {
      message.error('Failed to remove assignment')
    }
  }

  const columns = [
    { title: 'Supervisor', key: 'sup',
      render: (_, r) => `${r.supervisor?.firstName || ''} ${r.supervisor?.lastName || ''}`.trim() },
    { title: 'Module', key: 'mod', render: (_, r) => r.module?.moduleName || '—' },
    { title: 'Stage', key: 'stage', render: (_, r) => r.module?.stage?.stageName || '—' },
    { title: 'Assigned', dataIndex: 'assignedDate', key: 'date' },
    { title: 'Status', dataIndex: 'status', key: 'status',
      render: s => <Tag color="blue">{s?.toUpperCase()}</Tag> },
    { title: '', key: 'act',
      render: (_, r) => (
        <Popconfirm title="Remove this assignment?" onConfirm={() => unassign(r.supervisorModuleId)}>
          <Button size="small" danger icon={<DeleteOutlined />} />
        </Popconfirm>
      ) },
  ]

  return (
    <div>
      <Alert type="info" showIcon style={{ marginBottom: 16 }}
        message="Assign supervisors to production modules"
        description="Supervisors only see dispatches for modules they are assigned to in Dispatch Management." />
      <Button type="primary" icon={<PlusOutlined />} style={{ marginBottom: 16 }}
        onClick={() => setOpen(true)}>Assign Module</Button>
      <Table dataSource={assignments} columns={columns}
        rowKey="supervisorModuleId" loading={loading} scroll={{ x: 700 }} />

      <Modal title="Assign Module to Supervisor" open={open}
        onCancel={() => setOpen(false)} footer={null}>
        <Form form={form} layout="vertical" onFinish={onAssign}>
          <Form.Item name="supervisorId" label="Supervisor" rules={[{ required: true }]}>
            <Select placeholder="Select supervisor">
              {supervisors.map(s => (
                <Select.Option key={s.supervisorId} value={s.supervisorId}>
                  {s.firstName} {s.lastName || ''} {s.hasLogin ? '' : '(no login)'}
                </Select.Option>
              ))}
            </Select>
          </Form.Item>
          <Form.Item name="moduleId" label="Production module" rules={[{ required: true }]}>
            <Select placeholder="Design Studio, Cutting & Stitching, etc.">
              {modules.map(m => (
                <Select.Option key={m.moduleId} value={m.moduleId}>
                  {m.moduleName} ({m.stage?.stageName})
                </Select.Option>
              ))}
            </Select>
          </Form.Item>
          <Button type="primary" htmlType="submit">Assign</Button>
        </Form>
      </Modal>
    </div>
  )
}

export default function SetupPage() {
  const items = [
    { key: 'sizes', label: 'Sizes', children: <SizesTab /> },
    { key: 'designing', label: 'Designing Types', children: <DesigningTypesTab /> },
    { key: 'filling', label: 'Filling Types', children: <FillingTypesTab /> },
    { key: 'payments', label: 'Payment Methods', children: <PaymentMethodsTab /> },
    { key: 'modules', label: 'Supervisor Modules', children: <ModuleAssignmentsTab /> },
  ]

  return (
    <div>
      <Title level={4} className="page-title">Factory Setup</Title>
      <Text type="secondary" style={{ display: 'block', marginBottom: 16 }}>
        Configure sizes, designing/filling types, payment options, and supervisor module assignments.
      </Text>
      <Tabs items={items} />
    </div>
  )
}
