import React, { useState, useEffect, useRef } from 'react'
import {
  Card,
  Input,
  Button,
  Avatar,
  Image,
  Spin,
  Empty,
  Space,
} from 'antd'
import { SendOutlined, ArrowLeftOutlined } from '@ant-design/icons'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { getConversation, sendMessage, markAsRead } from '../api/message'
import { getProductDetail } from '../api/product'
import useStore from '../store'
import dayjs from 'dayjs'

const { TextArea } = Input

const Chat = () => {
  const navigate = useNavigate()
  const [searchParams] = useSearchParams()
  const toUserId = parseInt(searchParams.get('toUserId'))
  const productId = parseInt(searchParams.get('productId'))
  const user = useStore((state) => state.user)
  const [loading, setLoading] = useState(false)
  const [messages, setMessages] = useState([])
  const [inputValue, setInputValue] = useState('')
  const [product, setProduct] = useState(null)
  const chatRef = useRef(null)
  const pollingRef = useRef(null)

  useEffect(() => {
    if (toUserId && productId) {
      fetchMessages()
      fetchProduct()
      markRead()

      pollingRef.current = setInterval(() => {
        fetchMessages()
      }, 5000)
    }

    return () => {
      if (pollingRef.current) {
        clearInterval(pollingRef.current)
      }
    }
  }, [toUserId, productId])

  useEffect(() => {
    if (chatRef.current) {
      chatRef.current.scrollTop = chatRef.current.scrollHeight
    }
  }, [messages])

  const fetchMessages = async () => {
    try {
      const res = await getConversation({
        toUserId,
        productId,
      })
      setMessages(res.data || [])
    } catch (error) {
      console.error('获取聊天记录失败', error)
    }
  }

  const fetchProduct = async () => {
    try {
      const res = await getProductDetail(productId)
      setProduct(res.data)
    } catch (error) {
      console.error('获取商品信息失败', error)
    }
  }

  const markRead = async () => {
    try {
      await markAsRead({
        toUserId,
        productId,
      })
    } catch (error) {
      console.error('标记已读失败', error)
    }
  }

  const handleSend = async () => {
    if (!inputValue.trim()) return
    try {
      await sendMessage({
        productId,
        toUserId,
        content: inputValue.trim(),
      })
      setInputValue('')
      fetchMessages()
    } catch (error) {
      console.error('发送消息失败', error)
    }
  }

  const handleKeyPress = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault()
      handleSend()
    }
  }

  return (
    <div>
      <Button
        icon={<ArrowLeftOutlined />}
        onClick={() => navigate('/messages')}
        style={{ marginBottom: 16 }}
      >
        返回消息列表
      </Button>

      {product && (
        <Card
          size="small"
          style={{ marginBottom: 16 }}
          onClick={() => navigate(`/product/${productId}`)}
          style={{ marginBottom: 16, cursor: 'pointer' }}
        >
          <Space>
            <Image
              src={
                product.coverImage ||
                'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=default%20product%20placeholder&image_size=square'
              }
              style={{ width: 50, height: 50, objectFit: 'cover', borderRadius: 4 }}
              preview={false}
            />
            <div>
              <div style={{ fontWeight: 'bold' }}>{product.title}</div>
              <div className="product-price">¥{product.price?.toFixed(2)}</div>
            </div>
          </Space>
        </Card>
      )}

      <Card
        style={{ height: 600, display: 'flex', flexDirection: 'column' }}
      >
        <div
          ref={chatRef}
          style={{
            flex: 1,
            overflowY: 'auto',
            padding: 16,
            background: '#f5f5f5',
            marginBottom: 16,
          }}
        >
          {messages.length > 0 ? (
            messages.map((msg) => (
              <div
                key={msg.id}
                style={{
                  display: 'flex',
                  justifyContent: msg.isMe ? 'flex-end' : 'flex-start',
                  marginBottom: 16,
                }}
              >
                {!msg.isMe && (
                  <Avatar size={36} style={{ marginRight: 12 }}>
                    {msg.fromUserId}
                  </Avatar>
                )}
                <div
                  style={{
                    maxWidth: '60%',
                    padding: '8px 12px',
                    borderRadius: 8,
                    background: msg.isMe ? '#1890ff' : '#fff',
                    color: msg.isMe ? '#fff' : '#333',
                    wordBreak: 'break-word',
                    border: msg.isMe ? 'none' : '1px solid #e8e8e8',
                  }}
                >
                  {msg.content}
                  {msg.imageUrl && (
                    <Image
                      src={msg.imageUrl}
                      style={{ maxWidth: 200, maxHeight: 200, borderRadius: 4, marginTop: 8 }}
                    />
                  )}
                  <div
                    style={{
                      fontSize: 12,
                      color: msg.isMe ? 'rgba(255,255,255,0.7)' : '#999',
                      marginTop: 4,
                    }}
                  >
                    {dayjs(msg.createTime).format('HH:mm')}
                  </div>
                </div>
                {msg.isMe && (
                  <Avatar size={36} src={user?.avatar} style={{ marginLeft: 12 }}>
                    {user?.nickname?.[0]}
                  </Avatar>
                )}
              </div>
            ))
          ) : (
            <Empty description="暂无消息" style={{ marginTop: 100 }} />
          )}
        </div>

        <div style={{ display: 'flex', gap: 8 }}>
          <TextArea
            value={inputValue}
            onChange={(e) => setInputValue(e.target.value)}
            onKeyPress={handleKeyPress}
            placeholder="输入消息，按 Enter 发送"
            rows={2}
            style={{ flex: 1 }}
          />
          <Button
            type="primary"
            icon={<SendOutlined />}
            onClick={handleSend}
            style={{ height: 'auto' }}
          >
            发送
          </Button>
        </div>
      </Card>
    </div>
  )
}

export default Chat
