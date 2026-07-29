import { Link } from 'react-router-dom'
import { Typography, Button } from 'antd'
import { LoginOutlined, ArrowLeftOutlined } from '@ant-design/icons'

const { Title } = Typography

export default function ShopStorefrontHeader({ settings, showBack, backTo = '/store' }) {
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
      <div style={{ marginTop: 16 }}>
        <Link to="/login">
          <Button type="default" icon={<LoginOutlined />}>Login to order</Button>
        </Link>
      </div>
    </header>
  )
}
