import { useMemo } from 'react'
import { useAuth } from '../../context/AuthContext'
import { getOperationsTutorialForRole } from '../../content/tutorialContent'
import {
  usePortalTutorial,
  PortalTutorialWelcome,
  PortalTutorialHelp,
  PortalHelpButton,
  PortalNavTour,
} from './PortalTutorial'

export function useOperationsTutorial() {
  const { auth } = useAuth()
  const config = useMemo(() => getOperationsTutorialForRole(auth?.role), [auth?.role])
  const tutorial = usePortalTutorial(config.storageKey)
  return { ...tutorial, config }
}

export function OperationsTutorialWelcome({ open, onStartTour, onSkip, config }) {
  return (
    <PortalTutorialWelcome
      open={open}
      onStartTour={onStartTour}
      onSkip={onSkip}
      config={config}
    />
  )
}

export function OperationsTutorialHelp({
  open, onClose, onNavigate, onRestart, onWatchTour, config,
}) {
  return (
    <PortalTutorialHelp
      open={open}
      onClose={onClose}
      onNavigate={onNavigate}
      onRestart={onRestart}
      onWatchTour={onWatchTour}
      config={config}
      guidePath="/guide?tab=operations"
    />
  )
}

export { PortalHelpButton as OperationsHelpButton, PortalNavTour as OperationsNavTour }

export function buildOperationsTourSteps(config, showFinance) {
  const steps = config.sections
    .filter(s => s.path && s.path !== '/finance')
    .map(section => ({
      title: section.title,
      description: section.summary,
      target: () => document.querySelector(`[data-tour="ops-${section.path.replace(/^\//, '').replace(/\//g, '-')}"]`),
    }))

  if (showFinance) {
    steps.push({
      title: 'Finance Portal',
      description: 'Open the separate accounting workspace from this button.',
      target: () => document.querySelector('[data-tour="ops-finance-portal"]'),
    })
  }

  steps.push({
    title: 'Help anytime',
    description: 'Click Help in the header to read the full guide or watch the tour again.',
    target: () => document.querySelector('[data-tour="ops-help"]'),
  })

  return steps
}
