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
  Input,
} from 'antd'
import { ArrowLeftOutlined } from '@ant-design/icons'
import { useParams, useNavigate } from 'react-router-dom'
import { getExchangeDetail, acceptExchange, rejectExchange, cancelExchange } from '../api/exchange'
import dayjs from 'dayjs'

const { TextArea } = Input

const ExchangeDetail = () => {
  const { id } = useParams()
  const navigate = useNavigate()
  const [loading, setLoading] = useState(false)
  const [exchange, setExchange] = useState(null)

  useEffect(() => {
    if (id) {
      fetchExchangeDetail()
    }
  }, [id])

  const fetchExchangeDetail = async () => {
    setLoading(true)
    try {
      const res = await getExchangeDetail(id)
      setExchange(res.data)
    } catch (error) {
      console.error('获取换物详情失败', error)
    } finally {
      setLoading(false)
    }
  }

  const handleAccept = async () => {
    try {
      await acceptExchange(id)
      message.success('已同意换物申请')
      fetchExchangeDetail()
    } catch (error) {
      console.error('同意换物申请失败', error)
    }
  }

  const handleReject = async (reason) => {
    try {
      await rejectExchange(id, reason)
      message.success('已拒绝换物申请')
      fetchExchangeDetail()
    } catch (error) {
      console.error('拒绝换物申请失败', error)
    }
  }

  const handleCancel = async () => {
    try {
      await cancelExchange(id)
      message.success('已取消换物申请')
      fetchExchangeDetail()
    } catch (error) {
      console.error('取消换物申请失败', error)
    }
  }

  const getStatusTag = (status) => {
    const statusMap = {
      PENDING: { color: 'orange', text: '待处理' },
      ACCEPTED: { color: 'green', text: '已同意' },
      REJECTED: { color: 'red', text: '已拒绝' },
      CANCELLED: { color: 'default', text: '已取消' },
    }
    const info = statusMap[status] || { color: 'default', text: '未知' }
    return <Tag color={info.color}>{info.text}</Tag>
  }

  const renderActions = () => {
    if (!exchange) return null

    const actions = []

    if (exchange.status === 'PENDING') {
      if (exchange.targetId === exchange.initiatorId) {
        actions.push(
          <Button danger onClick={handleCancel}>
            取消申请
          </Button>
        )
      } else {
        actions.push(
          <Button type="primary" onClick={handleAccept}>
            同意换物
          </Button>
        )
        actions.push(
          <Button
            danger
            onClick={() => {
              Modal.confirm({
                title: '拒绝换物申请',
                content: (
                  <TextArea
                    rows={3}
                    placeholder="请输入拒绝原因"
                    id="rejectReason"
                  />
                ),
                onOk: () => {
                  const reason = document.getElementById('rejectReason')?.value
                  handleReject(reason)
                },
              })
            }}
          >
            拒绝申请
          </Button>
        )
      }
    }

    return actions.length > 0 ? <Space>{actions}</Space> : null
  }

  return (
    <div>
      <Button
        icon={<ArrowLeftOutlined />}
        onClick={() => navigate('/exchanges')}
        style={{ marginBottom: 16 }}
      >
        返回换物申请列表
      </Button>

      <Card loading={loading}>
        {exchange && (
          <div>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <div>
                <span style={{ fontSize: 16, marginRight: 16 }}>换物申请</span>
                {getStatusTag(exchange.status)}
              </div>
              {renderActions()}
            </div>

            <Divider />

            <Row gutter={24}>
              <Col span={12}>
                <Card size="small" title="发起方">
                  <Descriptions column={1} size="small">
                    <Descriptions.Item label="用户">{exchange.initiator?.nickname}</Descriptions.Item>
                    <Descriptions.Item label="信誉分">{exchange.initiator?.creditScore}</Descriptions.Item>
                    {exchange.initiatorProduct && (
                      <Descriptions.Item label="换出商品">
                        <Space>
                          <Image
                            src={
                              exchange.initiatorProduct.coverImage ||
                              'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=default%20product%20placeholder&image_size=square'
                            }
                            style={{ width: 60, height: 60, objectFit: 'cover', borderRadius: 4 }}
                            preview={false}
                          />
                          <div>
                            <div>{exchange.initiatorProduct.title}</div>
                            <div className="product-price" style={{ fontSize: 14 }}>
                              ¥{exchange.initiatorProduct.price?.toFixed(2)}
                            </div>
                          </div>
                        </Space>
                      </Descriptions.Item>
                    )}
                  </Descriptions>
                </Card>
              </Col>
              <Col span={12}>
                <Card size="small" title="接收方">
                  <Descriptions column={1} size="small">
                    <Descriptions.Item label="用户">{exchange.target?.nickname}</Descriptions.Item>
                    <Descriptions.Item label="信誉分">{exchange.target?.creditScore}</Descriptions.Item>
                    {exchange.targetProduct && (
                      <Descriptions.Item label="目标商品">
                        <Space>
                          <Image
                            src={
                              exchange.targetProduct.coverImage ||
                              'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=default%20product%20placeholder&image_size=square'
                            }
                            style={{ width: 60, height: 60, objectFit: 'cover', borderRadius: 4 }}
                            preview={false}
                          />
                          <div>
                            <div>{exchange.targetProduct.title}</div>
                            <div className="product-price" style={{ fontSize: 14 }}>
                              ¥{exchange.targetProduct.price?.toFixed(2)}
                            </div>
                          </div>
                        </Space>
                      </Descriptions.Item>
                    )}
                  </Descriptions>
                </Card>
              </Col>
            </Row>

            <Divider />

            <Card size="small" title="换物描述">
              <p>{exchange.initiatorDescription || '暂无描述'}</p>
            </Card>

            {exchange.status === 'REJECTED' && exchange.rejectReason && (
              <>
                <Divider />
                <Card size="small" title="拒绝原因" type="danger">
                  <p>{exchange.rejectReason}</p>
                </Card>
              </>
            )}

            <Divider />

            <Card size="small" title="时间线">
              <Descriptions column={2}>
                <Descriptions.Item label="申请时间">
                  {dayjs(exchange.createTime).format('YYYY-MM-DD HH:mm:ss')}
                </Descriptions.Item>
                {exchange.handleTime && (
                  <Descriptions.Item label="处理时间">
                    {dayjs(exchange.handleTime).format('YYYY-MM-DD HH:mm:ss')}
                  </Descriptions.Item>
                )}
              </Descriptions>
            </Card>
          </div>
        )}
      </Card>
    </div>
  )
}

export default ExchangeDetail
