import { useMock } from '@/mock'

export type WechatPhoneCodeEvent = {
  detail?: {
    code?: string
    errMsg?: string
  }
}

export const getWechatLoginCode = async (): Promise<string> => {
  if (useMock) {
    return 'mock-wx-login-code'
  }

  const result = await uni.login()

  if (!result.code) {
    throw new Error('微信登录凭证获取失败')
  }

  return result.code
}

export const getWechatPhoneCode = (event: WechatPhoneCodeEvent): string => {
  const phoneCode = event.detail?.code

  if (phoneCode) {
    return phoneCode
  }

  if (useMock) {
    return 'mock-wx-phone-code'
  }

  throw new Error('未授权获取手机号')
}
