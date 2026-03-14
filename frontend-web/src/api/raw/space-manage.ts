import { requestV1 } from '@/api/http'
import type { ApiEnvelope } from '@/types/http'

export interface SpaceRaw {
  id?: number
  name?: string
  pid?: number
  fullPath?: string
  type?: number
  area?: number
  sortIndex?: number
  ownAreaId?: number
  parentsIds?: number[]
  parentsNames?: string[]
  children?: SpaceRaw[]
}

export interface SpaceDetailRaw {
  id?: number
  name?: string
  pid?: number
  fullPath?: string
  type?: number
  area?: number
  sortIndex?: number
  ownAreaId?: number
  parentsIds?: number[]
  parentsNames?: string[]
}

export interface SpaceTreeQueryRaw {
  ids?: number[]
  pid?: number
  name?: string
  type?: number[]
}

export interface SpaceSavePayloadRaw {
  name: string
  pid: number
  type: number
  area: number
  sortIndex?: number
}

export const getSpaceTreeRaw = (query: SpaceTreeQueryRaw = {}) => {
  return requestV1<ApiEnvelope<SpaceRaw[]>>({
    method: 'GET',
    url: '/spaces/tree',
    params: {
      ids: query.ids,
      pid: query.pid,
      name: query.name,
      type: query.type
    }
  })
}

export const getSpaceDetailRaw = (id: number) => {
  return requestV1<ApiEnvelope<SpaceDetailRaw>>({
    method: 'GET',
    url: `/spaces/${id}`
  })
}

export const createSpaceRaw = (payload: SpaceSavePayloadRaw) => {
  return requestV1<ApiEnvelope<number>>({
    method: 'POST',
    url: '/spaces',
    data: payload
  })
}

export const updateSpaceRaw = (id: number, payload: SpaceSavePayloadRaw) => {
  return requestV1<ApiEnvelope<void>>({
    method: 'PUT',
    url: `/spaces/${id}`,
    data: payload
  })
}

export const deleteSpaceRaw = (id: number) => {
  return requestV1<ApiEnvelope<void>>({
    method: 'DELETE',
    url: `/spaces/${id}`
  })
}
