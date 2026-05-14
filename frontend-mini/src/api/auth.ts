import { useMockAuth } from '@/mock'
import {
  getMockMiniLogin,
  mockLogout,
  setMockMiniLoginScenario as setMockMiniLoginScenarioInMock,
  type MockMiniLoginScenario
} from '@/mock/auth'
import type { MiniLoginRequest, MiniLoginResponse } from '@/types/auth'
import { clearMiniAccessToken } from '@/utils/authToken'
import { request } from '@/utils/request'

export { clearMiniAccessToken, getMiniAccessToken, saveMiniAccessToken } from '@/utils/authToken'

export const setMockMiniLoginScenario = (scenario: MockMiniLoginScenario) => {
  if (useMockAuth) {
    setMockMiniLoginScenarioInMock(scenario)
  }
}

export const miniLogin = async (data: MiniLoginRequest): Promise<MiniLoginResponse> => {
  if (useMockAuth) {
    return getMockMiniLogin(data)
  }

  return request<MiniLoginResponse>({
    url: '/v1/mini/auth/login',
    method: 'POST',
    data
  })
}

export const logout = async (): Promise<void> => {
  if (useMockAuth) {
    clearMiniAccessToken()
    return mockLogout()
  }

  await request<void>({
    url: '/v1/mini/auth/logout',
    method: 'POST'
  })
  clearMiniAccessToken()
}
