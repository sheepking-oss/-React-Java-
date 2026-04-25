import request from '../utils/request'

export const createReport = (data) => {
  return request.post('/report/create', data)
}

export const getMyReports = (params) => {
  return request.get('/report/myReports', { params })
}

export const getReportDetail = (id) => {
  return request.get(`/report/detail/${id}`)
}
