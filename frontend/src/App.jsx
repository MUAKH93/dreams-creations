import { useState, useEffect, useMemo } from 'react'
import { Routes, Route, Navigate, useNavigate, useLocation, Outlet } from 'react-router-dom'
import { Layout, Menu, Typography, Avatar, Dropdown, Drawer, Button, Grid } from 'antd'
import {
  DashboardOutlined, ShoppingOutlined, TeamOutlined,
  AlertOutlined, UserOutlined, LogoutOutlined, MenuOutlined,
  AppstoreOutlined, SendOutlined, FileTextOutlined,
  PictureOutlined, InboxOutlined, CheckSquareOutlined, SafetyCertificateOutlined,
  SettingOutlined, SolutionOutlined,
  IdcardOutlined, BookOutlined, AccountBookOutlined,
} from '@ant-design/icons'
import { useAuth } from './context/AuthContext'
import ProtectedRoute from './routes/ProtectedRoute'
import { MANAGEMENT_ROLES, ROLES, portalLabel, homeForRole } from './utils/roles'
import { financeModuleEnabled } from './config/modules'
import BackToModuleHub from './components/BackToModuleHub'

// Pages
import LoginPage          from './pages/auth/LoginPage'
import RegisterPage       from './pages/auth/RegisterPage'
import ForgotPasswordPage from './pages/auth/ForgotPasswordPage'
import ResetPasswordPage  from './pages/auth/ResetPasswordPage'
import DashboardPage      from './pages/dashboard/DashboardPage'
import AlertsPage         from './pages/dashboard/AlertsPage'
import BatchesPage        from './pages/production/BatchesPage'
import DispatchPage       from './pages/production/DispatchPage'
import CustomersPage      from './pages/sales/CustomersPage'
import BillsPage          from './pages/sales/BillsPage'
import QuotationsPage     from './pages/sales/QuotationsPage'
import MyOrdersPage       from './pages/sales/MyOrdersPage'
import MyQuotesPage       from './pages/sales/MyQuotesPage'
import DesignsCatalogPage from './pages/catalog/DesignsCatalogPage'
import InventoryPage      from './pages/inventory/InventoryPage'
import AssignmentsPage    from './pages/production/AssignmentsPage'
import StaffPage          from './pages/admin/StaffPage'
import SetupPage          from './pages/admin/SetupPage'
import ProfilePage        from './pages/profile/ProfilePage'
import VerifyEmailPage    from './pages/auth/VerifyEmailPage'
import FinanceHomePage    from './pages/finance/FinanceHomePage'
import FinanceLayout      from './layouts/FinanceLayout'
import ChartOfAccountsPage from './pages/finance/ChartOfAccountsPage'
import JournalEntriesPage from './pages/finance/JournalEntriesPage'
import FinanceReportsPage from './pages/finance/FinanceReportsPage'
import FinancePayablesPage from './pages/finance/FinancePayablesPage'
import FinanceBankPage from './pages/finance/FinanceBankPage'
import BackendStatus from './components/BackendStatus'
import SessionCheck from './components/SessionCheck'
import BrandLogo from './components/BrandLogo'
import { profileAPI } from './api/profile'
import { modulesAPI } from './api/modules'
import TutorialGuidePage from './pages/TutorialGuidePage'
import ModuleHubPage from './pages/home/ModuleHubPage'
import {
  useOperationsTutorial,
  OperationsTutorialWelcome,
  OperationsTutorialHelp,
  OperationsHelpButton,
  OperationsNavTour,
  buildOperationsTourSteps,
} from './components/tutorial/OperationsTutorial'
import './styles/module-hub.css'

const { Sider, Content, Header } = Layout
const { Text } = Typography
const { useBreakpoint } = Grid

function SidebarBrand({ role }) {
  return (
    <div className="app-sider-brand">
      <BrandLogo variant="sidebar" />
      <Text style={{ color: 'rgba(255,255,255,0.65)', fontSize: 11, display: 'block', marginTop: 8, textAlign: 'center' }}>
        {portalLabel(role)}
      </Text>
    </div>
  )
}

