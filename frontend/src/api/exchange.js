import request from '../utils/request'

export const createExchange = (data) => {
  return request.post('/exchange/create', data)
}

export const getExchangeDetail = (id) => {
  return request.get(`/exchange/detail/${id}`)
}

export const getMyExchanges = (params) => {
  return request.get('/exchange/myExchanges', { params })
}

export const getMyReceivedExchanges = (params) => {
  return request.get('/exchange/myReceived', { params })
}

export const acceptExchange = (id) => {
  return request.post(`/exchange/accept/${id}`)
}

export const rejectExchange = (id, reason) => {
  return request.post(`/exchange/reject/${id}`, { reason })
}

export const cancelExchange = (id) => {
  return request.post(`/exchange/cancel/${id}`)
}
