import { useCallback, useEffect, useState } from 'react'
import { Drawer, Button, Typography, Space, Tour } from 'antd'
import {
  QuestionCircleOutlined, BookOutlined, RocketOutlined,
} from '@ant-design/icons'

const { Title, Paragraph, Text } = Typography

const STORAGE_KEY = 'dc-finance-tutorial-done'

export const FINANCE_TUTORIAL_STEPS = [
  {
    title: 'Welcome to Finance',
    description: 'This is a separate accounting workspace — simpler than the factory operations portal. Use it for ledgers, journals, and financial reports.',
  },
  {
    title: 'Chart of Accounts',
    description: 'Start here to review or add accounts (Cash, Accounts Receivable, Sales Revenue, etc.). System accounts are pre-seeded when you run the finance SQL script.',
    path: '/finance/accounts',
  },
  {
    title: 'Journal Entries',
    description: 'Record manual adjustments and opening balances. Each entry must balance — total debits must equal total credits.',
    path: '/finance/journals',
  },
  {
    title: 'Payables',
    description: 'Add vendors, record supplier invoices, and apply payments. Journals post automatically (Dr expense / Cr AP; payment Dr AP / Cr cash).',
    path: '/finance/payables',
  },
  {
    title: 'Reports',
    description: 'Run Trial Balance, General Ledger, AR/AP Aging, P&L, and Balance Sheet.',
    path: '/finance/reports',
  },
  {
    title: 'Back to Operations',
    description: 'Use “Back to Operations” in the sidebar anytime to return to production, sales, and inventory.',
  },
]

export function useFinanceTutorial() {
  const [done, setDone] = useState(() => localStorage.getItem(STORAGE_KEY) === '1')
  const [welcomeOpen, setWelcomeOpen] = useState(false)
  const [helpOpen, setHelpOpen] = useState(false)
  const [tourOpen, setTourOpen] = useState(false)

  useEffect(() => {
    if (!done) {
      setWelcomeOpen(true)
    }
  }, [done])

  const markDone = useCallback(() => {
    localStorage.setItem(STORAGE_KEY, '1')
    setDone(true)
    setWelcomeOpen(false)
    setTourOpen(false)
  }, [])

  const resetTutorial = useCallback(() => {
    localStorage.removeItem(STORAGE_KEY)
    setDone(false)
    setWelcomeOpen(true)
  }, [])

  const openHelp = useCallback(() => setHelpOpen(true), [])
  const closeHelp = useCallback(() => setHelpOpen(false), [])

  const startTour = useCallback(() => {
    setWelcomeOpen(false)
    setTourOpen(true)
  }, [])

  return {
    done,
    welcomeOpen,
    helpOpen,
    tourOpen,
    markDone,
    resetTutorial,
    openHelp,
    closeHelp,
    startTour,
    setTourOpen,
    setWelcomeOpen,
  }
}

function TutorialStepsList({ onNavigate }) {
  return (
    <div>
      {FINANCE_TUTORIAL_STEPS.map((step, i) => (
        <div key={step.title} className="finance-tutorial-step">
          <Title level={5} style={{ marginBottom: 4 }}>
            <span className="finance-tutorial-step__num">{i + 1}</span>
            {step.title}
          </Title>
          <Paragraph type="secondary" style={{ marginBottom: 8, paddingLeft: 34 }}>
            {step.description}
          </Paragraph>
          {step.path && onNavigate && (
            <Button type="link" size="small" style={{ paddingLeft: 34 }}
              onClick={() => onNavigate(step.path)}>
              Open this section →
            </Button>
          )}
        </div>
      ))}
    </div>
  )
}

export function FinanceTutorialWelcome({ open, onStartTour, onSkip }) {
  return (
    <Drawer
      title={<><RocketOutlined style={{ color: '#0d9488', marginRight: 8 }} />Welcome to Finance</>}
      placement="right"
      width={400}
      open={open}
      onClose={onSkip}
      footer={
        <Space style={{ width: '100%', justifyContent: 'flex-end' }}>
          <Button onClick={onSkip}>Skip for now</Button>
          <Button type="primary" onClick={onStartTour} style={{ background: '#0d9488' }}>
            Quick tour
          </Button>
        </Space>
      }
    >
      <Paragraph>
        The Finance portal is designed to be <Text strong>simple and separate</Text> from daily factory operations.
      </Paragraph>
      <Paragraph type="secondary">
        In a few steps you will learn where to manage accounts, post journals, and view reports.
      </Paragraph>
      <TutorialStepsList />
    </Drawer>
  )
}

export function FinanceTutorialHelp({ open, onClose, onNavigate, onRestart }) {
  return (
    <Drawer
      title={<><BookOutlined style={{ color: '#0d9488', marginRight: 8 }} />Finance guide</>}
      placement="right"
      width={420}
      open={open}
      onClose={onClose}
      extra={
        <Button type="link" size="small" onClick={onRestart}>
          Restart tutorial
        </Button>
      }
    >
      <TutorialStepsList onNavigate={(path) => { onClose(); onNavigate(path) }} />
    </Drawer>
  )
}

export function FinanceHelpButton({ onClick }) {
  return (
    <Button
      type="text"
      icon={<QuestionCircleOutlined />}
      onClick={onClick}
      aria-label="Finance help and tutorial"
    >
      Help
    </Button>
  )
}

export function FinanceNavTour({ open, onClose, steps }) {
  return (
    <Tour
      open={open}
      onClose={onClose}
      steps={steps}
      indicatorsRender={(current, total) => (
        <span style={{ color: '#64748b', fontSize: 12 }}>{current + 1} / {total}</span>
      )}
    />
  )
}
