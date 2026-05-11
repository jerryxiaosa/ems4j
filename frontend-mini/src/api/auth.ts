import { useMock } from '@/mock'
import { getMockMiniLogin, mockLogout } from '@/mock/auth'
import type { MiniLoginRequest, MiniLoginResponse } from '@/types/auth'
import { request } from '@/utils/request'

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
    return mockLogout()
  }

  return request<void>({
    url: '/v1/mini/auth/logout',
    method: 'POST'
  })
}
