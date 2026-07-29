import { useState, useEffect } from 'react'
import { Routes, Route, Navigate, useNavigate, useLocation, Outlet } from 'react-router-dom'
import { Layout, Menu, Typography, Avatar, Dropdown, Drawer, Button, Grid } from 'antd'
import {
  DashboardOutlined, ShoppingOutlined, TeamOutlined,
  AlertOutlined, UserOutlined, LogoutOutlined, MenuOutlined,
  AppstoreOutlined, SendOutlined, FileTextOutlined,
  PictureOutlined, InboxOutlined, CheckSquareOutlined, SafetyCertificateOutlined,
  SettingOutlined,   BarChartOutlined, HistoryOutlined, SolutionOutlined, LineChartOutlined,
  IdcardOutlined, AccountBookOutlined, BookOutlined,
} from '@ant-design/icons'
import { useAuth } from './context/AuthContext'
import ProtectedRoute from './routes/ProtectedRoute'
import { MANAGEMENT_ROLES, ROLES, portalLabel, homeForRole } from './utils/roles'
import { useModuleFlags } from './hooks/useModuleFlags'

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
import ActivityLogPage    from './pages/admin/ActivityLogPage'
import ReportsPage        from './pages/reports/ReportsPage'
import AnalyticsPage      from './pages/analytics/AnalyticsPage'
import ProfilePage        from './pages/profile/ProfilePage'
import VerifyEmailPage    from './pages/auth/VerifyEmailPage'
import FinanceHomePage    from './pages/finance/FinanceHomePage'
import FinanceLayout      from './layouts/FinanceLayout'
import ChartOfAccountsPage from './pages/finance/ChartOfAccountsPage'
import JournalEntriesPage from './pages/finance/JournalEntriesPage'
import FinanceReportsPage from './pages/finance/FinanceReportsPage'
import FinancePayablesPage from './pages/finance/FinancePayablesPage'
import FinanceBankPage from './pages/finance/FinanceBankPage'
import ShopHomePage from './pages/shop/ShopHomePage'
import ShopSettingsPage from './pages/shop/ShopSettingsPage'
import ShopCatalogPage from './pages/shop/ShopCatalogPage'
import ShopStorefrontPage from './pages/shop/ShopStorefrontPage'
import ShopDesignDetailPage from './pages/shop/ShopDesignDetailPage'
import ShopCartPage from './pages/shop/ShopCartPage'
import ShopCheckoutPage from './pages/shop/ShopCheckoutPage'
import ShopOrdersPage from './pages/shop/ShopOrdersPage'
import ShopLayout from './layouts/ShopLayout'
import BackendStatus from './components/BackendStatus'
import SessionCheck from './components/SessionCheck'
import BrandLogo from './components/BrandLogo'
import { profileAPI } from './api/profile'
import TutorialGuidePage from './pages/TutorialGuidePage'
import {
  useOperationsTutorial,
  OperationsTutorialWelcome,
  OperationsTutorialHelp,
  OperationsHelpButton,
  OperationsNavTour,
  buildOperationsTourSteps,
} from './components/tutorial/OperationsTutorial'

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
  { key: '/batches',    icon: <AppstoreOutlined />,  label: 'Production Batches' },
  { key: '/dispatch',   icon: <SendOutlined />,       label: 'Dispatch Management' },
  { key: '/inventory',  icon: <InboxOutlined />,        label: 'Inventory' },
  { key: '/designs',    icon: <PictureOutlined />,    label: 'Designs Catalog' },
  { key: '/customers',  icon: <TeamOutlined />,       label: 'Customers' },
  { key: '/quotations', icon: <SolutionOutlined />,   label: 'Quotations' },
  { key: '/bills',      icon: <FileTextOutlined />,   label: 'Bills & Payments' },
  { key: '/reports',    icon: <BarChartOutlined />,   label: 'Reports' },
  { key: '/analytics',  icon: <LineChartOutlined />,  label: 'Analytics' },
  { key: '/activity',   icon: <HistoryOutlined />,    label: 'Activity Log' },
  { key: '/alerts',     icon: <AlertOutlined />,      label: 'Alerts' },
  PROFILE_ITEM,
]

const ADMIN_MENU = [
  ...MANAGER_MENU,
  { key: '/staff', icon: <SafetyCertificateOutlined />, label: 'Staff & Supervisors' },
  { key: '/setup', icon: <SettingOutlined />, label: 'Factory Setup' },
]

const CUSTOMER_SHOP_ITEM = { key: '/store', icon: <ShoppingOutlined />, label: 'Online Shop' }

const CUSTOMER_MENU_BASE = [
  { key: '/dashboard',  icon: <DashboardOutlined />,  label: 'Dashboard' },
  { key: '/designs',    icon: <PictureOutlined />,    label: 'Designs Catalog' },
  { key: '/my-quotes',  icon: <SolutionOutlined />,   label: 'My Quotes' },
  { key: '/my-orders',  icon: <FileTextOutlined />,   label: 'My Bills' },
]

const CUSTOMER_MENU = [...CUSTOMER_MENU_BASE, PROFILE_ITEM]

const SUPERVISOR_MENU = [
  { key: '/dashboard',   icon: <DashboardOutlined />,   label: 'Dashboard' },
  { key: '/assignments', icon: <CheckSquareOutlined />, label: 'My Assignments' },
  { key: '/designs',     icon: <PictureOutlined />,    label: 'Designs Catalog' },
  PROFILE_ITEM,
]

