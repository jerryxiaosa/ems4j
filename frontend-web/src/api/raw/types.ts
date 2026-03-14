import type { ApiEnvelope, PageResult } from '@/types/http'
import { removeAccessToken, removeRefreshToken } from '@/utils/token'

export const SUCCESS_CODE = 100001
export const NOT_LOGIN_CODE = -103001
export const PERMISSION_ERROR_CODE = -103002
export const PERMISSION_DENIED_EVENT = 'ems4j:permission-denied'

export interface BusinessError extends Error {
  code?: number
}

const toNumber = (value: unknown): number | undefined => {
  if (typeof value === 'number') {
    return Number.isFinite(value) ? value : undefined
  }

  if (typeof value === 'string' && value.trim()) {
    const parsed = Number(value)
    return Number.isFinite(parsed) ? parsed : undefined
  }

  return undefined
}

const isEnvelope = <T>(payload: ApiEnvelope<T> | T): payload is ApiEnvelope<T> => {
  if (!payload || typeof payload !== 'object') {
    return false
  }
  return 'success' in payload || 'code' in payload || 'data' in payload
}

const redirectToLogin = () => {
  removeAccessToken()
  removeRefreshToken()

  if (typeof window !== 'undefined' && window.location.pathname !== '/login') {
    const redirect = `${window.location.pathname}${window.location.search}`
    window.location.replace(`/login?redirect=${encodeURIComponent(redirect)}`)
  }
}

const dispatchPermissionDenied = () => {
  if (typeof window === 'undefined') {
    return
  }

  window.dispatchEvent(new CustomEvent(PERMISSION_DENIED_EVENT))
}

const createBusinessError = (code: number | undefined, message: string): BusinessError => {
  const error = new Error(message) as BusinessError
  error.code = code
  return error
}

export const unwrapEnvelope = <T>(payload: ApiEnvelope<T> | T): T => {
  if (!isEnvelope(payload)) {
    return payload as T
  }

  const code = toNumber(payload.code)
  const message = payload.message || payload.msg || '接口请求失败'
  const isSuccess = payload.success === true && code === SUCCESS_CODE

  if (isSuccess) {
    return payload.data as T
  }

  if (code === NOT_LOGIN_CODE) {
    redirectToLogin()
  }

  if (code === PERMISSION_ERROR_CODE) {
    dispatchPermissionDenied()
  }

  throw createBusinessError(code, message)
}

export const normalizePageResult = <T>(
  payload: Partial<PageResult<T>> | null | undefined
): PageResult<T> => {
  const source = payload as Record<string, unknown> | null | undefined
  const pickNumber = (...keys: string[]): number | undefined => {
    if (!source) {
      return undefined
    }
    for (const key of keys) {
      const parsed = toNumber(source[key])
      if (parsed !== undefined) {
        return parsed
      }
    }
    return undefined
  }

  return {
    list: Array.isArray(payload?.list)
      ? payload.list
      : Array.isArray((payload as Record<string, unknown> | null | undefined)?.records)
      ? ((payload as Record<string, unknown>).records as unknown[] as T[])
      : Array.isArray((payload as Record<string, unknown> | null | undefined)?.items)
      ? ((payload as Record<string, unknown>).items as unknown[] as T[])
      : [],
    total: pickNumber('total', 'totalSize', 'totalCount') ?? 0,
    pageNum: pickNumber('pageNum', 'page'),
    pageSize: pickNumber('pageSize', 'limit')
  }
}
