import { useEffect, useState } from 'react'
import {
  Table, Button, Modal, Form, Input, InputNumber, Select, Typography, Space,
  message, Alert, DatePicker, Row, Col, Divider
} from 'antd'
import { PlusOutlined, DeleteOutlined } from '@ant-design/icons'
import { financeAPI } from '../../api/finance'
import { apiErrorMessage } from '../../api/client'
import dayjs from 'dayjs'

const { Title, Text } = Typography

export default function JournalEntriesPage() {
  const [entries, setEntries] = useState([])
  const [accounts, setAccounts] = useState([])
  const [loading, setLoading] = useState(true)
  const [modalOpen, setModalOpen] = useState(false)
  const [detailOpen, setDetailOpen] = useState(false)
  const [selected, setSelected] = useState(null)
  const [form] = Form.useForm()

  const load = () => {
    setLoading(true)
    Promise.all([
      financeAPI.getJournals(),
      financeAPI.getAccounts(true),
    ])
      .then(([j, a]) => {
        setEntries(j.data)
        setAccounts(a.data)
      })
      .catch(err => message.error(apiErrorMessage(err)))
      .finally(() => setLoading(false))
  }

  useEffect(() => { load() }, [])

  const openCreate = () => {
    form.resetFields()
    form.setFieldsValue({
      entryDate: dayjs(),
      lines: [
        { accountId: null, debitAmount: 0, creditAmount: 0 },
        { accountId: null, debitAmount: 0, creditAmount: 0 },
      ],
    })
    setModalOpen(true)
  }

  const onCreate = async (values) => {
    try {
      const payload = {
        entryDate: values.entryDate.format('YYYY-MM-DD'),
        memo: values.memo,
        lines: values.lines.map(line => ({
          accountId: line.accountId,
          debitAmount: Number(line.debitAmount) || 0,
          creditAmount: Number(line.creditAmount) || 0,
          lineMemo: line.lineMemo,
        })),
      }
      await financeAPI.createJournal(payload)
      message.success('Journal entry posted')
      setModalOpen(false)
      load()
    } catch (err) {
      message.error(err.response?.data?.message || 'Post failed')
    }
  }

  const viewEntry = async (record) => {
    try {
      const res = await financeAPI.getJournal(record.entryId)
      setSelected(res.data)
      setDetailOpen(true)
    } catch (err) {
      message.error(apiErrorMessage(err))
    }
  }

  const columns = [
    { title: 'Entry #', dataIndex: 'entryNumber', key: 'num', width: 120 },
    { title: 'Date', dataIndex: 'entryDate', key: 'date', width: 110 },
    { title: 'Memo', dataIndex: 'memo', key: 'memo', ellipsis: true },
    { title: 'Debit', dataIndex: 'totalDebit', key: 'debit', width: 100,
      render: v => Number(v || 0).toLocaleString() },
    { title: 'Credit', dataIndex: 'totalCredit', key: 'credit', width: 100,
      render: v => Number(v || 0).toLocaleString() },
    { title: 'Source', dataIndex: 'sourceType', key: 'source', width: 90 },
    { title: 'Action', key: 'action', width: 90,
      render: (_, r) => <Button size="small" onClick={() => viewEntry(r)}>View</Button> },
  ]

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16 }}>
        <Title level={4} className="page-title" style={{ margin: 0 }}>Journal Entries</Title>
        <Button type="primary" icon={<PlusOutlined />} onClick={openCreate}>New Entry</Button>
      </div>

      <Alert type="info" showIcon style={{ marginBottom: 16 }}
        message="Manual journal entries must balance — total debits must equal total credits." />

      <Table dataSource={entries} columns={columns} rowKey="entryId"
        loading={loading} scroll={{ x: 900 }} />

      <Modal title="New Journal Entry" open={modalOpen} onCancel={() => setModalOpen(false)}
        footer={null} width={720}>
        <Form form={form} layout="vertical" onFinish={onCreate}>
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item name="entryDate" label="Entry Date" rules={[{ required: true }]}>
                <DatePicker style={{ width: '100%' }} />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item name="memo" label="Memo">
                <Input placeholder="Optional description" />
              </Form.Item>
            </Col>
          </Row>

          <Divider orientation="left">Lines</Divider>
          <Form.List name="lines">
            {(fields, { add, remove }) => (
              <>
                {fields.map(({ key, name, ...rest }) => (
                  <Row gutter={8} key={key} style={{ marginBottom: 8 }}>
                    <Col span={9}>
                      <Form.Item {...rest} name={[name, 'accountId']} rules={[{ required: true, message: 'Account required' }]}>
                        <Select placeholder="Account" showSearch optionFilterProp="label">
                          {accounts.map(a => (
                            <Select.Option key={a.accountId} value={a.accountId}
                              label={`${a.accountCode} ${a.accountName}`}>
                              {a.accountCode} — {a.accountName}
                            </Select.Option>
                          ))}
                        </Select>
                      </Form.Item>
                    </Col>
                    <Col span={5}>
                      <Form.Item {...rest} name={[name, 'debitAmount']}>
                        <InputNumber min={0} style={{ width: '100%' }} placeholder="Debit" />
                      </Form.Item>
                    </Col>
                    <Col span={5}>
                      <Form.Item {...rest} name={[name, 'creditAmount']}>
                        <InputNumber min={0} style={{ width: '100%' }} placeholder="Credit" />
                      </Form.Item>
                    </Col>
                    <Col span={4}>
                      <Form.Item {...rest} name={[name, 'lineMemo']}>
                        <Input placeholder="Line memo" />
                      </Form.Item>
                    </Col>
                    <Col span={1}>
                      {fields.length > 2 && (
                        <Button danger icon={<DeleteOutlined />} onClick={() => remove(name)} />
                      )}
                    </Col>
                  </Row>
                ))}
                <Button type="dashed" block onClick={() => add()} style={{ marginBottom: 16 }}>
                  + Add line
                </Button>
              </>
            )}
          </Form.List>

          <Button type="primary" htmlType="submit" block>Post Entry</Button>
        </Form>
      </Modal>

      <Modal title={selected?.entryNumber} open={detailOpen}
        onCancel={() => setDetailOpen(false)} footer={null} width={640}>
        {selected && (
          <>
            <Text type="secondary">{selected.entryDate} · {selected.memo || 'No memo'}</Text>
            <Table
              style={{ marginTop: 16 }}
              size="small"
              pagination={false}
              rowKey="lineId"
              dataSource={selected.lines || []}
              columns={[
                { title: 'Account', key: 'acc', render: (_, r) => `${r.accountCode} — ${r.accountName}` },
                { title: 'Debit', dataIndex: 'debitAmount', render: v => Number(v || 0).toLocaleString() },
                { title: 'Credit', dataIndex: 'creditAmount', render: v => Number(v || 0).toLocaleString() },
                { title: 'Memo', dataIndex: 'lineMemo' },
              ]}
            />
          </>
        )}
      </Modal>
    </div>
  )
}
