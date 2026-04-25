import React, { useEffect } from 'react'
import { Layout as AntLayout, Menu, Input, Button, Avatar, Dropdown, Badge, Space, theme } from 'antd'
import {
  HomeOutlined,
  ShoppingCartOutlined,
  HeartOutlined,
  MessageOutlined,
  UserOutlined,
  LogoutOutlined,
  PlusOutlined,
  SearchOutlined,
  SwapOutlined,
  FlagOutlined,
  SettingOutlined,
} from '@ant-design/icons'
import { Outlet, useNavigate, useLocation } from 'react-router-dom'
import useStore from '../store'
import { getUnreadCount } from '../api/message'

const { Header, Sider, Content } = AntLayout

const Layout = () => {
  const navigate = useNavigate()
  const location = useLocation()
  const user = useStore((state) => state.user)
  const token = useStore((state) => state.token)
  const logout = useStore((state) => state.logout)
  const unreadCount = useStore((state) => state.unreadCount)
  const setUnreadCount = useStore((state) => state.setUnreadCount)

  useEffect(() => {
    if (token) {
      fetchUnreadCount()
    }
  }, [token])

  const fetchUnreadCount = async () => {
    try {
      const res = await getUnreadCount()
      setUnreadCount(res.data.count)
    } catch (error) {
      console.error('获取未读消息数失败', error)
    }
  }

  const userMenuItems = [
    {
      key: 'profile',
      icon: <UserOutlined />,
      label: '个人中心',
      onClick: () => navigate('/profile'),
    },
    {
      key: 'edit',
      icon: <SettingOutlined />,
      label: '编辑资料',
      onClick: () => navigate('/profile/edit'),
    },
    {
      key: 'password',
      icon: <SettingOutlined />,
      label: '修改密码',
      onClick: () => navigate('/profile/password'),
    },
    {
      type: 'divider',
    },
    {
      key: 'logout',
      icon: <LogoutOutlined />,
      label: '退出登录',
      onClick: () => {
        logout()
        navigate('/login')
      },
    },
  ]

  const siderMenuItems = [
    {
      key: '/',
      icon: <HomeOutlined />,
      label: '首页',
      onClick: () => navigate('/'),
    },
    {
      key: '/publish',
      icon: <PlusOutlined />,
      label: '发布商品',
      onClick: () => navigate('/publish'),
    },
    {
      key: '/my-products',
      icon: <ShoppingCartOutlined />,
      label: '我的商品',
      onClick: () => navigate('/my-products'),
    },
    {
      key: '/my-orders',
      icon: <ShoppingCartOutlined />,
      label: '我的订单',
      onClick: () => navigate('/my-orders'),
    },
    {
      key: '/my-sales',
      icon: <ShoppingCartOutlined />,
      label: '卖出的商品',
      onClick: () => navigate('/my-sales'),
    },
    {
      key: '/favorites',
      icon: <HeartOutlined />,
      label: '我的收藏',
      onClick: () => navigate('/favorites'),
    },
    {
      key: '/messages',
      icon: <MessageOutlined />,
      label: '消息中心',
      onClick: () => navigate('/messages'),
    },
    {
      key: '/exchanges',
      icon: <SwapOutlined />,
      label: '换物申请',
      onClick: () => navigate('/exchanges'),
    },
    {
      key: '/my-reports',
      icon: <FlagOutlined />,
      label: '我的举报',
      onClick: () => navigate('/my-reports'),
    },
  ]

  const {
    token: { colorBgContainer },
  } = theme.useToken()

  const selectedKey = location.pathname === '/' ? '/' : 
    siderMenuItems.find(item => location.pathname.startsWith(item.key))?.key || '/'

  return (
    <AntLayout style={{ minHeight: '100vh' }}>
      <Header
        style={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          background: colorBgContainer,
          padding: '0 24px',
        }}
      >
        <div
          className="logo"
          style={{ cursor: 'pointer', fontSize: '20px', fontWeight: 'bold', color: '#1890ff' }}
          onClick={() => navigate('/')}
        >
          校园二手交易平台
        </div>

        <Space size="middle">
          <Input.Search
            placeholder="搜索商品"
            allowClear
            size="large"
            style={{ width: 400 }}
            onSearch={(value) => navigate(`/?keyword=${encodeURIComponent(value)}`)}
            enterButton={<SearchOutlined />}
          />

          {token ? (
            <>
              <Button type="primary" icon={<PlusOutlined />} onClick={() => navigate('/publish')}>
                发布商品
              </Button>
              <Badge count={unreadCount} overflowCount={99}>
                <Button icon={<MessageOutlined />} onClick={() => navigate('/messages')}>
                  消息
                </Button>
              </Badge>
              <Dropdown menu={{ items: userMenuItems }} placement="bottomRight">
                <div style={{ cursor: 'pointer', display: 'flex', alignItems: 'center', gap: 8 }}>
                  <Avatar size="large" icon={<UserOutlined />} src={user?.avatar} />
                  <span>{user?.nickname || user?.username}</span>
                </div>
              </Dropdown>
            </>
          ) : (
            <Space>
              <Button onClick={() => navigate('/login')}>登录</Button>
              <Button type="primary" onClick={() => navigate('/register')}>
                注册
              </Button>
            </Space>
          )}
        </Space>
      </Header>

      <AntLayout>
        {token && (
          <Sider
            width={200}
            style={{
              background: colorBgContainer,
              borderRight: '1px solid #f0f0f0',
            }}
          >
            <Menu
              mode="inline"
              selectedKeys={[selectedKey]}
              style={{ height: '100%', borderRight: 0 }}
              items={siderMenuItems}
            />
          </Sider>
        )}
        <Content
          style={{
            padding: 24,
            minHeight: 280,
            background: '#f5f5f5',
          }}
        >
          <Outlet />
        </Content>
      </AntLayout>
    </AntLayout>
  )
}

export default Layout
