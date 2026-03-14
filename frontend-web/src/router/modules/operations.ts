import type { RouteRecordRaw } from 'vue-router'

export const operationRoutes: RouteRecordRaw[] = [
  {
    path: '/operations',
    component: () => import('@/views/ModulePlaceholderView.vue'),
    meta: { requiresAuth: true, title: '命令记录' }
  }
]
