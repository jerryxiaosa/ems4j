import { useMock } from '@/mock'
import {
  getMockMiniLogin,
  mockLogout,
  setMockMiniLoginScenario as setMockMiniLoginScenarioInMock,
  type MockMiniLoginScenario
} from '@/mock/auth'
import type { MiniLoginRequest, MiniLoginResponse } from '@/types/auth'
import { request } from '@/utils/request'

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

export const setMockMiniLoginScenario = (scenario: MockMiniLoginScenario) => {
  if (useMock) {
    setMockMiniLoginScenarioInMock(scenario)
  }
}

export const miniLogin = async (data: MiniLoginRequest): Promise<MiniLoginResponse> => {
  if (useMock) {
    return getMockMiniLogin(data)
  }

  return request<MiniLoginResponse>({
    url: '/v1/mini/auth/login',
    method: 'POST',
    data
  })
}

export const logout = async (): Promise<void> => {
  if (useMock) {
    clearMiniAccessToken()
    return mockLogout()
  }

  await request<void>({
    url: '/v1/mini/auth/logout',
    method: 'POST'
  })
  clearMiniAccessToken()
}
