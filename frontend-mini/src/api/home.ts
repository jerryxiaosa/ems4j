import { useMock } from '@/mock'
import { getMockHomeSummary, getMockHomeTrend } from '@/mock/home'
import type { HomeSummaryResponse, HomeTrendMetric, HomeTrendResponse } from '@/types/home'
import { request } from '@/utils/request'

export const getHomeSummary = async (): Promise<HomeSummaryResponse> => {
  if (useMock) {
    return getMockHomeSummary()
  }

  return request<HomeSummaryResponse>({
    url: '/v1/mini/home/summary',
    method: 'GET'
  })
}

export const getHomeTrend = async (metric: HomeTrendMetric): Promise<HomeTrendResponse> => {
  if (useMock) {
    return getMockHomeTrend(metric)
  }

  return request<HomeTrendResponse>({
    url: '/v1/mini/home/trend',
    method: 'GET',
    data: {
      metric
    }
  })
}
