import React, { useState, useEffect } from 'react'
import {
  Card,
  Tag,
  Button,
  Space,
  Table,
  Image,
  Tabs,
  message,
} from 'antd'
import { useNavigate } from 'react-router-dom'
import { getMySales, shipOrder } from '../api/order'
import dayjs from 'dayjs'

const MySales = () => {
  const navigate = useNavigate()
  const [activeTab, setActiveTab] = useState('all')
  const [loading, setLoading] = useState(false)
  const [data, setData] = useState([])
  const [pagination, setPagination] = useState({
    current: 1,
    pageSize: 10,
    total: 0,
  })

  useEffect(() => {
    fetchSales()
  }, [activeTab, pagination.current, pagination.pageSize])

  const fetchSales = async () => {
    setLoading(true)
    try {
      const res = await getMySales({
        status: activeTab === 'all' ? undefined : activeTab,
        current: pagination.current,
        size: pagination.pageSize,
      })
      setData(res.data.records || [])
      setPagination((prev) => ({ ...prev, total: res.data.total || 0 }))
    } catch (error) {
      console.error('获取卖出订单列表失败', error)
    } finally {
      setLoading(false)
    }
  }

  const handleShip = async (orderNo) => {
    try {
      await shipOrder(orderNo)
      message.success('发货成功')
      fetchSales()
    } catch (error) {
      console.error('发货失败', error)
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

  const tabItems = [
    { key: 'all', label: '全部订单' },
    { key: 'PENDING', label: '待支付' },
    { key: 'PAID', label: '已支付' },
    { key: 'SHIPPED', label: '已发货' },
    { key: 'COMPLETED', label: '已完成' },
    { key: 'CANCELLED', label: '已取消' },
  ]

  const columns = [
    {
      title: '订单号',
      dataIndex: 'orderNo',
      width: 180,
    },
    {
      title: '商品信息',
      key: 'product',
      width: 250,
      render: (_, record) => (
        <Space>
          <Image
            src={
              record.product?.coverImage ||
              'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=default%20product%20placeholder&image_size=square'
            }
            width={60}
            height={60}
            style={{ objectFit: 'cover', borderRadius: 4, cursor: 'pointer' }}
            onClick={() => navigate(`/product/${record.productId}`)}
          />
          <div style={{ maxWidth: 160 }}>
            <div
              style={{
                overflow: 'hidden',
                textOverflow: 'ellipsis',
                whiteSpace: 'nowrap',
                cursor: 'pointer',
              }}
              onClick={() => navigate(`/product/${record.productId}`)}
            >
              {record.product?.title}
            </div>
            <div className="product-price">¥{record.price?.toFixed(2)}</div>
          </div>
        </Space>
      ),
    },
    {
      title: '买家',
      dataIndex: 'buyer',
      width: 100,
      render: (buyer) => buyer?.nickname,
    },
    {
      title: '收货信息',
      key: 'delivery',
      width: 200,
      render: (_, record) => (
        <div>
          <div>姓名：{record.buyerName}</div>
          <div>电话：{record.buyerPhone}</div>
          <div style={{ maxWidth: 200, overflow: 'hidden', textOverflow: 'ellipsis' }}>
            地址：{record.buyerAddress}
          </div>
        </div>
      ),
    },
    {
      title: '订单状态',
      dataIndex: 'status',
      width: 100,
      render: (status) => getStatusTag(status),
    },
    {
      title: '下单时间',
      dataIndex: 'createTime',
      width: 160,
      render: (time) => dayjs(time).format('YYYY-MM-DD HH:mm'),
    },
    {
      title: '操作',
      width: 180,
      fixed: 'right',
      render: (_, record) => (
        <Space>
          <Button type="link" size="small" onClick={() => navigate(`/order/${record.orderNo}`)}>
            详情
          </Button>
          {record.status === 'PAID' && (
            <Button type="primary" size="small" onClick={() => handleShip(record.orderNo)}>
              确认发货
            </Button>
          )}
        </Space>
      ),
    },
  ]

  return (
    <div>
      <Card title="卖出的商品">
        <Tabs
          activeKey={activeTab}
          onChange={setActiveTab}
          items={tabItems}
        />
        <Table
          columns={columns}
          dataSource={data}
          rowKey="id"
          loading={loading}
          pagination={{
            ...pagination,
            showQuickJumper: true,
            showSizeChanger: true,
            showTotal: (total) => `共 ${total} 条订单`,
            onChange: (page, pageSize) => {
              setPagination((prev) => ({ ...prev, current: page, pageSize }))
            },
          }}
          scroll={{ x: 1100 }}
        />
      </Card>
    </div>
  )
}

export default MySales
