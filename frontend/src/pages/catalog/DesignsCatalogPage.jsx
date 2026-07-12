import { useEffect, useState } from 'react'
import {
  Card, Row, Col, Tag, Typography, Input, Select, Empty, Spin, Badge,
  Modal, Form, Button, Descriptions, Space, message, Checkbox, Divider, Alert, InputNumber, Upload, Popconfirm
} from 'antd'
import { SearchOutlined, PlusOutlined, UploadOutlined, PictureOutlined, DeleteOutlined, EditOutlined } from '@ant-design/icons'
import { productionAPI } from '../../api/production'
import { apiErrorMessage } from '../../api/client'
import { useAuth } from '../../context/AuthContext'
import { designImageUrl } from '../../utils/designImage'

const { Title, Text } = Typography
const { Search } = Input

const IMAGE_ACCEPT = 'image/jpeg,image/png,image/webp,image/gif'

function beforeImageUpload(file) {
  const ok = ['image/jpeg', 'image/png', 'image/webp', 'image/gif'].includes(file.type)
  if (!ok) {
    message.error('Only JPG, PNG, WEBP, or GIF images are allowed')
    return Upload.LIST_IGNORE
  }
  if (file.size > 10 * 1024 * 1024) {
    message.error('Image must be 10 MB or smaller')
    return Upload.LIST_IGNORE
  }
  return false
}

