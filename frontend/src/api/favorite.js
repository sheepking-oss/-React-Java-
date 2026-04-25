import request from '../utils/request'

export const toggleFavorite = (productId) => {
  return request.post(`/favorite/toggle/${productId}`)
}

export const checkFavorite = (productId) => {
  return request.get(`/favorite/check/${productId}`)
}

export const getFavoriteList = (params) => {
  return request.get('/favorite/list', { params })
}
