import type { RouteRecordRaw } from 'vue-router'

export const accountRoutes: RouteRecordRaw[] = [
  {
    path: '',
    redirect: '/accounts/info'
  },
  {
    path: '/accounts',
    redirect: '/accounts/info'
  },
  {
    path: '/accounts/info',
    component: () => import('@/views/accounts/AccountInfoView.vue'),
    meta: { requiresAuth: true, title: '账户信息' }
  },
  {
    path: '/accounts/cancel-records',
    component: () => import('@/views/accounts/AccountCancelRecordView.vue'),
    meta: { requiresAuth: true, title: '销户记录' }
  }
]
