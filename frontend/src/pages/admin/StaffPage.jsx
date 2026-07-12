import { useEffect, useState } from 'react'
import {
  Table, Button, Modal, Form, Input, Tag, Typography, Space, message, Alert, Tabs, Select, Popconfirm, Switch
} from 'antd'
import { PlusOutlined, KeyOutlined, UserAddOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons'
import { adminAPI } from '../../api/admin'
import { apiErrorMessage } from '../../api/client'
import { useAuth } from '../../context/AuthContext'

const { Title, Text } = Typography

export default function StaffPage() {
  const { auth } = useAuth()
  const isAdmin = auth?.role === 'ADMIN'

  const [supervisors, setSupervisors] = useState([])
  const [managers, setManagers] = useState([])
  const [loadingSup, setLoadingSup] = useState(true)
  const [loadingMgr, setLoadingMgr] = useState(true)
  const [loadError, setLoadError] = useState(null)

  const [createSupOpen, setCreateSupOpen] = useState(false)
  const [loginOpen, setLoginOpen] = useState(false)
  const [editSupOpen, setEditSupOpen] = useState(false)
  const [loginEditOpen, setLoginEditOpen] = useState(false)
  const [createMgrOpen, setCreateMgrOpen] = useState(false)
  const [editMgrOpen, setEditMgrOpen] = useState(false)

  const [selectedSup, setSelectedSup] = useState(null)
  const [selectedMgr, setSelectedMgr] = useState(null)

  const [createSupForm] = Form.useForm()
  const [loginForm] = Form.useForm()
  const [editSupForm] = Form.useForm()
  const [loginEditForm] = Form.useForm()
  const [createMgrForm] = Form.useForm()
  const [editMgrForm] = Form.useForm()

  const loadSupervisors = () => {
    setLoadingSup(true)
    adminAPI.getSupervisorAccounts()
      .then(r => setSupervisors(r.data))
      .catch(err => {
        setLoadError(apiErrorMessage(err))
        message.error(apiErrorMessage(err))
      })
      .finally(() => setLoadingSup(false))
  }

  const loadManagers = () => {
    setLoadingMgr(true)
    adminAPI.getManagerAccounts()
      .then(r => setManagers(r.data))
      .catch(err => message.error(apiErrorMessage(err)))
      .finally(() => setLoadingMgr(false))
  }

  useEffect(() => {
    if (isAdmin) {
      loadSupervisors()
      loadManagers()
    }
  }, [isAdmin])

  const onCreateSupervisor = async (values) => {
    try {
      await adminAPI.createSupervisorAccount(values)
      message.success('Supervisor created — they sign in at /login/supervisor')
      setCreateSupOpen(false)
      createSupForm.resetFields()
      loadSupervisors()
    } catch (err) {
      message.error(err.response?.data?.message || 'Failed to create supervisor')
    }
  }

  const openLoginModal = (record) => {
    setSelectedSup(record)
    loginForm.setFieldsValue({
      email: record.email || '',
      username: record.firstName?.toLowerCase().replace(/\s+/g, '') || '',
    })
    setLoginOpen(true)
  }

  const onCreateLogin = async (values) => {
    try {
      await adminAPI.createSupervisorLogin(selectedSup.supervisorId, values)
      message.success('Login created')
      setLoginOpen(false)
      loginForm.resetFields()
      setSelectedSup(null)
      loadSupervisors()
    } catch (err) {
      message.error(err.response?.data?.message || 'Failed to create login')
    }
  }

  const openEditSupervisor = (record) => {
    setSelectedSup(record)
    editSupForm.setFieldsValue({
      firstName: record.firstName,
      lastName: record.lastName,
      phone: record.phone,
      email: record.email,
      status: record.status || 'active',
    })
    setEditSupOpen(true)
  }

  const onEditSupervisor = async (values) => {
    try {
      await adminAPI.updateSupervisorAccount(selectedSup.supervisorId, values)
      message.success('Supervisor updated')
      setEditSupOpen(false)
      setSelectedSup(null)
      loadSupervisors()
    } catch (err) {
      message.error(err.response?.data?.message || 'Failed to update supervisor')
    }
  }

  const openLoginEdit = (record) => {
    setSelectedSup(record)
    loginEditForm.setFieldsValue({
      username: record.username,
      email: record.email,
      loginEnabled: record.loginEnabled !== false,
    })
    setLoginEditOpen(true)
  }

  const onUpdateLogin = async (values) => {
    try {
      await adminAPI.updateSupervisorLogin(selectedSup.supervisorId, {
        username: values.username,
        email: values.email,
        password: values.password || undefined,
        loginEnabled: values.loginEnabled,
      })
      message.success('Login updated')
      setLoginEditOpen(false)
      setSelectedSup(null)
      loadSupervisors()
    } catch (err) {
      message.error(err.response?.data?.message || 'Failed to update login')
    }
  }

  const deleteSupervisor = async (supervisorId) => {
    try {
      await adminAPI.deleteSupervisorAccount(supervisorId)
      message.success('Supervisor removed')
      loadSupervisors()
    } catch (err) {
      message.error(err.response?.data?.message || 'Failed to delete supervisor')
    }
  }

  const onCreateManager = async (values) => {
    try {
      await adminAPI.createManagerAccount(values)
      message.success('Manager account created — sign in at /login/management')
      setCreateMgrOpen(false)
      createMgrForm.resetFields()
      loadManagers()
    } catch (err) {
      message.error(err.response?.data?.message || 'Failed to create manager')
    }
  }

  const openEditManager = (record) => {
    setSelectedMgr(record)
    editMgrForm.setFieldsValue({
      username: record.username,
      email: record.email,
      loginEnabled: record.loginEnabled !== false,
    })
    setEditMgrOpen(true)
  }

  const onEditManager = async (values) => {
    try {
      await adminAPI.updateManagerAccount(selectedMgr.userId, {
        username: values.username,
        email: values.email,
        password: values.password || undefined,
        loginEnabled: values.loginEnabled,
      })
      message.success('Manager account updated')
      setEditMgrOpen(false)
      setSelectedMgr(null)
      loadManagers()
    } catch (err) {
      message.error(err.response?.data?.message || 'Failed to update manager')
    }
  }

  const deleteManager = async (userId) => {
    try {
      await adminAPI.deleteManagerAccount(userId)
      message.success('Manager account removed')
      loadManagers()
    } catch (err) {
      message.error(err.response?.data?.message || 'Failed to delete manager')
    }
  }

  const supervisorColumns = [
    { title: 'Name', key: 'name', render: (_, r) => `${r.firstName} ${r.lastName || ''}`.trim() },
    { title: 'Email', dataIndex: 'email', render: e => e || <Text type="secondary">Not set</Text> },
    { title: 'Phone', dataIndex: 'phone', render: p => p || '—' },
    { title: 'Login', key: 'login',
      render: (_, r) => r.hasLogin
        ? <Tag color={r.loginEnabled === false ? 'default' : 'green'}>
            {r.loginEnabled === false ? 'Disabled' : 'Active'} — {r.username}
          </Tag>
        : <Tag color="orange">No login</Tag> },
    { title: 'Status', dataIndex: 'status',
      render: s => <Tag color={s === 'active' ? 'blue' : 'default'}>{s?.toUpperCase()}</Tag> },
    { title: 'Actions', key: 'actions', width: 280,
      render: (_, r) => (
        <Space wrap>
          <Button size="small" icon={<EditOutlined />} onClick={() => openEditSupervisor(r)}>Edit</Button>
          {!r.hasLogin && (
            <Button size="small" icon={<KeyOutlined />} onClick={() => openLoginModal(r)}>Add login</Button>
          )}
          {r.hasLogin && (
            <Button size="small" icon={<KeyOutlined />} onClick={() => openLoginEdit(r)}>Login</Button>
          )}
          <Popconfirm
            title="Delete this supervisor?"
            description="Cannot delete if they have production assignments."
            onConfirm={() => deleteSupervisor(r.supervisorId)}
            okText="Delete"
            okButtonProps={{ danger: true }}
          >
            <Button size="small" danger icon={<DeleteOutlined />} />
          </Popconfirm>
        </Space>
      ) },
  ]

  const managerColumns = [
    { title: 'Username', dataIndex: 'username' },
    { title: 'Email', dataIndex: 'email' },
    { title: 'Login', key: 'login',
      render: (_, r) => (
        <Tag color={r.loginEnabled === false ? 'default' : 'green'}>
          {r.loginEnabled === false ? 'Disabled' : 'Active'}
        </Tag>
      ) },
    { title: 'Actions', key: 'actions',
      render: (_, r) => (
        <Space>
          <Button size="small" icon={<EditOutlined />} onClick={() => openEditManager(r)}>Edit</Button>
          <Popconfirm
            title="Delete this manager account?"
            onConfirm={() => deleteManager(r.userId)}
            okText="Delete"
            okButtonProps={{ danger: true }}
          >
            <Button size="small" danger icon={<DeleteOutlined />} />
          </Popconfirm>
        </Space>
      ) },
  ]

  if (!isAdmin) {
    return (
      <Alert type="warning" showIcon message="Only Admin can manage staff logins and accounts." />
    )
  }

  const withoutLogin = supervisors.filter(s => !s.hasLogin).length

  return (
    <div>
      <Title level={4} className="page-title">Staff & Login Accounts</Title>

      <Alert
        type="info"
        showIcon
        style={{ marginBottom: 16 }}
        message="Admin creates all logins"
        description="Managers use /login/management. Supervisors use /login/supervisor. Supervisor login email must match their supervisor record."
      />

      {loadError && <Alert type="error" message={loadError} style={{ marginBottom: 16 }} showIcon />}

      <Tabs
        items={[
          {
            key: 'supervisors',
            label: `Supervisors (${supervisors.length})`,
            children: (
              <>
                {withoutLogin > 0 && (
                  <Alert type="warning" showIcon style={{ marginBottom: 16 }}
                    message={`${withoutLogin} supervisor(s) have no login yet`} />
                )}
                <Button type="primary" icon={<UserAddOutlined />} style={{ marginBottom: 16 }}
                  onClick={() => setCreateSupOpen(true)}>
                  Add Supervisor + Login
                </Button>
                <Table dataSource={supervisors} columns={supervisorColumns} rowKey="supervisorId"
                  loading={loadingSup} scroll={{ x: 1000 }} />
              </>
            ),
          },
          {
            key: 'managers',
            label: `Managers (${managers.length})`,
            children: (
              <>
                <Alert type="info" showIcon style={{ marginBottom: 16 }}
                  message="Managers share the Management portal with Admin (no separate UI)." />
                <Button type="primary" icon={<PlusOutlined />} style={{ marginBottom: 16 }}
                  onClick={() => setCreateMgrOpen(true)}>
                  Add Manager Login
                </Button>
                <Table dataSource={managers} columns={managerColumns} rowKey="userId"
                  loading={loadingMgr} locale={{ emptyText: 'No manager accounts yet' }} />
              </>
            ),
          },
        ]}
      />

      {/* Supervisor create */}
      <Modal title="Add Supervisor + Login" open={createSupOpen}
        onCancel={() => setCreateSupOpen(false)} footer={null} width={520}>
        <Form form={createSupForm} layout="vertical" onFinish={onCreateSupervisor}>
          <Form.Item name="firstName" label="First name" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="lastName" label="Last name"><Input /></Form.Item>
          <Form.Item name="phone" label="Phone"><Input /></Form.Item>
          <Form.Item name="email" label="Email" rules={[{ required: true }, { type: 'email' }]}>
            <Input />
          </Form.Item>
          <Form.Item name="username" label="Login username" rules={[{ required: true }, { min: 3 }]}>
            <Input />
          </Form.Item>
          <Form.Item name="password" label="Login password" rules={[{ required: true }, { min: 6 }]}>
            <Input.Password />
          </Form.Item>
          <Form.Item>
            <Space>
              <Button type="primary" htmlType="submit">Create</Button>
              <Button onClick={() => setCreateSupOpen(false)}>Cancel</Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>

      {/* Supervisor edit */}
      <Modal title="Edit Supervisor" open={editSupOpen}
        onCancel={() => { setEditSupOpen(false); setSelectedSup(null) }} footer={null} width={480}>
        <Form form={editSupForm} layout="vertical" onFinish={onEditSupervisor}>
          <Form.Item name="firstName" label="First name" rules={[{ required: true }]}><Input /></Form.Item>
          <Form.Item name="lastName" label="Last name"><Input /></Form.Item>
          <Form.Item name="phone" label="Phone"><Input /></Form.Item>
          <Form.Item name="email" label="Email" rules={[{ required: true }, { type: 'email' }]}><Input /></Form.Item>
          <Form.Item name="status" label="Status">
            <Select>
              <Select.Option value="active">Active</Select.Option>
              <Select.Option value="inactive">Inactive</Select.Option>
            </Select>
          </Form.Item>
          <Form.Item>
            <Space>
              <Button type="primary" htmlType="submit">Save</Button>
              <Button onClick={() => { setEditSupOpen(false); setSelectedSup(null) }}>Cancel</Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>

      {/* Supervisor create login */}
      <Modal title={`Create login — ${selectedSup?.firstName || ''}`} open={loginOpen}
        onCancel={() => { setLoginOpen(false); setSelectedSup(null) }} footer={null} width={480}>
        <Form form={loginForm} layout="vertical" onFinish={onCreateLogin}>
          <Form.Item name="email" label="Email" rules={[{ required: true }, { type: 'email' }]}><Input /></Form.Item>
          <Form.Item name="username" label="Username" rules={[{ required: true }, { min: 3 }]}><Input /></Form.Item>
          <Form.Item name="password" label="Password" rules={[{ required: true }, { min: 6 }]}>
            <Input.Password />
          </Form.Item>
          <Form.Item>
            <Button type="primary" htmlType="submit" icon={<KeyOutlined />}>Create login</Button>
          </Form.Item>
        </Form>
      </Modal>

      {/* Supervisor login edit */}
      <Modal title={`Update login — ${selectedSup?.username || ''}`} open={loginEditOpen}
        onCancel={() => { setLoginEditOpen(false); setSelectedSup(null) }} footer={null} width={480}>
        <Form form={loginEditForm} layout="vertical" onFinish={onUpdateLogin}>
          <Form.Item name="username" label="Username" rules={[{ required: true }, { min: 3 }]}><Input /></Form.Item>
          <Form.Item name="email" label="Email" rules={[{ required: true }, { type: 'email' }]}><Input /></Form.Item>
          <Form.Item name="password" label="New password (leave blank to keep)">
            <Input.Password placeholder="Min 6 characters" />
          </Form.Item>
          <Form.Item name="loginEnabled" label="Login enabled" valuePropName="checked">
            <Switch />
          </Form.Item>
          <Form.Item>
            <Button type="primary" htmlType="submit">Save login</Button>
          </Form.Item>
        </Form>
      </Modal>

      {/* Manager create */}
      <Modal title="Add Manager Login" open={createMgrOpen}
        onCancel={() => setCreateMgrOpen(false)} footer={null} width={480}>
        <Form form={createMgrForm} layout="vertical" onFinish={onCreateManager}>
          <Form.Item name="username" label="Username" rules={[{ required: true }, { min: 3 }]}><Input /></Form.Item>
          <Form.Item name="email" label="Email" rules={[{ required: true }, { type: 'email' }]}><Input /></Form.Item>
          <Form.Item name="password" label="Password" rules={[{ required: true }, { min: 6 }]}>
            <Input.Password />
          </Form.Item>
          <Form.Item>
            <Space>
              <Button type="primary" htmlType="submit">Create</Button>
              <Button onClick={() => setCreateMgrOpen(false)}>Cancel</Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>

      {/* Manager edit */}
      <Modal title={`Edit Manager — ${selectedMgr?.username || ''}`} open={editMgrOpen}
        onCancel={() => { setEditMgrOpen(false); setSelectedMgr(null) }} footer={null} width={480}>
        <Form form={editMgrForm} layout="vertical" onFinish={onEditManager}>
          <Form.Item name="username" label="Username" rules={[{ required: true }, { min: 3 }]}><Input /></Form.Item>
          <Form.Item name="email" label="Email" rules={[{ required: true }, { type: 'email' }]}><Input /></Form.Item>
          <Form.Item name="password" label="New password (leave blank to keep)">
            <Input.Password />
          </Form.Item>
          <Form.Item name="loginEnabled" label="Login enabled" valuePropName="checked">
            <Switch />
          </Form.Item>
          <Form.Item>
            <Button type="primary" htmlType="submit">Save</Button>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )
}
