import { fetchCurrentUser, loginByPassword, logoutCurrentUser } from '@/api/adapters/auth'
import { pinia } from '@/stores'
import { useAuthStore } from '@/stores/auth'
import { usePermissionStore } from '@/stores/permission'
import { getAccessToken, getRefreshToken } from '@/utils/token'

vi.mock('@/api/adapters/auth', () => ({
  loginByPassword: vi.fn(),
  fetchCurrentUser: vi.fn(),
  logoutCurrentUser: vi.fn()
}))

const mockedLoginByPassword = vi.mocked(loginByPassword)
const mockedFetchCurrentUser = vi.mocked(fetchCurrentUser)
const mockedLogoutCurrentUser = vi.mocked(logoutCurrentUser)

describe('auth store', () => {
  beforeEach(() => {
    mockedLoginByPassword.mockReset()
    mockedFetchCurrentUser.mockReset()
    mockedLogoutCurrentUser.mockReset()
    usePermissionStore(pinia).clear()
    useAuthStore(pinia).$reset()
  })

  test('testSetSession_WhenRefreshTokenProvided_ShouldPersistTokens', () => {
    const store = useAuthStore(pinia)

    store.setSession('access-token', 'refresh-token')

    expect(store.token).toBe('access-token')
    expect(store.refreshToken).toBe('refresh-token')
    expect(getAccessToken()).toBe('access-token')
    expect(getRefreshToken()).toBe('refresh-token')
    expect(store.isLoggedIn).toBe(true)
  })

  test('testClearSession_WhenPermissionStateExists_ShouldClearAllState', () => {
    const authStore = useAuthStore(pinia)
    const permissionStore = usePermissionStore(pinia)

    authStore.setSession('access-token', 'refresh-token')
    authStore.user = {
      id: 1,
      userName: 'tester',
      realName: '测试用户',
      userPhone: '13800000000'
    }
    permissionStore.$patch({
      rawMenus: [
        {
          id: 1,
          parentId: null,
          menuName: '首页',
          menuKey: 'dashboard:view',
          path: '/dashboard',
          sortNum: 1,
          menuType: 1,
          icon: 'home',
          hidden: false,
          menuSource: 1
        }
      ],
      loaded: true,
      allowedPaths: ['/dashboard']
    })

    authStore.clearSession()

    expect(authStore.token).toBe('')
    expect(authStore.refreshToken).toBe('')
    expect(authStore.user).toBeNull()
    expect(getAccessToken()).toBe('')
    expect(getRefreshToken()).toBe('')
    expect(permissionStore.rawMenus).toEqual([])
    expect(permissionStore.allowedPaths).toEqual([])
    expect(permissionStore.loaded).toBe(false)
  })

  test('testLoadCurrentUser_WhenAdapterReturnsUser_ShouldUpdateState', async () => {
    const store = useAuthStore(pinia)
    mockedFetchCurrentUser.mockResolvedValue({
      id: 1,
      userName: 'tester',
      realName: '测试用户',
      userPhone: '13800000000'
    })

    await store.loadCurrentUser()

    expect(store.user).toEqual({
      id: 1,
      userName: 'tester',
      realName: '测试用户',
      userPhone: '13800000000'
    })
  })

  test('testLogin_WhenLoginSuccess_ShouldSetSessionAndLoadUser', async () => {
    const store = useAuthStore(pinia)
    mockedLoginByPassword.mockResolvedValue({
      token: 'access-token',
      refreshToken: 'refresh-token'
    })
    mockedFetchCurrentUser.mockResolvedValue({
      id: 1,
      userName: 'tester',
      realName: '测试用户',
      userPhone: '13800000000'
    })

    await store.login({
      userName: 'admin',
      password: '123456',
      captchaKey: 'captcha-key',
      captchaValue: '8888'
    })

    expect(mockedLoginByPassword).toHaveBeenCalledWith({
      userName: 'admin',
      password: '123456',
      captchaKey: 'captcha-key',
      captchaValue: '8888'
    })
    expect(mockedFetchCurrentUser).toHaveBeenCalledTimes(1)
    expect(store.token).toBe('access-token')
    expect(store.refreshToken).toBe('refresh-token')
    expect(store.user?.userName).toBe('tester')
  })

  test('testLogout_WhenNoToken_ShouldOnlyClearSession', async () => {
    const store = useAuthStore(pinia)

    await store.logout()

    expect(mockedLogoutCurrentUser).not.toHaveBeenCalled()
    expect(store.token).toBe('')
    expect(store.user).toBeNull()
  })

  test('testLogout_WhenTokenExists_ShouldCallApiAndClearSession', async () => {
    const store = useAuthStore(pinia)
    store.setSession('access-token', 'refresh-token')
    mockedLogoutCurrentUser.mockResolvedValue()

    await store.logout()

    expect(mockedLogoutCurrentUser).toHaveBeenCalledTimes(1)
    expect(store.token).toBe('')
    expect(store.refreshToken).toBe('')
    expect(store.user).toBeNull()
  })
})
