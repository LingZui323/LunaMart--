import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/', redirect: '/home' },
  { path: '/home', component: () => import('../views/Home.vue') },
  { path: '/search/ai', component: () => import('../views/AiImageSearch.vue') },
  { path: '/chat/ai', component: () => import('../views/AiChat.vue') },
  { path: '/login', component: () => import('../views/user/Login.vue') },
  { path: '/register', component: () => import('../views/user/Register.vue') },
  { path: '/profile', component: () => import('../views/user/Profile.vue') },
  { path: '/settings', component: () => import('../views/user/ProfileSettings.vue') },
  { path: '/address', component: () => import('../views/user/AddressList.vue') },
  { path: '/favorite', component: () => import('../views/user/Favorite.vue') },
  { path: '/goods/list', component: () => import('../views/goods/GoodsList.vue') },
  { path: '/goods/detail/:id', component: () => import('../views/goods/GoodsDetail.vue') },
  { path: '/goods/search', component: () => import('../views/goods/Search.vue') },
  { path: '/cart', component: () => import('../views/cart/CartIndex.vue') },
  { path: '/order/confirm', component: () => import('../views/order/OrderConfirm.vue') },
  { path: '/order/list', component: () => import('../views/order/OrderList.vue') },
  { path: '/order/detail/:id', component: () => import('../views/order/OrderDetail.vue') },
  { path: '/pay/success', component: () => import('../views/order/PaySuccess.vue') },
  { path: '/chat', component: () => import('../views/chat/SessionList.vue') },
  { path: '/chat/:id', component: () => import('../views/chat/ChatWindow.vue') },
  { path: '/aftersale/apply', component: () => import('../views/afterSale/ApplyRefund.vue') },
  { path: '/aftersale/list', component: () => import('../views/afterSale/AfterSaleList.vue') },
  { path: '/merchant', component: () => import('../views/merchant/MerchantHome.vue') },
  { path: '/merchant/board', component: () => import('../views/merchant/Dashboard.vue') },
  { path: '/merchant/goods/list', component: () => import('../views/merchant/GoodsList.vue') },
  { path: '/merchant/order/list', component: () => import('../views/merchant/OrderList.vue') },
  { path: '/merchant/aftersale', component: () => import('../views/merchant/AfterSaleHandle.vue') },
  { path: '/admin/user', component: () => import('../views/admin/UserManage.vue') },
  { path: '/admin/merchant', component: () => import('../views/admin/MerchantManage.vue') },
  { path: '/admin/goods/audit', component: () => import('../views/admin/GoodsAudit.vue') },
  { path: '/admin/order/stat', component: () => import('../views/admin/OrderStatistics.vue') },
  { path: '/admin/ai/log', component: () => import('../views/admin/AiAuditLog.vue') },
  { path: '/admin/aftersale',component: () => import('../views/admin/AdminAfterSale.vue'), // 管理员售后审核meta: { title: '管理员售后审核', login: true, role: 'ADMIN' }
}
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router