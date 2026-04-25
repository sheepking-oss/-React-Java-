import request from '../utils/request'

export const createReview = (data) => {
  return request.post('/review/create', data)
}

export const getMyReviews = (params) => {
  return request.get('/review/myReviews', { params })
}

export const getUserReviews = (userId, params) => {
  return request.get(`/review/user/${userId}`, { params })
}

export const getReviewByOrderId = (orderId) => {
  return request.get(`/review/byOrder/${orderId}`)
}
