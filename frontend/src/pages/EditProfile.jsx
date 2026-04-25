import React, { useState, useEffect } from 'react'
import {
  Card,
  Form,
  Input,
  Select,
  Button,
  Upload,
  Avatar,
  message,
  Row,
  Col,
} from 'antd'
import { ArrowLeftOutlined, PlusOutlined, UserOutlined } from '@ant-design/icons'
import { useNavigate } from 'react-router-dom'
import { getUserInfo, updateUserInfo, uploadImage } from '../api/user'
import useStore from '../store'

const { Option } = Select
const { TextArea } = Input

const EditProfile = () => {
  const navigate = useNavigate()
  const user = useStore((state) => state.user)
  const setUser = useStore((state) => state.setUser)
  const [form] = Form.useForm()
  const [loading, setLoading] = useState(false)
  const [avatar, setAvatar] = useState('')

  useEffect(() => {
    if (user) {
      form.setFieldsValue({
        nickname: user.nickname,
        phone: user.phone,
        email: user.email,
        studentId: user.studentId,
        school: user.school,
        gender: user.gender,
        signature: user.signature,
      })
      setAvatar(user.avatar)
    }
  }, [user])

  const avatarProps = {
    name: 'file',
    listType: 'picture-card',
    maxCount: 1,
    accept: 'image/*',
    customRequest: async ({ file, onSuccess, onError }) => {
      try {
        const res = await uploadImage(file)
        onSuccess?.()
        setAvatar(res.data.url)
        form.setFieldsValue({
          avatar: res.data.url,
        })
      } catch (error) {
        onError?.(error)
        message.error('头像上传失败')
      }
    },
  }

  const handleSubmit = async (values) => {
    setLoading(true)
    try {
      const data = {
        ...values,
        avatar: avatar || user?.avatar,
      }
      await updateUserInfo(data)
      
      const res = await getUserInfo()
      setUser(res.data)
      
      message.success('资料更新成功')
      navigate('/profile')
    } catch (error) {
      console.error('更新资料失败', error)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div>
      <Button
        icon={<ArrowLeftOutlined />}
        onClick={() => navigate('/profile')}
        style={{ marginBottom: 16 }}
      >
        返回个人中心
      </Button>

      <Card title="编辑资料">
        <Form
          form={form}
          layout="vertical"
          onFinish={handleSubmit}
        >
          <Row gutter={24}>
            <Col span={8}>
              <Form.Item label="头像">
                <Upload {...avatarProps}>
                  {avatar ? (
                    <img src={avatar} style={{ width: '100%' }} />
                  ) : (
                    <div>
                      <PlusOutlined />
                      <div style={{ marginTop: 8 }}>上传头像</div>
                    </div>
                  )}
                </Upload>
              </Form.Item>
            </Col>
            <Col span={16}>
              <Row gutter={24}>
                <Col span={12}>
                  <Form.Item
                    name="nickname"
                    label="昵称"
                    rules={[{ required: true, message: '请输入昵称' }]}
                  >
                    <Input placeholder="请输入昵称" />
                  </Form.Item>
                </Col>
                <Col span={12}>
                  <Form.Item
                    name="gender"
                    label="性别"
                  >
                    <Select placeholder="请选择性别" allowClear>
                      <Option value={1}>男</Option>
                      <Option value={2}>女</Option>
                    </Select>
                  </Form.Item>
                </Col>
              </Row>
              <Row gutter={24}>
                <Col span={12}>
                  <Form.Item
                    name="phone"
                    label="手机号"
                    rules={[
                      { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确' },
                    ]}
                  >
                    <Input placeholder="请输入手机号" />
                  </Form.Item>
                </Col>
                <Col span={12}>
                  <Form.Item
                    name="email"
                    label="邮箱"
                    rules={[
                      { type: 'email', message: '邮箱格式不正确' },
                    ]}
                  >
                    <Input placeholder="请输入邮箱" />
                  </Form.Item>
                </Col>
              </Row>
              <Row gutter={24}>
                <Col span={12}>
                  <Form.Item
                    name="studentId"
                    label="学号"
                  >
                    <Input placeholder="请输入学号" />
                  </Form.Item>
                </Col>
                <Col span={12}>
                  <Form.Item
                    name="school"
                    label="学校"
                  >
                    <Input placeholder="请输入学校" />
                  </Form.Item>
                </Col>
              </Row>
              <Form.Item
                name="signature"
                label="个性签名"
              >
                <TextArea rows={3} placeholder="请输入个性签名" maxLength={100} />
              </Form.Item>
            </Col>
          </Row>

          <Form.Item>
            <Button type="primary" htmlType="submit" loading={loading} size="large">
              保存修改
            </Button>
          </Form.Item>
        </Form>
      </Card>
    </div>
  )
}

export default EditProfile
