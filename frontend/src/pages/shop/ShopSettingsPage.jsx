import { useEffect, useState } from 'react'
import { Typography, Form, Input, Switch, Button, Card, message, Spin } from 'antd'
import { shopAPI } from '../../api/shop'
import { apiErrorMessage } from '../../api/client'

const { Title, Text } = Typography

export default function ShopSettingsPage() {
  const [form] = Form.useForm()
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)

  useEffect(() => {
    shopAPI.getSettings()
      .then(r => {
        form.setFieldsValue({
          storeName: r.data.storeName,
          tagline: r.data.tagline,
          storefrontEnabled: r.data.storefrontEnabled,
          allowGuestBrowse: r.data.allowGuestBrowse,
        })
      })
      .catch(err => message.error(apiErrorMessage(err)))
      .finally(() => setLoading(false))
  }, [form])

  const onSave = async (values) => {
    setSaving(true)
    try {
      await shopAPI.updateSettings(values)
      message.success('Shop settings saved')
    } catch (err) {
      message.error(apiErrorMessage(err))
    } finally {
      setSaving(false)
    }
  }

  if (loading) {
    return <div style={{ textAlign: 'center', padding: 48 }}><Spin size="large" /></div>
  }

  return (
    <div>
      <Title level={3} className="shop-page-title">Shop settings</Title>
      <Text className="shop-page-subtitle">
        Control how your public storefront appears at <Text code>/store</Text>.
      </Text>

      <Card style={{ maxWidth: 560 }}>
        <Form form={form} layout="vertical" onFinish={onSave}>
          <Form.Item
            name="storeName"
            label="Store name"
            rules={[{ required: true, message: 'Store name is required' }]}
          >
            <Input placeholder="Dreams Creations Shop" />
          </Form.Item>
          <Form.Item name="tagline" label="Tagline">
            <Input placeholder="Premium suits — order online" />
          </Form.Item>
          <Form.Item
            name="storefrontEnabled"
            label="Storefront open"
            valuePropName="checked"
          >
            <Switch checkedChildren="Open" unCheckedChildren="Closed" />
          </Form.Item>
          <Form.Item
            name="allowGuestBrowse"
            label="Allow guest browsing"
            valuePropName="checked"
            extra="When off, customers must log in to view the catalog (enforced in S2)."
          >
            <Switch />
          </Form.Item>
          <Form.Item>
            <Button type="primary" htmlType="submit" loading={saving}>
              Save settings
            </Button>
          </Form.Item>
        </Form>
      </Card>
    </div>
  )
}
