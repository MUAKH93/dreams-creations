import { useEffect, useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  Typography, Row, Col, Card, Tag, Spin, Empty, Alert, Input, Select, Switch, Space,
} from 'antd'
import { ShoppingOutlined, SearchOutlined } from '@ant-design/icons'
import { shopAPI } from '../../api/shop'
import { apiErrorMessage } from '../../api/client'
import { shopModuleEnabled } from '../../config/modules'
import { modulesAPI } from '../../api/modules'
import ShopStorefrontHeader from '../../components/shop/ShopStorefrontHeader'
import '../../styles/shop-portal.css'

const { Title, Text } = Typography
const { Search } = Input

export default function ShopStorefrontPage() {
  const navigate = useNavigate()
  const [settings, setSettings] = useState(null)
  const [catalog, setCatalog] = useState([])
  const [categories, setCategories] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [shopEnabled, setShopEnabled] = useState(shopModuleEnabled)
  const [featuredOnly, setFeaturedOnly] = useState(false)
  const [category, setCategory] = useState(null)
  const [search, setSearch] = useState('')

  useEffect(() => {
    modulesAPI.getFlags()
      .then(r => { if (r.data?.shop?.enabled) setShopEnabled(true) })
      .catch(() => {})
  }, [])

  useEffect(() => {
    if (!shopEnabled) {
      setLoading(false)
      return
    }
    shopAPI.getPublicSettings()
      .then(r => setSettings(r.data))
      .catch(() => {})
    shopAPI.getCatalog()
      .then(r => {
        const items = r.data || []
        const cats = [...new Set(items.map(i => i.categoryName).filter(Boolean))].sort()
        setCategories(cats)
      })
      .catch(() => {})
  }, [shopEnabled])

  useEffect(() => {
    if (!shopEnabled) return
    setLoading(true)
    shopAPI.getCatalog({
      featured: featuredOnly ? true : undefined,
      category: category || undefined,
      q: search.trim() || undefined,
    })
      .then(r => setCatalog(r.data || []))
      .catch(err => setError(apiErrorMessage(err)))
      .finally(() => setLoading(false))
  }, [shopEnabled, featuredOnly, category, search])

  const browseMessage = useMemo(() => {
    if (settings?.allowGuestBrowse) {
      return 'Browse freely as a guest. Login to order — cart & checkout arrive in Phase S3.'
    }
    return 'Login will be required to place orders when checkout launches.'
  }, [settings])

  if (!shopEnabled) {
    return (
      <div className="shop-storefront" style={{ padding: 48, textAlign: 'center' }}>
        <Title level={3}>Online shop is not available</Title>
        <Text type="secondary">Enable the shop module and restart the backend.</Text>
      </div>
    )
  }

  if (!settings && loading) {
    return (
      <div className="shop-storefront" style={{ textAlign: 'center', padding: 80 }}>
        <Spin size="large" />
      </div>
    )
  }

  if (settings && !settings.storefrontEnabled) {
    return (
      <div className="shop-storefront" style={{ padding: 48, textAlign: 'center' }}>
        <Title level={3}>{settings.storeName || 'Dreams Creations Shop'}</Title>
        <Text>Our online storefront is temporarily closed.</Text>
      </div>
    )
  }

  return (
    <div className="shop-storefront">
      <ShopStorefrontHeader settings={settings} />

      <div className="shop-storefront-body">
        {error && (
          <Alert type="error" message={error} style={{ marginBottom: 24 }} showIcon />
        )}

        <Alert type="info" showIcon style={{ marginBottom: 24 }} message={browseMessage} />

        <div className="shop-storefront-toolbar">
          <Search
            placeholder="Search by name or design code"
            allowClear
            enterButton={<SearchOutlined />}
            onSearch={setSearch}
            onChange={e => { if (!e.target.value) setSearch('') }}
            style={{ maxWidth: 360, flex: 1 }}
          />
          <Select
            allowClear
            placeholder="All categories"
            style={{ minWidth: 160 }}
            value={category}
            onChange={setCategory}
            options={categories.map(c => ({ value: c, label: c }))}
          />
          <Space>
            <Text>Featured only</Text>
            <Switch checked={featuredOnly} onChange={setFeaturedOnly} />
          </Space>
        </div>

        {loading ? (
          <div style={{ textAlign: 'center', padding: 48 }}><Spin size="large" /></div>
        ) : catalog.length === 0 ? (
          <Empty description="No designs match your filters" />
        ) : (
          <Row gutter={[20, 20]}>
            {catalog.map(item => (
              <Col xs={24} sm={12} md={8} lg={6} key={item.designId}>
                <Card
                  className="shop-product-card"
                  hoverable
                  onClick={() => navigate(`/store/design/${item.designId}`)}
                  cover={
                    item.primaryImageUrl ? (
                      <img
                        src={item.primaryImageUrl}
                        alt={item.name}
                        style={{ height: 220, width: '100%', objectFit: 'cover', display: 'block' }}
                      />
                    ) : (
                      <div className="shop-product-card--placeholder">
                        <ShoppingOutlined />
                      </div>
                    )
                  }
                >
                  <Text type="secondary" style={{ fontSize: 12 }}>{item.designCode}</Text>
                  <Title level={5} style={{ margin: '4px 0 8px' }}>{item.name}</Title>
                  {item.categoryName && <Tag style={{ marginBottom: 8 }}>{item.categoryName}</Tag>}
                  {item.featured && <Tag color="gold">Featured</Tag>}
                  <div>
                    <Text strong>From Rs. {Number(item.basePrice || 0).toLocaleString()}</Text>
                  </div>
                  <Text
                    type={item.totalStock > 0 ? 'success' : 'secondary'}
                    style={{ display: 'block', marginTop: 4 }}
                  >
                    {item.totalStock > 0 ? `${item.totalStock} available` : 'Out of stock'}
                  </Text>
                </Card>
              </Col>
            ))}
          </Row>
        )}
      </div>
    </div>
  )
}
