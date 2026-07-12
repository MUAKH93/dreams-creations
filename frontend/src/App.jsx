import { Routes, Route, Navigate, useNavigate, useLocation } from 'react-router-dom'
import { Layout, Menu, Typography, Avatar, Dropdown } from 'antd'
import {
  DashboardOutlined, ShoppingOutlined, TeamOutlined,
  AlertOutlined, UserOutlined, LogoutOutlined,
  AppstoreOutlined, SendOutlined, FileTextOutlined,
  PictureOutlined, InboxOutlined, CheckSquareOutlined, SafetyCertificateOutlined,
  SettingOutlined, BarChartOutlined, HistoryOutlined,
} from '@ant-design/icons'
import { useAuth } from './context/AuthContext'
import ProtectedRoute from './routes/ProtectedRoute'
import { MANAGEMENT_ROLES, ROLES, portalLabel, homeForRole } from './utils/roles'

// Pages
import LoginPage          from './pages/auth/LoginPage'
import PortalLoginPage    from './pages/auth/PortalLoginPage'
import RegisterPage       from './pages/auth/RegisterPage'
import DashboardPage      from './pages/dashboard/DashboardPage'
import AlertsPage         from './pages/dashboard/AlertsPage'
import BatchesPage        from './pages/production/BatchesPage'
import DispatchPage       from './pages/production/DispatchPage'
import CustomersPage      from './pages/sales/CustomersPage'
import BillsPage          from './pages/sales/BillsPage'
import MyOrdersPage       from './pages/sales/MyOrdersPage'
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

const MANAGER_MENU = [
  { key: '/dashboard',  icon: <DashboardOutlined />, label: 'Dashboard' },
  { key: '/batches',    icon: <AppstoreOutlined />,  label: 'Production Batches' },
  { key: '/dispatch',   icon: <SendOutlined />,       label: 'Dispatch Management' },
  { key: '/inventory',  icon: <InboxOutlined />,        label: 'Inventory' },
  { key: '/designs',    icon: <PictureOutlined />,    label: 'Designs Catalog' },
  { key: '/customers',  icon: <TeamOutlined />,       label: 'Customers' },
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

  const menuItems = getMenu(auth?.role)

  const userMenu = {
    items: [{
      key: 'logout', icon: <LogoutOutlined />, label: 'Logout',
      onClick: () => { logout(); navigate('/login') }
    }]
  }

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sider theme="dark" width={220} style={{ background: '#1a237e' }}>
        <div style={{
          padding: '20px 16px', borderBottom: '1px solid rgba(255,255,255,0.1)', marginBottom: 8
        }}>
          <Text strong style={{ color: '#fff', fontSize: 16 }}>Dreams Creations</Text>
          <br />
          <Text style={{ color: 'rgba(255,255,255,0.5)', fontSize: 11 }}>
            {portalLabel(auth?.role)}
          </Text>
        </div>
        <Menu
          theme="dark"
          mode="inline"
          selectedKeys={[location.pathname]}
          style={{ background: '#1a237e', borderRight: 0 }}
          items={menuItems}
          onClick={({ key }) => navigate(key)}
        />
      </Sider>

      <Layout>
        <Header style={{
          background: '#fff', padding: '0 24px',
          display: 'flex', alignItems: 'center',
          justifyContent: 'flex-end',
          boxShadow: '0 1px 4px rgba(0,0,0,0.08)',
        }}>
          <Dropdown menu={userMenu} placement="bottomRight">
            <div style={{ cursor: 'pointer', display: 'flex', alignItems: 'center', gap: 8 }}>
              <Avatar icon={<UserOutlined />} style={{ background: '#1a237e' }} />
              <Text strong>{auth?.username}</Text>
              <Text type="secondary" style={{ fontSize: 12 }}>({auth?.role})</Text>
            </div>
          </Dropdown>
        </Header>

        <Content style={{ margin: 24 }}>
          <BackendStatus />
          {children}
        </Content>
      </Layout>
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
