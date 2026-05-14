import { API_BASE_URL, API_TIMEOUT } from '@/config/api'
import { clearMiniAccessToken, getMiniAccessToken } from '@/utils/authToken'
import { miniRoute } from '@/utils/route'

export type RequestMethod = 'GET' | 'POST'

export type RequestOptions = {
  url: string
  method?: RequestMethod
  data?: string | AnyObject | ArrayBuffer
  header?: Record<string, string>
}

type RestResult<T> = {
  success?: boolean
  code?: number
  message?: string
  data?: T
}

const NOT_LOGIN_CODE = -103001

let isRedirectingToLogin = false

export class ApiRequestError extends Error {
  code?: number
  statusCode?: number

  constructor(message: string, code?: number, statusCode?: number) {
    super(message)
    this.name = 'ApiRequestError'
    this.code = code
    this.statusCode = statusCode
  }
}

const normalizeUrl = (url: string) => {
  if (/^https?:\/\//.test(url)) {
    return url
  }

  const baseUrl = API_BASE_URL.replace(/\/$/, '')
  const path = url.startsWith('/') ? url : `/${url}`
  return `${baseUrl}${path}`
}

const buildHeader = (header?: Record<string, string>) => {
  const token = getMiniAccessToken()
  const requestHeader: Record<string, string> = {
    'content-type': 'application/json',
    ...header
  }

  if (token) {
    requestHeader.Authorization = token
  }

  return requestHeader
}

const isObject = (value: unknown): value is Record<string, unknown> => {
  return typeof value === 'object' && value !== null
}

const redirectToLogin = () => {
  clearMiniAccessToken()

  if (isRedirectingToLogin) {
    return
  }

  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  if (currentPage?.route && `/${currentPage.route}` === miniRoute.login) {
    return
  }

  isRedirectingToLogin = true
  uni.redirectTo({
    url: miniRoute.login,
    complete: () => {
      isRedirectingToLogin = false
    }
  })
}

const toNetworkError = (error: unknown) => {
  if (error instanceof ApiRequestError) {
    return error
  }

  const message = error instanceof Error && error.message ? error.message : '网络请求失败，请稍后重试'
  return new ApiRequestError(message)
}

export const request = async <T>(options: RequestOptions): Promise<T> => {
  const method = options.method ?? 'GET'

  try {
    const response = await uni.request({
      url: normalizeUrl(options.url),
      method,
      data: options.data,
      header: buildHeader(options.header),
      timeout: API_TIMEOUT
    })

    if (response.statusCode < 200 || response.statusCode >= 300) {
      throw new ApiRequestError(`接口请求失败(${response.statusCode})`, undefined, response.statusCode)
    }

    if (!isObject(response.data)) {
      throw new ApiRequestError('接口返回格式不正确', undefined, response.statusCode)
    }

    const result = response.data as RestResult<T>
    if (result.success !== true) {
      if (result.code === NOT_LOGIN_CODE) {
        redirectToLogin()
      }
      throw new ApiRequestError(result.message || '接口请求失败', result.code, response.statusCode)
    }

    return result.data as T
  } catch (error) {
    throw toNetworkError(error)
  }
}
