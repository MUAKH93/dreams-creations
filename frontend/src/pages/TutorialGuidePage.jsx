import { useEffect, useState, useMemo } from 'react'
import { Typography, Tabs, Card, Space, Button } from 'antd'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { BookOutlined, PlayCircleOutlined } from '@ant-design/icons'
import { useAuth } from '../context/AuthContext'
import { ROLES, MANAGEMENT_ROLES } from '../utils/roles'
import { financeModuleEnabled } from '../config/modules'
import { modulesAPI } from '../api/modules'
import {
  FINANCE_TUTORIAL,
  getOperationsTutorialForRole,
} from '../content/tutorialContent'
import {
  TutorialGuideSection,
  FinancePostingTable,
} from '../components/tutorial/PortalTutorial'

const { Title, Paragraph, Text } = Typography

export default function TutorialGuidePage() {
  const navigate = useNavigate()
  const [searchParams] = useSearchParams()
  const { auth } = useAuth()
  const role = auth?.role
  const opsConfig = useMemo(() => getOperationsTutorialForRole(role), [role])
  const defaultTab = searchParams.get('tab') === 'finance' ? 'finance' : 'operations'
  const [showFinance, setShowFinance] = useState(
    financeModuleEnabled && MANAGEMENT_ROLES.includes(role),
  )

  useEffect(() => {
    modulesAPI.getFlags()
      .then(r => {
        if (r.data?.finance?.enabled && MANAGEMENT_ROLES.includes(role)) {
          setShowFinance(true)
        }
      })
      .catch(() => {})
  }, [role])

  const handleNav = (path) => navigate(path)

  const operationsPanel = (
    <div className="tutorial-guide-panel">
      <Card className="tutorial-guide-intro">
        <Title level={4}>{opsConfig.portalName}</Title>
        <Paragraph>{opsConfig.intro}</Paragraph>
        <Space>
          <Button type="primary" icon={<PlayCircleOutlined />}
            onClick={() => navigate('/dashboard')}>
            Back to app — use Help for live tour
          </Button>
        </Space>
      </Card>
      {opsConfig.sections.map((section, i) => (
        <TutorialGuideSection
          key={section.id}
          section={section}
          index={i}
          themeColor={opsConfig.themeColor}
          onNavigate={handleNav}
        />
      ))}
    </div>
  )

  const financePanel = (
    <div className="tutorial-guide-panel">
      <Card className="tutorial-guide-intro">
        <Title level={4}>{FINANCE_TUTORIAL.portalName}</Title>
        <Paragraph>{FINANCE_TUTORIAL.intro}</Paragraph>
        <Paragraph type="secondary">
          Record a video walkthrough using <Text code>docs/finance-video-script.md</Text> in the repository.
        </Paragraph>
        <Button type="primary" style={{ background: FINANCE_TUTORIAL.themeColor }}
          onClick={() => navigate('/finance')}>
          Open Finance Portal
        </Button>
      </Card>
      {FINANCE_TUTORIAL.sections.map((section, i) => (
        <TutorialGuideSection
          key={section.id}
          section={section}
          index={i}
          themeColor={FINANCE_TUTORIAL.themeColor}
          onNavigate={handleNav}
        />
      ))}
      <FinancePostingTable rows={FINANCE_TUTORIAL.postingTable} />
    </div>
  )

  const tabItems = [
    { key: 'operations', label: 'Operations', children: operationsPanel },
  ]
  if (showFinance) {
    tabItems.push({ key: 'finance', label: 'Finance', children: financePanel })
  }

  return (
    <div className="tutorial-guide-page">
      <Title level={3}><BookOutlined style={{ marginRight: 10 }} />Software tutorials</Title>
      <Paragraph type="secondary">
        Detailed guides for {role === ROLES.CUSTOMER ? 'customers' : role === ROLES.SUPERVISOR ? 'supervisors' : 'operations'}
        {showFinance ? ' and finance' : ''}. Use the <Text strong>Help</Text> button in the app header to replay the interactive tour anytime.
      </Paragraph>
      <Tabs defaultActiveKey={defaultTab} items={tabItems} />
    </div>
  )
}
