import { useState } from 'react'
import { Layout, Menu, Typography, Avatar, Dropdown, Drawer, Button, Grid, ConfigProvider } from 'antd'
import {
  HomeOutlined, SettingOutlined, ShoppingOutlined, ShopOutlined,
  ArrowLeftOutlined, UserOutlined, LogoutOutlined, MenuOutlined, IdcardOutlined,
  GlobalOutlined, UnorderedListOutlined, BarChartOutlined,
} from '@ant-design/icons'
import { useNavigate, useLocation, Outlet } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import BrandLogo from '../components/BrandLogo'
import '../styles/shop-portal.css'

const { Sider, Content, Header } = Layout
const { Text } = Typography
const { useBreakpoint } = Grid

const SHOP_THEME = {
  token: {
    colorPrimary: '#ea580c',
    borderRadius: 8,
  },
}

const NAV_ITEMS = [
  { key: '/shop', icon: <HomeOutlined />, label: 'Overview' },
  { key: '/shop/settings', icon: <SettingOutlined />, label: 'Shop Settings' },
  { key: '/shop/catalog', icon: <ShoppingOutlined />, label: 'Catalog Preview' },
  { key: '/shop/orders', icon: <UnorderedListOutlined />, label: 'Orders' },
  { key: '/shop/analytics', icon: <BarChartOutlined />, label: 'Analytics' },
]

function ShopSidebarBrand() {
  return (
    <div className="shop-sider-brand">
      <BrandLogo variant="sidebar" />
      <span className="shop-sider-brand__title">Shop Portal</span>
      <span className="shop-sider-brand__subtitle">Online storefront</span>
    </div>
  )
}

export default function ShopLayout() {
  const { auth, logout } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const screens = useBreakpoint()
  const isMobile = !screens.lg
  const [drawerOpen, setDrawerOpen] = useState(false)

  const userMenu = {
    items: [
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

  const menu = (
    <Menu
      theme="dark"
      mode="inline"
      selectedKeys={[location.pathname]}
      style={{ background: '#7c2d12', borderRight: 0 }}
      items={NAV_ITEMS}
      onClick={({ key }) => handleNavigate(key)}
    />
  )

  return (
    <ConfigProvider theme={SHOP_THEME}>
      <Layout className="shop-portal">
        {!isMobile && (
          <Sider theme="dark" width={220} className="shop-sider">
            <ShopSidebarBrand />
            {menu}
            <div style={{ padding: 16 }}>
              <Button
                block
                icon={<ArrowLeftOutlined />}
                onClick={() => navigate('/dashboard')}
              >
                Back to Operations
              </Button>
              <Button
                block
                type="link"
                icon={<GlobalOutlined />}
                style={{ color: 'rgba(255,255,255,0.75)', marginTop: 8 }}
                onClick={() => window.open('/store', '_blank')}
              >
                Open storefront
              </Button>
            </div>
          </Sider>
        )}

        <Layout>
          <Header className="shop-header">
            {isMobile && (
              <Button
                type="text"
                icon={<MenuOutlined />}
                onClick={() => setDrawerOpen(true)}
                aria-label="Open menu"
              />
            )}
            <Text strong style={{ color: '#9a3412' }}>
              <ShopOutlined style={{ marginRight: 8 }} />
              Shop Portal
            </Text>
            <Dropdown menu={userMenu} placement="bottomRight">
              <div style={{ display: 'flex', alignItems: 'center', gap: 8, cursor: 'pointer' }}>
                <Avatar icon={<UserOutlined />} style={{ background: '#ea580c' }} />
                <Text strong>{auth?.username}</Text>
              </div>
            </Dropdown>
          </Header>

          <Content className="shop-content">
            <Outlet />
          </Content>
        </Layout>

        <Drawer
          title={<ShopSidebarBrand />}
          placement="left"
          open={drawerOpen}
          onClose={() => setDrawerOpen(false)}
          width={260}
          styles={{ body: { padding: 0, background: '#7c2d12' } }}
        >
          {menu}
          <div style={{ padding: 16 }}>
            <Button block icon={<ArrowLeftOutlined />} onClick={() => navigate('/dashboard')}>
              Back to Operations
            </Button>
          </div>
        </Drawer>
      </Layout>
    </ConfigProvider>
  )
}
