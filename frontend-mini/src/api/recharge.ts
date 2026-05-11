import { useMock } from '@/mock'
import { getMockRechargeInit } from '@/mock/recharge'
import type { RechargeInitResponse } from '@/types/recharge'
import { request } from '@/utils/request'

export const getRechargeInit = async (): Promise<RechargeInitResponse> => {
  if (useMock) {
    return getMockRechargeInit()
  }

  return request<RechargeInitResponse>({
    url: '/v1/mini/recharge/init',
    method: 'GET'
  })
}
