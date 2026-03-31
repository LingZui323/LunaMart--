import request from '../utils/request'

export function createOrder(data) {
  return request({ url: '/order/create', method: 'post', data })
}

export function getOrderList() {
  return request({ url: '/order/list', method: 'get' })
}

export function payOrder(orderNo) {
  return request({ url: `/order/pay/${orderNo}`, method: 'post' })
}