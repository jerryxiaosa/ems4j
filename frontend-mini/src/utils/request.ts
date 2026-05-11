export type RequestMethod = 'GET' | 'POST'

export type RequestOptions = {
  url: string
  method?: RequestMethod
  data?: unknown
}

export const request = async <T>(_options: RequestOptions): Promise<T> => {
  throw new Error('真实接口尚未接入，请先通过 api mock 分支返回数据')
}
