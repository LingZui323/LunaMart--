import request from '../utils/request'

export function getSessionList() {
  return request({ url: '/chat/session', method: 'get' })
}

export function getMessage(sessionId) {
  return request({ url: `/chat/message/${sessionId}`, method: 'get' })
}

export function sendMessage(data) {
  return request({ url: '/chat/send', method: 'post', data })
}