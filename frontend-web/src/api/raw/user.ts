import { requestV1 } from '@/api/http'
import type { ApiEnvelope } from '@/types/http'

export interface UserRoleRaw {
  id?: number
  roleName?: string
  roleKey?: string
}

export interface UserRaw {
  id?: number
  userName?: string
  organizationId?: number
  organizationName?: string
  realName?: string
  userPhone?: string
  userGender?: number
  remark?: string
  certificatesType?: number
  certificatesTypeText?: string
  certificatesNo?: string
  createTime?: string
  updateTime?: string
  roles?: UserRoleRaw[]
}

export interface UserPageQueryRaw {
  userNameLike?: string
  realNameLike?: string
  userPhoneLike?: string
  organizationId?: number
  roleId?: number
  pageNum?: number
  pageSize?: number
}

export interface UserPageResultRaw {
  pageNum?: number
  pageSize?: number
  total?: number
  list?: UserRaw[]
}

export interface UserCreatePayloadRaw {
  userName: string
  password: string
  realName: string
  userPhone: string
  userGender: number
  organizationId: number
  roleIds?: number[]
  remark?: string
}

export interface UserUpdatePayloadRaw {
  realName?: string
  userPhone?: string
  userGender?: number
  certificatesType?: number
  certificatesNo?: string
  organizationId?: number
  roleIds?: number[]
  remark?: string
}

export interface UserPasswordResetPayloadRaw {
  newPassword: string
}

export const getUserPageRaw = (query: UserPageQueryRaw) => {
  return requestV1<ApiEnvelope<UserPageResultRaw>>({
    method: 'GET',
    url: '/users/page',
    params: {
      userNameLike: query.userNameLike,
      realNameLike: query.realNameLike,
      userPhoneLike: query.userPhoneLike,
      organizationId: query.organizationId,
      roleId: query.roleId,
      pageNum: query.pageNum,
      pageSize: query.pageSize
    }
  })
}

export const getUserDetailRaw = (id: number) => {
  return requestV1<ApiEnvelope<UserRaw>>({
    method: 'GET',
    url: `/users/${id}`
  })
}

export const getUserDetailRawRaw = (id: number) => {
  return requestV1<ApiEnvelope<UserRaw>>({
    method: 'GET',
    url: `/users/${id}/raw`
  })
}

export const createUserRaw = (payload: UserCreatePayloadRaw) => {
  return requestV1<ApiEnvelope<number>>({
    method: 'POST',
    url: '/users',
    data: payload
  })
}

export const deleteUserRaw = (id: number) => {
  return requestV1<ApiEnvelope<void>>({
    method: 'DELETE',
    url: `/users/${id}`
  })
}

export const updateUserRaw = (id: number, payload: UserUpdatePayloadRaw) => {
  return requestV1<ApiEnvelope<void>>({
    method: 'PUT',
    url: `/users/${id}`,
    data: payload
  })
}

export const resetUserPasswordRaw = (id: number, payload: UserPasswordResetPayloadRaw) => {
  return requestV1<ApiEnvelope<void>>({
    method: 'PUT',
    url: `/users/${id}/password/reset`,
    data: payload
  })
}
