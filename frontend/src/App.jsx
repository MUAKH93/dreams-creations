import { Routes, Route, Navigate, useNavigate, useLocation } from 'react-router-dom'
import { Layout, Menu, Typography, Avatar, Dropdown } from 'antd'
import {
  DashboardOutlined, ShoppingOutlined, TeamOutlined,
  AlertOutlined, UserOutlined, LogoutOutlined,
  AppstoreOutlined, SendOutlined, FileTextOutlined,
  PictureOutlined, InboxOutlined, CheckSquareOutlined, SafetyCertificateOutlined,
  SettingOutlined, BarChartOutlined,
} from '@ant-design/icons'
import { useAuth } from './context/AuthContext'
import ProtectedRoute from './routes/ProtectedRoute'

// Pages
import LoginPage          from './pages/auth/LoginPage'
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
import ReportsPage        from './pages/reports/ReportsPage'
import BackendStatus from './components/BackendStatus'
import SessionCheck from './components/SessionCheck'

const { Sider, Content, Header } = Layout
const { Text } = Typography

const ADMIN_MANAGER_MENU = [
  { key: '/dashboard',  icon: <DashboardOutlined />, label: 'Dashboard' },
  { key: '/batches',    icon: <AppstoreOutlined />,  label: 'Production Batches' },
  { key: '/dispatch',   icon: <SendOutlined />,       label: 'Dispatch Management' },
  { key: '/inventory',  icon: <InboxOutlined />,        label: 'Inventory' },
  { key: '/designs',    icon: <PictureOutlined />,    label: 'Designs Catalog' },
  { key: '/customers',  icon: <TeamOutlined />,       label: 'Customers' },
  { key: '/bills',      icon: <FileTextOutlined />,   label: 'Bills & Payments' },
  { key: '/reports',    icon: <BarChartOutlined />,   label: 'Reports' },
  { key: '/staff',      icon: <SafetyCertificateOutlined />, label: 'Staff & Supervisors' },
  { key: '/setup',      icon: <SettingOutlined />,    label: 'Factory Setup' },
  { key: '/alerts',     icon: <AlertOutlined />,      label: 'Alerts' },
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
  if (role === 'ADMIN' || role === 'MANAGER') return ADMIN_MANAGER_MENU
  if (role === 'CUSTOMER') return CUSTOMER_MENU
  if (role === 'SUPERVISOR') return SUPERVISOR_MENU
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
            {auth?.role === 'CUSTOMER' ? 'Customer Portal'
              : auth?.role === 'SUPERVISOR' ? 'Supervisor Portal'
              : 'Management System'}
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

const ADMIN_MANAGER = ['ADMIN', 'MANAGER']

export default function App() {
  const { auth } = useAuth()

  return (
    <>
      {auth && <SessionCheck />}
      <Routes>
      {/* Public */}
      <Route path="/login"    element={auth ? <Navigate to="/" /> : <LoginPage />} />
      <Route path="/register" element={auth ? <Navigate to="/" /> : <RegisterPage />} />

      {/* Admin + Manager */}
      <Route path="/dashboard" element={
        <ProtectedRoute roles={['ADMIN','MANAGER','SUPERVISOR','CUSTOMER']}>
          <AppLayout><DashboardPage /></AppLayout>
        </ProtectedRoute>
      } />
      <Route path="/batches" element={
        <ProtectedRoute roles={ADMIN_MANAGER}>
          <AppLayout><BatchesPage /></AppLayout>
        </ProtectedRoute>
      } />
      <Route path="/dispatch" element={
        <ProtectedRoute roles={ADMIN_MANAGER}>
          <AppLayout><DispatchPage /></AppLayout>
        </ProtectedRoute>
      } />
      <Route path="/inventory" element={
        <ProtectedRoute roles={ADMIN_MANAGER}>
          <AppLayout><InventoryPage /></AppLayout>
        </ProtectedRoute>
      } />
      <Route path="/customers" element={
        <ProtectedRoute roles={ADMIN_MANAGER}>
          <AppLayout><CustomersPage /></AppLayout>
        </ProtectedRoute>
      } />
      <Route path="/bills" element={
        <ProtectedRoute roles={ADMIN_MANAGER}>
          <AppLayout><BillsPage /></AppLayout>
        </ProtectedRoute>
      } />
      <Route path="/reports" element={
        <ProtectedRoute roles={ADMIN_MANAGER}>
          <AppLayout><ReportsPage /></AppLayout>
        </ProtectedRoute>
      } />
      <Route path="/alerts" element={
        <ProtectedRoute roles={ADMIN_MANAGER}>
          <AppLayout><AlertsPage /></AppLayout>
        </ProtectedRoute>
      } />
      <Route path="/staff" element={
        <ProtectedRoute roles={ADMIN_MANAGER}>
          <AppLayout><StaffPage /></AppLayout>
        </ProtectedRoute>
      } />
      <Route path="/setup" element={
        <ProtectedRoute roles={ADMIN_MANAGER}>
          <AppLayout><SetupPage /></AppLayout>
        </ProtectedRoute>
      } />

      {/* Supervisor portal */}
      <Route path="/assignments" element={
        <ProtectedRoute roles={['SUPERVISOR','ADMIN','MANAGER']}>
          <AppLayout><AssignmentsPage /></AppLayout>
        </ProtectedRoute>
      } />

      {/* Shared — all logged-in roles */}
      <Route path="/designs" element={
        <ProtectedRoute roles={['ADMIN','MANAGER','CUSTOMER','SUPERVISOR']}>
          <AppLayout><DesignsCatalogPage /></AppLayout>
        </ProtectedRoute>
      } />

      {/* Customer portal */}
      <Route path="/my-orders" element={
        <ProtectedRoute roles={['CUSTOMER']}>
          <AppLayout><MyOrdersPage /></AppLayout>
        </ProtectedRoute>
      } />

      {/* Default redirect based on role */}
      <Route path="/" element={
        !auth
          ? <Navigate to="/login" />
          : auth.role === 'CUSTOMER'
            ? <Navigate to="/dashboard" />
            : auth.role === 'SUPERVISOR'
              ? <Navigate to="/dashboard" />
              : <Navigate to="/dashboard" />
      } />

      <Route path="/unauthorized" element={
        <div style={{ textAlign: 'center', padding: 80 }}>
          <h2>403 — Access Denied</h2>
        </div>
      } />
      <Route path="*" element={<Navigate to="/" />} />
    </Routes>
    </>
  )
}
