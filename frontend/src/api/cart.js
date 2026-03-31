import request from '../utils/request'

export function getCart() {
  return request({ url: '/cart/list', method: 'get' })
}

export function addCart(data) {
  return request({ url: '/cart/add', method: 'post', data })
}

export function deleteCart(id) {
  return request({ url: `/cart/delete/${id}`, method: 'post' })
}