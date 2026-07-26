import { useEffect, useState } from 'react'
import {
  Table, Button, Modal, Form, InputNumber, Tag, Typography,
  Space, message, Alert, Tabs, Row, Col
} from 'antd'
import { RollbackOutlined } from '@ant-design/icons'
import { productionAPI } from '../../api/production'
import { apiErrorMessage } from '../../api/client'
import { useAuth } from '../../context/AuthContext'
import dayjs from 'dayjs'

const { Title, Text } = Typography

export default function AssignmentsPage() {
  const { auth } = useAuth()
  const [assignments, setAssignments] = useState([])
  const [loading, setLoading]     = useState(true)
  const [loadError, setLoadError] = useState(null)
  const [returnModal, setReturnModal] = useState(false)
  const [selected, setSelected]       = useState(null)
  const [returnForm] = Form.useForm()

  const load = () => {
    setLoading(true)
    setLoadError(null)
    productionAPI.getMyAssignments()
      .then(r => setAssignments(r.data))
      .catch(err => {
        setLoadError(apiErrorMessage(err))
        message.error(apiErrorMessage(err))
      })
      .finally(() => setLoading(false))
  }

  useEffect(() => { load() }, [])

  const isPressAndPackingStage = (record) =>
    record?.module?.stage?.stageName === 'Press and Packing'

  const openReturn = (record) => {
    setSelected(record)
    returnForm.resetFields()
    setReturnModal(true)
  }

  const onReturn = async (values) => {
    try {
      const hasLines = selected?.skuLines?.length > 0
      const body = hasLines
        ? {
            skuLines: selected.skuLines.map((line, i) => ({
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
      await productionAPI.returnAssignment(selected.assignmentId, body)
      message.success('Return recorded successfully')
      setReturnModal(false)
      returnForm.resetFields()
      load()
    } catch (err) {
      message.error(err.response?.data?.message || 'Return failed')
    }
  }

  const statusColor = (s) => ({
    sent: 'blue', in_progress: 'processing',
    returned: 'green', overdue: 'red',
  }[s] || 'default')

  const columns = [
    { title: '#', dataIndex: 'assignmentId', key: 'id', width: 70 },
    { title: 'Batch', key: 'batch',
      render: (_, r) => r.batch?.batchNumber || `#${r.batch?.batchId}` },
    { title: 'Stage', key: 'stage',
      render: (_, r) => r.module?.stage?.stageName || '-' },
    { title: 'Work Type', key: 'workType',
      render: (_, r) => r.designingWorkType?.typeName
        || r.fillingWorkType?.typeName
        || (r.skuLines?.length ? `${r.skuLines.length} SKU line(s)` : '—') },
    { title: 'Module', key: 'module',
      render: (_, r) => r.module?.moduleName || '-' },
    { title: 'Sent', dataIndex: 'quantitySent', key: 'sent' },
    { title: 'OK', dataIndex: 'quantityReturnedOk', key: 'ok' },
    { title: 'Due', dataIndex: 'dueDate', key: 'due',
      render: d => d ? dayjs(d).format('DD MMM YYYY') : '-' },
    { title: 'Status', dataIndex: 'status', key: 'status',
      render: s => <Tag color={statusColor(s)}>{s?.toUpperCase()}</Tag> },
    { title: 'Action', key: 'action',
      render: (_, r) => r.status !== 'returned' ? (
        <Button type="primary" size="small" icon={<RollbackOutlined />}
          onClick={() => openReturn(r)}>
          Record Return
        </Button>
      ) : <Text type="secondary">Done</Text> },
  ]

  const active = assignments.filter(a => a.status !== 'returned')
  const completed = assignments.filter(a => a.status === 'returned')

  const tabItems = [
    {
      key: 'active',
      label: `Active (${active.length})`,
      children: (
        <Table dataSource={active} columns={columns} rowKey="assignmentId"
          loading={loading} scroll={{ x: 1000 }} />
      ),
    },
    {
      key: 'completed',
      label: `Completed (${completed.length})`,
      children: (
        <Table dataSource={completed} columns={columns} rowKey="assignmentId"
          loading={loading} scroll={{ x: 1000 }} />
      ),
    },
  ]

  return (
    <div>
      <Title level={4} className="page-title">My Assignments</Title>

      {!auth?.supervisorId && !loading && (
        <Alert type="warning" showIcon style={{ marginBottom: 16 }}
          message="Supervisor profile not linked"
          description="Your login email must match the email on your supervisor record." />
      )}

      {loadError && (
        <Alert type="error" message={loadError} style={{ marginBottom: 16 }} showIcon />
      )}

      <Alert type="info" showIcon style={{ marginBottom: 16 }}
        message="Record returns when work is finished"
        description="Cutting & Stitching returns auto-forward to Press and Packing. The packing supervisor records the return here — inventory updates only when Press and Packing is returned, not before." />

      <Tabs items={tabItems} />

      <Modal title={`Record Return — Dispatch #${selected?.assignmentId}`}
        open={returnModal} onCancel={() => setReturnModal(false)} footer={null} width={560}>
        {isPressAndPackingStage(selected) && (
          <Alert type="success" showIcon style={{ marginBottom: 16 }}
            message="This return sends OK pieces to inventory"
            description="Record how many pieces finished press and packing. Only OK counts are added to stock." />
        )}
        {selected?.skuLines?.length > 0 ? (
          <>
            <Alert type="info" showIcon style={{ marginBottom: 16 }}
              message="Return per size/color line" />
            <Form form={returnForm} onFinish={onReturn} layout="vertical">
              {selected.skuLines.map((line, i) => (
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
          <>
            {selected && (
              <Alert type="info" showIcon style={{ marginBottom: 16 }}
                message={`Quantity sent: ${selected.quantitySent}`} />
            )}
            <Form form={returnForm} onFinish={onReturn} layout="vertical">
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
          </>
        )}
      </Modal>
    </div>
  )
}
