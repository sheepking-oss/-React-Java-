import React, { useState, useEffect } from 'react'
import {
  Form,
  Input,
  InputNumber,
  Select,
  Button,
  Card,
  Upload,
  message,
  Space,
  Row,
  Col,
  Radio,
  Switch,
  Divider,
} from 'antd'
import { PlusOutlined, DeleteOutlined } from '@ant-design/icons'
import { useNavigate, useParams } from 'react-router-dom'
import { publishProduct, updateProduct, getCategories, getProductDetail, uploadImage } from '../api/product'

const { TextArea } = Input
const { Option } = Select

const PublishProduct = () => {
  const navigate = useNavigate()
  const { id } = useParams()
  const [form] = Form.useForm()
  const [loading, setLoading] = useState(false)
  const [categories, setCategories] = useState([])
  const [fileList, setFileList] = useState([])

  useEffect(() => {
    fetchCategories()
    if (id) {
      fetchProductDetail()
    }
  }, [id])

  const fetchCategories = async () => {
    try {
      const res = await getCategories()
      setCategories(res.data)
    } catch (error) {
      console.error('获取分类失败', error)
    }
  }

  const fetchProductDetail = async () => {
    setLoading(true)
    try {
      const res = await getProductDetail(id)
      const product = res.data
      form.setFieldsValue({
        ...product,
        images: product.images?.map((url, index) => ({
        uid: index.toString(),
        name: `image-${index}`,
        status: 'done',
        url: url,
      })),
    })
      setFileList(
        product.images?.map((url, index) => ({
          uid: index.toString(),
          name: `image-${index}`,
          status: 'done',
          url: url,
        })) || []
      )
    } catch (error) {
      console.error('获取商品详情失败', error)
    } finally {
      setLoading(false)
    }
  }

  const uploadProps = {
    listType: 'picture-card',
    fileList: fileList,
    maxCount: 9,
    accept: 'image/*',
    onChange: ({ fileList: newFileList }) => {
      setFileList(newFileList)
    },
    customRequest: async ({ file, onSuccess, onError }) => {
      try {
        const res = await uploadImage(file)
        onSuccess?.()
        const newFileList = [...fileList, {
          uid: res.data.filename,
          name: file.name,
          status: 'done',
          url: res.data.url,
        }]
        setFileList(newFileList)
      } catch (error) {
        onError?.(error)
        message.error('图片上传失败')
      }
    },
  }

  const handleSubmit = async (values) => {
    setLoading(true)
    try {
      const images = fileList
        .filter((file) => file.status === 'done')
        .map((file) => file.url)

      const data = {
        ...values,
        images: images,
        coverImage: images[0] || '',
      }

      if (id) {
        data.id = parseInt(id)
        await updateProduct(data)
        message.success('商品更新成功')
      } else {
        await publishProduct(data)
        message.success('商品发布成功')
      }
      navigate('/my-products')
    } catch (error) {
      console.error('发布商品失败', error)
    } finally {
      setLoading(false)
    }
  }

  const conditionOptions = [
    { label: '全新', value: '全新' },
    { label: '几乎全新', value: '几乎全新' },
    { label: '轻微使用', value: '轻微使用' },
    { label: '明显使用痕迹', value: '明显使用痕迹' },
    { label: '较旧', value: '较旧' },
  ]

  return (
    <div>
      <Card title={id ? '编辑商品' : '发布商品'}>
        <Form
          form={form}
          layout="vertical"
          onFinish={handleSubmit}
          initialValues={{
            isNegotiable: 0,
          }}
        >
          <Row gutter={24}>
            <Col xs={24} md={16}>
              <Form.Item
                name="title"
                label="商品标题"
                rules={[{ required: true, message: '请输入商品标题' }]}
              >
                <Input placeholder="请输入商品标题，最多50个字" maxLength={50} />
              </Form.Item>

              <Form.Item
                name="description"
                label="商品描述"
                rules={[{ required: true, message: '请输入商品描述' }]}
              >
                <TextArea
                rows={6}
                placeholder="请详细描述商品的情况、使用痕迹、购买时间、交易方式等信息"
              />
              </Form.Item>

              <Row gutter={24}>
                <Col span={12}>
                  <Form.Item
                    name="price"
                    label="售价"
                    rules={[{ required: true, message: '请输入售价' }]}
                  >
                    <InputNumber
                      style={{ width: '100%' }}
                      placeholder="请输入售价"
                      min={0}
                      precision={2}
                      prefix="¥"
                    />
                  </Form.Item>
                </Col>
                <Col span={12}>
                  <Form.Item name="originalPrice" label="原价">
                    <InputNumber
                      style={{ width: '100%' }}
                      placeholder="请输入原价（选填）"
                      min={0}
                      precision={2}
                      prefix="¥"
                    />
                  </Form.Item>
                </Col>
              </Row>

              <Row gutter={24}>
                <Col span={12}>
                  <Form.Item
                    name="category"
                    label="商品分类"
                    rules={[{ required: true, message: '请选择商品分类' }]}
                  >
                    <Select placeholder="请选择商品分类">
                      {categories.map((cat) => (
                      <Option key={cat} value={cat}>
                        {cat}
                      </Option>
                    ))}
                    </Select>
                  </Form.Item>
                </Col>
                <Col span={12}>
                  <Form.Item name="condition" label="商品成色">
                    <Select placeholder="请选择商品成色">
                      {conditionOptions.map((opt) => (
                      <Option key={opt.value} value={opt.value}>
                        {opt.label}
                      </Option>
                    ))}
                    </Select>
                  </Form.Item>
                </Col>
              </Row>

              <Form.Item name="location" label="交易地点">
                <Input placeholder="请输入交易地点，如：XX校区宿舍" />
              </Form.Item>

              <Form.Item name="tags" label="商品标签">
                <Input placeholder="请输入商品标签，多个标签用逗号分隔" />
              </Form.Item>

              <Form.Item
                name="isNegotiable"
                label="是否可议价"
                valuePropName="checked"
              >
                <Switch
                  checkedChildren="可议价"
                  unCheckedChildren="不议价"
                />
              </Form.Item>
            </Col>

            <Col xs={24} md={8}>
              <Form.Item
                label="商品图片"
                help="请上传商品图片，最多9张，第一张将作为封面"
              >
                <Upload {...uploadProps}>
                  {fileList.length >= 9 ? null : (
                    <div>
                      <PlusOutlined />
                      <div style={{ marginTop: 8 }}>上传图片</div>
                    </div>
                  )}
                </Upload>
              </Form.Item>
            </Col>
          </Row>

          <Divider />

          <Form.Item>
            <Space>
              <Button type="primary" htmlType="submit" loading={loading} size="large">
                {id ? '保存修改' : '发布商品'}
              </Button>
              <Button size="large" onClick={() => navigate(-1)}>
                取消
              </Button>
            </Space>
          </Form.Item>
        </Form>
      </Card>
    </div>
  )
}

export default PublishProduct
