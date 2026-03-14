import { normalizePageResult, unwrapEnvelope } from '@/api/raw/types'
import {
  createUserRaw,
  deleteUserRaw,
  getUserDetailRaw,
  getUserDetailRawRaw,
  getUserPageRaw,
  resetUserPasswordRaw,
  type UserCreatePayloadRaw,
  type UserPageResultRaw,
  type UserPasswordResetPayloadRaw,
  type UserUpdatePayloadRaw,
  type UserRaw,
  updateUserRaw
} from '@/api/raw/user'
import type { SystemUserItem } from '@/modules/system/users/types'
import type { PageResult } from '@/types/http'

export interface UserPageQuery {
  userNameLike?: string
  realNameLike?: string
  userPhoneLike?: string
  organizationId?: number
  roleId?: number
  pageNum: number
  pageSize: number
}

export interface UserCreatePayload {
  userName: string
  password: string
  realName: string
  userPhone: string
  userGender: number
  certificatesType?: number
  certificatesNo?: string
  organizationId: number
  roleIds?: number[]
  remark?: string
}

export interface UserUpdatePayload {
  realName?: string
  userPhone?: string
  userGender?: number
  certificatesType?: number
  certificatesNo?: string
  organizationId?: number
  roleIds?: number[]
  remark?: string
}

export interface UserPasswordResetPayload {
  newPassword: string
}

const toNumber = (value: unknown): number | undefined => {
  if (typeof value === 'number' && Number.isFinite(value)) {
    return value
  }

  if (typeof value === 'string' && value.trim()) {
    const parsed = Number(value)
    if (Number.isFinite(parsed)) {
      return parsed
    }
  }

  return undefined
}

const normalizeText = (value: unknown): string => {
  if (value === undefined || value === null) {
    return ''
  }

  return String(value).trim()
}

const padNumber = (value: number) => String(value).padStart(2, '0')

const formatDateTime = (value: unknown): string => {
  if (typeof value !== 'string' || !value.trim()) {
    return ''
  }

  const text = value.trim()
  if (!text.includes('T')) {
    return text.length >= 19 ? text.slice(0, 19) : text
  }

  const date = new Date(text)
  if (Number.isNaN(date.getTime())) {
    const fallback = text.replace('T', ' ')
    return fallback.length >= 19 ? fallback.slice(0, 19) : fallback
  }

  return `${date.getFullYear()}-${padNumber(date.getMonth() + 1)}-${padNumber(
    date.getDate()
  )} ${padNumber(date.getHours())}:${padNumber(date.getMinutes())}:${padNumber(date.getSeconds())}`
}

const normalizeRoleNames = (rawRoles: UserRaw['roles']): string => {
  if (!Array.isArray(rawRoles)) {
    return ''
  }

  return rawRoles
    .map((item) => normalizeText(item?.roleName))
    .filter(Boolean)
    .join('、')
}

const normalizePrimaryRoleId = (rawRoles: UserRaw['roles']): string => {
  if (!Array.isArray(rawRoles) || rawRoles.length === 0) {
    return ''
  }

  const roleId = toNumber(rawRoles[0]?.id)
  return roleId === undefined ? '' : String(roleId)
}

const normalizeRoleIds = (rawRoles: UserRaw['roles']): string[] => {
  if (!Array.isArray(rawRoles)) {
    return []
  }

  return rawRoles
    .map((item) => toNumber(item?.id))
    .filter((item): item is number => item !== undefined)
    .map((item) => String(item))
}

const normalizeUser = (raw: UserRaw): SystemUserItem => {
  const genderValue = toNumber(raw.userGender)
  const genderName =
    genderValue === 1 ? '男' : genderValue === 2 ? '女' : genderValue === 0 ? '未知' : ''

  return {
    id: toNumber(raw.id) ?? 0,
    username: normalizeText(raw.userName),
    realName: normalizeText(raw.realName),
    phone: normalizeText(raw.userPhone),
    organizationId: (() => {
      const value = toNumber(raw.organizationId)
      return value === undefined ? '' : String(value)
    })(),
    organizationName: normalizeText(raw.organizationName),
    roleId: normalizePrimaryRoleId(raw.roles),
    roleIds: normalizeRoleIds(raw.roles),
    roleName: normalizeRoleNames(raw.roles),
    createTime: formatDateTime(raw.createTime),
    updateTime: formatDateTime(raw.updateTime),
    remark: normalizeText(raw.remark),
    genderName,
    certificatesTypeText: normalizeText(raw.certificatesTypeText),
    certificatesNo: normalizeText(raw.certificatesNo)
  }
}

export const fetchUserPage = async (query: UserPageQuery): Promise<PageResult<SystemUserItem>> => {
  const payload = unwrapEnvelope<UserPageResultRaw>(await getUserPageRaw(query))
  const page = normalizePageResult(payload)

  return {
    ...page,
    list: page.list.map(normalizeUser)
  }
}

export const fetchUserDetail = async (id: number): Promise<SystemUserItem> => {
  const payload = unwrapEnvelope<UserRaw>(await getUserDetailRaw(id))
  return normalizeUser(payload)
}

export const fetchUserDetailRaw = async (id: number): Promise<SystemUserItem> => {
  const payload = unwrapEnvelope<UserRaw>(await getUserDetailRawRaw(id))
  return normalizeUser(payload)
}

export const createUser = async (payload: UserCreatePayload): Promise<number> => {
  const requestPayload: UserCreatePayloadRaw = {
    ...payload
  }
  return unwrapEnvelope<number>(await createUserRaw(requestPayload))
}

export const deleteUser = async (id: number): Promise<void> => {
  await unwrapEnvelope<void>(await deleteUserRaw(id))
}

export const updateUser = async (id: number, payload: UserUpdatePayload): Promise<void> => {
  const requestPayload: UserUpdatePayloadRaw = {
    ...payload
  }
  await unwrapEnvelope<void>(await updateUserRaw(id, requestPayload))
}

export const resetUserPassword = async (
  id: number,
  payload: UserPasswordResetPayload
): Promise<void> => {
  const requestPayload: UserPasswordResetPayloadRaw = {
    ...payload
  }
  await unwrapEnvelope<void>(await resetUserPasswordRaw(id, requestPayload))
}
