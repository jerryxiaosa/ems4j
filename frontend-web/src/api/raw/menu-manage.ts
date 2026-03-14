import { requestV1 } from '@/api/http'
import type { ApiEnvelope } from '@/types/http'

export interface MenuRaw {
  id?: number | string
  menuName?: string
  menuKey?: string
  pid?: number | string | null
  sortNum?: number | string
  path?: string
  menuSource?: number | string
  menuType?: number | string
  icon?: string
  remark?: string
  hidden?: boolean
  permissionCodes?: string[]
  children?: MenuRaw[]
}

export interface MenuCreatePayloadRaw {
  menuName: string
  menuKey: string
  pid: number
  sortNum?: number
  path?: string
  menuSource: number
  menuType: number
  icon?: string
  remark?: string
  hidden?: boolean
  permissionCodes?: string[]
}

export interface MenuUpdatePayloadRaw {
  menuName: string
  menuKey: string
  sortNum?: number
  path?: string
  menuSource: number
  menuType: number
  icon?: string
  remark?: string
  hidden?: boolean
  permissionCodes?: string[]
}

export interface MenuTreeQueryRaw {
  menuSource?: number | string
}

export const getMenuTreeRaw = (params: MenuTreeQueryRaw = {}) => {
  return requestV1<ApiEnvelope<MenuRaw[]>>({
    method: 'GET',
    url: '/menus',
    params
  })
}

export const getMenuDetailRaw = (id: number) => {
  return requestV1<ApiEnvelope<MenuRaw>>({
    method: 'GET',
    url: `/menus/${id}`
  })
}

export const createMenuRaw = (payload: MenuCreatePayloadRaw) => {
  return requestV1<ApiEnvelope<void>>({
    method: 'POST',
    url: '/menus',
    data: payload
  })
}

export const updateMenuRaw = (id: number, payload: MenuUpdatePayloadRaw) => {
  return requestV1<ApiEnvelope<void>>({
    method: 'PUT',
    url: `/menus/${id}`,
    data: payload
  })
}

export const deleteMenuRaw = (id: number) => {
  return requestV1<ApiEnvelope<void>>({
    method: 'DELETE',
    url: `/menus/${id}`
  })
}