function tourIdForPath(path) {
  return `ops-${(path || '').replace(/^\//, '').replace(/\//g, '-') || 'home'}`
}

function NavMenu({ items, selectedKey, onNavigate }) {
  return (
    <Menu
      theme="dark"
      mode="inline"
      selectedKeys={[selectedKey]}
      style={{ background: '#1a237e', borderRight: 0 }}
      items={items.map(item => ({
        ...item,
        label: <span data-tour={tourIdForPath(item.key)}>{item.label}</span>,
      }))}
      onClick={({ key }) => onNavigate(key)}
    />
  )
}

const PROFILE_ITEM = { key: '/profile', icon: <IdcardOutlined />, label: 'My Profile' }

const MANAGER_MENU = [
  { key: '/dashboard',  icon: <DashboardOutlined />, label: 'Dashboard' },
  { key: '/batches',    icon: <AppstoreOutlined />,  label: 'Production' },
  { key: '/dispatch',   icon: <SendOutlined />,       label: 'Dispatch' },
  { key: '/inventory',  icon: <InboxOutlined />,        label: 'Inventory' },
  { key: '/designs',    icon: <PictureOutlined />,    label: 'Designs' },
  { key: '/customers',  icon: <TeamOutlined />,       label: 'Customers' },
  { key: '/quotations', icon: <SolutionOutlined />,   label: 'Quotations' },
  { key: '/bills',      icon: <FileTextOutlined />,   label: 'Bills' },
  { key: '/alerts',     icon: <AlertOutlined />,      label: 'Alerts' },
  PROFILE_ITEM,
]

const ADMIN_MENU = [
  ...MANAGER_MENU.slice(0, -1),
  { key: '/staff', icon: <SafetyCertificateOutlined />, label: 'Staff' },
  { key: '/setup', icon: <SettingOutlined />, label: 'Setup' },
  PROFILE_ITEM,
]

const CUSTOMER_MENU = [
  { key: '/dashboard',  icon: <DashboardOutlined />,  label: 'Dashboard' },
  { key: '/designs',    icon: <PictureOutlined />,    label: 'Designs Catalog' },
  { key: '/my-quotes',  icon: <SolutionOutlined />,   label: 'My Quotes' },
  { key: '/my-orders',  icon: <FileTextOutlined />,   label: 'My Bills' },
  PROFILE_ITEM,
]

const SUPERVISOR_MENU = [
  { key: '/dashboard',   icon: <DashboardOutlined />,   label: 'Dashboard' },
  { key: '/assignments', icon: <CheckSquareOutlined />, label: 'My Assignments' },
  { key: '/designs',     icon: <PictureOutlined />,    label: 'Designs Catalog' },
  PROFILE_ITEM,
]

const FINANCE_MENU_ITEM = {
  key: '/finance',
  icon: <AccountBookOutlined />,
  label: 'Finance Portal',
}

function getMenu(role, { showFinance = false } = {}) {
  let items
  if (role === ROLES.ADMIN) items = [...ADMIN_MENU]
  else if (role === ROLES.MANAGER) items = [...MANAGER_MENU]
  else if (role === ROLES.CUSTOMER) items = [...CUSTOMER_MENU]
  else if (role === ROLES.SUPERVISOR) items = [...SUPERVISOR_MENU]
  else items = []

  if (showFinance && MANAGEMENT_ROLES.includes(role)) {
    const profileIdx = items.findIndex(i => i.key === '/profile')
    const insertAt = profileIdx >= 0 ? profileIdx : items.length
    items = [...items.slice(0, insertAt), FINANCE_MENU_ITEM, ...items.slice(insertAt)]
  }
  return items
}

function menuSelectedKey(pathname, items) {
  if (items.some(i => i.key === pathname)) return pathname
  if (pathname.startsWith('/finance')) return '/finance'
  return pathname
}

