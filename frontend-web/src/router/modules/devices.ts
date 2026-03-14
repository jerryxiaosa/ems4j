import type { RouteRecordRaw } from 'vue-router'

export const deviceRoutes: RouteRecordRaw[] = [
  {
    path: '/devices/electric-meters',
    component: () => import('@/views/devices/DeviceElectricMeterView.vue'),
    meta: { requiresAuth: true, title: '智能电表' }
  },
  {
    path: '/devices/gateways',
    component: () => import('@/views/devices/DeviceGatewayView.vue'),
    meta: { requiresAuth: true, title: '智能网关' }
  },
  {
    path: '/devices/categories',
    component: () => import('@/views/devices/DeviceCategoryView.vue'),
    meta: { requiresAuth: true, title: '设备品类' }
  }
]
