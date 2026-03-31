import { defineStore } from 'pinia'

export const useCartStore = defineStore('cart', {
  state: () => ({
    cartList: [],
  }),
  actions: {
    addCart(goods) {
      const exist = this.cartList.find(item => item.goodsId === goods.goodsId)
      if (exist) {
        exist.quantity += goods.quantity
      } else {
        this.cartList.push(goods)
      }
    },
    removeCart(goodsId) {
      this.cartList = this.cartList.filter(item => item.goodsId !== goodsId)
    },
    clearCart() {
      this.cartList = []
    },
  },
})