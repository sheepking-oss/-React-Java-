import React, { useState, useEffect } from 'react'
import {
  Card,
  Table,
  Tag,
  Button,
  Space,
  Image,
  Tabs,
  message,
} from 'antd'
import { useNavigate } from 'react-router-dom'
import { getMyReports } from '../api/report'
import dayjs from 'dayjs'

const MyReports = () => {
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
    fetchReports()
  }, [activeTab, pagination.current, pagination.pageSize])

  const fetchReports = async () => {
    setLoading(true)
    try {
      const res = await getMyReports({
        status: activeTab === 'all' ? undefined : activeTab,
        current: pagination.current,
        size: pagination.pageSize,
      })
      setData(res.data.records || [])
      setPagination((prev) => ({ ...prev, total: res.data.total || 0 }))
    } catch (error) {
      console.error('获取举报列表失败', error)
    } finally {
      setLoading(false)
    }
  }

  const getStatusTag = (status) => {
    const statusMap = {
      PENDING: { color: 'orange', text: '待处理' },
      PROCESSING: { color: 'processing', text: '处理中' },
      RESOLVED: { color: 'green', text: '已解决' },
      DISMISSED: { color: 'default', text: '已驳回' },
    }
    const info = statusMap[status] || { color: 'default', text: '未知' }
    return <Tag color={info.color}>{info.text}</Tag>
  }

  const tabItems = [
    { key: 'all', label: '全部' },
    { key: 'PENDING', label: '待处理' },
    { key: 'PROCESSING', label: '处理中' },
    { key: 'RESOLVED', label: '已解决' },
    { key: 'DISMISSED', label: '已驳回' },
  ]

  const columns = [
    {
      title: '被举报用户',
      dataIndex: 'reportedUser',
      width: 120,
      render: (user) => user?.nickname || '-',
    },
    {
      title: '举报类型',
      dataIndex: 'type',
      width: 120,
    },
    {
      title: '举报原因',
      dataIndex: 'reason',
      width: 200,
      ellipsis: true,
    },
    {
      title: '相关商品',
      dataIndex: 'product',
      width: 200,
      render: (product) =>
        product ? (
          <Space>
            <Image
              src={
                product.coverImage ||
                'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=default%20product%20placeholder&image_size=square'
              }
              style={{ width: 40, height: 40, objectFit: 'cover', borderRadius: 4 }}
              preview={false}
            />
            <span style={{ maxWidth: 120, overflow: 'hidden', textOverflow: 'ellipsis' }}>
              {product.title}
            </span>
          </Space>
        ) : (
          '-'
        ),
    },
    {
      title: '状态',
      dataIndex: 'status',
      width: 100,
      render: (status) => getStatusTag(status),
    },
    {
      title: '处理结果',
      dataIndex: 'handleResult',
      width: 150,
      ellipsis: true,
      render: (result) => result || '-',
    },
    {
      title: '举报时间',
      dataIndex: 'createTime',
      width: 160,
      render: (time) => dayjs(time).format('YYYY-MM-DD HH:mm'),
    },
    {
      title: '操作',
      width: 100,
      fixed: 'right',
      render: (_, record) => (
        <Button type="link" size="small" onClick={() => navigate(`/report/detail/${record.id}`)}>
          详情
        </Button>
      ),
    },
  ]

  return (
    <div>
      <Card
        title="我的举报"
        extra={
          <Button type="primary" onClick={() => navigate('/report')}>
            发起举报
          </Button>
        }
      >
        <Tabs activeKey={activeTab} onChange={setActiveTab} items={tabItems} />
        <Table
          columns={columns}
          dataSource={data}
          rowKey="id"
          loading={loading}
          pagination={{
            ...pagination,
            showQuickJumper: true,
            showSizeChanger: true,
            showTotal: (total) => `共 ${total} 条记录`,
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

export default MyReports
