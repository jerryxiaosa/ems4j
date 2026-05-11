import { useMock } from '@/mock'
import { getMockBillDayList, getMockBillMonthList } from '@/mock/billing'
import type { BillDayListResponse, BillDayQuery, BillMonthListResponse } from '@/types/billing'
import { request } from '@/utils/request'

export const getBillMonthList = async (): Promise<BillMonthListResponse> => {
  if (useMock) {
    return getMockBillMonthList()
  }

  return request<BillMonthListResponse>({
    url: '/v1/mini/bills/months',
    method: 'GET'
  })
}

export const getBillDayList = async (query: BillDayQuery): Promise<BillDayListResponse> => {
  if (useMock) {
    return getMockBillDayList(query)
  }

  return request<BillDayListResponse>({
    url: '/v1/mini/bills/days',
    method: 'GET',
    data: query
  })
}