function getMenu(role, showShop = false) {
  if (role === ROLES.ADMIN) return ADMIN_MENU
  if (role === ROLES.MANAGER) return MANAGER_MENU
  if (role === ROLES.CUSTOMER) {
    if (!showShop) return CUSTOMER_MENU
    return [...CUSTOMER_MENU_BASE, CUSTOMER_SHOP_ITEM, PROFILE_ITEM]
  }
  if (role === ROLES.SUPERVISOR) return SUPERVISOR_MENU
  return []
}

function ShopPortalButton({ showShop }) {
  const navigate = useNavigate()
  if (!showShop) return null
  return (
    <div className="ops-finance-portal-btn" data-tour="ops-shop-portal">
      <Button
        block
        size="large"
        icon={<ShoppingOutlined />}
        className="ops-finance-portal-btn__inner"
        style={{ background: '#ea580c', borderColor: '#ea580c' }}
        onClick={() => navigate('/shop')}
      >
        Open Shop Portal
      </Button>
      <Text style={{ color: 'rgba(255,255,255,0.45)', fontSize: 10, display: 'block', textAlign: 'center', marginTop: 6 }}>
        Online storefront workspace
      </Text>
    </div>
  )
}

function FinancePortalButton({ showFinance }) {
  const navigate = useNavigate()
  if (!showFinance) return null
  return (
    <div className="ops-finance-portal-btn" data-tour="ops-finance-portal">
      <Button
        block
        size="large"
        icon={<AccountBookOutlined />}
        className="ops-finance-portal-btn__inner"
        onClick={() => navigate('/finance')}
      >
        Open Finance Portal
      </Button>
      <Text style={{ color: 'rgba(255,255,255,0.45)', fontSize: 10, display: 'block', textAlign: 'center', marginTop: 6 }}>
        Separate accounting workspace
      </Text>
    </div>
  )
}

function AppLayout({ children }) {
  const { auth, logout, updateProfilePhoto } = useAuth()
  const navigate          = useNavigate()
  const location          = useLocation()
  const screens           = useBreakpoint()
  const isMobile          = !screens.lg
  const [drawerOpen, setDrawerOpen] = useState(false)
  const { showFinance, showShop } = useModuleFlags()
  const opsTutorial = useOperationsTutorial()
  const isManagement = MANAGEMENT_ROLES.includes(auth?.role)
  const showFinancePortal = showFinance && isManagement
  const showShopPortal = showShop && isManagement

  useEffect(() => {
    if (!auth?.token || auth?.profilePhotoUrl) return
    profileAPI.getMy()
      .then(r => {
        if (r.data?.profilePhotoUrl) updateProfilePhoto(r.data.profilePhotoUrl)
      })
      .catch(() => {})
  }, [auth?.token])

  const menuItems = getMenu(auth?.role, showShop)

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

  const opsTourSteps = buildOperationsTourSteps(opsTutorial.config, showFinancePortal)

  return (
    <Layout className="app-layout">
      {!isMobile && (
        <Sider theme="dark" width={220} className="app-sider">
          <div className="app-sider-inner">
            <SidebarBrand role={auth?.role} />
            <div className="app-sider-menu">
              <NavMenu
                items={menuItems}
                selectedKey={location.pathname}
                onNavigate={handleNavigate}
              />
            </div>
            <FinancePortalButton showFinance={showFinancePortal} />
            <ShopPortalButton showShop={showShopPortal} />
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
        <NavMenu
          items={menuItems}
          selectedKey={location.pathname}
          onNavigate={handleNavigate}
        />
        <FinancePortalButton showFinance={showFinancePortal} />
        <ShopPortalButton showShop={showShopPortal} />
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
  const { showFinance, showShop } = useModuleFlags()
  const isManagement = auth?.role ? MANAGEMENT_ROLES.includes(auth.role) : false

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

      {/* Public storefront — always routed; page handles module disabled */}
      <Route path="/store" element={<ShopStorefrontPage />} />
      <Route path="/store/design/:designId" element={<ShopDesignDetailPage />} />
      <Route path="/store/cart" element={<ShopCartPage />} />
      <Route path="/store/checkout" element={<ShopCheckoutPage />} />

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
      <Route path="/reports" element={
        <ProtectedRoute roles={MANAGEMENT_ROLES}>
          <AppLayout><ReportsPage /></AppLayout>
        </ProtectedRoute>
      } />
      <Route path="/analytics" element={
        <ProtectedRoute roles={MANAGEMENT_ROLES}>
          <AppLayout><AnalyticsPage /></AppLayout>
        </ProtectedRoute>
      } />
      <Route path="/activity" element={
        <ProtectedRoute roles={MANAGEMENT_ROLES}>
          <AppLayout><ActivityLogPage /></AppLayout>
        </ProtectedRoute>
      } />
      <Route path="/alerts" element={
        <ProtectedRoute roles={MANAGEMENT_ROLES}>
          <AppLayout><AlertsPage /></AppLayout>
        </ProtectedRoute>
      } />

      {showFinance && isManagement && (
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

      {showShop && isManagement && (
        <Route path="/shop" element={
          <ProtectedRoute roles={MANAGEMENT_ROLES}>
            <ShopLayout />
          </ProtectedRoute>
        }>
          <Route index element={<ShopHomePage />} />
          <Route path="settings" element={<ShopSettingsPage />} />
          <Route path="catalog" element={<ShopCatalogPage />} />
          <Route path="orders" element={<ShopOrdersPage />} />
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
