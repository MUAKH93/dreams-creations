import { useCallback, useEffect, useState } from 'react'
import { Drawer, Button, Typography, Space, Tour, Collapse, Table, Divider } from 'antd'
import {
  QuestionCircleOutlined, BookOutlined, RocketOutlined, ReloadOutlined, PlayCircleOutlined,
} from '@ant-design/icons'
import { useNavigate } from 'react-router-dom'

const { Title, Paragraph, Text } = Typography

export function usePortalTutorial(storageKey) {
  const [done, setDone] = useState(() => localStorage.getItem(storageKey) === '1')
  const [welcomeOpen, setWelcomeOpen] = useState(false)
  const [helpOpen, setHelpOpen] = useState(false)
  const [tourOpen, setTourOpen] = useState(false)

  useEffect(() => {
    if (!done) {
      setWelcomeOpen(true)
    }
  }, [done])

  const markDone = useCallback(() => {
    localStorage.setItem(storageKey, '1')
    setDone(true)
    setWelcomeOpen(false)
    setTourOpen(false)
  }, [storageKey])

  const resetTutorial = useCallback(() => {
    localStorage.removeItem(storageKey)
    setDone(false)
    setWelcomeOpen(true)
    setHelpOpen(false)
  }, [storageKey])

  const openHelp = useCallback(() => setHelpOpen(true), [])
  const closeHelp = useCallback(() => setHelpOpen(false), [])

  const startTour = useCallback(() => {
    setWelcomeOpen(false)
    setHelpOpen(false)
    setTourOpen(true)
  }, [])

  const watchTourAgain = useCallback(() => {
    setHelpOpen(false)
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
    watchTourAgain,
    setTourOpen,
    setWelcomeOpen,
  }
}

function TutorialSectionList({ sections, onNavigate, themeColor }) {
  return (
    <Collapse
      accordion
      bordered={false}
      className="portal-tutorial-collapse"
      items={sections.map((section, i) => ({
        key: section.id,
        label: (
          <Text strong>
            <span className="portal-tutorial-step-num" style={{ background: themeColor }}>{i + 1}</span>
            {section.title}
          </Text>
        ),
        children: (
          <div className="portal-tutorial-section-body">
            <Paragraph style={{ marginBottom: 8 }}>{section.summary}</Paragraph>
            <ul className="portal-tutorial-list">
              {section.details.map(line => (
                <li key={line}>{line}</li>
              ))}
            </ul>
            {section.tips?.length > 0 && (
              <>
                <Text type="secondary" style={{ fontSize: 12 }}>Tips</Text>
                <ul className="portal-tutorial-list portal-tutorial-list--tips">
                  {section.tips.map(tip => (
                    <li key={tip}>{tip}</li>
                  ))}
                </ul>
              </>
            )}
            {section.path && onNavigate && (
              <Button type="link" size="small" style={{ paddingLeft: 0, color: themeColor }}
                onClick={() => onNavigate(section.path)}>
                Open {section.title} →
              </Button>
            )}
          </div>
        ),
      }))}
    />
  )
}

export function PortalTutorialWelcome({
  open, onStartTour, onSkip, config,
}) {
  return (
    <Drawer
      title={<><RocketOutlined style={{ color: config.themeColor, marginRight: 8 }} />{config.welcomeTitle}</>}
      placement="right"
      width={440}
      open={open}
      onClose={onSkip}
      className="portal-tutorial-drawer"
      footer={
        <Space style={{ width: '100%', justifyContent: 'flex-end' }}>
          <Button onClick={onSkip}>Skip for now</Button>
          <Button type="primary" onClick={onStartTour} style={{ background: config.themeColor }}>
            Start quick tour
          </Button>
        </Space>
      }
    >
      <Paragraph>{config.intro}</Paragraph>
      <Paragraph type="secondary" style={{ fontSize: 13 }}>
        You can reopen this guide anytime from the <Text strong>Help</Text> button in the header.
      </Paragraph>
      <TutorialSectionList sections={config.sections} themeColor={config.themeColor} />
    </Drawer>
  )
}

export function PortalTutorialHelp({
  open, onClose, onNavigate, onRestart, onWatchTour, config, guidePath, extraContent,
}) {
  const navigate = useNavigate()

  return (
    <Drawer
      title={<><BookOutlined style={{ color: config.themeColor, marginRight: 8 }} />{config.guideTitle}</>}
      placement="right"
      width={480}
      open={open}
      onClose={onClose}
      className="portal-tutorial-drawer"
      extra={
        <Space size="small">
          <Button type="link" size="small" icon={<PlayCircleOutlined />} onClick={onWatchTour}>
            Watch tour again
          </Button>
          <Button type="link" size="small" icon={<ReloadOutlined />} onClick={onRestart}>
            Reset &amp; show welcome
          </Button>
        </Space>
      }
      footer={
        guidePath ? (
          <Button block type="primary" style={{ background: config.themeColor }}
            onClick={() => { onClose(); navigate(guidePath) }}>
            Open full written guide
          </Button>
        ) : null
      }
    >
      <Paragraph type="secondary">{config.intro}</Paragraph>
      <TutorialSectionList
        sections={config.sections}
        onNavigate={(path) => { onClose(); onNavigate(path) }}
        themeColor={config.themeColor}
      />
      {extraContent}
    </Drawer>
  )
}

export function PortalHelpButton({ onClick, label = 'Help' }) {
  return (
    <Button
      type="text"
      icon={<QuestionCircleOutlined />}
      onClick={onClick}
      aria-label="Help and tutorial"
    >
      {label}
    </Button>
  )
}

export function PortalNavTour({ open, onClose, steps, themeColor = '#0d9488' }) {
  return (
    <Tour
      open={open}
      onClose={onClose}
      steps={steps}
      indicatorsRender={(current, total) => (
        <span style={{ color: '#64748b', fontSize: 12 }}>{current + 1} / {total}</span>
      )}
      mask={{ color: 'rgba(15, 23, 42, 0.45)' }}
      arrow={{ pointAtCenter: true }}
      type="primary"
      rootClassName="portal-nav-tour"
    />
  )
}

export function FinancePostingTable({ rows }) {
  if (!rows?.length) return null
  return (
    <>
      <Divider orientation="left" plain style={{ marginTop: 24 }}>Auto-posting reference</Divider>
      <Table
        size="small"
        pagination={false}
        dataSource={rows.map((r, i) => ({ ...r, key: i }))}
        columns={[
          { title: 'Event', dataIndex: 'event', key: 'event' },
          { title: 'Debit', dataIndex: 'debit', key: 'debit', width: 130 },
          { title: 'Credit', dataIndex: 'credit', key: 'credit', width: 130 },
        ]}
      />
    </>
  )
}

export function TutorialGuideSection({ section, index, themeColor, onNavigate }) {
  return (
    <div className="tutorial-guide-section" id={`section-${section.id}`}>
      <Title level={4}>
        <span className="portal-tutorial-step-num" style={{ background: themeColor }}>{index + 1}</span>
        {section.title}
      </Title>
      <Paragraph>{section.summary}</Paragraph>
      <ul className="portal-tutorial-list">
        {section.details.map(line => (
          <li key={line}>{line}</li>
        ))}
      </ul>
      {section.tips?.length > 0 && (
        <>
          <Text strong type="secondary">Tips</Text>
          <ul className="portal-tutorial-list portal-tutorial-list--tips">
            {section.tips.map(tip => (
              <li key={tip}>{tip}</li>
            ))}
          </ul>
        </>
      )}
      {section.path && onNavigate && (
        <Button type="primary" ghost size="small" style={{ borderColor: themeColor, color: themeColor }}
          onClick={() => onNavigate(section.path)}>
          Go to {section.title}
        </Button>
      )}
    </div>
  )
}
