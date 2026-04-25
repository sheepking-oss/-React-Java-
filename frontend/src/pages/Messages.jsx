import React, { useState, useEffect } from 'react'
import {
  Card,
  List,
  Avatar,
  Badge,
  Image,
  Tag,
  Button,
  Empty,
  Spin,
  Pagination,
  Space,
} from 'antd'
import { useNavigate } from 'react-router-dom'
import { getMessageList, markAsRead } from '../api/message'
import dayjs from 'dayjs'

const Messages = () => {
  const navigate = useNavigate()
  const [loading, setLoading] = useState(false)
  const [messages, setMessages] = useState([])
  const [current, setCurrent] = useState(1)
  const [pageSize, setPageSize] = useState(10)
  const [total, setTotal] = useState(0)

  useEffect(() => {
    fetchMessages()
  }, [current, pageSize])

  const fetchMessages = async () => {
    setLoading(true)
    try {
      const res = await getMessageList({
        current,
        size: pageSize,
      })
      setMessages(res.data.records || [])
      setTotal(res.data.total || 0)
    } catch (error) {
      console.error('获取消息列表失败', error)
    } finally {
      setLoading(false)
    }
  }

  const handleClick = async (item) => {
    try {
      await markAsRead({
        toUserId: item.otherUserId,
        productId: item.productId,
      })
      fetchMessages()
    } catch (error) {
      console.error('标记已读失败', error)
    }
    navigate(
      `/chat?toUserId=${item.otherUserId}&productId=${item.productId}`
    )
  }

  return (
    <div>
      <Card title="消息中心">
        <Spin spinning={loading}>
          {messages.length > 0 ? (
            <>
              <List
                dataSource={messages}
                renderItem={(item) => (
                  <List.Item
                    style={{ cursor: 'pointer', padding: 16 }}
                    onClick={() => handleClick(item)}
                  >
                    <List.Item.Meta
                      avatar={
                        <Badge count={item.unreadCount} overflowCount={99}>
                          <Avatar size={48} src={item.otherUser?.avatar}>
                            {item.otherUser?.nickname?.[0]}
                          </Avatar>
                        </Badge>
                      }
                      title={
                        <Space>
                          <span style={{ fontWeight: 'bold' }}>
                            {item.otherUser?.nickname}
                          </span>
                          {item.product && (
                            <Tag color="blue">
                              {item.product.title}
                            </Tag>
                          )}
                        </Space>
                      }
                      description={
                        <div style={{ display: 'flex', alignItems: 'center', gap: 16 }}>
                          <span
                            style={{
                              maxWidth: 300,
                              overflow: 'hidden',
                              textOverflow: 'ellipsis',
                              whiteSpace: 'nowrap',
                            }}
                          >
                            {item.lastMessage}
                          </span>
                          <span style={{ color: '#999', fontSize: 12 }}>
                            {dayjs(item.lastTime).format('MM-DD HH:mm')}
                          </span>
                        </div>
                      }
                    />
                    {item.product && (
                      <Image
                        src={
                          item.product.coverImage ||
                          'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=default%20product%20placeholder&image_size=square'
                        }
                        style={{ width: 60, height: 60, objectFit: 'cover', borderRadius: 4 }}
                        preview={false}
                      />
                    )}
                  </List.Item>
                )}
              />
              {total > 0 && (
                <div style={{ marginTop: 24, textAlign: 'center' }}>
                  <Pagination
                    current={current}
                    pageSize={pageSize}
                    total={total}
                    onChange={(page, size) => {
                      setCurrent(page)
                      if (size) setPageSize(size)
                    }}
                    showSizeChanger
                    showQuickJumper
                    showTotal={(total) => `共 ${total} 条会话`}
                  />
                </div>
              )}
            </>
          ) : (
            <Empty description="暂无消息" />
          )}
        </Spin>
      </Card>
    </div>
  )
}

export default Messages
