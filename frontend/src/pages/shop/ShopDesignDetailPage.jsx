import { useEffect, useMemo, useState } from 'react'
import { useParams, Link } from 'react-router-dom'
import {
  Typography, Row, Col, Tag, Spin, Button, Alert, Select, Space, Divider,
} from 'antd'
import { LoginOutlined, ShoppingCartOutlined } from '@ant-design/icons'
import { shopAPI } from '../../api/shop'
import { apiErrorMessage } from '../../api/client'
import { shopModuleEnabled } from '../../config/modules'
import { modulesAPI } from '../../api/modules'
import ShopStorefrontHeader from '../../components/shop/ShopStorefrontHeader'
import '../../styles/shop-portal.css'

const { Title, Text, Paragraph } = Typography

function variantLabel(v) {
  const size = v.sizeValue || '—'
  const color = v.color || '—'
  return `${size} / ${color}`
}

export default function ShopDesignDetailPage() {
  const { designId } = useParams()
  const [settings, setSettings] = useState(null)
  const [design, setDesign] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [shopEnabled, setShopEnabled] = useState(shopModuleEnabled)
  const [selectedSize, setSelectedSize] = useState(null)
  const [selectedColor, setSelectedColor] = useState(null)

  useEffect(() => {
    modulesAPI.getFlags()
      .then(r => { if (r.data?.shop?.enabled) setShopEnabled(true) })
      .catch(() => {})
  }, [])

  useEffect(() => {
    if (!shopEnabled || !designId) {
      setLoading(false)
      return
    }
    setLoading(true)
    Promise.all([
      shopAPI.getPublicSettings(),
      shopAPI.getDesign(designId),
    ])
      .then(([settingsRes, designRes]) => {
        setSettings(settingsRes.data)
        setDesign(designRes.data)
      })
      .catch(err => setError(apiErrorMessage(err)))
      .finally(() => setLoading(false))
  }, [shopEnabled, designId])

  const variants = design?.variants || []

  const sizes = useMemo(
    () => [...new Set(variants.map(v => v.sizeValue).filter(Boolean))],
    [variants],
  )

  const colors = useMemo(() => {
    if (!selectedSize) {
      return [...new Set(variants.map(v => v.color).filter(Boolean))]
    }
    return [...new Set(
      variants.filter(v => v.sizeValue === selectedSize).map(v => v.color).filter(Boolean),
    )]
  }, [variants, selectedSize])

  const selectedVariant = useMemo(() => {
    if (!variants.length) return null
    if (selectedSize && selectedColor) {
      return variants.find(v => v.sizeValue === selectedSize && v.color === selectedColor) || null
    }
    if (selectedSize) {
      return variants.find(v => v.sizeValue === selectedSize && v.inStock)
        || variants.find(v => v.sizeValue === selectedSize)
        || null
    }
    return variants.find(v => v.inStock) || variants[0]
  }, [variants, selectedSize, selectedColor])

  useEffect(() => {
    if (!design?.variants?.length) return
    const first = design.variants.find(v => v.inStock) || design.variants[0]
    if (first) {
      setSelectedSize(first.sizeValue || null)
      setSelectedColor(first.color || null)
    }
  }, [design])

  if (!shopEnabled) {
    return (
      <div className="shop-storefront" style={{ padding: 48, textAlign: 'center' }}>
        <Title level={3}>Online shop is not available</Title>
        <Link to="/login"><Button type="primary">Go to login</Button></Link>
      </div>
    )
  }

  if (loading) {
    return (
      <div className="shop-storefront" style={{ textAlign: 'center', padding: 80 }}>
        <Spin size="large" />
      </div>
    )
  }

  if (error || !design) {
    return (
      <div className="shop-storefront">
        <ShopStorefrontHeader settings={settings} showBack />
        <div className="shop-storefront-body">
          <Alert type="error" message={error || 'Design not found'} showIcon />
        </div>
      </div>
    )
  }

  const price = selectedVariant?.sellingPrice ?? design.basePrice

  return (
    <div className="shop-storefront">
      <ShopStorefrontHeader settings={settings} showBack />

      <div className="shop-storefront-body shop-detail-page">
        <Row gutter={[32, 24]}>
          <Col xs={24} md={10}>
            {design.primaryImageUrl ? (
              <img
                src={design.primaryImageUrl}
                alt={design.name}
                className="shop-detail-image"
              />
            ) : (
              <div className="shop-product-card--placeholder shop-detail-image">✦</div>
            )}
          </Col>
          <Col xs={24} md={14}>
            <Text type="secondary">{design.designCode}</Text>
            <Title level={2} style={{ marginTop: 4 }}>{design.name}</Title>
            <Space wrap style={{ marginBottom: 16 }}>
              {design.categoryName && <Tag>{design.categoryName}</Tag>}
              {design.featured && <Tag color="gold">Featured</Tag>}
              <Tag color={design.totalStock > 0 ? 'green' : 'default'}>
                {design.totalStock > 0 ? `${design.totalStock} total in stock` : 'Out of stock'}
              </Tag>
            </Space>

            {design.description && (
              <Paragraph type="secondary" style={{ marginBottom: 24 }}>
                {design.description}
              </Paragraph>
            )}

            <Divider />

            <Title level={5}>Choose size &amp; color</Title>
            <Space direction="vertical" size="middle" style={{ width: '100%', maxWidth: 360 }}>
              {sizes.length > 0 && (
                <div>
                  <Text type="secondary">Size</Text>
                  <Select
                    style={{ width: '100%', marginTop: 4 }}
                    placeholder="Select size"
                    value={selectedSize}
                    onChange={val => {
                      setSelectedSize(val)
                      const next = variants.find(v => v.sizeValue === val && v.inStock)
                        || variants.find(v => v.sizeValue === val)
                      setSelectedColor(next?.color || null)
                    }}
                    options={sizes.map(s => ({ value: s, label: s }))}
                  />
                </div>
              )}
              {colors.length > 0 && (
                <div>
                  <Text type="secondary">Color</Text>
                  <Select
                    style={{ width: '100%', marginTop: 4 }}
                    placeholder="Select color"
                    value={selectedColor}
                    onChange={setSelectedColor}
                    options={colors.map(c => ({ value: c, label: c }))}
                  />
                </div>
              )}
              {variants.length > 0 && sizes.length === 0 && colors.length === 0 && (
                <Select
                  style={{ width: '100%' }}
                  placeholder="Select variant"
                  value={selectedVariant?.productId}
                  onChange={productId => {
                    const v = variants.find(x => x.productId === productId)
                    if (v) {
                      setSelectedSize(v.sizeValue)
                      setSelectedColor(v.color)
                    }
                  }}
                  options={variants.map(v => ({
                    value: v.productId,
                    label: `${variantLabel(v)} — Rs. ${Number(v.sellingPrice || 0).toLocaleString()}${v.inStock ? '' : ' (out of stock)'}`,
                    disabled: !v.inStock,
                  }))}
                />
              )}
            </Space>

            <div className="shop-detail-price" style={{ marginTop: 24 }}>
              <Text type="secondary">Price</Text>
              <Title level={3} style={{ margin: '4px 0 0' }}>
                Rs. {Number(price || 0).toLocaleString()}
              </Title>
              {selectedVariant && (
                <Text type={selectedVariant.inStock ? 'success' : 'danger'}>
                  {selectedVariant.inStock
                    ? `${selectedVariant.stockQty} available for this variant`
                    : 'Selected variant is out of stock'}
                </Text>
              )}
            </div>

            <Alert
              type="info"
              showIcon
              style={{ marginTop: 24 }}
              message="Cart & checkout coming in Phase S3"
              description={
                settings?.allowGuestBrowse
                  ? 'You can browse as a guest. Login as a customer to place orders when checkout is ready.'
                  : 'Login is required to place orders when checkout launches.'
              }
            />

            <Space style={{ marginTop: 24 }}>
              <Link to="/login">
                <Button type="primary" size="large" icon={<LoginOutlined />}>
                  Login to order
                </Button>
              </Link>
              <Button size="large" icon={<ShoppingCartOutlined />} disabled>
                Add to cart (S3)
              </Button>
            </Space>
          </Col>
        </Row>
      </div>
    </div>
  )
}
