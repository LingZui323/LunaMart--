import { defineStore } from 'pinia'

export const useChatStore = defineStore('chat', {
  state: () => ({
    unread: 0,
  }),
  actions: {
    setUnread(num) {
      this.unread = num
    },
    clearUnread() {
      this.unread = 0
    },
  },
})