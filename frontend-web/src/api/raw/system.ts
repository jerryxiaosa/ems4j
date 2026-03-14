import { requestV1 } from '@/api/http'
import type { ApiEnvelope } from '@/types/http'

export interface EnumItemRaw {
  value?: unknown
  info?: string
}

export type SystemEnumMapRaw = Record<string, EnumItemRaw[]>

export interface ApiOptionRaw {
  key?: string
  permissionCode?: string
}

export const getSystemEnumsRaw = () => {
  return requestV1<ApiEnvelope<SystemEnumMapRaw>>({
    method: 'GET',
    url: '/system/enums'
  })
}

export const getSystemApiOptionsRaw = () => {
  return requestV1<ApiEnvelope<ApiOptionRaw[]>>({
    method: 'GET',
    url: '/system/api-options'
  })
}
