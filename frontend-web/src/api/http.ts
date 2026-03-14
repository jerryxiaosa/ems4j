import axios from 'axios'
import type { AxiosError, AxiosInstance, AxiosRequestConfig } from 'axios'
import { getAccessToken } from '@/utils/token'
import { removeAccessToken, removeRefreshToken } from '@/utils/token'

const serializeParams = (params: Record<string, unknown>): string => {
  const search = new URLSearchParams()

  Object.entries(params).forEach(([key, value]) => {
    if (value === undefined || value === null || value === '') {
      return
    }

    if (Array.isArray(value)) {
      value.forEach((item) => {
        if (item !== undefined && item !== null && item !== '') {
          search.append(key, String(item))
        }
      })
      return
    }

    search.append(key, String(value))
  })

  return search.toString()
}

const redirectToLogin = () => {
  removeAccessToken()
  removeRefreshToken()

  if (typeof window !== 'undefined' && window.location.pathname !== '/login') {
    const redirect = `${window.location.pathname}${window.location.search}`
    window.location.replace(`/login?redirect=${encodeURIComponent(redirect)}`)
  }
}

const resolveApiRoot = (url: string) => url.replace(/\/v\d+\/?$/, '')

const baseApiRoot = resolveApiRoot(import.meta.env.VITE_API_BASE_URL || '/api')

const createHttpClient = (baseURL: string): AxiosInstance => {
  const instance = axios.create({
    baseURL,
    timeout: 30000,
    paramsSerializer: {
      serialize: serializeParams
    }
  })

  instance.interceptors.request.use((config) => {
    const token = getAccessToken()
    if (token) {
      config.headers.Authorization = token
    }
    return config
  })

  instance.interceptors.response.use(
    (response) => response.data,
    (error: AxiosError<{ message?: string; msg?: string }>) => {
      if (error.response?.status === 401) {
        redirectToLogin()
        return Promise.reject(new Error('请先登录'))
      }

      const message = error.response?.data?.message || error.response?.data?.msg || error.message
      return Promise.reject(new Error(message))
    }
  )

  return instance
}

const requestBase = <T>(client: AxiosInstance, config: AxiosRequestConfig) => {
  return client.request<T, T>(config)
}

const http = createHttpClient(baseApiRoot)
const httpV1 = createHttpClient(`${baseApiRoot}/v1`)
const httpV2 = createHttpClient(`${baseApiRoot}/v2`)

export const request = <T>(config: AxiosRequestConfig) => {
  return requestBase<T>(http, config)
}

export const requestV1 = <T>(config: AxiosRequestConfig) => {
  return requestBase<T>(httpV1, config)
}

export const requestV2 = <T>(config: AxiosRequestConfig) => {
  return requestBase<T>(httpV2, config)
}
