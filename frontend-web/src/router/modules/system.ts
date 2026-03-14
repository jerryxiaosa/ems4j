import type { RouteRecordRaw } from 'vue-router'

export const systemRoutes: RouteRecordRaw[] = [
  {
    path: '/system/users',
    component: () => import('@/views/system/UserManagementView.vue'),
    meta: { requiresAuth: true, title: '用户管理' }
  },
  {
    path: '/system/roles',
    component: () => import('@/views/system/RoleManagementView.vue'),
    meta: { requiresAuth: true, title: '角色管理' }
  },
  {
    path: '/system/menus',
    component: () => import('@/views/system/MenuManagementView.vue'),
    meta: { requiresAuth: true, title: '菜单管理' }
  },
  {
    path: '/system/spaces',
    component: () => import('@/views/system/SpaceManagementView.vue'),
    meta: { requiresAuth: true, title: '空间管理' }
  },
  {
    path: '/system/organizations',
    component: () => import('@/views/system/OrganizationManagementView.vue'),
    meta: { requiresAuth: true, title: '机构管理' }
  }
]
