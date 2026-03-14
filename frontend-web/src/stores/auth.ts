import { defineStore } from 'pinia'
import type { CurrentUser, LoginForm } from '@/types/auth'
import { fetchCurrentUser, loginByPassword, logoutCurrentUser } from '@/api/adapters/auth'
import { usePermissionStore } from '@/stores/permission'
import { pinia } from '@/stores'
import {
  getAccessToken,
  getRefreshToken,
  removeAccessToken,
  removeRefreshToken,
  setAccessToken,
  setRefreshToken
} from '@/utils/token'

interface AuthState {
  token: string
  refreshToken: string
  user: CurrentUser | null
}

export const useAuthStore = defineStore('auth', {
  state: (): AuthState => ({
    token: getAccessToken(),
    refreshToken: getRefreshToken(),
    user: null
  }),
  getters: {
    isLoggedIn: (state) => Boolean(state.token)
  },
  actions: {
    setSession(token: string, refreshToken?: string) {
      this.token = token
      setAccessToken(token)
      if (refreshToken) {
        this.refreshToken = refreshToken
        setRefreshToken(refreshToken)
      }
    },
    clearSession() {
      usePermissionStore(pinia).clear()
      this.token = ''
      this.refreshToken = ''
      this.user = null
      removeAccessToken()
      removeRefreshToken()
    },
    async login(payload: LoginForm) {
      const loginResult = await loginByPassword(payload)
      this.setSession(loginResult.token, loginResult.refreshToken)
      await this.loadCurrentUser()
    },
    async loadCurrentUser() {
      this.user = await fetchCurrentUser()
    },
    async logout() {
      try {
        if (this.token) {
          await logoutCurrentUser()
        }
      } finally {
        this.clearSession()
      }
    }
  }
})
