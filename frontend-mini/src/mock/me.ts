import { mockDelay } from './index'
import type { CurrentMiniUserResponse } from '@/types/me'

export const getMockMyProfile = async (): Promise<CurrentMiniUserResponse> => {
  await mockDelay()

  return {
    userPhone: '13800000000',
    electricAccountId: 'ea-10001',
    electricAccountName: '星河家园 2 栋住户账',
    electricAccountType: 2,
    balance: 328.6,
    meterCount: 6
  }
}
