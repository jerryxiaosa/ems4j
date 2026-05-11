import type { PageQuery, PageResponse } from './common'

export type OrderRecordItem = {
  orderSn: string
  amount: number
  arrivalAmount?: number
  serviceFeeAmount?: number
  status: string
  statusName: string
  createTime: string
}

export type OrderListQuery = PageQuery

export type OrderListResponse = PageResponse<OrderRecordItem>

export type TopUpOrderRequest = {
  orderAmount: number
  meterId?: string
}

export type TopUpOrderResponse = {
  orderSn: string
  orderAmount: number
  arrivalAmount: number
  serviceFeeAmount: number
  orderPayStopTime: string
}

export type WechatPaymentParamsResponse = {
  timeStamp: string
  nonceStr: string
  packageValue: string
  signType: string
  paySign: string
}
