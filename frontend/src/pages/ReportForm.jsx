import React, { useState, useEffect } from 'react'
import {
  Card,
  Form,
  Input,
  Select,
  Button,
  Upload,
  message,
  Row,
  Col,
  Divider,
} from 'antd'
import { ArrowLeftOutlined, PlusOutlined } from '@ant-design/icons'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { createReport, uploadImage } from '../api/report'
import { getProductDetail } from '../api/product'
import dayjs from 'dayjs'

const { Option } = Select
const { TextArea } = Input

const ReportForm = () => {
  const navigate = useNavigate()
  const [searchParams] = useSearchParams()
  const reportedUserId = searchParams.get('userId') ? parseInt(searchParams.get('userId')) : null
  const productId = searchParams.get('productId') ? parseInt(searchParams.get('productId')) : null
  const [form] = Form.useForm()
  const [loading, setLoading] = useState(false)
  const [product, setProduct] = useState(null)
  const [fileList, setFileList] = useState([])
  const [submitting, setSubmitting] = useState(false)

  useEffect(() => {
    if (productId) {
      fetchProduct()
    }
  }, [productId])

  const fetchProduct = async () => {
    setLoading(true)
    try {
      const res = await getProductDetail(productId)
      setProduct(res.data)
      form.setFieldsValue({
        reportedUserId: res.data.seller?.id,
        productId: productId,
      })
    } catch (error) {
      console.error('获取商品详情失败', error)
    } finally {
      setLoading(false)
    }
  }

  const uploadProps = {
    listType: 'picture-card',
    fileList: fileList,
    maxCount: 3,
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
    setSubmitting(true)
    try {
      const images = fileList
        .filter((file) => file.status === 'done')
        .map((file) => file.url)

      await createReport({
        ...values,
        imageUrls: images,
      })
      message.success('举报已提交，我们会尽快处理')
      navigate('/my-reports')
    } catch (error) {
      console.error('提交举报失败', error)
    } finally {
      setSubmitting(false)
    }
  }

  const reportTypes = [
    { label: '虚假信息', value: 'FAKE' },
    { label: '骚扰/辱骂', value: 'HARASSMENT' },
    { label: '欺诈行为', value: 'FRAUD' },
    { label: '违禁品', value: 'ILLEGAL' },
    { label: '盗图/抄袭', value: 'COPYRIGHT' },
    { label: '其他', value: 'OTHER' },
  ]

  return (
    <div>
      <Button
        icon={<ArrowLeftOutlined />}
        onClick={() => navigate(-1)}
        style={{ marginBottom: 16 }}
      >
        返回
      </Button>

      <Card title="发起举报">
        {product && (
          <>
            <Card size="small" style={{ marginBottom: 16 }}>
              <Row gutter={16}>
                <Col span={4}>
                  <img
                    src={
                      product.coverImage ||
                      'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=default%20product%20placeholder&image_size=square'
                    }
                    style={{ width: 80, height: 80, objectFit: 'cover', borderRadius: 4 }}
                  />
                </Col>
                <Col span={20}>
                  <div style={{ fontWeight: 'bold', fontSize: 16 }}>{product.title}</div>
                  <div className="product-price" style={{ fontSize: 16 }}>
                    ¥{product.price?.toFixed(2)}
                  </div>
                  <div style={{ color: '#666' }}>
                    卖家：{product.seller?.nickname}
                  </div>
                </Col>
              </Row>
            </Card>
            <Divider />
          </>
        )}

        <Form
          form={form}
          layout="vertical"
          onFinish={handleSubmit}
          initialValues={{
            reportedUserId: reportedUserId,
            productId: productId,
          }}
        >
          <Form.Item
            name="type"
            label="举报类型"
            rules={[{ required: true, message: '请选择举报类型' }]}
          >
            <Select placeholder="请选择举报类型">
              {reportTypes.map((type) => (
                <Option key={type.value} value={type.value}>
                  {type.label}
                </Option>
              ))}
            </Select>
          </Form.Item>

          <Form.Item
            name="reason"
            label="举报原因"
            rules={[{ required: true, message: '请输入举报原因' }]}
          >
            <TextArea
              rows={4}
              placeholder="请详细描述举报的原因和情况..."
            />
          </Form.Item>

          <Form.Item label="上传证据（选填）" help="最多上传3张图片作为证据">
            <Upload {...uploadProps}>
              {fileList.length >= 3 ? null : (
                <div>
                  <PlusOutlined />
                  <div style={{ marginTop: 8 }}>上传图片</div>
                </div>
              )}
            </Upload>
          </Form.Item>

          <Form.Item>
            <Button type="primary" htmlType="submit" loading={submitting} size="large">
              提交举报
            </Button>
          </Form.Item>
        </Form>
      </Card>
    </div>
  )
}

export default ReportForm
