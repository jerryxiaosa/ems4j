import { useMock } from '@/mock'
import { createMockTopUpOrder, getMockOrderPage } from '@/mock/order'
import type {
  OrderListQuery,
  OrderListResponse,
  TopUpOrderRequest,
  TopUpOrderResponse
} from '@/types/order'
import { request } from '@/utils/request'

export const createTopUpOrder = async (data: TopUpOrderRequest): Promise<TopUpOrderResponse> => {
  if (useMock) {
    return createMockTopUpOrder(data)
  }

  return request<TopUpOrderResponse>({
    url: '/v1/mini/orders/top-up',
    method: 'POST',
    data
  })
}

export const getOrderPage = async (query: OrderListQuery = {}): Promise<OrderListResponse> => {
  if (useMock) {
    return getMockOrderPage(query)
  }

  return request<OrderListResponse>({
    url: '/v1/mini/orders',
    method: 'GET',
    data: query
  })
}
