import React, { useState, useEffect } from 'react'
import {
  Card,
  Tag,
  Button,
  Space,
  Table,
  Image,
  Tabs,
  Popconfirm,
  message,
} from 'antd'
import { useNavigate } from 'react-router-dom'
import { getMyOrders, payOrder, cancelOrder, confirmOrder } from '../api/order'
import dayjs from 'dayjs'

const MyOrders = () => {
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
    fetchOrders()
  }, [activeTab, pagination.current, pagination.pageSize])

  const fetchOrders = async () => {
    setLoading(true)
    try {
      const res = await getMyOrders({
        status: activeTab === 'all' ? undefined : activeTab,
        current: pagination.current,
        size: pagination.pageSize,
      })
      setData(res.data.records || [])
      setPagination((prev) => ({ ...prev, total: res.data.total || 0 }))
    } catch (error) {
      console.error('获取订单列表失败', error)
    } finally {
      setLoading(false)
    }
  }

  const handlePay = async (orderNo) => {
    try {
      await payOrder(orderNo)
      message.success('支付成功')
      fetchOrders()
    } catch (error) {
      console.error('支付失败', error)
    }
  }

  const handleCancel = async (orderNo) => {
    try {
      await cancelOrder(orderNo, '用户取消')
      message.success('订单已取消')
      fetchOrders()
    } catch (error) {
      console.error('取消订单失败', error)
    }
  }

  const handleConfirm = async (orderNo) => {
    try {
      await confirmOrder(orderNo)
      message.success('确认收货成功')
      fetchOrders()
    } catch (error) {
      console.error('确认收货失败', error)
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
      title: '卖家',
      dataIndex: 'seller',
      width: 100,
      render: (seller) => seller?.nickname,
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
      width: 220,
      fixed: 'right',
      render: (_, record) => (
        <Space>
          <Button type="link" size="small" onClick={() => navigate(`/order/${record.orderNo}`)}>
            详情
          </Button>
          {record.status === 'PENDING' && (
            <>
              <Button type="primary" size="small" onClick={() => handlePay(record.orderNo)}>
                支付
              </Button>
              <Popconfirm title="确定取消订单吗？" onConfirm={() => handleCancel(record.orderNo)}>
                <Button type="link" size="small" danger>
                  取消
                </Button>
              </Popconfirm>
            </>
          )}
          {record.status === 'SHIPPED' && (
            <Button type="primary" size="small" onClick={() => handleConfirm(record.orderNo)}>
              确认收货
            </Button>
          )}
        </Space>
      ),
    },
  ]

  return (
    <div>
      <Card title="我的订单">
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
          scroll={{ x: 1000 }}
        />
      </Card>
    </div>
  )
}

export default MyOrders
