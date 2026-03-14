import { fetchCurrentUserMenus } from '@/api/adapters/permission'
import { pinia } from '@/stores'
import { usePermissionStore } from '@/stores/permission'
import type { CurrentUserMenuItem } from '@/types/permission'

vi.mock('@/api/adapters/permission', () => ({
  fetchCurrentUserMenus: vi.fn()
}))

const mockedFetchCurrentUserMenus = vi.mocked(fetchCurrentUserMenus)

const createMenuList = (): CurrentUserMenuItem[] => [
  {
    id: 1,
    parentId: null,
    menuName: '首页',
    menuKey: 'dashboard:view',
    path: '/dashboard/',
    sortNum: 2,
    menuType: 1,
    icon: 'home',
    hidden: false,
    menuSource: 1
  },
  {
    id: 2,
    parentId: null,
    menuName: '隐藏页',
    menuKey: 'hidden:view',
    path: '/hidden',
    sortNum: 4,
    menuType: 1,
    icon: 'hidden',
    hidden: true,
    menuSource: 1
  },
  {
    id: 3,
    parentId: 1,
    menuName: '新增按钮',
    menuKey: 'user:create',
    path: '',
    sortNum: 1,
    menuType: 2,
    icon: 'plus',
    hidden: false,
    menuSource: 1
  },
  {
    id: 4,
    parentId: null,
    menuName: '用户管理',
    menuKey: 'user:group',
    path: '',
    sortNum: 1,
    menuType: 1,
    icon: 'user',
    hidden: false,
    menuSource: 1
  },
  {
    id: 5,
    parentId: 4,
    menuName: '用户列表',
    menuKey: 'user:list',
    path: '/users/',
    sortNum: 1,
    menuType: 1,
    icon: 'users',
    hidden: false,
    menuSource: 1
  },
  {
    id: 6,
    parentId: 999,
    menuName: '孤儿节点',
    menuKey: 'orphan:view',
    path: '/orphan',
    sortNum: 3,
    menuType: 1,
    icon: 'file',
    hidden: false,
    menuSource: 1
  },
  {
    id: 7,
    parentId: null,
    menuName: '未注册页面',
    menuKey: 'missing:view',
    path: '/missing',
    sortNum: 5,
    menuType: 1,
    icon: 'missing',
    hidden: false,
    menuSource: 1
  }
]

const createDeferred = <T>() => {
  let resolvePromise!: (value: T) => void
  let rejectPromise!: (reason?: unknown) => void

  const promise = new Promise<T>((resolve, reject) => {
    resolvePromise = resolve
    rejectPromise = reject
  })

  return {
    promise,
    resolve: resolvePromise,
    reject: rejectPromise
  }
}

