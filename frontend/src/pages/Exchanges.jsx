import React, { useState, useEffect } from 'react'
import {
  Card,
  Tabs,
  Table,
  Tag,
  Button,
  Space,
  Modal,
  Form,
  Input,
  Select,
  message,
  Image,
} from 'antd'
import { useNavigate } from 'react-router-dom'
import { getMyExchanges, getMyReceivedExchanges, createExchange, getProductList } from '../api/exchange'
import { getProductList as getProducts } from '../api/product'
import dayjs from 'dayjs'

const { Option } = Select
const { TextArea } = Input

const Exchanges = () => {
  const navigate = useNavigate()
  const [activeTab, setActiveTab] = useState('sent')
  const [loading, setLoading] = useState(false)
  const [data, setData] = useState([])
  const [pagination, setPagination] = useState({
    current: 1,
    pageSize: 10,
    total: 0,
  })
  const [createModalVisible, setCreateModalVisible] = useState(false)
  const [createForm] = Form.useForm()
  const [myProducts, setMyProducts] = useState([])
  const [submitting, setSubmitting] = useState(false)

  useEffect(() => {
    fetchExchanges()
  }, [activeTab, pagination.current, pagination.pageSize])

  const fetchExchanges = async () => {
    setLoading(true)
    try {
      const api = activeTab === 'sent' ? getMyExchanges : getMyReceivedExchanges
      const res = await api({
        current: pagination.current,
        size: pagination.pageSize,
      })
      setData(res.data.records || [])
      setPagination((prev) => ({ ...prev, total: res.data.total || 0 }))
    } catch (error) {
      console.error('获取换物申请列表失败', error)
    } finally {
      setLoading(false)
    }
  }

  const fetchMyProducts = async () => {
    try {
      const res = await getProducts({
        status: 1,
        current: 1,
        size: 100,
      })
      setMyProducts(res.data.records || [])
    } catch (error) {
      console.error('获取我的商品失败', error)
    }
  }

  const handleAccept = async (id) => {
    try {
      await api.acceptExchange(id)
      message.success('已同意换物申请')
      fetchExchanges()
    } catch (error) {
      console.error('同意换物申请失败', error)
    }
  }

  const handleReject = async (id, reason) => {
    try {
      await api.rejectExchange(id, reason)
      message.success('已拒绝换物申请')
      fetchExchanges()
    } catch (error) {
      console.error('拒绝换物申请失败', error)
    }
  }

  const handleCancel = async (id) => {
    try {
      await api.cancelExchange(id)
      message.success('已取消换物申请')
      fetchExchanges()
    } catch (error) {
      console.error('取消换物申请失败', error)
    }
  }

  const handleCreate = async (values) => {
    setSubmitting(true)
    try {
      await createExchange(values)
      message.success('换物申请已发送')
      setCreateModalVisible(false)
      createForm.resetFields()
      fetchExchanges()
    } catch (error) {
      console.error('创建换物申请失败', error)
    } finally {
      setSubmitting(false)
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

  const tabItems = [
    { key: 'sent', label: '我发起的' },
    { key: 'received', label: '我收到的' },
  ]

  const columns = [
    {
      title: activeTab === 'sent' ? '目标商品' : '发起商品',
      dataIndex: activeTab === 'sent' ? 'targetProduct' : 'initiatorProduct',
      width: 200,
      render: (product) =>
        product ? (
          <Space>
            <Image
              src={
                product.coverImage ||
                'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=default%20product%20placeholder&image_size=square'
              }
              style={{ width: 50, height: 50, objectFit: 'cover', borderRadius: 4 }}
              preview={false}
            />
            <div style={{ maxWidth: 120 }}>
              <div style={{ overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                {product.title}
              </div>
              <div className="product-price" style={{ fontSize: 12 }}>
                ¥{product.price?.toFixed(2)}
              </div>
            </div>
          </Space>
        ) : (
          <span>-</span>
        ),
    },
    {
      title: activeTab === 'sent' ? '对方用户' : '发起用户',
      dataIndex: activeTab === 'sent' ? 'target' : 'initiator',
      width: 120,
      render: (user) => user?.nickname || '-',
    },
    {
      title: '申请描述',
      dataIndex: 'initiatorDescription',
      width: 200,
      ellipsis: true,
    },
    {
      title: '状态',
      dataIndex: 'status',
      width: 100,
      render: (status) => getStatusTag(status),
    },
    {
      title: '申请时间',
      dataIndex: 'createTime',
      width: 160,
      render: (time) => dayjs(time).format('YYYY-MM-DD HH:mm'),
    },
    {
      title: '操作',
      width: 200,
      fixed: 'right',
      render: (_, record) => (
        <Space>
          <Button type="link" size="small" onClick={() => navigate(`/exchange/${record.id}`)}>
            详情
          </Button>
          {activeTab === 'received' && record.status === 'PENDING' && (
            <>
              <Button type="primary" size="small" onClick={() => handleAccept(record.id)}>
                同意
              </Button>
              <Button
                danger
                size="small"
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
                      handleReject(record.id, reason)
                    },
                  })
                }}
              >
                拒绝
              </Button>
            </>
          )}
          {activeTab === 'sent' && record.status === 'PENDING' && (
            <Button type="link" size="small" danger onClick={() => handleCancel(record.id)}>
              取消
            </Button>
          )}
        </Space>
      ),
    },
  ]

  return (
    <div>
      <Card
        title="换物申请"
        extra={
          <Button
            type="primary"
            onClick={() => {
              fetchMyProducts()
              setCreateModalVisible(true)
            }}
          >
            发起换物
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
          scroll={{ x: 900 }}
        />
      </Card>

      <Modal
        title="发起换物申请"
        open={createModalVisible}
        onCancel={() => setCreateModalVisible(false)}
        footer={null}
        width={600}
      >
        <Form form={createForm} layout="vertical" onFinish={handleCreate}>
          <Form.Item
            name="initiatorProductId"
            label="我的商品"
            help="选择您想要换出的商品（选填，可只描述想换的物品）"
          >
            <Select placeholder="请选择商品" allowClear>
              {myProducts.map((product) => (
                <Option key={product.id} value={product.id}>
                  {product.title} (¥{product.price?.toFixed(2)})
                </Option>
              ))}
            </Select>
          </Form.Item>
          <Form.Item
            name="initiatorDescription"
            label="换物描述"
            rules={[{ required: true, message: '请输入换物描述' }]}
          >
            <TextArea
              rows={4}
              placeholder="请描述您想要换出的物品或想换入的物品，如：我想用这本教材换一本计算机相关的书..."
            />
          </Form.Item>
          <Form.Item>
            <Button type="primary" htmlType="submit" loading={submitting} block>
              发起申请
            </Button>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )
}

export default Exchanges
