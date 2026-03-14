import { requestV1 } from '@/api/http'
import type { ApiEnvelope } from '@/types/http'

export interface CurrentUserMenuRaw {
  id?: number | string
  pid?: number | string | null
  menuName?: string
  menuKey?: string
  path?: string
  sortNum?: number | string
  menuType?: number | string
  icon?: string
  hidden?: boolean
  menuSource?: number | string
}

export const getCurrentUserMenusRaw = (source: number) => {
  return requestV1<ApiEnvelope<CurrentUserMenuRaw[]>>({
    method: 'GET',
    url: '/users/current/menus',
    params: {
      source
    }
  })
}