function AppLayout({ children }) {
  const { auth, logout, updateProfilePhoto } = useAuth()
  const navigate          = useNavigate()
  const location          = useLocation()
  const screens           = useBreakpoint()
  const isMobile          = !screens.lg
  const [drawerOpen, setDrawerOpen] = useState(false)
  const [showFinance, setShowFinance] = useState(financeModuleEnabled)
  const opsTutorial = useOperationsTutorial()

  useEffect(() => {
    modulesAPI.getFlags()
      .then(r => { if (r.data?.finance?.enabled) setShowFinance(true) })
      .catch(() => {})
  }, [])

  useEffect(() => {
    if (!auth?.token || auth?.profilePhotoUrl) return
    profileAPI.getMy()
      .then(r => {
        if (r.data?.profilePhotoUrl) updateProfilePhoto(r.data.profilePhotoUrl)
      })
      .catch(() => {})
  }, [auth?.token])

  const menuItems = useMemo(
    () => getMenu(auth?.role, { showFinance }),
    [auth?.role, showFinance],
  )
  const selectedMenuKey = menuSelectedKey(location.pathname, menuItems)
  const isManagement = MANAGEMENT_ROLES.includes(auth?.role)

  const userMenu = {
    items: [
      {
        key: 'guide', icon: <BookOutlined />, label: 'Tutorials & guide',
        onClick: () => navigate('/guide?tab=operations'),
      },
      {
        key: 'profile', icon: <IdcardOutlined />, label: 'My Profile',
        onClick: () => navigate('/profile'),
      },
      {
        key: 'logout', icon: <LogoutOutlined />, label: 'Logout',
        onClick: () => { logout(); navigate('/login') },
      },
    ],
  }

  const handleNavigate = (key) => {
    navigate(key)
    setDrawerOpen(false)
  }

  const opsTourSteps = buildOperationsTourSteps(opsTutorial.config)

  return (
    <Layout className="app-layout">
      {!isMobile && (
        <Sider theme="dark" width={220} className="app-sider">
          <div className="app-sider-inner">
            <SidebarBrand role={auth?.role} />
            {isManagement && <BackToModuleHub />}
            <div className="app-sider-menu">
              <NavMenu
                items={menuItems}
                selectedKey={selectedMenuKey}
                onNavigate={handleNavigate}
              />
            </div>
          </div>
        </Sider>
      )}

      <Layout>
        <Header className="app-header">
          {isMobile && (
            <Button
              type="text"
              icon={<MenuOutlined />}
              className="app-menu-trigger"
              onClick={() => setDrawerOpen(true)}
              aria-label="Open menu"
            />
          )}
          <div className="app-header-user">
            <span data-tour="ops-help" className="app-header-help">
              <OperationsHelpButton onClick={opsTutorial.openHelp} />
            </span>
            <Dropdown menu={userMenu} placement="bottomRight">
              <div className="app-header-user-trigger">
                <Avatar
                  src={auth?.profilePhotoUrl || undefined}
                  icon={<UserOutlined />}
                  style={{ background: '#1a237e' }}
                />
                <Text strong className="app-header-username">{auth?.username}</Text>
                <Text type="secondary" className="app-header-role">({auth?.role})</Text>
              </div>
            </Dropdown>
          </div>
        </Header>

        <Content className="app-content">
          <BackendStatus />
          {children}
        </Content>
      </Layout>

      <Drawer
        title={<SidebarBrand role={auth?.role} />}
        placement="left"
        onClose={() => setDrawerOpen(false)}
        open={drawerOpen}
        width={260}
        styles={{ body: { padding: 0, background: '#1a237e' }, header: { background: '#1a237e', borderBottom: '1px solid rgba(255,255,255,0.1)' } }}
        className="app-drawer"
      >
        {isManagement && <BackToModuleHub className="app-module-hub-back--drawer" />}
        <NavMenu
          items={menuItems}
          selectedKey={selectedMenuKey}
          onNavigate={handleNavigate}
        />
      </Drawer>

      <OperationsTutorialWelcome
        open={opsTutorial.welcomeOpen}
        config={opsTutorial.config}
        onStartTour={() => { opsTutorial.startTour(); opsTutorial.markDone() }}
        onSkip={opsTutorial.markDone}
      />
      <OperationsTutorialHelp
        open={opsTutorial.helpOpen}
        config={opsTutorial.config}
        onClose={opsTutorial.closeHelp}
        onNavigate={handleNavigate}
        onRestart={opsTutorial.resetTutorial}
        onWatchTour={opsTutorial.watchTourAgain}
      />
      <OperationsNavTour
        open={opsTutorial.tourOpen}
        onClose={() => { opsTutorial.setTourOpen(false); opsTutorial.markDone() }}
        steps={opsTourSteps}
      />
    </Layout>
  )
}

