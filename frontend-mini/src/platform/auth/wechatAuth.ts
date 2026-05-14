import { useMockAuth } from '@/mock'

export type WechatPhoneCodeEvent = {
  detail?: {
    code?: string
    errMsg?: string
    errno?: number
  }
}

export const getWechatLoginCode = async (): Promise<string> => {
  if (useMockAuth) {
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

  if (useMockAuth) {
    return 'mock-wx-phone-code'
  }

  const errorMessage = event.detail?.errMsg ?? '未授权获取手机号'
  console.warn('获取手机号失败', event.detail)

  if (/deny|cancel/i.test(errorMessage)) {
    throw new Error('请允许获取手机号后再登录')
  }

  if (/permission|auth/i.test(errorMessage)) {
    throw new Error('当前小程序未开通获取手机号权限')
  }

  throw new Error(errorMessage)
}
