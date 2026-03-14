import type { NavigationGuardReturn, RouteLocationNormalizedLoaded, RouteRecordNormalized } from 'vue-router'

interface AuthStateLike {
  clearSession: () => void
}

interface PermissionStateLike {
  allowedPaths: string[]
  firstAccessiblePath: string
  clear: () => void
  hasPath: (path: string) => boolean
  loadMenus: (options: { registeredPaths: string[] }) => Promise<void>
}

interface RouteLike {
  path: string
  fullPath: string
  meta?: {
    requiresAuth?: boolean
    public?: boolean
    title?: unknown
  }
}

interface CreateRouteGuardOptions {
  getAccessToken: () => string
  getAuthStore: () => AuthStateLike
  getPermissionStore: () => PermissionStateLike
  getRegisteredProtectedPaths: () => string[]
}

const DEFAULT_APP_TITLE = __APP_TITLE__

export const resolveDocumentTitle = (routeTitle?: unknown, appTitle = DEFAULT_APP_TITLE) => {
  if (typeof routeTitle === 'string' && routeTitle.trim().length > 0) {
    return `${routeTitle} - ${appTitle}`
  }

  return appTitle
}

export const normalizePath = (path: string) => {
  if (!path || path === '/') {
    return path
  }

  return path.replace(/\/+$/, '')
}

export const buildRegisteredProtectedPaths = (routes: RouteRecordNormalized[]) => {
  return [
    ...new Set(
      routes
        .filter((route) => route.meta?.requiresAuth && route.path && !route.path.includes('/:pathMatch'))
        .map((route) => normalizePath(route.path))
        .filter(Boolean)
    )
  ]
}

export const resolveFirstAccessiblePath = (permissionStore: Pick<PermissionStateLike, 'firstAccessiblePath' | 'allowedPaths'>) => {
  return permissionStore.firstAccessiblePath || permissionStore.allowedPaths[0] || ''
}

export const handleMenuLoadFailure = (authStore: AuthStateLike, redirectPath?: string): NavigationGuardReturn => {
  authStore.clearSession()

  return redirectPath
    ? {
        path: '/login',
        query: {
          redirect: redirectPath
        },
        replace: true
      }
    : {
        path: '/login',
        replace: true
      }
}

export const createRouteGuard = ({
  getAccessToken,
  getAuthStore,
  getPermissionStore,
  getRegisteredProtectedPaths
}: CreateRouteGuardOptions) => {
  const ensurePermissionMenusLoaded = async () => {
    const permissionStore = getPermissionStore()
    await permissionStore.loadMenus({
      registeredPaths: getRegisteredProtectedPaths()
    })
    return permissionStore
  }

  return async (to: RouteLike): Promise<NavigationGuardReturn> => {
    const token = getAccessToken()

    if (to.meta?.requiresAuth && !token) {
      return {
        path: '/login',
        query: {
          redirect: to.fullPath
        }
      }
    }

    if (to.path === '/login' && token) {
      const authStore = getAuthStore()
      const permissionStore = getPermissionStore()

      try {
        await ensurePermissionMenusLoaded()
      } catch (_error) {
        authStore.clearSession()
        return true
      }

      const redirectPath = resolveFirstAccessiblePath(permissionStore)
      if (redirectPath) {
        return { path: redirectPath, replace: true }
      }

      authStore.clearSession()
      permissionStore.clear()
      return true
    }

    if (to.meta?.requiresAuth && token) {
      const authStore = getAuthStore()
      const permissionStore = getPermissionStore()

      try {
        await ensurePermissionMenusLoaded()
      } catch (_error) {
        return handleMenuLoadFailure(authStore, to.fullPath)
      }

      if (!permissionStore.hasPath(normalizePath(to.path))) {
        const redirectPath = resolveFirstAccessiblePath(permissionStore)
        if (redirectPath && redirectPath !== to.path) {
          return { path: redirectPath, replace: true }
        }

        authStore.clearSession()
        permissionStore.clear()
        return {
          path: '/login',
          query: {
            redirect: to.fullPath
          }
        }
      }
    }

    return true
  }
}

export const updateDocumentTitle = (to: Pick<RouteLocationNormalizedLoaded, 'meta'>, appTitle = DEFAULT_APP_TITLE) => {
  document.title = resolveDocumentTitle(to.meta?.title, appTitle)
}
