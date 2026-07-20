import { Typography, Alert, List, Card } from 'antd'

const { Title } = Typography

const PLANNED_REPORTS = [
  'Trial Balance (F1)',
  'General Ledger (F1)',
  'AR Aging (F2)',
  'Profit & Loss (F4)',
  'Balance Sheet (F4)',
  'Inventory Valuation (F3)',
  'Bank Reconciliation (F6)',
]

export default function FinanceReportsPage() {
  return (
    <div>
      <Title level={4} className="page-title">Finance Reports</Title>
      <Alert
        type="info"
        showIcon
        message="Reports hub — phases F1 through F6"
        style={{ marginBottom: 16 }}
      />
      <Card title="Planned reports">
        <List
          dataSource={PLANNED_REPORTS}
          renderItem={item => <List.Item>{item}</List.Item>}
        />
      </Card>
    </div>
  )
}
