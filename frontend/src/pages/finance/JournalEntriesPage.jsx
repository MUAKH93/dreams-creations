import { Typography, Alert, Card } from 'antd'

const { Title, Text } = Typography

export default function JournalEntriesPage() {
  return (
    <div>
      <Title level={4} className="page-title">Journal Entries</Title>
      <Alert
        type="warning"
        showIcon
        message="Phase F1 — not implemented yet"
        description="Manual journal entry form with debit/credit validation will be added in Phase F1."
        style={{ marginBottom: 16 }}
      />
      <Card>
        <Text type="secondary">
          Each entry must balance: total debits = total credits before posting.
        </Text>
      </Card>
    </div>
  )
}