function RoleHome() {
  const { auth } = useAuth()
  if (!auth) return <Navigate to="/login" replace />
  return <Navigate to={homeForRole(auth.role)} replace />
}

export default function App() {
  const { auth } = useAuth()
  const [showFinance, setShowFinance] = useState(financeModuleEnabled)

  useEffect(() => {
    modulesAPI.getFlags()
      .then(r => {
        if (r.data?.finance?.enabled) setShowFinance(true)
      })
      .catch(() => {})
  }, [])

  return (
    <>
      {auth && <SessionCheck />}
      <Routes>
      {/* Public auth */}
      <Route path="/login" element={auth ? <RoleHome /> : <LoginPage />} />
      <Route path="/login/management" element={<Navigate to="/login" replace />} />
      <Route path="/login/supervisor" element={<Navigate to="/login" replace />} />
      <Route path="/login/customer" element={<Navigate to="/login" replace />} />
      <Route path="/register" element={auth ? <RoleHome /> : <RegisterPage />} />
      <Route path="/verify-email" element={<VerifyEmailPage />} />
      <Route path="/forgot-password" element={auth ? <RoleHome /> : <ForgotPasswordPage />} />
      <Route path="/reset-password" element={auth ? <RoleHome /> : <ResetPasswordPage />} />

      {/* In-app written tutorials — all authenticated roles */}
      <Route path="/guide" element={
        <ProtectedRoute roles={[ROLES.ADMIN, ROLES.MANAGER, ROLES.SUPERVISOR, ROLES.CUSTOMER]}>
          <AppLayout><TutorialGuidePage /></AppLayout>
        </ProtectedRoute>
      } />

      {/* Profile — all authenticated roles */}
      <Route path="/profile" element={
        <ProtectedRoute roles={[ROLES.ADMIN, ROLES.MANAGER, ROLES.SUPERVISOR, ROLES.CUSTOMER]}>
          <AppLayout><ProfilePage /></AppLayout>
        </ProtectedRoute>
      } />

      {/* Module hub — Admin & Manager choose Production vs Finance */}
      <Route path="/modules" element={
        <ProtectedRoute roles={MANAGEMENT_ROLES}>
          <AppLayout><ModuleHubPage /></AppLayout>
        </ProtectedRoute>
      } />

      {/* Dashboard — all roles, role-specific content inside */}
      <Route path="/dashboard" element={
        <ProtectedRoute roles={[ROLES.ADMIN, ROLES.MANAGER, ROLES.SUPERVISOR, ROLES.CUSTOMER]}>
          <AppLayout><DashboardPage /></AppLayout>
        </ProtectedRoute>
      } />

      {/* Management operations — Admin & Manager (shared UI) */}
      <Route path="/batches" element={
        <ProtectedRoute roles={MANAGEMENT_ROLES}>
          <AppLayout><BatchesPage /></AppLayout>
        </ProtectedRoute>
      } />
      <Route path="/dispatch" element={
        <ProtectedRoute roles={MANAGEMENT_ROLES}>
          <AppLayout><DispatchPage /></AppLayout>
        </ProtectedRoute>
      } />
      <Route path="/inventory" element={
        <ProtectedRoute roles={MANAGEMENT_ROLES}>
          <AppLayout><InventoryPage /></AppLayout>
        </ProtectedRoute>
      } />
      <Route path="/customers" element={
        <ProtectedRoute roles={MANAGEMENT_ROLES}>
          <AppLayout><CustomersPage /></AppLayout>
        </ProtectedRoute>
      } />
      <Route path="/bills" element={
        <ProtectedRoute roles={MANAGEMENT_ROLES}>
          <AppLayout><BillsPage /></AppLayout>
        </ProtectedRoute>
      } />
      <Route path="/quotations" element={
        <ProtectedRoute roles={MANAGEMENT_ROLES}>
          <AppLayout><QuotationsPage /></AppLayout>
        </ProtectedRoute>
      } />
      <Route path="/reports" element={<Navigate to={showFinance ? '/finance/reports' : '/dashboard'} replace />} />
      <Route path="/analytics" element={<Navigate to="/dashboard" replace />} />
      <Route path="/activity" element={<Navigate to="/dashboard" replace />} />
      <Route path="/alerts" element={
        <ProtectedRoute roles={MANAGEMENT_ROLES}>
          <AppLayout><AlertsPage /></AppLayout>
        </ProtectedRoute>
      } />

      {showFinance && (
        <Route path="/finance" element={
          <ProtectedRoute roles={MANAGEMENT_ROLES}>
            <FinanceLayout />
          </ProtectedRoute>
        }>
          <Route index element={<FinanceHomePage />} />
          <Route path="accounts" element={<ChartOfAccountsPage />} />
          <Route path="journals" element={<JournalEntriesPage />} />
          <Route path="payables" element={<FinancePayablesPage />} />
          <Route path="bank" element={<FinanceBankPage />} />
          <Route path="reports" element={<FinanceReportsPage />} />
        </Route>
      )}

      {/* Admin-only setup (Manager has no separate portal; these are Admin-only) */}
      <Route path="/staff" element={
        <ProtectedRoute roles={[ROLES.ADMIN]}>
          <AppLayout><StaffPage /></AppLayout>
        </ProtectedRoute>
      } />
      <Route path="/setup" element={
        <ProtectedRoute roles={[ROLES.ADMIN]}>
          <AppLayout><SetupPage /></AppLayout>
        </ProtectedRoute>
      } />

      {/* Supervisor portal */}
      <Route path="/assignments" element={
        <ProtectedRoute roles={[ROLES.SUPERVISOR]}>
          <AppLayout><AssignmentsPage /></AppLayout>
        </ProtectedRoute>
      } />

      {/* Designs — management, supervisor (read), customer (browse) */}
      <Route path="/designs" element={
        <ProtectedRoute roles={[ROLES.ADMIN, ROLES.MANAGER, ROLES.CUSTOMER, ROLES.SUPERVISOR]}>
          <AppLayout><DesignsCatalogPage /></AppLayout>
        </ProtectedRoute>
      } />

      {/* Customer portal */}
      <Route path="/my-orders" element={
        <ProtectedRoute roles={[ROLES.CUSTOMER]}>
          <AppLayout><MyOrdersPage /></AppLayout>
        </ProtectedRoute>
      } />
      <Route path="/my-quotes" element={
        <ProtectedRoute roles={[ROLES.CUSTOMER]}>
          <AppLayout><MyQuotesPage /></AppLayout>
        </ProtectedRoute>
      } />

      <Route path="/" element={<RoleHome />} />

      <Route path="/unauthorized" element={
        <div style={{ textAlign: 'center', padding: 80 }}>
          <h2>403 — Access Denied</h2>
          <p>You do not have permission to view this page for your role.</p>
        </div>
      } />
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
    </>
  )
}
