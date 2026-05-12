import { useMock } from '@/mock'
import { getMockMeterDetail, getMockMeterList } from '@/mock/meter'
import type { MeterDetailResponse, MeterListResponse } from '@/types/meter'
import { request } from '@/utils/request'

export const getMeterList = async (): Promise<MeterListResponse> => {
  if (useMock) {
    return getMockMeterList()
  }

  return request<MeterListResponse>({
    url: '/v1/mini/meters',
    method: 'GET'
  })
}

export const getMeterDetail = async (meterId: number): Promise<MeterDetailResponse> => {
  if (useMock) {
    return getMockMeterDetail(meterId)
  }

  return request<MeterDetailResponse>({
    url: `/v1/mini/meters/${encodeURIComponent(String(meterId))}`,
    method: 'GET'
  })
}