describe('permission store', () => {
  beforeEach(() => {
    mockedFetchCurrentUserMenus.mockReset()
    usePermissionStore(pinia).clear()
  })

  test('testSetMenus_WhenMenusLoaded_ShouldDeriveVisibleState', () => {
    const store = usePermissionStore(pinia)
    store.registeredPaths = ['/dashboard', '/users', '/orphan']

    store.setMenus(createMenuList())

    expect(store.pageMenus.map((item) => item.id)).toEqual([4, 1, 6])
    expect(store.pageMenus[0].children.map((item) => item.id)).toEqual([5])
    expect(store.allowedPaths).toEqual(['/dashboard', '/users', '/orphan'])
    expect(store.allMenuKeys).toEqual([
      'dashboard:view',
      'hidden:view',
      'user:create',
      'user:group',
      'user:list',
      'orphan:view',
      'missing:view'
    ])
    expect(store.buttonMenuKeys).toEqual(['user:create'])
    expect(store.firstAccessiblePath).toBe('/users/')
    expect(store.hasPath('/users/')).toBe(true)
    expect(store.hasMenuPermission('user:create')).toBe(true)
    expect(store.loaded).toBe(true)
  })

  test('testSetMenus_WhenRootPathAndEqualSort_ShouldSortByIdAndKeepRootPath', () => {
    const store = usePermissionStore(pinia)
    store.registeredPaths = ['/', '/dashboard']

    store.setMenus([
      {
        id: 2,
        parentId: null,
        menuName: '首页',
        menuKey: 'dashboard:view',
        path: '/dashboard/',
        sortNum: 1,
        menuType: 1,
        icon: 'home',
        hidden: false,
        menuSource: 1
      },
      {
        id: 1,
        parentId: null,
        menuName: '根路径',
        menuKey: 'root:view',
        path: '/',
        sortNum: 1,
        menuType: 1,
        icon: 'root',
        hidden: false,
        menuSource: 1
      }
    ])

    expect(store.pageMenus.map((item) => item.id)).toEqual([1, 2])
    expect(store.allowedPaths).toEqual(['/dashboard', '/'])
    expect(store.firstAccessiblePath).toBe('/')
  })

  test('testLoadMenus_WhenRequestInFlight_ShouldReusePromise', async () => {
    const store = usePermissionStore(pinia)
    const deferred = createDeferred<CurrentUserMenuItem[]>()
    mockedFetchCurrentUserMenus.mockReturnValueOnce(deferred.promise)

    const firstLoadPromise = store.loadMenus({ registeredPaths: ['/dashboard'] })
    const secondLoadPromise = store.loadMenus()

    expect(mockedFetchCurrentUserMenus).toHaveBeenCalledTimes(1)
    deferred.resolve(createMenuList())

    await Promise.all([firstLoadPromise, secondLoadPromise])

    expect(store.loaded).toBe(true)
    expect(store.registeredPaths).toEqual(['/dashboard'])
  })

  test('testLoadMenus_WhenAlreadyLoadedWithoutForce_ShouldSkipFetch', async () => {
    const store = usePermissionStore(pinia)
    mockedFetchCurrentUserMenus.mockResolvedValue(createMenuList())

    await store.loadMenus({ registeredPaths: ['/dashboard'] })
    await store.loadMenus()

    expect(mockedFetchCurrentUserMenus).toHaveBeenCalledTimes(1)
  })

  test('testRefreshMenus_WhenAlreadyLoaded_ShouldForceReload', async () => {
    const store = usePermissionStore(pinia)
    mockedFetchCurrentUserMenus
      .mockResolvedValueOnce(createMenuList().slice(0, 1))
      .mockResolvedValueOnce(createMenuList().slice(0, 2))

    await store.loadMenus({ registeredPaths: ['/dashboard', '/hidden'] })
    await store.refreshMenus()

    expect(mockedFetchCurrentUserMenus).toHaveBeenCalledTimes(2)
    expect(store.rawMenus).toHaveLength(2)
  })

  test('testSetMenus_WhenNoVisiblePathExists_ShouldFallbackToEmptyPath', () => {
    const store = usePermissionStore(pinia)

    store.setMenus([
      {
        id: 10,
        parentId: null,
        menuName: '按钮',
        menuKey: 'button:only',
        path: '',
        sortNum: 1,
        menuType: 2,
        icon: 'button',
        hidden: false,
        menuSource: 1
      }
    ])

    expect(store.pageMenus).toEqual([])
    expect(store.allowedPaths).toEqual([])
    expect(store.firstAccessiblePath).toBe('')
  })

  test('testClear_WhenStateHasData_ShouldResetState', () => {
    const store = usePermissionStore(pinia)
    store.registeredPaths = ['/dashboard']
    store.setMenus(createMenuList())

    store.clear()

    expect(store.rawMenus).toEqual([])
    expect(store.pageMenus).toEqual([])
    expect(store.allowedPaths).toEqual([])
    expect(store.allMenuKeys).toEqual([])
    expect(store.buttonMenuKeys).toEqual([])
    expect(store.firstAccessiblePath).toBe('')
    expect(store.registeredPaths).toEqual([])
    expect(store.loaded).toBe(false)
  })
})
