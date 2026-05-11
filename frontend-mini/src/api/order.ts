import { useMock } from '@/mock'
import { createMockTopUpOrder, getMockOrderPage, getMockPaymentParams } from '@/mock/order'
import type {
  OrderListQuery,
  OrderListResponse,
  TopUpOrderRequest,
  TopUpOrderResponse,
  WechatPaymentParamsResponse
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

export const getPaymentParams = async (orderSn: string): Promise<WechatPaymentParamsResponse> => {
  if (useMock) {
    return getMockPaymentParams(orderSn)
  }

  return request<WechatPaymentParamsResponse>({
    url: `/v1/mini/orders/${encodeURIComponent(orderSn)}/payment-params`,
    method: 'POST'
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
