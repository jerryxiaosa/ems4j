import { useMock } from '@/mock'
import { getMockMyProfile } from '@/mock/me'
import type { CurrentMiniUserResponse } from '@/types/me'
import { request } from '@/utils/request'

export const getMyProfile = async (): Promise<CurrentMiniUserResponse> => {
  if (useMock) {
    return getMockMyProfile()
  }

  return request<CurrentMiniUserResponse>({
    url: '/v1/mini/me',
    method: 'GET'
  })
}
