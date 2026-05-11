export type ElectricAccountType = 0 | 1 | 2

export type PageQuery = {
  pageNum?: number
  pageSize?: number
}

export type PageResponse<T> = {
  list: T[]
  pageNum: number
  pageSize: number
  total: number
}

export type ListResponse<T> = {
  list: T[]
}
