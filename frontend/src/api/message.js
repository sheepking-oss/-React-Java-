import request from '../utils/request'

export const sendMessage = (data) => {
  return request.post('/message/send', data)
}

export const getMessageList = (params) => {
  return request.get('/message/list', { params })
}

export const getConversation = (params) => {
  return request.get('/message/conversation', { params })
}

export const markAsRead = (params) => {
  return request.post('/message/markRead', null, { params })
}

export const getUnreadCount = () => {
  return request.get('/message/unreadCount')
}
