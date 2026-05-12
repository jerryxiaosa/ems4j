import { useMock } from '@/mock'
import type { WechatPaymentParamsResponse } from '@/types/order'

export type MockWechatPaymentResult = 'success' | 'fail'

let mockWechatPaymentResult: MockWechatPaymentResult = 'success'

export const setMockWechatPaymentResult = (result: MockWechatPaymentResult) => {
  mockWechatPaymentResult = result
}

export const requestWechatPayment = async (params: WechatPaymentParamsResponse) => {
  if (useMock) {
    if (mockWechatPaymentResult === 'fail') {
      throw new Error('模拟微信支付失败')
    }

    return {
      errMsg: 'requestPayment:ok'
    }
  }

  return uni.requestPayment({
    provider: 'wxpay',
    timeStamp: params.timeStamp,
    nonceStr: params.nonceStr,
    package: params.packageValue,
    signType: params.signType,
    paySign: params.paySign
  })
}
