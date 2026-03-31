import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
  state: () => ({
    userInfo: null,
    token: localStorage.getItem('token') || ''
  }),

  getters: {
    isLogin: (state) => !!state.token && !!state.userInfo
  },

  actions: {
    login(userData) {
      this.token = userData.token
      this.userInfo = {
        id: userData.id,
        username: userData.username,
        email: userData.email,
        role: userData.role
      }
      localStorage.setItem('token', this.token)
      localStorage.setItem('userInfo', JSON.stringify(this.userInfo))

    },

    logout() {
      this.token = ''
      this.userInfo = null
      localStorage.removeItem('token')
    }
  },

  persist: {
    key: 'user-store',
    storage: localStorage,
    paths: ['token', 'userInfo']
  }
})