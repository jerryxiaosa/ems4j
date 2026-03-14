const ACCESS_TOKEN_KEY = 'energy_access_token'
const REFRESH_TOKEN_KEY = 'energy_refresh_token'

const getWebStorage = (): Storage | null => {
  if (typeof window !== 'undefined' && typeof window.localStorage !== 'undefined') {
    return window.localStorage
  }

  const storage = (globalThis as { localStorage?: Storage }).localStorage
  if (
    storage &&
    typeof storage.getItem === 'function' &&
    typeof storage.setItem === 'function' &&
    typeof storage.removeItem === 'function'
  ) {
    return storage
  }

  return null
}

export const getAccessToken = (): string => {
  return getWebStorage()?.getItem(ACCESS_TOKEN_KEY) || ''
}

export const setAccessToken = (token: string): void => {
  getWebStorage()?.setItem(ACCESS_TOKEN_KEY, token)
}

export const removeAccessToken = (): void => {
  getWebStorage()?.removeItem(ACCESS_TOKEN_KEY)
}

export const getRefreshToken = (): string => {
  return getWebStorage()?.getItem(REFRESH_TOKEN_KEY) || ''
}

export const setRefreshToken = (token: string): void => {
  getWebStorage()?.setItem(REFRESH_TOKEN_KEY, token)
}

export const removeRefreshToken = (): void => {
  getWebStorage()?.removeItem(REFRESH_TOKEN_KEY)
}
