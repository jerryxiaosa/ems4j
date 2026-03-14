import { requestV1 } from '@/api/http'
import type { ApiEnvelope } from '@/types/http'

export interface RoleRaw {
  id?: number | string
  roleName?: string
  roleKey?: string
  remark?: string
  isSystem?: boolean
  isDisabled?: boolean
  menuIds?: Array<number | string>
}

export interface RoleListQueryRaw {
  roleNameLike?: string
  roleKeyLike?: string
  isSystem?: boolean
  isDisabled?: boolean
}

export const getRoleListRaw = (query: RoleListQueryRaw = {}) => {
  return requestV1<ApiEnvelope<RoleRaw[]>>({
    method: 'GET',
    url: '/roles',
    params: {
      roleNameLike: query.roleNameLike,
      roleKeyLike: query.roleKeyLike,
      isSystem: query.isSystem,
      isDisabled: query.isDisabled
    }
  })
}

export const getRoleDetailRaw = (id: number) => {
  return requestV1<ApiEnvelope<RoleRaw>>({
    method: 'GET',
    url: `/roles/${id}`
  })
}

export interface RoleCreatePayloadRaw {
  roleName: string
  roleKey: string
  remark?: string
}

export const createRoleRaw = (data: RoleCreatePayloadRaw) => {
  return requestV1<ApiEnvelope<number>>({
    method: 'POST',
    url: '/roles',
    data
  })
}

export interface RoleUpdatePayloadRaw {
  roleName: string
  roleKey?: string
  remark?: string
}

export const updateRoleRaw = (id: number, data: RoleUpdatePayloadRaw) => {
  return requestV1<ApiEnvelope<void>>({
    method: 'PUT',
    url: `/roles/${id}`,
    data
  })
}

export const deleteRoleRaw = (id: number) => {
  return requestV1<ApiEnvelope<void>>({
    method: 'DELETE',
    url: `/roles/${id}`
  })
}

export interface RoleMenuSavePayloadRaw {
  menuIds: number[]
}

export const saveRoleMenusRaw = (id: number, data: RoleMenuSavePayloadRaw) => {
  return requestV1<ApiEnvelope<void>>({
    method: 'PUT',
    url: `/roles/${id}/menus`,
    data
  })
}

export interface MenuWithChildrenRaw {
  id?: number | string
  menuName?: string
  children?: MenuWithChildrenRaw[]
}

export const getMenuTreeRaw = () => {
  return requestV1<ApiEnvelope<MenuWithChildrenRaw[]>>({
    method: 'GET',
    url: '/menus',
    params: {}
  })
}
