import request from '../utils/request'

export function aiSearchImage(data) {
  return request({ url: '/ai/search/image', method: 'post', data })
}

export function aiChat(data) {
  return request({ url: '/ai/chat', method: 'post', data })
}