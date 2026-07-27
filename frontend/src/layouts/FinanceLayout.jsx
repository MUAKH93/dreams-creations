import { useState } from 'react'
import { Layout, Menu, Typography, Avatar, Dropdown, Drawer, Button, Grid, ConfigProvider } from 'antd'
import {
  HomeOutlined, UnorderedListOutlined, FileTextOutlined, BarChartOutlined,
  ArrowLeftOutlined, UserOutlined, LogoutOutlined, MenuOutlined, IdcardOutlined,
} from '@ant-design/icons'
import { useNavigate, useLocation, Outlet } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import BrandLogo from '../components/BrandLogo'
import {
  useFinanceTutorial,
  FinanceTutorialWelcome,
  FinanceTutorialHelp,
  FinanceHelpButton,
  FinanceNavTour,
} from '../components/finance/FinanceTutorial'
import '../styles/finance-portal.css'

const { Sider, Content, Header } = Layout
const { Text } = Typography
const { useBreakpoint } = Grid

const FINANCE_THEME = {
  token: {
    colorPrimary: '#0d9488',
    borderRadius: 8,
  },
}

const NAV_ITEMS = [
  { key: '/finance', tour: 'finance-overview', icon: <HomeOutlined />, label: 'Overview' },
  { key: '/finance/accounts', tour: 'finance-accounts', icon: <UnorderedListOutlined />, label: 'Chart of Accounts' },
  { key: '/finance/journals', tour: 'finance-journals', icon: <FileTextOutlined />, label: 'Journal Entries' },
  { key: '/finance/reports', tour: 'finance-reports', icon: <BarChartOutlined />, label: 'Reports' },
]

function FinanceSidebarBrand() {
  return (
    <div className="finance-sider-brand">
      <BrandLogo variant="sidebar" />
      <span className="finance-sider-brand__title">Finance Portal</span>
      <span className="finance-sider-brand__subtitle">Accounting &amp; reports</span>
    </div>
  )
}

export default function FinanceLayout() {
  const { auth, logout } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const screens = useBreakpoint()
  const isMobile = !screens.lg
  const [drawerOpen, setDrawerOpen] = useState(false)

  const tutorial = useFinanceTutorial()

  const tourSteps = [
    {
      title: 'Finance overview',
      description: 'Your home page with quick links and module status.',
      target: () => document.querySelector('[data-tour="finance-overview"]'),
    },
    {
      title: 'Chart of Accounts',
      description: 'Manage account codes for assets, income, expenses, and more.',
      target: () => document.querySelector('[data-tour="finance-accounts"]'),
    },
    {
      title: 'Journal Entries',
      description: 'Post balanced manual entries and review automated bill/payment journals.',
      target: () => document.querySelector('[data-tour="finance-journals"]'),
    },
    {
      title: 'Reports',
      description: 'Trial balance, general ledger, and AR aging live here.',
      target: () => document.querySelector('[data-tour="finance-reports"]'),
    },
    {
      title: 'Return to operations',
      description: 'Switch back to production, sales, and inventory anytime.',
      target: () => document.querySelector('[data-tour="finance-back-ops"]'),
    },
  ]

  const userMenu = {
    items: [
      { key: 'profile', icon: <IdcardOutlined />, label: 'My Profile', onClick: () => navigate('/profile') },
      { key: 'logout', icon: <LogoutOutlined />, label: 'Logout', onClick: () => { logout(); navigate('/login') } },
    ],
  }

  const handleNav = (key) => {
    navigate(key)
    setDrawerOpen(false)
  }

  const renderMenu = () => (
    <>
      <Menu
        mode="inline"
        selectedKeys={[location.pathname]}
        className="finance-nav-menu"
        items={NAV_ITEMS.map(({ key, tour, icon, label }) => ({
          key,
          icon,
          label: <span data-tour={tour}>{label}</span>,
        }))}
        onClick={({ key }) => handleNav(key)}
      />
      <div className="finance-back-link">
        <Button
          type="link"
          icon={<ArrowLeftOutlined />}
          data-tour="finance-back-ops"
          onClick={() => navigate('/dashboard')}
          style={{ color: '#64748b', padding: '4px 0' }}
        >
          Back to Operations
        </Button>
      </div>
    </>
  )

  return (
    <ConfigProvider theme={FINANCE_THEME}>
      <Layout className="finance-layout">
        {!isMobile && (
          <Sider width={240} className="finance-sider">
            <FinanceSidebarBrand />
            {renderMenu()}
          </Sider>
        )}

        <Layout>
          <Header className="finance-header">
            <div className="finance-header__left">
              {isMobile && (
                <Button
                  type="text"
                  icon={<MenuOutlined />}
                  onClick={() => setDrawerOpen(true)}
                  aria-label="Open finance menu"
                />
              )}
              <Text strong style={{ fontSize: 14, color: '#0f766e' }}>
                Finance Portal
              </Text>
              <Text type="secondary" style={{ fontSize: 12, marginLeft: 8 }}>
                · separate from operations
              </Text>
            </div>
            <div className="finance-header__right">
              <FinanceHelpButton onClick={tutorial.openHelp} />
              <Dropdown menu={userMenu} placement="bottomRight">
                <div style={{ cursor: 'pointer', display: 'flex', alignItems: 'center', gap: 8 }}>
                  <Avatar src={auth?.profilePhotoUrl || undefined} icon={<UserOutlined />} style={{ background: '#0d9488' }} />
                  <Text strong>{auth?.username}</Text>
                </div>
              </Dropdown>
            </div>
          </Header>

          <Content className="finance-content">
            <Outlet />
          </Content>
        </Layout>

        <Drawer
          title={<FinanceSidebarBrand />}
          placement="left"
          open={drawerOpen}
          onClose={() => setDrawerOpen(false)}
          width={280}
          className="finance-drawer"
          styles={{ body: { padding: 0 } }}
        >
          {renderMenu()}
        </Drawer>

        <FinanceTutorialWelcome
          open={tutorial.welcomeOpen}
          onStartTour={() => { tutorial.startTour(); tutorial.markDone() }}
          onSkip={tutorial.markDone}
        />
        <FinanceTutorialHelp
          open={tutorial.helpOpen}
          onClose={tutorial.closeHelp}
          onNavigate={handleNav}
          onRestart={() => { tutorial.resetTutorial(); tutorial.closeHelp() }}
        />
        <FinanceNavTour
          open={tutorial.tourOpen}
          onClose={() => { tutorial.setTourOpen(false); tutorial.markDone() }}
          steps={tourSteps}
        />
      </Layout>
    </ConfigProvider>
  )
}
