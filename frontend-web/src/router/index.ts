import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { pinia } from '@/stores'
import { useAuthStore } from '@/stores/auth'
import { usePermissionStore } from '@/stores/permission'
import { getAccessToken } from '@/utils/token'
import { accountRoutes } from '@/router/modules/accounts'
import { deviceRoutes } from '@/router/modules/devices'
import { operationRoutes } from '@/router/modules/operations'
import { planRoutes } from '@/router/modules/plans'
import { reportRoutes } from '@/router/modules/reports'
import { systemRoutes } from '@/router/modules/system'
import { tradeRoutes } from '@/router/modules/trades'
import {
  buildRegisteredProtectedPaths,
  createRouteGuard,
  updateDocumentTitle
} from '@/router/guard'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/LoginView.vue'),
    meta: { public: true, title: '登录' }
  },
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      ...accountRoutes,
      ...planRoutes,
      ...tradeRoutes,
      ...reportRoutes,
      ...deviceRoutes,
      ...systemRoutes,
      ...operationRoutes
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/accounts/info'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

let registeredProtectedPathSet: Set<string> | null = null

const getRegisteredProtectedPaths = () => {
  if (registeredProtectedPathSet) {
    return [...registeredProtectedPathSet]
  }

  registeredProtectedPathSet = new Set(buildRegisteredProtectedPaths(router.getRoutes()))
  return [...registeredProtectedPathSet]
}

router.beforeEach(
  createRouteGuard({
    getAccessToken,
    getAuthStore: () => useAuthStore(pinia),
    getPermissionStore: () => usePermissionStore(pinia),
    getRegisteredProtectedPaths
  })
)

router.afterEach((to) => {
  updateDocumentTitle(to)
})

export default router
