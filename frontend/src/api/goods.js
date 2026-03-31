import request from '../utils/request'

// 商品列表
export function getGoodsList(params) {
  return request({
    url: '/goods/list',
    method: 'get',
    params,
  })
}

// 商品详情
export function getGoodsDetail(id) {
  return request({
    url: `/goods/detail/${id}`,
    method: 'get',
  })
}