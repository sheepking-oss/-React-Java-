import React, { useState, useEffect } from 'react'
import {
  Row,
  Col,
  Card,
  Tag,
  Select,
  Input,
  Pagination,
  Spin,
  Empty,
  Space,
  Button,
} from 'antd'
import { HeartOutlined, EyeOutlined, TagOutlined } from '@ant-design/icons'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { getProductList, getCategories } from '../api/product'
import dayjs from 'dayjs'

const { Search } = Input
const { Meta } = Card

const Home = () => {
  const navigate = useNavigate()
  const [searchParams] = useSearchParams()
  const [loading, setLoading] = useState(false)
  const [products, setProducts] = useState([])
  const [categories, setCategories] = useState([])
  const [keyword, setKeyword] = useState(searchParams.get('keyword') || '')
  const [category, setCategory] = useState('')
  const [current, setCurrent] = useState(1)
  const [pageSize, setPageSize] = useState(12)
  const [total, setTotal] = useState(0)

  useEffect(() => {
    fetchCategories()
  }, [])

  useEffect(() => {
    fetchProducts()
  }, [keyword, category, current, pageSize])

  const fetchCategories = async () => {
    try {
      const res = await getCategories()
      setCategories(res.data)
    } catch (error) {
      console.error('获取分类失败', error)
    }
  }

  const fetchProducts = async () => {
    setLoading(true)
    try {
      const res = await getProductList({
        keyword,
        category: category || undefined,
        status: 1,
        current,
        size: pageSize,
      })
      setProducts(res.data.records || [])
      setTotal(res.data.total || 0)
    } catch (error) {
      console.error('获取商品列表失败', error)
    } finally {
      setLoading(false)
    }
  }

  const handleCategoryChange = (value) => {
    setCategory(value)
    setCurrent(1)
  }

  const handleSearch = (value) => {
    setKeyword(value)
    setCurrent(1)
  }

  const handlePageChange = (page, size) => {
    setCurrent(page)
    if (size) setPageSize(size)
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

  return (
    <div>
      <Card style={{ marginBottom: 24 }}>
        <Space wrap size="middle">
          <span>分类：</span>
          <Select
            style={{ width: 150 }}
            placeholder="全部分类"
            allowClear
            value={category || undefined}
            onChange={handleCategoryChange}
            options={categories.map((cat) => ({ label: cat, value: cat }))}
          />
          <Search
            placeholder="搜索商品名称、描述"
            allowClear
            style={{ width: 300 }}
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
            onSearch={handleSearch}
          />
          <Button type="primary" onClick={() => {
            setCurrent(1)
            fetchProducts()
          }}>
            搜索
          </Button>
        </Space>
      </Card>

      <Spin spinning={loading}>
        {products.length > 0 ? (
          <>
            <Row gutter={[24, 24]}>
              {products.map((product) => (
                <Col xs={24} sm={12} md={8} lg={6} key={product.id}>
                  <Card
                    hoverable
                    cover={
                      <div
                        style={{
                          height: 200,
                          overflow: 'hidden',
                          position: 'relative',
                        }}
                      >
                        <img
                          alt={product.title}
                          src={product.coverImage || 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=default%20product%20placeholder&image_size=square'}
                          style={{
                            width: '100%',
                            height: '100%',
                            objectFit: 'cover',
                          }}
                        />
                        <div
                          style={{
                            position: 'absolute',
                            top: 8,
                            left: 8,
                          }}
                        >
                          {getStatusTag(product.status)}
                        </div>
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
                          <div className="product-price">
                            ¥{product.price?.toFixed(2)}
                            {product.originalPrice && (
                              <span className="product-original-price">
                                ¥{product.originalPrice?.toFixed(2)}
                              </span>
                            )}
                          </div>
                          <div style={{ marginTop: 8, color: '#999', fontSize: 12 }}>
                            <Space split="·">
                              <span>
                                <EyeOutlined /> {product.viewCount}
                              </span>
                              <span>
                                <HeartOutlined /> {product.favoriteCount}
                              </span>
                              {product.category && (
                                <span>
                                  <TagOutlined /> {product.category}
                                </span>
                              )}
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
                  onChange={handlePageChange}
                  showSizeChanger
                  showQuickJumper
                  showTotal={(total) => `共 ${total} 件商品`}
                />
              </div>
            )}
          </>
        ) : (
          <Empty description="暂无商品" />
        )}
      </Spin>
    </div>
  )
}

export default Home
