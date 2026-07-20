import { Typography, Alert, Card } from 'antd'

const { Title, Text } = Typography

export default function ChartOfAccountsPage() {
  return (
    <div>
      <Title level={4} className="page-title">Chart of Accounts</Title>
      <Alert
        type="warning"
        showIcon
        message="Phase F1 — not implemented yet"
        description="Run add-finance-module.sql on staging, then implement account CRUD and list view here."
        style={{ marginBottom: 16 }}
      />
      <Card>
        <Text type="secondary">
          Default seed accounts (1000 Cash, 1100 AR, 4000 Sales, 5000 COGS, etc.) are defined in the SQL migration.
        </Text>
      </Card>
    </div>
  )
}
