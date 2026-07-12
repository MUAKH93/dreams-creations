import { useState } from 'react'
import { Routes, Route, Navigate, useNavigate, useLocation } from 'react-router-dom'
import { Layout, Menu, Typography, Avatar, Dropdown, Drawer, Button, Grid } from 'antd'
import {
  DashboardOutlined, ShoppingOutlined, TeamOutlined,
  AlertOutlined, UserOutlined, LogoutOutlined, MenuOutlined,
  AppstoreOutlined, SendOutlined, FileTextOutlined,
  PictureOutlined, InboxOutlined, CheckSquareOutlined, SafetyCertificateOutlined,
  SettingOutlined, BarChartOutlined, HistoryOutlined, SolutionOutlined,
} from '@ant-design/icons'
import { useAuth } from './context/AuthContext'
import ProtectedRoute from './routes/ProtectedRoute'
import { MANAGEMENT_ROLES, ROLES, portalLabel, homeForRole } from './utils/roles'

// Pages
import LoginPage          from './pages/auth/LoginPage'
import PortalLoginPage    from './pages/auth/PortalLoginPage'
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
import BackendStatus from './components/BackendStatus'
import SessionCheck from './components/SessionCheck'

const { Sider, Content, Header } = Layout
const { Text } = Typography
const { useBreakpoint } = Grid

function SidebarBrand({ role }) {
  return (
    <div className="app-sider-brand">
      <Text strong style={{ color: '#fff', fontSize: 16 }}>Dreams Creations</Text>
      <br />
      <Text style={{ color: 'rgba(255,255,255,0.5)', fontSize: 11 }}>
        {portalLabel(role)}
      </Text>
    </div>
  )
}

function NavMenu({ items, selectedKey, onNavigate }) {
  return (
    <Menu
      theme="dark"
      mode="inline"
      selectedKeys={[selectedKey]}
      style={{ background: '#1a237e', borderRight: 0 }}
      items={items}
      onClick={({ key }) => onNavigate(key)}
    />
  )
}

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
  { key: '/activity',   icon: <HistoryOutlined />,    label: 'Activity Log' },
  { key: '/alerts',     icon: <AlertOutlined />,      label: 'Alerts' },
]

const ADMIN_MENU = [
  ...MANAGER_MENU,
  { key: '/staff', icon: <SafetyCertificateOutlined />, label: 'Staff & Supervisors' },
  { key: '/setup', icon: <SettingOutlined />, label: 'Factory Setup' },
]

const CUSTOMER_MENU = [
  { key: '/dashboard',  icon: <DashboardOutlined />,  label: 'Dashboard' },
  { key: '/designs',    icon: <PictureOutlined />,    label: 'Designs Catalog' },
  { key: '/my-quotes',  icon: <SolutionOutlined />,   label: 'My Quotes' },
  { key: '/my-orders',  icon: <FileTextOutlined />,   label: 'My Bills' },
]

const SUPERVISOR_MENU = [
  { key: '/dashboard',   icon: <DashboardOutlined />,   label: 'Dashboard' },
  { key: '/assignments', icon: <CheckSquareOutlined />, label: 'My Assignments' },
  { key: '/designs',     icon: <PictureOutlined />,    label: 'Designs Catalog' },
]

function getMenu(role) {
  if (role === ROLES.ADMIN) return ADMIN_MENU
  if (role === ROLES.MANAGER) return MANAGER_MENU
  if (role === ROLES.CUSTOMER) return CUSTOMER_MENU
  if (role === ROLES.SUPERVISOR) return SUPERVISOR_MENU
  return []
}

function AppLayout({ children }) {
  const { auth, logout } = useAuth()
  const navigate          = useNavigate()
  const location          = useLocation()
  const screens           = useBreakpoint()
  const isMobile          = !screens.lg
  const [drawerOpen, setDrawerOpen] = useState(false)

  const menuItems = getMenu(auth?.role)

  const userMenu = {
    items: [{
      key: 'logout', icon: <LogoutOutlined />, label: 'Logout',
      onClick: () => { logout(); navigate('/login') }
    }]
  }

  const handleNavigate = (key) => {
    navigate(key)
    setDrawerOpen(false)
  }

  return (
    <Layout className="app-layout">
      {!isMobile && (
        <Sider theme="dark" width={220} className="app-sider">
          <SidebarBrand role={auth?.role} />
          <NavMenu
            items={menuItems}
            selectedKey={location.pathname}
            onNavigate={handleNavigate}
          />
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
            <Dropdown menu={userMenu} placement="bottomRight">
              <div className="app-header-user-trigger">
                <Avatar icon={<UserOutlined />} style={{ background: '#1a237e' }} />
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
      </Drawer>
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

  return (
    <>
      {auth && <SessionCheck />}
      <Routes>
      {/* Public — separate login per portal; Manager shares Management with Admin */}
      <Route path="/login" element={auth ? <RoleHome /> : <LoginPage />} />
      <Route path="/login/management" element={
        auth ? <RoleHome /> : <PortalLoginPage portalKey="management" />
      } />
      <Route path="/login/supervisor" element={
        auth ? <RoleHome /> : <PortalLoginPage portalKey="supervisor" />
      } />
      <Route path="/login/customer" element={
        auth ? <RoleHome /> : <PortalLoginPage portalKey="customer" />
      } />
      <Route path="/register" element={auth ? <RoleHome /> : <RegisterPage />} />
      <Route path="/forgot-password" element={auth ? <RoleHome /> : <ForgotPasswordPage />} />
      <Route path="/reset-password" element={auth ? <RoleHome /> : <ResetPasswordPage />} />

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
