import React, { useState, useEffect } from 'react'
import {
  Card,
  Descriptions,
  Button,
  Tag,
  Image,
  Space,
  Divider,
  message,
  Row,
  Col,
  Modal,
  Form,
  Input,
  InputNumber,
  Radio,
  Rate,
  Upload,
} from 'antd'
import { useParams, useNavigate } from 'react-router-dom'
import { getOrderDetail, payOrder, shipOrder, confirmOrder, cancelOrder } from '../api/order'
import { createReview, getReviewByOrderId, uploadImage } from '../api/review'
import dayjs from 'dayjs'

const { TextArea } = Input

const OrderDetail = () => {
  const { orderNo } = useParams()
  const navigate = useNavigate()
  const [loading, setLoading] = useState(false)
  const [order, setOrder] = useState(null)
  const [review, setReview] = useState(null)
  const [reviewModalVisible, setReviewModalVisible] = useState(false)
  const [reviewForm] = Form.useForm()
  const [submitting, setSubmitting] = useState(false)
  const [fileList, setFileList] = useState([])

  useEffect(() => {
    if (orderNo) {
      fetchOrderDetail()
    }
  }, [orderNo])

  const fetchOrderDetail = async () => {
    setLoading(true)
    try {
      const res = await getOrderDetail(orderNo)
      setOrder(res.data)
      
      if (res.data.status === 'COMPLETED') {
        fetchReview()
      }
    } catch (error) {
      console.error('获取订单详情失败', error)
    } finally {
      setLoading(false)
    }
  }

  const fetchReview = async () => {
    try {
      const res = await getReviewByOrderId(order.id)
      setReview(res.data)
    } catch (error) {
      console.error('获取评价失败', error)
    }
  }

  const handlePay = async () => {
    try {
      await payOrder(orderNo)
      message.success('支付成功')
      fetchOrderDetail()
    } catch (error) {
      console.error('支付失败', error)
    }
  }

  const handleShip = async () => {
    try {
      await shipOrder(orderNo)
      message.success('发货成功')
      fetchOrderDetail()
    } catch (error) {
      console.error('发货失败', error)
    }
  }

  const handleConfirm = async () => {
    try {
      await confirmOrder(orderNo)
      message.success('确认收货成功')
      fetchOrderDetail()
    } catch (error) {
      console.error('确认收货失败', error)
    }
  }

  const handleCancel = async () => {
    try {
      await cancelOrder(orderNo, '用户取消')
      message.success('订单已取消')
      fetchOrderDetail()
    } catch (error) {
      console.error('取消订单失败', error)
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

  const handleReview = async (values) => {
    setSubmitting(true)
    try {
      const images = fileList
        .filter((file) => file.status === 'done')
        .map((file) => file.url)

      await createReview({
        orderId: order.id,
        rating: values.rating,
        content: values.content,
        imageUrls: images,
        isAnonymous: values.isAnonymous ? 1 : 0,
      })
      message.success('评价成功')
      setReviewModalVisible(false)
      fetchOrderDetail()
    } catch (error) {
      console.error('评价失败', error)
    } finally {
      setSubmitting(false)
    }
  }

  const getStatusTag = (status) => {
    const statusMap = {
      PENDING: { color: 'orange', text: '待支付' },
      PAID: { color: 'blue', text: '已支付' },
      SHIPPED: { color: 'processing', text: '已发货' },
      COMPLETED: { color: 'green', text: '已完成' },
      CANCELLED: { color: 'default', text: '已取消' },
    }
    const info = statusMap[status] || { color: 'default', text: '未知' }
    return <Tag color={info.color}>{info.text}</Tag>
  }

  const renderActions = () => {
    if (!order) return null

    const actions = []

    switch (order.status) {
      case 'PENDING':
        if (order.buyer.id === order.buyerId) {
          actions.push(
            <Button key="pay" type="primary" onClick={handlePay}>
              立即支付
            </Button>
          )
          actions.push(
            <Button key="cancel" danger onClick={handleCancel}>
              取消订单
            </Button>
          )
        }
        break
      case 'PAID':
        if (order.seller.id === order.sellerId) {
          actions.push(
            <Button key="ship" type="primary" onClick={handleShip}>
              确认发货
            </Button>
          )
        }
        break
      case 'SHIPPED':
        if (order.buyer.id === order.buyerId) {
          actions.push(
            <Button key="confirm" type="primary" onClick={handleConfirm}>
              确认收货
            </Button>
          )
        }
        break
      case 'COMPLETED':
        if (order.buyer.id === order.buyerId && !review) {
          actions.push(
            <Button key="review" type="primary" onClick={() => setReviewModalVisible(true)}>
              去评价
            </Button>
          )
        }
        break
    }

    return actions.length > 0 ? <Space>{actions}</Space> : null
  }

  return (
    <div>
      <Card loading={loading}>
        {order && (
          <div>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <div>
                <span style={{ fontSize: 16, marginRight: 16 }}>订单号：{order.orderNo}</span>
                {getStatusTag(order.status)}
              </div>
              {renderActions()}
            </div>

            <Divider />

            <Card size="small" title="商品信息">
              <Row gutter={16}>
                <Col span={4}>
                  <Image
                    src={
                      order.product?.coverImage ||
                      'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=default%20product%20placeholder&image_size=square'
                    }
                    style={{ width: 100, height: 100, objectFit: 'cover', borderRadius: 8 }}
                    onClick={() => navigate(`/product/${order.productId}`)}
                  />
                </Col>
                <Col span={20}>
                  <div style={{ fontSize: 16, fontWeight: 'bold', marginBottom: 8 }}>
                    {order.product?.title}
                  </div>
                  <div className="product-price" style={{ fontSize: 20 }}>
                    ¥{order.price?.toFixed(2)}
                  </div>
                </Col>
              </Row>
            </Card>

            <Divider />

            <Row gutter={24}>
              <Col span={12}>
                <Card size="small" title="买家信息">
                  <Descriptions column={1} size="small">
                    <Descriptions.Item label="买家">{order.buyer?.nickname}</Descriptions.Item>
                    <Descriptions.Item label="收货姓名">{order.buyerName}</Descriptions.Item>
                    <Descriptions.Item label="联系电话">{order.buyerPhone}</Descriptions.Item>
                    <Descriptions.Item label="收货地址">{order.buyerAddress}</Descriptions.Item>
                  </Descriptions>
                </Card>
              </Col>
              <Col span={12}>
                <Card size="small" title="卖家信息">
                  <Descriptions column={1} size="small">
                    <Descriptions.Item label="卖家">{order.seller?.nickname}</Descriptions.Item>
                    <Descriptions.Item label="联系电话">{order.seller?.phone}</Descriptions.Item>
                    <Descriptions.Item label="学校">{order.seller?.school || '未填'}</Descriptions.Item>
                    <Descriptions.Item label="信誉分">{order.seller?.creditScore}</Descriptions.Item>
                  </Descriptions>
                </Card>
              </Col>
            </Row>

            <Divider />

            <Card size="small" title="订单时间线">
              <Descriptions column={2}>
                <Descriptions.Item label="下单时间">
                  {dayjs(order.createTime).format('YYYY-MM-DD HH:mm:ss')}
                </Descriptions.Item>
                {order.payTime && (
                  <Descriptions.Item label="支付时间">
                    {dayjs(order.payTime).format('YYYY-MM-DD HH:mm:ss')}
                  </Descriptions.Item>
                )}
                {order.shipTime && (
                  <Descriptions.Item label="发货时间">
                    {dayjs(order.shipTime).format('YYYY-MM-DD HH:mm:ss')}
                  </Descriptions.Item>
                )}
                {order.receiveTime && (
                  <Descriptions.Item label="收货时间">
                    {dayjs(order.receiveTime).format('YYYY-MM-DD HH:mm:ss')}
                  </Descriptions.Item>
                )}
                {order.completeTime && (
                  <Descriptions.Item label="完成时间">
                    {dayjs(order.completeTime).format('YYYY-MM-DD HH:mm:ss')}
                  </Descriptions.Item>
                )}
                {order.cancelTime && (
                  <Descriptions.Item label="取消时间">
                    {dayjs(order.cancelTime).format('YYYY-MM-DD HH:mm:ss')}
                  </Descriptions.Item>
                )}
                {order.cancelReason && (
                  <Descriptions.Item label="取消原因">{order.cancelReason}</Descriptions.Item>
                )}
              </Descriptions>
            </Card>

            {review && (
              <>
                <Divider />
                <Card size="small" title="评价信息">
                  <div style={{ marginBottom: 8 }}>
                    <Rate disabled defaultValue={review.rating} />
                    <span style={{ marginLeft: 8 }}>{review.rating}分</span>
                  </div>
                  <div>{review.content}</div>
                  {review.imageUrls && review.imageUrls.length > 0 && (
                    <div style={{ marginTop: 16 }}>
                      <Image.PreviewGroup>
                        <Row gutter={8}>
                          {review.imageUrls.map((url, index) => (
                            <Col key={index} span={4}>
                              <Image src={url} style={{ width: 80, height: 80, objectFit: 'cover', borderRadius: 4 }} />
                            </Col>
                          ))}
                        </Row>
                      </Image.PreviewGroup>
                    </div>
                  )}
                  <div style={{ marginTop: 8, color: '#999', fontSize: 12 }}>
                    评价时间：{dayjs(review.createTime).format('YYYY-MM-DD HH:mm')}
                  </div>
                </Card>
              </>
            )}
          </div>
        )}
      </Card>

      <Modal
        title="发表评价"
        open={reviewModalVisible}
        onCancel={() => setReviewModalVisible(false)}
        footer={null}
      >
        <Form form={reviewForm} onFinish={handleReview}>
          <Form.Item
            name="rating"
            label="评分"
            rules={[{ required: true, message: '请选择评分' }]}
          >
            <Rate />
          </Form.Item>
          <Form.Item
            name="content"
            label="评价内容"
            rules={[{ required: true, message: '请输入评价内容' }]}
          >
            <TextArea rows={4} placeholder="请输入您的评价内容..." />
          </Form.Item>
          <Form.Item label="上传图片">
            <Upload {...uploadProps}>
              {fileList.length >= 3 ? null : (
                <div>
                  <div style={{ marginTop: 8 }}>上传图片</div>
                </div>
              )}
            </Upload>
          </Form.Item>
          <Form.Item
            name="isAnonymous"
            label="匿名评价"
            valuePropName="checked"
          >
            <Radio>匿名评价</Radio>
          </Form.Item>
          <Form.Item>
            <Button type="primary" htmlType="submit" loading={submitting} block>
              提交评价
            </Button>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )
}

export default OrderDetail
