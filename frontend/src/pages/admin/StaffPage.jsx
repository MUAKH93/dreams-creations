import { useEffect, useState } from 'react'
import {
  Table, Button, Modal, Form, Input, Tag, Typography, Space, message, Alert
} from 'antd'
import { PlusOutlined, KeyOutlined, UserAddOutlined } from '@ant-design/icons'
import { adminAPI } from '../../api/admin'
import { apiErrorMessage } from '../../api/client'
import { useAuth } from '../../context/AuthContext'

const { Title, Text } = Typography

export default function StaffPage() {
  const { auth } = useAuth()
  const isAdmin = auth?.role === 'ADMIN'

  const [staff, setStaff]       = useState([])
  const [loading, setLoading]   = useState(true)
  const [loadError, setLoadError] = useState(null)
  const [createOpen, setCreateOpen] = useState(false)
  const [loginOpen, setLoginOpen]   = useState(false)
  const [selected, setSelected]     = useState(null)
  const [createForm] = Form.useForm()
  const [loginForm]  = Form.useForm()

  const load = () => {
    setLoading(true)
    setLoadError(null)
    adminAPI.getSupervisorAccounts()
      .then(r => setStaff(r.data))
      .catch(err => {
        setLoadError(apiErrorMessage(err))
        message.error(apiErrorMessage(err))
      })
      .finally(() => setLoading(false))
  }

  useEffect(() => { load() }, [])

  const onCreate = async (values) => {
    try {
      await adminAPI.createSupervisorAccount(values)
      message.success('Supervisor and login created — they can sign in at /login')
      setCreateOpen(false)
      createForm.resetFields()
      load()
    } catch (err) {
      message.error(err.response?.data?.message || 'Failed to create supervisor')
    }
  }

  const openLoginModal = (record) => {
    setSelected(record)
    loginForm.setFieldsValue({
      email: record.email || '',
      username: record.firstName?.toLowerCase().replace(/\s+/g, '') || '',
    })
    setLoginOpen(true)
  }

  const onCreateLogin = async (values) => {
    try {
      await adminAPI.createSupervisorLogin(selected.supervisorId, values)
      message.success('Login created — supervisor can sign in now')
      setLoginOpen(false)
      loginForm.resetFields()
      setSelected(null)
      load()
    } catch (err) {
      message.error(err.response?.data?.message || 'Failed to create login')
    }
  }

  const columns = [
    { title: 'Name', key: 'name',
      render: (_, r) => `${r.firstName} ${r.lastName || ''}`.trim() },
    { title: 'Email', dataIndex: 'email', key: 'email',
      render: e => e || <Text type="secondary">Not set</Text> },
    { title: 'Phone', dataIndex: 'phone', key: 'phone',
      render: p => p || '—' },
    { title: 'Portal login', key: 'login',
      render: (_, r) => r.hasLogin
        ? <Tag color="green">Active — {r.username}</Tag>
        : <Tag color="orange">No login</Tag> },
    { title: 'Status', dataIndex: 'status', key: 'status',
      render: s => <Tag color={s === 'active' ? 'blue' : 'default'}>{s?.toUpperCase()}</Tag> },
    { title: 'Action', key: 'action',
      render: (_, r) => !r.hasLogin && isAdmin ? (
        <Button size="small" icon={<KeyOutlined />} onClick={() => openLoginModal(r)}>
          Create login
        </Button>
      ) : r.hasLogin ? <Text type="secondary">Ready</Text> : '—' },
  ]

  const withoutLogin = staff.filter(s => !s.hasLogin).length

  return (
    <div>
      <Title level={4} className="page-title">Staff & Supervisor Accounts</Title>

      <Alert
        type="info"
        showIcon
        style={{ marginBottom: 16 }}
        message="Supervisor portal linking"
        description="The supervisor's login email must match their supervisor record email. Use this page to create both in one step, or add a login to an existing supervisor."
      />

      {loadError && (
        <Alert type="error" message={loadError} style={{ marginBottom: 16 }} showIcon />
      )}

      {withoutLogin > 0 && (
        <Alert type="warning" showIcon style={{ marginBottom: 16 }}
          message={`${withoutLogin} supervisor(s) have no login yet`}
          description="They cannot access My Assignments until an admin creates a login account." />
      )}

      <Space style={{ marginBottom: 16 }}>
        {isAdmin && (
          <Button type="primary" icon={<UserAddOutlined />} onClick={() => setCreateOpen(true)}>
            Add Supervisor + Login
          </Button>
        )}
        {!isAdmin && (
          <Text type="secondary">Only ADMIN can create new login accounts.</Text>
        )}
      </Space>

      <Table
        dataSource={staff}
        columns={columns}
        rowKey="supervisorId"
        loading={loading}
        scroll={{ x: 900 }}
      />

      <Modal
        title="Add Supervisor + Login"
        open={createOpen}
        onCancel={() => setCreateOpen(false)}
        footer={null}
        width={520}
      >
        <Form form={createForm} layout="vertical" onFinish={onCreate}>
          <Form.Item name="firstName" label="First name" rules={[{ required: true }]}>
            <Input placeholder="Ahmed" />
          </Form.Item>
          <Form.Item name="lastName" label="Last name">
            <Input placeholder="Khan" />
          </Form.Item>
          <Form.Item name="phone" label="Phone">
            <Input placeholder="+92..." />
          </Form.Item>
          <Form.Item name="email" label="Email (used for portal linking)"
            rules={[{ required: true }, { type: 'email' }]}>
            <Input placeholder="supervisor@factory.com" />
          </Form.Item>
          <Form.Item name="username" label="Login username"
            rules={[{ required: true }, { min: 3 }]}>
            <Input placeholder="ahmed.supervisor" />
          </Form.Item>
          <Form.Item name="password" label="Login password"
            rules={[{ required: true }, { min: 6 }]}>
            <Input.Password placeholder="Min 6 characters" />
          </Form.Item>
          <Form.Item>
            <Space>
              <Button type="primary" htmlType="submit" icon={<PlusOutlined />}>
                Create
              </Button>
              <Button onClick={() => setCreateOpen(false)}>Cancel</Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title={`Create login — ${selected?.firstName || ''} ${selected?.lastName || ''}`}
        open={loginOpen}
        onCancel={() => { setLoginOpen(false); setSelected(null) }}
        footer={null}
        width={480}
      >
        <Form form={loginForm} layout="vertical" onFinish={onCreateLogin}>
          <Form.Item name="email" label="Email (must match supervisor record)"
            rules={[{ required: true }, { type: 'email' }]}>
            <Input />
          </Form.Item>
          <Form.Item name="username" label="Login username"
            rules={[{ required: true }, { min: 3 }]}>
            <Input />
          </Form.Item>
          <Form.Item name="password" label="Login password"
            rules={[{ required: true }, { min: 6 }]}>
            <Input.Password />
          </Form.Item>
          <Form.Item>
            <Space>
              <Button type="primary" htmlType="submit" icon={<KeyOutlined />}>
                Create login
              </Button>
              <Button onClick={() => { setLoginOpen(false); setSelected(null) }}>Cancel</Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )
}
