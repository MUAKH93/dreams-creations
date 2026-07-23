import { useEffect, useState } from 'react'
import {
  Table, Button, Modal, Form, Input, Select, Tag, Typography, Space,
  message, Popconfirm, Alert
} from 'antd'
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons'
import { financeAPI } from '../../api/finance'
import { apiErrorMessage } from '../../api/client'

const { Title } = Typography

const ACCOUNT_TYPES = ['ASSET', 'LIABILITY', 'EQUITY', 'INCOME', 'EXPENSE']

const typeColor = (t) => ({
  ASSET: 'blue', LIABILITY: 'orange', EQUITY: 'purple', INCOME: 'green', EXPENSE: 'red',
}[t] || 'default')

export default function ChartOfAccountsPage() {
  const [accounts, setAccounts] = useState([])
  const [loading, setLoading] = useState(true)
  const [modalOpen, setModalOpen] = useState(false)
  const [editing, setEditing] = useState(null)
  const [form] = Form.useForm()

  const load = () => {
    setLoading(true)
    financeAPI.getAccounts()
      .then(r => setAccounts(r.data))
      .catch(err => message.error(apiErrorMessage(err)))
      .finally(() => setLoading(false))
  }

  useEffect(() => { load() }, [])

  const openAdd = () => {
    setEditing(null)
    form.resetFields()
    setModalOpen(true)
  }

  const openEdit = (record) => {
    setEditing(record)
    form.setFieldsValue({
      accountCode: record.accountCode,
      accountName: record.accountName,
      accountType: record.accountType,
      parentId: record.parentId,
      description: record.description,
      isActive: record.isActive,
    })
    setModalOpen(true)
  }

  const onSave = async (values) => {
    try {
      if (editing) {
        await financeAPI.updateAccount(editing.accountId, {
          accountName: values.accountName,
          accountType: values.accountType,
          parentId: values.parentId,
          description: values.description,
          isActive: values.isActive,
        })
        message.success('Account updated')
      } else {
        await financeAPI.createAccount(values)
        message.success('Account created')
      }
      setModalOpen(false)
      form.resetFields()
      load()
    } catch (err) {
      message.error(err.response?.data?.message || 'Save failed')
    }
  }

  const onDelete = async (id) => {
    try {
      await financeAPI.deleteAccount(id)
      message.success('Account deleted')
      load()
    } catch (err) {
      message.error(err.response?.data?.message || 'Delete failed')
    }
  }

  const columns = [
    { title: 'Code', dataIndex: 'accountCode', key: 'code', width: 90 },
    { title: 'Name', dataIndex: 'accountName', key: 'name' },
    { title: 'Type', dataIndex: 'accountType', key: 'type', width: 110,
      render: t => <Tag color={typeColor(t)}>{t}</Tag> },
    { title: 'Status', key: 'status', width: 90,
      render: (_, r) => r.isActive
        ? <Tag color="green">Active</Tag>
        : <Tag>Inactive</Tag> },
    { title: 'System', key: 'system', width: 80,
      render: (_, r) => r.isSystem ? <Tag color="blue">Yes</Tag> : '—' },
    { title: 'Actions', key: 'actions', width: 120,
      render: (_, r) => (
        <Space>
          <Button size="small" icon={<EditOutlined />} onClick={() => openEdit(r)} />
          {!r.isSystem && (
            <Popconfirm title="Delete this account?" onConfirm={() => onDelete(r.accountId)}>
              <Button size="small" danger icon={<DeleteOutlined />} />
            </Popconfirm>
          )}
        </Space>
      ) },
  ]

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16 }}>
        <Title level={4} className="page-title" style={{ margin: 0 }}>Chart of Accounts</Title>
        <Button type="primary" icon={<PlusOutlined />} onClick={openAdd}>Add Account</Button>
      </div>

      <Alert type="info" showIcon style={{ marginBottom: 16 }}
        message="Default system accounts were seeded by the SQL migration. You can add custom accounts for your factory." />

      <Table dataSource={accounts} columns={columns} rowKey="accountId"
        loading={loading} scroll={{ x: 800 }} pagination={{ pageSize: 20 }} />

      <Modal
        title={editing ? 'Edit Account' : 'Add Account'}
        open={modalOpen}
        onCancel={() => setModalOpen(false)}
        footer={null}
        width={520}
      >
        <Form form={form} layout="vertical" onFinish={onSave}>
          {!editing && (
            <Form.Item name="accountCode" label="Account Code" rules={[{ required: true }]}>
              <Input placeholder="e.g. 5210" />
            </Form.Item>
          )}
          <Form.Item name="accountName" label="Account Name" rules={[{ required: true }]}>
            <Input placeholder="Account name" />
          </Form.Item>
          <Form.Item name="accountType" label="Type" rules={[{ required: true }]}>
            <Select options={ACCOUNT_TYPES.map(t => ({ value: t, label: t }))} />
          </Form.Item>
          <Form.Item name="parentId" label="Parent Account (optional)">
            <Select allowClear placeholder="None">
              {accounts.filter(a => !editing || a.accountId !== editing.accountId).map(a => (
                <Select.Option key={a.accountId} value={a.accountId}>
                  {a.accountCode} — {a.accountName}
                </Select.Option>
              ))}
            </Select>
          </Form.Item>
          <Form.Item name="description" label="Description">
            <Input.TextArea rows={2} />
          </Form.Item>
          {editing && (
            <Form.Item name="isActive" label="Active" initialValue={true}>
              <Select options={[{ value: true, label: 'Active' }, { value: false, label: 'Inactive' }]} />
            </Form.Item>
          )}
          <Button type="primary" htmlType="submit" block>Save</Button>
        </Form>
      </Modal>
    </div>
  )
}
