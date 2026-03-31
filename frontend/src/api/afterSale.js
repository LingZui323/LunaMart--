import request from '../utils/request'

export function applyAfterSale(data) {
  return request({ url: '/aftersale/apply', method: 'post', data })
}

export function getAfterSaleList() {
  return request({ url: '/aftersale/list', method: 'get' })
}