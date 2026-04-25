import request from '../utils/request'

export const createOrder = (data) => {
  return request.post('/order/create', data)
}

export const getOrderDetail = (orderNo) => {
  return request.get(`/order/detail/${orderNo}`)
}

export const getMyOrders = (params) => {
  return request.get('/order/myOrders', { params })
}

export const getMySales = (params) => {
  return request.get('/order/mySales', { params })
}

export const cancelOrder = (orderNo, reason) => {
  return request.post(`/order/cancel/${orderNo}`, { reason })
}

export const payOrder = (orderNo) => {
  return request.post(`/order/pay/${orderNo}`)
}

export const shipOrder = (orderNo) => {
  return request.post(`/order/ship/${orderNo}`)
}

export const confirmOrder = (orderNo) => {
  return request.post(`/order/confirm/${orderNo}`)
}
