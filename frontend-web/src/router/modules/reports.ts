import type { RouteRecordRaw } from 'vue-router'

export const reportRoutes: RouteRecordRaw[] = [
  {
    path: '/reports/electric-bill',
    component: () => import('@/views/reports/ElectricBillReportView.vue'),
    meta: { requiresAuth: true, title: '电费报表' }
  },
  {
    path: '/reports/monthly-electricity',
    component: () => import('@/views/ModulePlaceholderView.vue'),
    meta: { requiresAuth: true, title: '每月用电统计' }
  }
]
