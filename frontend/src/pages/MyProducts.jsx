import React, { useState, useEffect } from 'react'
import {
  Table,
  Card,
  Tag,
  Space,
  Button,
  Popconfirm,
  message,
  Image,
  Select,
  Row,
  Col,
} from 'antd'
import { EditOutlined, DeleteOutlined, EyeOutlined, EyeInvisibleOutlined } from '@ant-design/icons'
import { useNavigate } from 'react-router-dom'
import { getProductList, deleteProduct, updateProductStatus } from '../api/product'
import dayjs from 'dayjs'

const { Option } = Select

const MyProducts = () => {
  const navigate = useNavigate()
  const [loading, setLoading] = useState(false)
  const [data, setData] = useState([])
  const [status, setStatus] = useState(null)
  const [pagination, setPagination] = useState({
    current: 1,
    pageSize: 10,
    total: 0,
  })

  useEffect(() => {
    fetchProducts()
  }, [pagination.current, pagination.pageSize, status])

  const fetchProducts = async () => {
    setLoading(true)
    try {
      const res = await getProductList({
        userId: 'self',
        status: status ?? undefined,
        current: pagination.current,
        size: pagination.pageSize,
      })
      setData(res.data.records || [])
      setPagination((prev) => ({ ...prev, total: res.data.total || 0 }))
    } catch (error) {
      console.error('获取商品列表失败', error)
    } finally {
      setLoading(false)
    }
  }

  const handleDelete = async (id) => {
    try {
      await deleteProduct(id)
      message.success('删除成功')
      fetchProducts()
    } catch (error) {
      console.error('删除失败', error)
    }
  }

  const handleToggleStatus = async (id, currentStatus) => {
    const newStatus = currentStatus === 1 ? 0 : 1
    try {
      await updateProductStatus(id, newStatus)
      message.success(newStatus === 1 ? '已上架' : '已下架')
      fetchProducts()
    } catch (error) {
      console.error('操作失败', error)
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

  const getAuditTag = (status) => {
    const statusMap = {
      0: { color: 'orange', text: '待审核' },
      1: { color: 'green', text: '已通过' },
      2: { color: 'red', text: '已拒绝' },
    }
    const info = statusMap[status] || { color: 'default', text: '未知' }
    return <Tag color={info.color}>{info.text}</Tag>
  }

  const columns = [
    {
      title: '商品图片',
      dataIndex: 'coverImage',
      width: 80,
      render: (coverImage) => (
        <Image
          src={coverImage || 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=default%20product%20placeholder&image_size=square'}
          width={60}
          height={60}
          style={{ objectFit: 'cover', borderRadius: 4 }}
        />
      ),
    },
    {
      title: '商品标题',
      dataIndex: 'title',
      ellipsis: true,
    },
    {
      title: '价格',
      dataIndex: 'price',
      width: 100,
      render: (price) => <span style={{ color: '#ff4d4f', fontWeight: 'bold' }}>¥{price?.toFixed(2)}</span>,
    },
    {
      title: '分类',
      dataIndex: 'category',
      width: 100,
      render: (category) => <Tag>{category}</Tag>,
    },
    {
      title: '状态',
      dataIndex: 'status',
      width: 100,
      render: (status, record) => (
        <Space>
          {getStatusTag(status)}
          {getAuditTag(record.auditStatus)}
        </Space>
      ),
    },
    {
      title: '浏览/收藏',
      width: 120,
      render: (_, record) => (
        <span>
          {record.viewCount} / {record.favoriteCount}
        </span>
      ),
    },
    {
      title: '发布时间',
      dataIndex: 'createTime',
      width: 160,
      render: (time) => dayjs(time).format('YYYY-MM-DD HH:mm'),
    },
    {
      title: '操作',
      width: 220,
      fixed: 'right',
      render: (_, record) => (
        <Space>
          <Button type="link" size="small" onClick={() => navigate(`/product/${record.id}`)}>
            查看
          </Button>
          <Button
            type="link"
            size="small"
            icon={<EditOutlined />}
            onClick={() => navigate(`/publish/${record.id}`)}
          >
            编辑
          </Button>
          {record.status !== 2 && (
            <Button
              type="link"
              size="small"
              icon={record.status === 1 ? <EyeInvisibleOutlined /> : <EyeOutlined />}
              onClick={() => handleToggleStatus(record.id, record.status)}
            >
              {record.status === 1 ? '下架' : '上架'}
            </Button>
          )}
          <Popconfirm title="确定删除此商品吗？" onConfirm={() => handleDelete(record.id)}>
            <Button type="link" size="small" danger icon={<DeleteOutlined />}>
              删除
            </Button>
          </Popconfirm>
        </Space>
      ),
    },
  ]

  return (
    <div>
      <Card
        title="我的商品"
        extra={
          <Row gutter={16}>
            <Col>
              <Select
                placeholder="全部状态"
                allowClear
                style={{ width: 120 }}
                value={status}
                onChange={(value) => {
                  setStatus(value)
                  setPagination((prev) => ({ ...prev, current: 1 }))
                }}
              >
                <Option value={1}>在售</Option>
                <Option value={0}>下架</Option>
                <Option value={2}>已售出</Option>
              </Select>
            </Col>
            <Col>
              <Button type="primary" onClick={() => navigate('/publish')}>
                发布商品
              </Button>
            </Col>
          </Row>
        }
      >
        <Table
          columns={columns}
          dataSource={data}
          rowKey="id"
          loading={loading}
          pagination={{
            ...pagination,
            showQuickJumper: true,
            showSizeChanger: true,
            showTotal: (total) => `共 ${total} 件商品`,
            onChange: (page, pageSize) => {
              setPagination((prev) => ({ ...prev, current: page, pageSize }))
            },
          }}
          scroll={{ x: 1000 }}
        />
      </Card>
    </div>
  )
}

export default MyProducts
