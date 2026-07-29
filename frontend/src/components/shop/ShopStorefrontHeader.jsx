import { Link } from 'react-router-dom'
import { Typography, Button, Badge } from 'antd'
import { LoginOutlined, ArrowLeftOutlined, ShoppingCartOutlined } from '@ant-design/icons'
import { useShopCart } from '../../hooks/useShopCart'

const { Title } = Typography

export default function ShopStorefrontHeader({ settings, showBack, backTo = '/store' }) {
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
        <Link to="/login">
          <Button type="default" icon={<LoginOutlined />}>Login to order</Button>
        </Link>
      </div>
    </header>
  )
}
