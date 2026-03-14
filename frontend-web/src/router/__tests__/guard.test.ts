import type { RouteRecordNormalized } from 'vue-router'
import {
  buildRegisteredProtectedPaths,
  createRouteGuard,
  handleMenuLoadFailure,
  normalizePath,
  resolveDocumentTitle,
  resolveFirstAccessiblePath
} from '@/router/guard'

const createAuthStore = () => ({
  clearSession: vi.fn()
})

const createPermissionStore = () => ({
  allowedPaths: [] as string[],
  firstAccessiblePath: '',
  clear: vi.fn(),
  hasPath: vi.fn<(path: string) => boolean>(),
  loadMenus: vi.fn<(options: { registeredPaths: string[] }) => Promise<void>>()
})

describe('router guard', () => {
  test('testResolveDocumentTitle_WhenTitleProvided_ShouldAppendAppTitle', () => {
    expect(resolveDocumentTitle('用户管理', 'EMS')).toBe('用户管理 - EMS')
    expect(resolveDocumentTitle('', 'EMS')).toBe('EMS')
  })

  test('testNormalizePath_WhenRootAndTrailingSlash_ShouldNormalize', () => {
    expect(normalizePath('/')).toBe('/')
    expect(normalizePath('/system/users/')).toBe('/system/users')
  })

  test('testBuildRegisteredProtectedPaths_WhenRoutesProvided_ShouldFilterWildcardAndNormalize', () => {
    const routes = [
      {
        path: '/system/users/',
        meta: { requiresAuth: true }
      },
      {
        path: '/login',
        meta: { public: true }
      },
      {
        path: '/:pathMatch(.*)*',
        meta: { requiresAuth: true }
      }
    ] as unknown as RouteRecordNormalized[]

    expect(buildRegisteredProtectedPaths(routes)).toEqual(['/system/users'])
  })

  test('testResolveFirstAccessiblePath_WhenFirstPathMissing_ShouldFallbackAllowedPath', () => {
    expect(
      resolveFirstAccessiblePath({
        firstAccessiblePath: '',
        allowedPaths: ['/accounts/info']
      })
    ).toBe('/accounts/info')
  })

  test('testHandleMenuLoadFailure_WhenRedirectProvided_ShouldReturnLoginRedirect', () => {
    const authStore = createAuthStore()

    expect(handleMenuLoadFailure(authStore, '/system/users')).toEqual({
      path: '/login',
      query: {
        redirect: '/system/users'
      },
      replace: true
    })
    expect(authStore.clearSession).toHaveBeenCalledTimes(1)
  })

  test('testCreateRouteGuard_WhenProtectedRouteWithoutToken_ShouldRedirectLogin', async () => {
    const guard = createRouteGuard({
      getAccessToken: () => '',
      getAuthStore: createAuthStore,
      getPermissionStore: createPermissionStore,
      getRegisteredProtectedPaths: () => ['/system/users']
    })

    await expect(
      guard({
        path: '/system/users',
        fullPath: '/system/users?page=1',
        meta: { requiresAuth: true }
      })
    ).resolves.toEqual({
      path: '/login',
      query: {
        redirect: '/system/users?page=1'
      }
    })
  })

  test('testCreateRouteGuard_WhenLoginRouteWithTokenAndAccessiblePath_ShouldRedirectFirstPath', async () => {
    const authStore = createAuthStore()
    const permissionStore = createPermissionStore()
    permissionStore.firstAccessiblePath = '/accounts/info'
    permissionStore.loadMenus.mockResolvedValue()
    const guard = createRouteGuard({
      getAccessToken: () => 'access-token',
      getAuthStore: () => authStore,
      getPermissionStore: () => permissionStore,
      getRegisteredProtectedPaths: () => ['/accounts/info']
    })

    await expect(
      guard({
        path: '/login',
        fullPath: '/login',
        meta: { public: true }
      })
    ).resolves.toEqual({
      path: '/accounts/info',
      replace: true
    })
    expect(permissionStore.loadMenus).toHaveBeenCalledWith({
      registeredPaths: ['/accounts/info']
    })
  })

  test('testCreateRouteGuard_WhenLoginRouteWithMenuLoadFailure_ShouldClearSessionAndAllow', async () => {
    const authStore = createAuthStore()
    const permissionStore = createPermissionStore()
    permissionStore.loadMenus.mockRejectedValue(new Error('菜单失败'))
    const guard = createRouteGuard({
      getAccessToken: () => 'access-token',
      getAuthStore: () => authStore,
      getPermissionStore: () => permissionStore,
      getRegisteredProtectedPaths: () => ['/accounts/info']
    })

    await expect(
      guard({
        path: '/login',
        fullPath: '/login',
        meta: { public: true }
      })
    ).resolves.toBe(true)
    expect(authStore.clearSession).toHaveBeenCalledTimes(1)
  })

  test('testCreateRouteGuard_WhenProtectedRouteLoadMenusFails_ShouldRedirectLogin', async () => {
    const authStore = createAuthStore()
    const permissionStore = createPermissionStore()
    permissionStore.loadMenus.mockRejectedValue(new Error('菜单失败'))
    const guard = createRouteGuard({
      getAccessToken: () => 'access-token',
      getAuthStore: () => authStore,
      getPermissionStore: () => permissionStore,
      getRegisteredProtectedPaths: () => ['/system/users']
    })

    await expect(
      guard({
        path: '/system/users',
        fullPath: '/system/users',
        meta: { requiresAuth: true }
      })
    ).resolves.toEqual({
      path: '/login',
      query: {
        redirect: '/system/users'
      },
      replace: true
    })
  })

  test('testCreateRouteGuard_WhenProtectedRouteHasPermission_ShouldAllow', async () => {
    const authStore = createAuthStore()
    const permissionStore = createPermissionStore()
    permissionStore.loadMenus.mockResolvedValue()
    permissionStore.hasPath.mockReturnValue(true)
    const guard = createRouteGuard({
      getAccessToken: () => 'access-token',
      getAuthStore: () => authStore,
      getPermissionStore: () => permissionStore,
      getRegisteredProtectedPaths: () => ['/system/users']
    })

    await expect(
      guard({
        path: '/system/users/',
        fullPath: '/system/users/',
        meta: { requiresAuth: true }
      })
    ).resolves.toBe(true)
    expect(permissionStore.hasPath).toHaveBeenCalledWith('/system/users')
  })

  test('testCreateRouteGuard_WhenProtectedRouteDeniedAndHasFallback_ShouldRedirectFallback', async () => {
    const permissionStore = createPermissionStore()
    permissionStore.loadMenus.mockResolvedValue()
    permissionStore.hasPath.mockReturnValue(false)
    permissionStore.firstAccessiblePath = '/accounts/info'
    const guard = createRouteGuard({
      getAccessToken: () => 'access-token',
      getAuthStore: createAuthStore,
      getPermissionStore: () => permissionStore,
      getRegisteredProtectedPaths: () => ['/system/users']
    })

    await expect(
      guard({
        path: '/system/users',
        fullPath: '/system/users',
        meta: { requiresAuth: true }
      })
    ).resolves.toEqual({
      path: '/accounts/info',
      replace: true
    })
  })

  test('testCreateRouteGuard_WhenProtectedRouteDeniedAndNoFallback_ShouldClearSessionAndRedirectLogin', async () => {
    const authStore = createAuthStore()
    const permissionStore = createPermissionStore()
    permissionStore.loadMenus.mockResolvedValue()
    permissionStore.hasPath.mockReturnValue(false)
    const guard = createRouteGuard({
      getAccessToken: () => 'access-token',
      getAuthStore: () => authStore,
      getPermissionStore: () => permissionStore,
      getRegisteredProtectedPaths: () => ['/system/users']
    })

    await expect(
      guard({
        path: '/system/users',
        fullPath: '/system/users',
        meta: { requiresAuth: true }
      })
    ).resolves.toEqual({
      path: '/login',
      query: {
        redirect: '/system/users'
      }
    })
    expect(authStore.clearSession).toHaveBeenCalledTimes(1)
    expect(permissionStore.clear).toHaveBeenCalledTimes(1)
  })

  test('testCreateRouteGuard_WhenLoginRouteHasNoFallback_ShouldClearSessionAndAllow', async () => {
    const authStore = createAuthStore()
    const permissionStore = createPermissionStore()
    permissionStore.loadMenus.mockResolvedValue()
    const guard = createRouteGuard({
      getAccessToken: () => 'access-token',
      getAuthStore: () => authStore,
      getPermissionStore: () => permissionStore,
      getRegisteredProtectedPaths: () => ['/accounts/info']
    })

    await expect(
      guard({
        path: '/login',
        fullPath: '/login',
        meta: { public: true }
      })
    ).resolves.toBe(true)
    expect(authStore.clearSession).toHaveBeenCalledTimes(1)
    expect(permissionStore.clear).toHaveBeenCalledTimes(1)
  })
})
