import { requestV1 } from '@/api/http'
import type { ApiEnvelope } from '@/types/http'

export interface SpaceTreeRaw {
  id?: number | string
  pid?: number | string
  name?: string
  fullPath?: string
  type?: number | string
  sortIndex?: number | string
  ownAreaId?: number | string
  parentsIds?: Array<number | string>
  parentsNames?: string[]
  children?: SpaceTreeRaw[]
}

export const getSpaceTreeRaw = () => {
  return requestV1<ApiEnvelope<SpaceTreeRaw[]>>({
    method: 'GET',
    url: '/spaces/tree'
  })
}
