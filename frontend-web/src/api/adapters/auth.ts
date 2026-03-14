import type { CaptchaInfo, CurrentUser, LoginForm, LoginResult } from '@/types/auth'
import { getCaptchaRaw, getCurrentUserRaw, loginRaw, logoutRaw } from '@/api/raw/auth'
import { unwrapEnvelope } from '@/api/raw/types'

const normalizeCaptchaImage = (rawValue: string): string => {
  const value = (rawValue || '').trim()
  if (!value) {
    return ''
  }
  if (value.startsWith('data:image')) {
    return value
  }
  return `data:image/png;base64,${value}`
}

export const fetchCaptcha = async (): Promise<CaptchaInfo> => {
  const raw = unwrapEnvelope(await getCaptchaRaw()) || {}
  const image = raw.imageBase64 || raw.captchaImg || raw.img || ''
  return {
    captchaKey: raw.captchaKey || raw.key || '',
    imageBase64: normalizeCaptchaImage(image)
  }
}

export const loginByPassword = async (payload: LoginForm): Promise<LoginResult> => {
  const raw = unwrapEnvelope(await loginRaw(payload)) || {}
  const token = raw.accessToken || raw.token || ''
  if (!token) {
    throw new Error('登录成功但未返回 token')
  }

  return {
    token,
    refreshToken: raw.refreshToken
  }
}

export const fetchCurrentUser = async (): Promise<CurrentUser> => {
  const raw = unwrapEnvelope(await getCurrentUserRaw()) || {}
  return {
    id: raw.id ?? raw.userId ?? '',
    userName: raw.userName || raw.username || '',
    realName: raw.realName || raw.nickname || '',
    userPhone: raw.userPhone || ''
  }
}

export const logoutCurrentUser = async (): Promise<void> => {
  await logoutRaw()
}
