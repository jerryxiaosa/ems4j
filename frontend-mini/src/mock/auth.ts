import { mockDelay } from './index'
import type { MiniLoginRequest, MiniLoginResponse } from '@/types/auth'

export const getMockMiniLogin = async (_request: MiniLoginRequest): Promise<MiniLoginResponse> => {
  await mockDelay()

  return {
    accessToken: 'mock-mini-access-token',
    expireIn: 7200
  }
}

export const mockLogout = async (): Promise<void> => {
  await mockDelay()
}
