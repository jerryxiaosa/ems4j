import type { RouteRecordRaw } from 'vue-router'

export const operationRoutes: RouteRecordRaw[] = [
  {
    path: '/device-operations',
    component: () => import('@/views/device/DeviceOperationsView.vue'),
    meta: { requiresAuth: true, title: '设备操作' }
  },
  {
    path: '/device-operations/detail',
    component: () => import('@/views/device/DeviceOperationDetailView.vue'),
    meta: { requiresAuth: true, title: '设备操作详情' }
  }
]
