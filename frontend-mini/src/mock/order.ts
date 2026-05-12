import { mockDelay, paginateMockList } from './index'
import type {
  OrderListQuery,
  OrderListResponse,
  OrderRecordItem,
  TopUpOrderRequest,
  TopUpOrderResponse,
  WechatPaymentParamsResponse
} from '@/types/order'

const mockOrderList: OrderRecordItem[] = [
  {
    orderSn: 'RC202605110001',
    payAmount: 200,
    topUpAmount: 196,
    serviceFeeAmount: 4,
    status: 'SUCCESS',
    statusName: '支付成功',
    createTime: '2026-05-10 18:24'
  },
  {
    orderSn: 'RC202605090003',
    payAmount: 100,
    topUpAmount: 98,
    serviceFeeAmount: 2,
    status: 'SUCCESS',
    statusName: '支付成功',
    createTime: '2026-05-09 09:12'
  },
  {
    orderSn: 'RC202605050006',
    payAmount: 50,
    topUpAmount: 49,
    serviceFeeAmount: 1,
    status: 'CLOSED',
    statusName: '已关闭',
    createTime: '2026-05-05 21:08'
  }
]

export const getMockOrderPage = async (query: OrderListQuery = {}): Promise<OrderListResponse> => {
  await mockDelay()

  return paginateMockList(mockOrderList, query.pageNum ?? 1, query.pageSize ?? 10)
}

export const createMockTopUpOrder = async (request: TopUpOrderRequest): Promise<TopUpOrderResponse> => {
  await mockDelay()

  const serviceFeeAmount = Number((request.orderAmount * 0.02).toFixed(2))

  return {
    orderSn: 'RC202605110888',
    orderAmount: request.orderAmount,
    arrivalAmount: Number((request.orderAmount - serviceFeeAmount).toFixed(2)),
    serviceFeeAmount,
    orderPayStopTime: '2026-05-11 23:59:59'
  }
}

export const getMockPaymentParams = async (_orderSn: string): Promise<WechatPaymentParamsResponse> => {
  await mockDelay()

  return {
    timeStamp: '1799654399',
    nonceStr: 'mockNonceStr',
    packageValue: 'prepay_id=mock_prepay_id',
    signType: 'RSA',
    paySign: 'mockPaySign'
  }
}

export const getMockEmptyOrderPage = async (query: OrderListQuery = {}): Promise<OrderListResponse> => {
  await mockDelay()

  return paginateMockList([], query.pageNum ?? 1, query.pageSize ?? 10)
}
