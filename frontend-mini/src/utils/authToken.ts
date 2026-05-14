import type { MiniLoginResponse } from '@/types/auth'

const MINI_ACCESS_TOKEN_STORAGE_KEY = 'miniAccessToken'

export const saveMiniAccessToken = (response: MiniLoginResponse) => {
  uni.setStorageSync(MINI_ACCESS_TOKEN_STORAGE_KEY, response.accessToken)
}

export const getMiniAccessToken = () => {
  return uni.getStorageSync(MINI_ACCESS_TOKEN_STORAGE_KEY) as string | undefined
}

export const clearMiniAccessToken = () => {
  uni.removeStorageSync(MINI_ACCESS_TOKEN_STORAGE_KEY)
}
