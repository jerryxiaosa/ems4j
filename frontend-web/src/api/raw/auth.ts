import { requestV1 } from '@/api/http'
import type { ApiEnvelope } from '@/types/http'
import type { LoginForm } from '@/types/auth'

export interface CaptchaRaw {
  captchaKey?: string
  key?: string
  imageBase64?: string
  captchaImg?: string
  img?: string
}

export interface LoginRaw {
  token?: string
  accessToken?: string
  refreshToken?: string
}

export interface UserRaw {
  id?: string | number
  userId?: string | number
  userName?: string
  username?: string
  realName?: string
  nickname?: string
  userPhone?: string
}

export const getCaptchaRaw = () => {
  return requestV1<ApiEnvelope<CaptchaRaw>>({
    method: 'GET',
    url: '/users/captcha'
  })
}

export const loginRaw = (data: LoginForm) => {
  return requestV1<ApiEnvelope<LoginRaw>>({
    method: 'POST',
    url: '/users/login',
    data
  })
}

export const getCurrentUserRaw = () => {
  return requestV1<ApiEnvelope<UserRaw>>({
    method: 'GET',
    url: '/users/current'
  })
}

export const logoutRaw = () => {
  return requestV1<ApiEnvelope<unknown>>({
    method: 'POST',
    url: '/users/logout'
  })
}
