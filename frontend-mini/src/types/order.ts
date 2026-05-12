import type { PageQuery, PageResponse } from './common'

export type OrderRecordItem = {
  orderSn: string
  payAmount: number
  topUpAmount?: number
  serviceFeeAmount?: number
  status: string
  statusName: string
  createTime: string
}

export type OrderListQuery = PageQuery

export type OrderListResponse = PageResponse<OrderRecordItem>

export type TopUpOrderRequest = {
  payAmount: number
  meterId?: number
}

export type TopUpOrderResponse = {
  orderSn: string
  payAmount: number
  topUpAmount: number
  serviceFeeAmount: number
  orderPayStopTime: string
  paymentParams: WechatPaymentParamsResponse
}

export type WechatPaymentParamsResponse = {
  timeStamp: string
  nonceStr: string
  packageValue: string
  signType: string
  paySign: string
}
