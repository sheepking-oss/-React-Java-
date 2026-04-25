import React, { useState, useEffect } from 'react'
import {
  Card,
  Row,
  Col,
  Tag,
  Button,
  Empty,
  Pagination,
  Spin,
  Image,
  Space,
} from 'antd'
import { HeartOutlined, HeartFilled, EyeOutlined } from '@ant-design/icons'
import { useNavigate } from 'react-router-dom'
import { getFavoriteList, toggleFavorite } from '../api/favorite'
import dayjs from 'dayjs'

const { Meta } = Card

const Favorites = () => {
  const navigate = useNavigate()
  const [loading, setLoading] = useState(false)
  const [favorites, setFavorites] = useState([])
  const [current, setCurrent] = useState(1)
  const [pageSize, setPageSize] = useState(12)
  const [total, setTotal] = useState(0)

  useEffect(() => {
    fetchFavorites()
  }, [current, pageSize])

  const fetchFavorites = async () => {
    setLoading(true)
    try {
      const res = await getFavoriteList({
        current,
        size: pageSize,
      })
      setFavorites(res.data.records || [])
      setTotal(res.data.total || 0)
    } catch (error) {
      console.error('获取收藏列表失败', error)
    } finally {
      setLoading(false)
    }
  }

  const handleToggleFavorite = async (productId) => {
    try {
      await toggleFavorite(productId)
      fetchFavorites()
    } catch (error) {
      console.error('取消收藏失败', error)
    }
  }

  return (
    <div>
      <Card title="我的收藏">
        <Spin spinning={loading}>
          {favorites.length > 0 ? (
            <>
              <Row gutter={[24, 24]}>
                {favorites.map((product) => (
                  <Col xs={24} sm={12} md={8} lg={6} key={product.id}>
                    <Card
                      hoverable
                      cover={
                        <div style={{ height: 200, overflow: 'hidden', position: 'relative' }}>
                          <Image
                            src={
                              product.coverImage ||
                              'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=default%20product%20placeholder&image_size=square'
                            }
                            style={{ width: '100%', height: '100%', objectFit: 'cover' }}
                            preview={false}
                          />
                          <Button
                            type="text"
                            icon={<HeartFilled style={{ color: '#ff4d4f' }} />}
                            style={{ position: 'absolute', top: 8, right: 8, background: 'rgba(255,255,255,0.8)' }}
                            onClick={(e) => {
                              e.stopPropagation()
                              handleToggleFavorite(product.id)
                            }}
                          />
                        </div>
                      }
                      onClick={() => navigate(`/product/${product.id}`)}
                      className="product-card"
                    >
                      <Meta
                        title={
                          <div
                            style={{
                              overflow: 'hidden',
                              textOverflow: 'ellipsis',
                              whiteSpace: 'nowrap',
                            }}
                          >
                            {product.title}
                          </div>
                        }
                        description={
                          <div>
                            <div className="product-price">¥{product.price?.toFixed(2)}</div>
                            <div style={{ marginTop: 8, color: '#999', fontSize: 12 }}>
                              <Space split="·">
                                <span>
                                  <EyeOutlined /> {product.viewCount}
                                </span>
                                {product.seller?.nickname && <span>卖家：{product.seller.nickname}</span>}
                              </Space>
                            </div>
                          </div>
                        }
                      />
                    </Card>
                  </Col>
                ))}
              </Row>

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
                    showTotal={(total) => `共 ${total} 件收藏`}
                  />
                </div>
              )}
            </>
          ) : (
            <Empty description="暂无收藏" />
          )}
        </Spin>
      </Card>
    </div>
  )
}

export default Favorites
