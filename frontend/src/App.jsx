import React, { useEffect } from 'react'
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import Layout from './components/Layout'
import Home from './pages/Home'
import Login from './pages/Login'
import Register from './pages/Register'
import ProductDetail from './pages/ProductDetail'
import PublishProduct from './pages/PublishProduct'
import MyProducts from './pages/MyProducts'
import OrderDetail from './pages/OrderDetail'
import MyOrders from './pages/MyOrders'
import MySales from './pages/MySales'
import Favorites from './pages/Favorites'
import Messages from './pages/Messages'
import Chat from './pages/Chat'
import Exchanges from './pages/Exchanges'
import ExchangeDetail from './pages/ExchangeDetail'
import MyReports from './pages/MyReports'
import ReportForm from './pages/ReportForm'
import Profile from './pages/Profile'
import EditProfile from './pages/EditProfile'
import ChangePassword from './pages/ChangePassword'
import useStore from './store'
import { getUnreadCount } from './api/message'

const PrivateRoute = ({ children }) => {
  const token = useStore((state) => state.token)
  return token ? children : <Navigate to="/login" />
}

function App() {
  const token = useStore((state) => state.token)
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

  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route path="/" element={<Layout />}>
          <Route index element={<Home />} />
          <Route path="product/:id" element={<ProductDetail />} />
          <Route path="publish" element={
            <PrivateRoute>
              <PublishProduct />
            </PrivateRoute>
          } />
          <Route path="publish/:id" element={
            <PrivateRoute>
              <PublishProduct />
            </PrivateRoute>
          } />
          <Route path="my-products" element={
            <PrivateRoute>
              <MyProducts />
            </PrivateRoute>
          } />
          <Route path="order/:orderNo" element={
            <PrivateRoute>
              <OrderDetail />
            </PrivateRoute>
          } />
          <Route path="my-orders" element={
            <PrivateRoute>
              <MyOrders />
            </PrivateRoute>
          } />
          <Route path="my-sales" element={
            <PrivateRoute>
              <MySales />
            </PrivateRoute>
          } />
          <Route path="favorites" element={
            <PrivateRoute>
              <Favorites />
            </PrivateRoute>
          } />
          <Route path="messages" element={
            <PrivateRoute>
              <Messages />
            </PrivateRoute>
          } />
          <Route path="chat" element={
            <PrivateRoute>
              <Chat />
            </PrivateRoute>
          } />
          <Route path="exchanges" element={
            <PrivateRoute>
              <Exchanges />
            </PrivateRoute>
          } />
          <Route path="exchange/:id" element={
            <PrivateRoute>
              <ExchangeDetail />
            </PrivateRoute>
          } />
          <Route path="my-reports" element={
            <PrivateRoute>
              <MyReports />
            </PrivateRoute>
          } />
          <Route path="report" element={
            <PrivateRoute>
              <ReportForm />
            </PrivateRoute>
          } />
          <Route path="profile" element={
            <PrivateRoute>
              <Profile />
            </PrivateRoute>
          } />
          <Route path="profile/edit" element={
            <PrivateRoute>
              <EditProfile />
            </PrivateRoute>
          } />
          <Route path="profile/password" element={
            <PrivateRoute>
              <ChangePassword />
            </PrivateRoute>
          } />
        </Route>
      </Routes>
    </BrowserRouter>
  )
}

export default App
