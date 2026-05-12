import { mockDelay } from './index'
import type { MiniLoginRequest, MiniLoginResponse } from '@/types/auth'

export type MockMiniLoginScenario = 'success' | 'accountError'

let mockMiniLoginScenario: MockMiniLoginScenario = 'success'

export const setMockMiniLoginScenario = (scenario: MockMiniLoginScenario) => {
  mockMiniLoginScenario = scenario
}

export const getMockMiniLogin = async (_request: MiniLoginRequest): Promise<MiniLoginResponse> => {
  await mockDelay()

  if (mockMiniLoginScenario === 'accountError') {
    throw new Error('账户未开户或状态异常')
  }

  return {
    accessToken: 'mock-mini-access-token',
    expireIn: 7200
  }
}

export const mockLogout = async (): Promise<void> => {
  await mockDelay()
}