export default function DesignsCatalogPage() {
  const { auth } = useAuth()
  const canManage = auth?.role === 'ADMIN' || auth?.role === 'MANAGER'

  const [designs,     setDesigns]     = useState([])
  const [categories,  setCategories]  = useState([])
  const [designTypes, setDesignTypes] = useState([])
  const [loading,     setLoading]     = useState(true)
  const [search,      setSearch]      = useState('')
  const [filterCat,   setFilterCat]   = useState(null)
  const [detail,      setDetail]      = useState(null)
  const [addOpen,     setAddOpen]     = useState(false)
  const [editOpen,    setEditOpen]    = useState(false)
  const [catOpen,     setCatOpen]     = useState(false)
  const [typeOpen,    setTypeOpen]    = useState(false)
  const [form]                        = Form.useForm()
  const [editForm]                    = Form.useForm()
  const [catForm]                     = Form.useForm()
  const [typeForm]                    = Form.useForm()
  const [allStages, setAllStages]     = useState([])
  const [pathStageIds, setPathStageIds] = useState([])
  const [pathLoading, setPathLoading] = useState(false)
  const [pendingImage, setPendingImage] = useState(null)
  const [imageUploading, setImageUploading] = useState(false)

  const load = () => {
    setLoading(true)
    Promise.all([
      productionAPI.getDesigns(),
      productionAPI.getCategories(),
      productionAPI.getDesignTypes(),
    ]).then(([d, c, dt]) => {
      setDesigns(d.data)
      setCategories(c.data)
      setDesignTypes(dt.data)
    }).catch((err) => message.error(apiErrorMessage(err)))
      .finally(() => setLoading(false))
  }

  useEffect(() => { load() }, [])

  const isMandatoryStage = (stage) =>
    stage.isMandatory === true
    || ['Designing', 'Cutting & Stitching', 'Cutting'].includes(stage.stageName)

  const togglePathStage = (stageId, checked) => {
    const stage = allStages.find(s => s.stageId === stageId)
    if (stage && isMandatoryStage(stage)) return
    setPathStageIds(prev =>
      checked ? [...new Set([...prev, stageId])] : prev.filter(id => id !== stageId)
    )
  }

  useEffect(() => {
    if (!detail || !canManage) return
    setPathLoading(true)
    Promise.all([
      productionAPI.getStages(),
      productionAPI.getRequiredStages(detail.designId),
    ]).then(([st, req]) => {
      const sorted = [...st.data].sort((a, b) => (a.stageOrder || 0) - (b.stageOrder || 0))
      setAllStages(sorted)
      const configured = req.data.map(s => s.stageId)
      const mandatory = sorted.filter(isMandatoryStage).map(s => s.stageId)
      setPathStageIds([...new Set([...mandatory, ...configured])])
    }).catch(() => message.error('Could not load production path'))
      .finally(() => setPathLoading(false))
  }, [detail, canManage])

  const saveProductionPath = async () => {
    const ordered = allStages
      .filter(s => isMandatoryStage(s) || pathStageIds.includes(s.stageId))
      .map(s => s.stageId)
    if (ordered.length === 0) {
      message.error('Select at least one production stage')
      return
    }
    try {
      await productionAPI.saveRequiredStages(detail.designId, ordered)
      message.success('Production path saved')
    } catch (err) {
      message.error(err.response?.data?.message || 'Failed to save path')
    }
  }

  const filtered = designs.filter(d => {
    const matchSearch = !search ||
      d.name?.toLowerCase().includes(search.toLowerCase()) ||
      d.designCode?.toLowerCase().includes(search.toLowerCase())
    const matchCat = !filterCat || d.category?.categoryId === filterCat
    return matchSearch && matchCat
  })

  const onAddDesign = async (values) => {
    if (!pendingImage) {
      message.error('Design image is required')
      return
    }
    try {
      const res = await productionAPI.createDesign({
        designCode: values.designCode,
        name: values.name,
        description: values.description,
        basePrice: values.basePrice ?? null,
        category: { categoryId: values.categoryId },
        designType: { designTypeId: values.designTypeId },
        isFeatured: values.isFeatured || false,
      })
      await productionAPI.uploadDesignImage(res.data.designId, pendingImage)
      message.success('Design and image saved')
      setAddOpen(false)
      form.resetFields()
      setPendingImage(null)
      load()
    } catch (err) {
      message.error(err.response?.data?.message || 'Failed to add design')
    }
  }

  const openDetail = async (design) => {
    try {
      const res = await productionAPI.getDesign(design.designId)
      setDetail(res.data)
    } catch {
      setDetail(design)
    }
  }

  const handleDeleteDesign = async (designId) => {
    try {
      await productionAPI.deleteDesign(designId)
      message.success('Design deleted')
      setDetail(null)
      load()
    } catch (err) {
      message.error(err.response?.data?.message || 'Failed to delete design')
    }
  }

  const openEditDesign = () => {
    if (!detail) return
    editForm.setFieldsValue({
      designCode: detail.designCode,
      name: detail.name,
      description: detail.description,
      basePrice: detail.basePrice != null ? Number(detail.basePrice) : undefined,
      categoryId: detail.category?.categoryId,
      designTypeId: detail.designType?.designTypeId,
      isFeatured: detail.isFeatured || false,
    })
    setEditOpen(true)
  }

  const onEditDesign = async (values) => {
    try {
      await productionAPI.updateDesign(detail.designId, {
        designCode: values.designCode,
        name: values.name,
        description: values.description,
        basePrice: values.basePrice,
        category: { categoryId: values.categoryId },
        designType: { designTypeId: values.designTypeId },
        isFeatured: values.isFeatured || false,
      })
      message.success('Design updated')
      setEditOpen(false)
      const refreshed = await productionAPI.getDesign(detail.designId)
      setDetail(refreshed.data)
      load()
    } catch (err) {
      message.error(err.response?.data?.message || 'Failed to update design')
    }
  }

  const handleImageUpload = (designId, file) => {
    const valid = beforeImageUpload(file)
    if (valid === Upload.LIST_IGNORE) return Upload.LIST_IGNORE
    uploadImageForDesign(designId, file)
    return false
  }

  const uploadImageForDesign = async (designId, file) => {
    setImageUploading(true)
    try {
      await productionAPI.uploadDesignImage(designId, file)
      message.success('Image updated')
      const refreshed = await productionAPI.getDesign(designId)
      setDetail(refreshed.data)
      load()
    } catch (err) {
      message.error(err.response?.data?.message || 'Failed to upload image')
    } finally {
      setImageUploading(false)
    }
  }

  const removeDesignImage = async (design) => {
    const img = design.images?.[0]
    if (!img?.designImageId) return
    try {
      await productionAPI.deleteDesignImage(img.designImageId)
      message.success('Image removed')
      const refreshed = await productionAPI.getDesign(design.designId)
      setDetail(refreshed.data)
      load()
    } catch (err) {
      message.error(err.response?.data?.message || 'Failed to remove image')
    }
  }

  const onAddCategory = async (values) => {
    try {
      await productionAPI.createCategory({
        categoryName: values.categoryName,
        description: values.description,
      })
      message.success('Category added')
      setCatOpen(false)
      catForm.resetFields()
      load()
    } catch (err) {
      message.error(err.response?.data?.message || 'Failed to add category')
    }
  }

  const onAddDesignType = async (values) => {
    try {
      await productionAPI.createDesignType({
        typeName: values.typeName,
        description: values.description,
      })
      message.success('Design type added')
      setTypeOpen(false)
      typeForm.resetFields()
      load()
    } catch (err) {
      message.error(err.response?.data?.message || 'Failed to add design type')
    }
  }

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16 }}>
        <Title level={4} className="page-title" style={{ margin: 0 }}>Designs Catalog</Title>
        {canManage && (
          <Space>
            <Button icon={<PlusOutlined />} onClick={() => setCatOpen(true)}>
              Add Category
            </Button>
            <Button icon={<PlusOutlined />} onClick={() => setTypeOpen(true)}>
              Add Design Type
            </Button>
            <Button type="primary" icon={<PlusOutlined />} onClick={() => setAddOpen(true)}>
              Add Design
            </Button>
          </Space>
        )}
      </div>

      <Row gutter={16} style={{ marginBottom: 24 }}>
        <Col xs={24} sm={14}>
          <Search
            placeholder="Search by name or code..."
            prefix={<SearchOutlined />}
            onChange={e => setSearch(e.target.value)}
            allowClear
          />
        </Col>
        <Col xs={24} sm={10}>
          <Select
            placeholder="Filter by category"
            style={{ width: '100%' }}
            allowClear
            onChange={setFilterCat}
          >
            {categories.map(c => (
              <Select.Option key={c.categoryId} value={c.categoryId}>
                {c.categoryName}
              </Select.Option>
            ))}
          </Select>
        </Col>
      </Row>

      {loading ? (
        <div style={{ textAlign: 'center', padding: 60 }}><Spin size="large" /></div>
      ) : filtered.length === 0 ? (
        <Empty description="No designs found" />
      ) : (
        <Row gutter={[16, 16]}>
          {filtered.map(design => {
            const imgUrl = designImageUrl(design)
            return (
              <Col key={design.designId} xs={24} sm={12} md={8} lg={6}>
                <Badge.Ribbon text="Featured" color="gold" style={{ display: design.isFeatured ? undefined : 'none' }}>
                  <Card
                    hoverable
                    onClick={() => openDetail(design)}
                    cover={imgUrl ? (
                      <img alt={design.name} src={imgUrl}
                        style={{ height: 200, objectFit: 'cover' }}
                        onError={e => { e.target.style.display = 'none' }} />
                    ) : (
                      <div style={{
                        height: 200, background: '#f0f2f5',
                        display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#bbb',
                      }}>No Image — {canManage ? 'Upload in details' : 'Click for details'}</div>
                    )}
                  >
                    <Card.Meta
                      title={design.name}
                      description={
                        <>
                          <Text type="secondary" style={{ fontSize: 12 }}>{design.designCode}</Text>
                          <div style={{ marginTop: 8 }}>
                            <Tag color="blue">{design.category?.categoryName}</Tag>
                            <Tag>{design.designType?.typeName}</Tag>
                          </div>
                          {design.basePrice != null && (
                            <div style={{ marginTop: 8 }}>
                              <Text strong>Rs. {Number(design.basePrice).toLocaleString()}</Text>
                            </div>
                          )}
                        </>
                      }
                    />
                  </Card>
                </Badge.Ribbon>
              </Col>
            )
          })}
        </Row>
      )}

      <Modal
        title={detail?.name || 'Design Details'}
        open={!!detail}
        onCancel={() => setDetail(null)}
        footer={canManage ? (
          <Space style={{ width: '100%', justifyContent: 'space-between' }}>
            <Space>
              <Button icon={<EditOutlined />} onClick={openEditDesign}>Edit design</Button>
              <Popconfirm
                title="Delete this design?"
                description="Designs with suits, stock, or production cannot be deleted."
                onConfirm={() => handleDeleteDesign(detail.designId)}
                okText="Delete"
                okButtonProps={{ danger: true }}
              >
                <Button danger icon={<DeleteOutlined />}>Delete design</Button>
              </Popconfirm>
            </Space>
            <Button onClick={() => setDetail(null)}>Close</Button>
          </Space>
        ) : null}
        width={560}
      >
        {detail && (
          <>
            <div style={{ marginBottom: 16 }} onClick={e => e.stopPropagation()}>
              {designImageUrl(detail) ? (
                <img src={designImageUrl(detail)} alt={detail.name}
                  style={{ width: '100%', maxHeight: 240, objectFit: 'cover', borderRadius: 8 }} />
              ) : (
                <div style={{
                  height: 180, background: '#fafafa', border: '1px dashed #d9d9d9',
                  borderRadius: 8, display: 'flex', alignItems: 'center', justifyContent: 'center',
                  color: '#999', flexDirection: 'column', gap: 8,
                }}>
                  <PictureOutlined style={{ fontSize: 28 }} />
                  <Text type="secondary">No image uploaded yet</Text>
                </div>
              )}
              {canManage ? (
                <Space style={{ marginTop: 12 }} wrap>
                  <Upload
                    accept={IMAGE_ACCEPT}
                    showUploadList={false}
                    beforeUpload={(file) => handleImageUpload(detail.designId, file)}
                  >
                    <Button type="primary" icon={<UploadOutlined />} loading={imageUploading}>
                      {designImageUrl(detail) ? 'Replace image' : 'Upload image'}
                    </Button>
                  </Upload>
                  {designImageUrl(detail) && (
                    <Popconfirm
                      title="Remove this image?"
                      onConfirm={() => removeDesignImage(detail)}
                      okText="Remove"
                      okButtonProps={{ danger: true }}
                    >
                      <Button danger icon={<DeleteOutlined />}>Remove image</Button>
                    </Popconfirm>
                  )}
                </Space>
              ) : (
                <Alert type="info" showIcon style={{ marginTop: 12 }}
                  message="Only Admin/Manager can upload or replace design images." />
              )}
            </div>
            <Descriptions bordered column={1} size="small">
              <Descriptions.Item label="Design Code">{detail.designCode}</Descriptions.Item>
              <Descriptions.Item label="Name">{detail.name}</Descriptions.Item>
              <Descriptions.Item label="Category">{detail.category?.categoryName}</Descriptions.Item>
              <Descriptions.Item label="Type">{detail.designType?.typeName}</Descriptions.Item>
              {detail.embroideryType && (
                <Descriptions.Item label="Embroidery">{detail.embroideryType.name}</Descriptions.Item>
              )}
              <Descriptions.Item label="Description">{detail.description || '—'}</Descriptions.Item>
              {detail.basePrice != null && (
                <Descriptions.Item label="Base Price">
                  Rs. {Number(detail.basePrice).toLocaleString()}
                </Descriptions.Item>
              )}
            </Descriptions>
            {canManage && (
              <>
                <Divider orientation="left" style={{ marginTop: 20 }}>Production path</Divider>
                <Alert type="info" showIcon style={{ marginBottom: 12 }}
                  message="Designing → (optional Filling) → Cutting & Stitching"
                  description="Cutting and stitching are one combined stage. Enable Filling only when needed." />
                {pathLoading ? <Spin /> : allStages.length === 0 ? (
                  <Alert type="warning" message="No production stages in database. Run seed-production-flow.sql." />
                ) : (
                  <>
                    <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
                      {allStages.map(s => {
                        const mandatory = isMandatoryStage(s)
                        const checked = mandatory || pathStageIds.includes(s.stageId)
                        return (
                          <Checkbox
                            key={s.stageId}
                            checked={checked}
                            disabled={mandatory}
                            onChange={e => togglePathStage(s.stageId, e.target.checked)}
                          >
                            {s.stageOrder}. {s.stageName}
                            <Tag style={{ marginLeft: 8 }}
                              color={mandatory ? 'blue' : 'default'}>
                              {mandatory ? 'Required' : 'Optional'}
                            </Tag>
                          </Checkbox>
                        )
                      })}
                    </div>
                    <Button type="primary" size="small" style={{ marginTop: 12 }}
                      onClick={saveProductionPath}>
                      Save production path
                    </Button>
                  </>
                )}
              </>
            )}
          </>
        )}
      </Modal>

      <Modal title="Edit Design" open={editOpen}
        onCancel={() => setEditOpen(false)} footer={null}>
        <Form form={editForm} onFinish={onEditDesign} layout="vertical">
          <Form.Item name="designCode" label="Design Code" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="name" label="Design Name" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="categoryId" label="Category" rules={[{ required: true }]}>
            <Select placeholder="Select category">
              {categories.map(c => (
                <Select.Option key={c.categoryId} value={c.categoryId}>{c.categoryName}</Select.Option>
              ))}
            </Select>
          </Form.Item>
          <Form.Item name="designTypeId" label="Design Type" rules={[{ required: true }]}>
            <Select placeholder="Select type">
              {designTypes.map(t => (
                <Select.Option key={t.designTypeId} value={t.designTypeId}>{t.typeName}</Select.Option>
              ))}
            </Select>
          </Form.Item>
          <Form.Item name="description" label="Description">
            <Input.TextArea rows={3} />
          </Form.Item>
          <Form.Item name="basePrice" label="Price (Rs.)" rules={[{ required: true, message: 'Enter design price' }]}>
            <InputNumber min={1} style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="isFeatured" valuePropName="checked">
            <Checkbox>Featured design</Checkbox>
          </Form.Item>
          <Form.Item>
            <Space>
              <Button type="primary" htmlType="submit">Save changes</Button>
              <Button onClick={() => setEditOpen(false)}>Cancel</Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>

      <Modal title="Add New Design" open={addOpen}
        onCancel={() => { setAddOpen(false); setPendingImage(null) }} footer={null}>
        <Form form={form} onFinish={onAddDesign} layout="vertical">
          <Form.Item name="designCode" label="Design Code" rules={[{ required: true }]}>
            <Input placeholder="e.g. DC-2026-002" />
          </Form.Item>
          <Form.Item name="name" label="Design Name" rules={[{ required: true }]}>
            <Input placeholder="e.g. Bridal Suit A" />
          </Form.Item>
          <Form.Item name="categoryId" label="Category" rules={[{ required: true }]}>
            <Select placeholder="Ladies, Kids, etc."
              dropdownRender={menu => (
                <>
                  {menu}
                  {canManage && (
                    <div style={{ padding: '8px 12px', borderTop: '1px solid #f0f0f0' }}>
                      <Button type="link" size="small" icon={<PlusOutlined />}
                        onClick={() => { setAddOpen(false); setCatOpen(true) }}>
                        Add new category
                      </Button>
                    </div>
                  )}
                </>
              )}>
              {categories.map(c => (
                <Select.Option key={c.categoryId} value={c.categoryId}>{c.categoryName}</Select.Option>
              ))}
            </Select>
          </Form.Item>
          <Form.Item name="designTypeId" label="Design Type" rules={[{ required: true }]}>
            <Select placeholder="Formal, Casual, etc."
              dropdownRender={menu => (
                <>
                  {menu}
                  {canManage && (
                    <div style={{ padding: '8px 12px', borderTop: '1px solid #f0f0f0' }}>
                      <Button type="link" size="small" icon={<PlusOutlined />}
                        onClick={() => { setAddOpen(false); setTypeOpen(true) }}>
                        Add new design type
                      </Button>
                    </div>
                  )}
                </>
              )}>
              {designTypes.map(t => (
                <Select.Option key={t.designTypeId} value={t.designTypeId}>{t.typeName}</Select.Option>
              ))}
            </Select>
          </Form.Item>
          <Form.Item name="description" label="Description">
            <Input.TextArea rows={3} />
          </Form.Item>
          <Form.Item name="basePrice" label="Price (Rs.)" rules={[{ required: true, message: 'Enter design price' }]}>
            <InputNumber min={1} style={{ width: '100%' }} placeholder="Selling price for this design" />
          </Form.Item>
          <Form.Item label="Design Image" required>
            <Upload
              accept={IMAGE_ACCEPT}
              maxCount={1}
              listType="picture-card"
              beforeUpload={beforeImageUpload}
              onRemove={() => setPendingImage(null)}
              onChange={({ fileList }) => {
                setPendingImage(fileList[0]?.originFileObj || null)
              }}
            >
              {!pendingImage && (
                <div>
                  <PlusOutlined />
                  <div style={{ marginTop: 8 }}>Upload</div>
                </div>
              )}
            </Upload>
            <Text type="secondary" style={{ fontSize: 12 }}>JPG, PNG, WEBP, or GIF — max 10 MB</Text>
          </Form.Item>
          <Form.Item>
            <Space>
              <Button type="primary" htmlType="submit">Save Design</Button>
              <Button onClick={() => { setAddOpen(false); setPendingImage(null) }}>Cancel</Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>

      <Modal title="Add Category" open={catOpen}
        onCancel={() => setCatOpen(false)} footer={null}>
        <Form form={catForm} onFinish={onAddCategory} layout="vertical">
          <Form.Item name="categoryName" label="Category Name" rules={[{ required: true }]}>
            <Input placeholder="e.g. Ladies, Kids" />
          </Form.Item>
          <Form.Item name="description" label="Description">
            <Input.TextArea rows={2} placeholder="Optional description" />
          </Form.Item>
          <Form.Item>
            <Space>
              <Button type="primary" htmlType="submit">Save Category</Button>
              <Button onClick={() => setCatOpen(false)}>Cancel</Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>

      <Modal title="Add Design Type" open={typeOpen}
        onCancel={() => setTypeOpen(false)} footer={null}>
        <Form form={typeForm} onFinish={onAddDesignType} layout="vertical">
          <Form.Item name="typeName" label="Type Name" rules={[{ required: true }]}>
            <Input placeholder="e.g. Formal, Bridal, Casual" />
          </Form.Item>
          <Form.Item name="description" label="Description">
            <Input.TextArea rows={2} placeholder="Optional description" />
          </Form.Item>
          <Form.Item>
            <Space>
              <Button type="primary" htmlType="submit">Save Design Type</Button>
              <Button onClick={() => setTypeOpen(false)}>Cancel</Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )
}
