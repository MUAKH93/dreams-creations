import { useEffect, useState } from 'react'
import {
  Card, Form, Input, Button, Upload, Avatar, Typography, message, Alert, Tag,
} from 'antd'
import { UserOutlined, UploadOutlined, MailOutlined, PhoneOutlined } from '@ant-design/icons'
import { useAuth } from '../../context/AuthContext'
import { profileAPI } from '../../api/profile'
import { apiErrorMessage } from '../../api/client'
import { portalLabel } from '../../utils/roles'

const { Title, Text } = Typography

export default function ProfilePage() {
  const { auth, updateProfilePhoto } = useAuth()
  const [profile, setProfile] = useState(null)
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [uploading, setUploading] = useState(false)
  const [form] = Form.useForm()
  const isCustomer = auth?.role === 'CUSTOMER'

  const load = () => {
    setLoading(true)
    profileAPI.getMy()
      .then(res => {
        setProfile(res.data)
        form.setFieldsValue({
          firstName: res.data.firstName,
          lastName: res.data.lastName,
          email: res.data.email,
          phone: res.data.phone,
          address: res.data.address,
          city: res.data.city,
        })
        if (res.data.profilePhotoUrl) {
          updateProfilePhoto?.(res.data.profilePhotoUrl)
        }
      })
      .catch(err => message.error(apiErrorMessage(err)))
      .finally(() => setLoading(false))
  }

  useEffect(() => { load() }, [])

  const onSave = async (values) => {
    setSaving(true)
    try {
      const res = await profileAPI.updateMy(values)
      setProfile(res.data)
      if (isCustomer && !res.data.emailVerified) {
        message.success('Profile updated — verify your new email address')
      } else {
        message.success('Profile updated')
      }
      if (res.data.profilePhotoUrl) {
        updateProfilePhoto?.(res.data.profilePhotoUrl)
      }
    } catch (err) {
      message.error(apiErrorMessage(err))
    } finally {
      setSaving(false)
    }
  }

  const onPhotoUpload = async ({ file }) => {
    setUploading(true)
    try {
      const res = await profileAPI.uploadPhoto(file)
      setProfile(res.data)
      updateProfilePhoto?.(res.data.profilePhotoUrl)
      message.success('Profile photo updated')
    } catch (err) {
      message.error(apiErrorMessage(err))
    } finally {
      setUploading(false)
    }
    return false
  }

  const photoSrc = profile?.profilePhotoUrl || null
  const roleLabel = portalLabel(auth?.role)

  return (
    <div>
      <Title level={4} className="page-title">My Profile</Title>

      {!profile?.profileComplete && (
        <Alert
          type="warning"
          showIcon
          style={{ marginBottom: 16 }}
          message="Complete your profile"
          description="Email, phone number, and first name are required."
        />
      )}

      {isCustomer && !profile?.emailVerified && (
        <Alert
          type="info"
          showIcon
          style={{ marginBottom: 16 }}
          message="Email not verified"
          description="Check your inbox for the verification link, or update your email to resend one."
        />
      )}

      <Card loading={loading}>
        <div style={{ display: 'flex', gap: 24, alignItems: 'center', marginBottom: 24, flexWrap: 'wrap' }}>
          <Avatar size={96} src={photoSrc || undefined} icon={<UserOutlined />} />
          <div>
            <Title level={5} style={{ margin: 0 }}>{profile?.username}</Title>
            <Text type="secondary">{roleLabel}</Text>
            <div style={{ marginTop: 8 }}>
              {isCustomer && (
                <Tag color={profile?.emailVerified ? 'green' : 'orange'}>
                  {profile?.emailVerified ? 'Email verified' : 'Email pending'}
                </Tag>
              )}
              {profile?.profileComplete && <Tag color="blue">Profile complete</Tag>}
            </div>
            <Upload showUploadList={false} beforeUpload={onPhotoUpload} accept="image/*">
              <Button icon={<UploadOutlined />} loading={uploading} style={{ marginTop: 12 }}>
                Upload photo
              </Button>
            </Upload>
          </div>
        </div>

        <Form form={form} layout="vertical" onFinish={onSave}>
          <Form.Item name="firstName" label="First name" rules={[{ required: true, message: 'Required' }]}>
            <Input prefix={<UserOutlined />} />
          </Form.Item>
          <Form.Item name="lastName" label="Last name">
            <Input />
          </Form.Item>
          <Form.Item name="email" label="Email" rules={[{ required: true, type: 'email' }]}>
            <Input prefix={<MailOutlined />} />
          </Form.Item>
          <Form.Item name="phone" label="Phone number" rules={[{ required: true, message: 'Required' }]}>
            <Input prefix={<PhoneOutlined />} />
          </Form.Item>
          {isCustomer && (
            <>
              <Form.Item name="address" label="Address">
                <Input.TextArea rows={2} />
              </Form.Item>
              <Form.Item name="city" label="City">
                <Input />
              </Form.Item>
            </>
          )}
          <Form.Item>
            <Button type="primary" htmlType="submit" loading={saving}>Save profile</Button>
          </Form.Item>
        </Form>
      </Card>
    </div>
  )
}
