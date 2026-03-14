export interface ApiEnvelope<T = unknown> {
  success?: boolean
  code?: string | number
  message?: string
  msg?: string
  data?: T
}

export interface PageResult<T = unknown> {
  list: T[]
  total: number
  pageNum?: number
  pageSize?: number
}
