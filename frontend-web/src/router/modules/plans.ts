import type { RouteRecordRaw } from 'vue-router'

export const planRoutes: RouteRecordRaw[] = [
  {
    path: '/plans/electric',
    component: () => import('@/views/plans/ElectricPricePlanView.vue'),
    meta: { requiresAuth: true, title: '电价方案' }
  },
  {
    path: '/plans/warn',
    component: () => import('@/views/plans/WarnPlanView.vue'),
    meta: { requiresAuth: true, title: '预警方案' }
  }
]
