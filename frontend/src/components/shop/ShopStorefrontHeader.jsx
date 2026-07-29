import { Link, useNavigate } from 'react-router-dom'
import { Typography, Button, Badge } from 'antd'
import { LoginOutlined, ArrowLeftOutlined, ShoppingCartOutlined, UserOutlined } from '@ant-design/icons'
import { useShopCart } from '../../hooks/useShopCart'
import { useAuth } from '../../context/AuthContext'
import { homeForRole, MANAGEMENT_ROLES, ROLES } from '../../utils/roles'

const { Title } = Typography

export default function ShopStorefrontHeader({ settings, showBack, backTo = '/store' }) {
  const navigate = useNavigate()
  const { auth } = useAuth()
  const { itemCount } = useShopCart()

  return (
    <header className="shop-storefront-header">
      {showBack && (
        <div style={{ textAlign: 'left', marginBottom: 12 }}>
          <Link to={backTo}>
            <Button type="text" icon={<ArrowLeftOutlined />} style={{ color: '#fff' }}>
              Back to shop
            </Button>
          </Link>
        </div>
      )}
      <Title level={2}>{settings?.storeName || 'Dreams Creations Shop'}</Title>
      {settings?.tagline && <p>{settings.tagline}</p>}
      <div style={{ marginTop: 16, display: 'flex', gap: 12, justifyContent: 'center', flexWrap: 'wrap' }}>
        <Link to="/store/cart">
          <Badge count={itemCount} size="small">
            <Button type="default" icon={<ShoppingCartOutlined />}>Cart</Button>
          </Badge>
        </Link>
        {auth ? (
          <>
            {MANAGEMENT_ROLES.includes(auth.role) && (
              <Button type="default" onClick={() => navigate('/shop')}>
                Shop Portal
              </Button>
            )}
            <Button
              type="default"
              icon={<UserOutlined />}
              onClick={() => navigate(homeForRole(auth.role))}
            >
              {auth.role === ROLES.CUSTOMER ? 'My account' : auth.username}
            </Button>
          </>
        ) : (
          <Link to="/login">
            <Button type="default" icon={<LoginOutlined />}>Login to order</Button>
          </Link>
        )}
      </div>
    </header>
  )
}
