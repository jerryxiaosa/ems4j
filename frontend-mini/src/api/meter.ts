import { useMock } from '@/mock'
import { getMockMeterDetail, getMockMeterList, getMockMeterTodayUsage } from '@/mock/meter'
import type { MeterDetailResponse, MeterListResponse, MeterTodayUsageResponse } from '@/types/meter'
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

export const getMeterTodayUsage = async (meterId: number): Promise<MeterTodayUsageResponse> => {
  if (useMock) {
    return getMockMeterTodayUsage(meterId)
  }

  return request<MeterTodayUsageResponse>({
    url: `/v1/mini/meters/${encodeURIComponent(String(meterId))}/today-usage`,
    method: 'GET'
  })
}
