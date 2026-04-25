import request from '../utils/request'

export const getProductList = (params) => {
  return request.get('/product/list', { params })
}

export const getProductDetail = (id) => {
  return request.get(`/product/detail/${id}`)
}

export const getCategories = () => {
  return request.get('/product/category')
}

export const publishProduct = (data) => {
  return request.post('/product/publish', data)
}

export const updateProduct = (data) => {
  return request.put('/product/update', data)
}

export const deleteProduct = (id) => {
  return request.delete(`/product/delete/${id}`)
}

export const updateProductStatus = (id, status) => {
  return request.put(`/product/status/${id}`, null, { params: { status } })
}

export const uploadImage = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/upload/image', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

export const uploadImages = (files) => {
  const formData = new FormData()
  files.forEach((file) => {
    formData.append('files', file)
  })
  return request.post('/upload/images', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}
