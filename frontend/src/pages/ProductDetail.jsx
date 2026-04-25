import React, { useState, useEffect } from 'react'
import {
  Row,
  Col,
  Card,
  Image,
  Descriptions,
  Button,
  Tag,
  Avatar,
  Divider,
  Space,
  message,
  Modal,
  Form,
  Input,
  Popconfirm,
  Badge,
} from 'antd'
import {
  HeartOutlined,
  HeartFilled,
  ShoppingCartOutlined,
  MessageOutlined,
  FlagOutlined,
  SwapOutlined,
  EyeOutlined,
  ArrowLeftOutlined,
} from '@ant-design/icons'
import { useParams, useNavigate } from 'react-router-dom'
import { getProductDetail } from '../api/product'
import { toggleFavorite, checkFavorite } from '../api/favorite'
import { sendMessage } from '../api/message'
import { createOrder } from '../api/order'
import useStore from '../store'
import dayjs from 'dayjs'

const ProductDetail = () => {
  const { id } = useParams()
  const navigate = useNavigate()
  const user = useStore((state) => state.user)
  const token = useStore((state) => state.token)
  const [loading, setLoading] = useState(false)
  const [product, setProduct] = useState(null)
  const [isFavorite, setIsFavorite] = useState(false)
  const [messageModalVisible, setMessageModalVisible] = useState(false)
  const [orderModalVisible, setOrderModalVisible] = useState(false)
  const [messageForm] = Form.useForm()
  const [orderForm] = Form.useForm()
  const [submitting, setSubmitting] = useState(false)

  useEffect(() => {
    if (id) {
      fetchProductDetail()
      if (token) {
        checkIsFavorite()
      }
    }
  }, [id, token])

  const fetchProductDetail = async () => {
    setLoading(true)
    try {
      const res = await getProductDetail(id)
      setProduct(res.data)
    } catch (error) {
      console.error('获取商品详情失败', error)
    } finally {
      setLoading(false)
    }
  }

  const checkIsFavorite = async () => {
    try {
      const res = await checkFavorite(id)
      setIsFavorite(res.data.isFavorite)
    } catch (error) {
      console.error('检查收藏状态失败', error)
    }
  }

  const handleToggleFavorite = async () => {
    if (!token) {
      message.warning('请先登录')
      navigate('/login')
      return
    }
    try {
      await toggleFavorite(id)
      setIsFavorite(!isFavorite)
      message.success(isFavorite ? '已取消收藏' : '已收藏')
    } catch (error) {
      console.error('收藏失败', error)
    }
  }

  const handleSendMessage = async (values) => {
    setSubmitting(true)
    try {
      await sendMessage({
        productId: product.id,
        toUserId: product.seller.id,
        content: values.content,
      })
      message.success('消息发送成功')
      setMessageModalVisible(false)
      messageForm.resetFields()
    } catch (error) {
      console.error('发送消息失败', error)
    } finally {
      setSubmitting(false)
    }
  }

  const handleCreateOrder = async (values) => {
    setSubmitting(true)
    try {
      const res = await createOrder({
        productId: product.id,
        ...values,
      })
      message.success('订单创建成功')
      setOrderModalVisible(false)
      navigate(`/order/${res.data.orderNo}`)
    } catch (error) {
      console.error('创建订单失败', error)
    } finally {
      setSubmitting(false)
    }
  }

  const getStatusTag = (status) => {
    const statusMap = {
      1: { color: 'green', text: '在售' },
      2: { color: 'gray', text: '已售出' },
      0: { color: 'orange', text: '下架' },
    }
    const info = statusMap[status] || { color: 'default', text: '未知' }
    return <Tag color={info.color}>{info.text}</Tag>
  }

  const isOwnProduct = user && product && user.id === product.userId

  return (
    <div>
      <Button
        icon={<ArrowLeftOutlined />}
        onClick={() => navigate(-1)}
        style={{ marginBottom: 16 }}
      >
        返回
      </Button>

      <Card loading={loading}>
        {product && (
          <Row gutter={24}>
            <Col xs={24} md={12}>
              <Image.PreviewGroup>
                <Image
                  src={
                    product.coverImage ||
                    'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=default%20product%20placeholder&image_size=square'
                  }
                  alt={product.title}
                  style={{ width: '100%', borderRadius: 8 }}
                />
              </Image.PreviewGroup>
              {product.images && product.images.length > 0 && (
                <Row gutter={8} style={{ marginTop: 16 }}>
                  {product.images.slice(0, 5).map((img, index) => (
                    <Col key={index} span={4}>
                      <Image
                        src={img}
                        style={{ width: '100%', height: 60, objectFit: 'cover', borderRadius: 4 }}
                      />
                    </Col>
                  ))}
                </Row>
              )}
            </Col>

            <Col xs={24} md={12}>
              <div>
                <Space size="middle" wrap>
                  <h2 style={{ margin: 0 }}>{product.title}</h2>
                  {getStatusTag(product.status)}
                </Space>
              </div>

              <div style={{ marginTop: 16 }}>
                <span className="product-price" style={{ fontSize: 28 }}>
                  ¥{product.price?.toFixed(2)}
                </span>
                {product.originalPrice && (
                  <span className="product-original-price" style={{ fontSize: 16 }}>
                    原价 ¥{product.originalPrice?.toFixed(2)}
                  </span>
                )}
              </div>

              <Divider />

              <Descriptions column={1} size="small">
                <Descriptions.Item label="分类">
                  <Tag>{product.category}</Tag>
                </Descriptions.Item>
                <Descriptions.Item label="成色">
                  {product.condition || '未填'}
                </Descriptions.Item>
                <Descriptions.Item label="交易方式">
                  {product.isNegotiable ? '可议价' : '不议价'}
                </Descriptions.Item>
                <Descriptions.Item label="发布时间">
                  {dayjs(product.createTime).format('YYYY-MM-DD HH:mm')}
                </Descriptions.Item>
                <Descriptions.Item label="浏览">
                  <EyeOutlined /> {product.viewCount}
                </Descriptions.Item>
                <Descriptions.Item label="收藏">
                  <HeartOutlined /> {product.favoriteCount}
                </Descriptions.Item>
              </Descriptions>

              {product.tags && (
                <div style={{ marginTop: 16 }}>
                  标签：
                  {product.tags.split(',').map((tag, index) => (
                    <Tag key={index}>{tag}</Tag>
                  ))}
                </div>
              )}

              <Divider />

              <Card size="small" title="商品描述">
                <p style={{ whiteSpace: 'pre-wrap' }}>{product.description}</p>
              </Card>

              <Divider />

              {product.seller && (
                <Card size="small">
                  <Space size="middle">
                    <Avatar size={64} src={product.seller.avatar}>
                      {product.seller.nickname?.[0]}
                    </Avatar>
                    <div>
                      <div style={{ fontWeight: 'bold', fontSize: 16 }}>
                        {product.seller.nickname}
                      </div>
                      <div style={{ color: '#666' }}>
                        信誉分：<Badge status="success" text={product.seller.creditScore} />
                      </div>
                      <div style={{ color: '#666' }}>
                        学校：{product.seller.school || '未填'}
                      </div>
                    </div>
                  </Space>
                </Card>
              )}

              <div style={{ marginTop: 24 }}>
                <Space size="middle" wrap>
                  {!isOwnProduct && product.status === 1 && (
                    <>
                      <Button
                        type="primary"
                        size="large"
                        icon={<ShoppingCartOutlined />}
                        onClick={() => setOrderModalVisible(true)}
                      >
                        立即购买
                      </Button>
                      <Button
                        size="large"
                        icon={<MessageOutlined />}
                        onClick={() => setMessageModalVisible(true)}
                      >
                        留言咨询
                      </Button>
                      <Button
                        size="large"
                        icon={<SwapOutlined />}
                        onClick={() =>
                          navigate(
                            `/exchange?targetId=${product.seller.id}&productId=${product.id}`
                          )
                        }
                      >
                        换物申请
                      </Button>
                      <Button
                        size="large"
                        icon={<FlagOutlined />}
                        onClick={() =>
                          navigate(
                            `/report?userId=${product.seller.id}&productId=${product.id}`
                          )
                        }
                      >
                        举报
                      </Button>
                    </>
                  )}
                  <Button
                    size="large"
                    icon={isFavorite ? <HeartFilled style={{ color: '#ff4d4f' }} /> : <HeartOutlined />}
                    onClick={handleToggleFavorite}
                  >
                    {isFavorite ? '已收藏' : '收藏'}
                  </Button>
                </Space>
              </div>
            </Col>
          </Row>
        )}
      </Card>

      <Modal
        title="留言咨询"
        open={messageModalVisible}
        onCancel={() => setMessageModalVisible(false)}
        footer={null}
      >
        <Form form={messageForm} onFinish={handleSendMessage}>
          <Form.Item
            name="content"
            rules={[{ required: true, message: '请输入留言内容' }]}
          >
            <Input.TextArea
              rows={4}
              placeholder="请输入您想咨询的内容..."
            />
          </Form.Item>
          <Form.Item>
            <Button type="primary" htmlType="submit" loading={submitting} block>
              发送
            </Button>
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title="确认下单"
        open={orderModalVisible}
        onCancel={() => setOrderModalVisible(false)}
        footer={null}
      >
        <Form form={orderForm} onFinish={handleCreateOrder}>
          <Form.Item
            name="buyerName"
            label="收货人姓名"
            rules={[{ required: true, message: '请输入收货人姓名' }]}
          >
            <Input placeholder="请输入收货人姓名" />
          </Form.Item>
          <Form.Item
            name="buyerPhone"
            label="联系电话"
            rules={[
              { required: true, message: '请输入联系电话' },
              { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确' },
            ]}
          >
            <Input placeholder="请输入联系电话" />
          </Form.Item>
          <Form.Item
            name="buyerAddress"
            label="收货地址"
            rules={[{ required: true, message: '请输入收货地址' }]}
          >
            <Input.TextArea rows={3} placeholder="请输入详细收货地址" />
          </Form.Item>
          <div style={{ marginBottom: 16, padding: 16, background: '#f5f5f5', borderRadius: 8 }}>
            <div style={{ fontWeight: 'bold', marginBottom: 8 }}>订单金额</div>
            <div className="product-price" style={{ fontSize: 24 }}>
              ¥{product?.price?.toFixed(2)}
            </div>
          </div>
          <Form.Item>
            <Button type="primary" htmlType="submit" loading={submitting} block>
              确认下单
            </Button>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )
}

export default ProductDetail
