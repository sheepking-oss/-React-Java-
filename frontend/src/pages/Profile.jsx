import React, { useState, useEffect } from 'react'
import {
  Card,
  Avatar,
  Descriptions,
  Button,
  Space,
  Tag,
  Statistic,
  Row,
  Col,
  Progress,
  Tabs,
  Table,
  Image,
  Rate,
} from 'antd'
import {
  EditOutlined,
  SafetyOutlined,
  ShoppingCartOutlined,
  HeartOutlined,
  MessageOutlined,
} from '@ant-design/icons'
import { useNavigate } from 'react-router-dom'
import { getUserInfo } from '../api/user'
import { getUserReviews } from '../api/review'
import useStore from '../store'
import dayjs from 'dayjs'

const Profile = () => {
  const navigate = useNavigate()
  const user = useStore((state) => state.user)
  const [reviews, setReviews] = useState([])
  const [pagination, setPagination] = useState({
    current: 1,
    pageSize: 10,
    total: 0,
  })

  useEffect(() => {
    if (user) {
      fetchReviews()
    }
  }, [user, pagination.current, pagination.pageSize])

  const fetchReviews = async () => {
    try {
      const res = await getUserReviews(user.id, {
        current: pagination.current,
        size: pagination.pageSize,
      })
      setReviews(res.data.records || [])
      setPagination((prev) => ({ ...prev, total: res.data.total || 0 }))
    } catch (error) {
      console.error('获取评价列表失败', error)
    }
  }

  const getCreditColor = (score) => {
    if (score >= 80) return '#52c41a'
    if (score >= 60) return '#faad14'
    return '#ff4d4f'
  }

  const reviewColumns = [
    {
      title: '评价人',
      dataIndex: 'reviewer',
      width: 120,
      render: (reviewer) => (
        <Space>
          <Avatar src={reviewer?.avatar}>{reviewer?.nickname?.[0]}</Avatar>
          <span>{reviewer?.nickname}</span>
        </Space>
      ),
    },
    {
      title: '评分',
      dataIndex: 'rating',
      width: 120,
      render: (rating) => <Rate disabled defaultValue={rating} />,
    },
    {
      title: '评价内容',
      dataIndex: 'content',
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
      title: '评价时间',
      dataIndex: 'createTime',
      width: 160,
      render: (time) => dayjs(time).format('YYYY-MM-DD HH:mm'),
    },
  ]

  const tabItems = [
    {
      key: 'info',
      label: '基本信息',
      children: (
        <Card>
          <Row gutter={24}>
            <Col span={12}>
              <Descriptions column={1} size="large">
                <Descriptions.Item label="用户名">{user?.username}</Descriptions.Item>
                <Descriptions.Item label="昵称">{user?.nickname}</Descriptions.Item>
                <Descriptions.Item label="手机号">{user?.phone || '未填写'}</Descriptions.Item>
                <Descriptions.Item label="邮箱">{user?.email || '未填写'}</Descriptions.Item>
                <Descriptions.Item label="学号">{user?.studentId || '未填写'}</Descriptions.Item>
                <Descriptions.Item label="学校">{user?.school || '未填写'}</Descriptions.Item>
                <Descriptions.Item label="性别">
                  {user?.gender === 1 ? '男' : user?.gender === 2 ? '女' : '未填写'}
                </Descriptions.Item>
                <Descriptions.Item label="个性签名">{user?.signature || '未填写'}</Descriptions.Item>
              </Descriptions>
            </Col>
            <Col span={12}>
              <Card size="small" title="交易统计">
                <Row gutter={16}>
                  <Col span={12}>
                    <Statistic
                      title="交易次数"
                      value={user?.tradeCount || 0}
                      prefix={<ShoppingCartOutlined />}
                    />
                  </Col>
                  <Col span={12}>
                    <Statistic
                      title="成功交易"
                      value={user?.successCount || 0}
                      prefix={<HeartOutlined />}
                      valueStyle={{ color: '#52c41a' }}
                    />
                  </Col>
                </Row>
              </Card>
              <Card size="small" title="信誉评价" style={{ marginTop: 16 }}>
                <div style={{ textAlign: 'center' }}>
                  <Statistic
                    title="信誉分"
                    value={user?.creditScore?.toFixed(2) || 100}
                    prefix={<SafetyOutlined />}
                    valueStyle={{ color: getCreditColor(user?.creditScore) }}
                  />
                  <Progress
                    percent={user?.creditScore || 100}
                    strokeColor={getCreditColor(user?.creditScore)}
                    showInfo={false}
                    style={{ marginTop: 16 }}
                  />
                  <div style={{ marginTop: 8, color: '#999' }}>
                    信誉分越高，交易越安全
                  </div>
                </div>
              </Card>
            </Col>
          </Row>
        </Card>
      ),
    },
    {
      key: 'reviews',
      label: '收到的评价',
      children: (
        <Card>
          <Table
            columns={reviewColumns}
            dataSource={reviews}
            rowKey="id"
            pagination={{
              ...pagination,
              showQuickJumper: true,
              showSizeChanger: true,
              showTotal: (total) => `共 ${total} 条评价`,
              onChange: (page, pageSize) => {
                setPagination((prev) => ({ ...prev, current: page, pageSize }))
              },
            }}
            scroll={{ x: 800 }}
          />
        </Card>
      ),
    },
  ]

  return (
    <div>
      <Card
        style={{ marginBottom: 24 }}
        extra={
          <Space>
            <Button type="primary" icon={<EditOutlined />} onClick={() => navigate('/profile/edit')}>
              编辑资料
            </Button>
            <Button onClick={() => navigate('/profile/password')}>修改密码</Button>
          </Space>
        }
      >
        <Space size="large">
          <Avatar size={100} src={user?.avatar}>
            {user?.nickname?.[0]}
          </Avatar>
          <div>
            <div style={{ fontSize: 24, fontWeight: 'bold', marginBottom: 8 }}>
              {user?.nickname}
            </div>
            <Space size="middle">
              <Tag color="blue">{user?.school || '未填写学校'}</Tag>
              <Tag color="green">
                信誉分：{user?.creditScore?.toFixed(2) || 100}
              </Tag>
              <Tag>交易次数：{user?.tradeCount || 0}</Tag>
            </Space>
            {user?.signature && (
              <div style={{ marginTop: 8, color: '#666' }}>
                {user.signature}
              </div>
            )}
          </div>
        </Space>
      </Card>

      <Tabs defaultActiveKey="info" items={tabItems} />
    </div>
  )
}

export default Profile
