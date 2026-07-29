import { FINANCE_TUTORIAL } from '../../content/tutorialContent'
import {
  usePortalTutorial,
  PortalTutorialWelcome,
  PortalTutorialHelp,
  PortalHelpButton,
  PortalNavTour,
  FinancePostingTable,
} from '../tutorial/PortalTutorial'

export function useFinanceTutorial() {
  return usePortalTutorial(FINANCE_TUTORIAL.storageKey)
}

export function FinanceTutorialWelcome(props) {
  return <PortalTutorialWelcome {...props} config={FINANCE_TUTORIAL} />
}

export function FinanceTutorialHelp({ open, onClose, onNavigate, onRestart, onWatchTour }) {
  return (
    <PortalTutorialHelp
      open={open}
      onClose={onClose}
      onNavigate={onNavigate}
      onRestart={onRestart}
      onWatchTour={onWatchTour}
      config={FINANCE_TUTORIAL}
      guidePath="/guide?tab=finance"
      extraContent={<FinancePostingTable rows={FINANCE_TUTORIAL.postingTable} />}
    />
  )
}

export { PortalHelpButton as FinanceHelpButton, PortalNavTour as FinanceNavTour }
