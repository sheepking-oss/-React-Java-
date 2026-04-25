import React from 'react'
import { Form, Input, Button, Card, message, Tabs } from 'antd'
import { UserOutlined, LockOutlined } from '@ant-design/icons'
import { useNavigate } from 'react-router-dom'
import { login, register } from '../api/user'
import useStore from '../store'

const Login = () => {
  const navigate = useNavigate()
  const [form] = Form.useForm()
  const [loading, setLoading] = React.useState(false)
  const setLogin = useStore((state) => state.login)

  const handleLogin = async (values) => {
    setLoading(true)
    try {
      const res = await login(values)
      setLogin(res.data.user, res.data.token)
      message.success('登录成功')
      navigate('/')
    } catch (error) {
      console.error('登录失败', error)
    } finally {
      setLoading(false)
    }
  }

  const handleRegister = async (values) => {
    if (values.password !== values.confirmPassword) {
      message.error('两次输入的密码不一致')
      return
    }
    setLoading(true)
    try {
      await register(values)
      message.success('注册成功，请登录')
      form.resetFields()
    } catch (error) {
      console.error('注册失败', error)
    } finally {
      setLoading(false)
    }
  }

  const loginForm = (
    <Form
      name="login"
      form={form}
      initialValues={{ remember: true }}
      onFinish={handleLogin}
      size="large"
    >
      <Form.Item
        name="username"
        rules={[{ required: true, message: '请输入用户名' }]}
      >
        <Input prefix={<UserOutlined />} placeholder="用户名" />
      </Form.Item>

      <Form.Item
        name="password"
        rules={[{ required: true, message: '请输入密码' }]}
      >
        <Input.Password prefix={<LockOutlined />} placeholder="密码" />
      </Form.Item>

      <Form.Item>
        <Button type="primary" htmlType="submit" loading={loading} block>
          登录
        </Button>
      </Form.Item>
    </Form>
  )

  const registerForm = (
    <Form
      name="register"
      form={form}
      onFinish={handleRegister}
      size="large"
    >
      <Form.Item
        name="username"
        rules={[{ required: true, message: '请输入用户名' }]}
      >
        <Input prefix={<UserOutlined />} placeholder="用户名" />
      </Form.Item>

      <Form.Item
        name="nickname"
        rules={[{ required: true, message: '请输入昵称' }]}
      >
        <Input placeholder="昵称" />
      </Form.Item>

      <Form.Item
        name="phone"
        rules={[{ pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确' }]}
      >
        <Input placeholder="手机号" />
      </Form.Item>

      <Form.Item
        name="studentId"
      >
        <Input placeholder="学号（选填）" />
      </Form.Item>

      <Form.Item
        name="school"
      >
        <Input placeholder="学校（选填）" />
      </Form.Item>

      <Form.Item
        name="password"
        rules={[{ required: true, message: '请输入密码' }]}
      >
        <Input.Password prefix={<LockOutlined />} placeholder="密码" />
      </Form.Item>

      <Form.Item
        name="confirmPassword"
        rules={[{ required: true, message: '请确认密码' }]}
      >
        <Input.Password prefix={<LockOutlined />} placeholder="确认密码" />
      </Form.Item>

      <Form.Item>
        <Button type="primary" htmlType="submit" loading={loading} block>
          注册
        </Button>
      </Form.Item>
    </Form>
  )

  const tabItems = [
    { key: 'login', label: '登录', children: loginForm },
    { key: 'register', label: '注册', children: registerForm },
  ]

  return (
    <div
      style={{
        height: '100vh',
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
      }}
    >
      <Card
        style={{ width: 400 }}
        title={
          <div style={{ textAlign: 'center', fontSize: 20, fontWeight: 'bold' }}>
            校园二手交易平台
          </div>
        }
      >
        <Tabs defaultActiveKey="login" items={tabItems} />
      </Card>
    </div>
  )
}

export default Login
