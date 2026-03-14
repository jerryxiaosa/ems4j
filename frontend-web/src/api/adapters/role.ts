import {
  createRoleRaw,
  deleteRoleRaw,
  getMenuTreeRaw,
  getRoleDetailRaw,
  getRoleListRaw,
  saveRoleMenusRaw,
  updateRoleRaw,
  type MenuWithChildrenRaw,
  type RoleRaw
} from '@/api/raw/role'
import { unwrapEnvelope } from '@/api/raw/types'
import type { SystemOption } from '@/types/system'
import type { SystemRoleItem, SystemRolePermissionNode } from '@/modules/system/roles/types'

const toNumber = (value: unknown): number | null => {
  if (typeof value === 'number' && Number.isFinite(value)) {
    return value
  }

  if (typeof value === 'string' && value.trim()) {
    const parsed = Number(value)
    if (Number.isFinite(parsed)) {
      return parsed
    }
  }

  return null
}

const normalizeText = (value: unknown): string => {
  if (value === undefined || value === null) {
    return ''
  }

  return String(value).trim()
}

const normalizeRoleOption = (raw: RoleRaw): SystemOption | null => {
  const id = toNumber(raw.id)
  const roleName = normalizeText(raw.roleName)

  if (id === null || !roleName) {
    return null
  }

  return {
    value: String(id),
    label: roleName
  }
}

export const fetchRoleOptions = async (): Promise<SystemOption[]> => {
  const list = unwrapEnvelope<RoleRaw[]>(await getRoleListRaw({ isDisabled: false })) || []

  return list
    .map(normalizeRoleOption)
    .filter((item): item is SystemOption => item !== null)
}

const normalizeRole = (raw: RoleRaw): SystemRoleItem | null => {
  const id = toNumber(raw.id)
  const name = normalizeText(raw.roleName)

  if (id === null || !name) {
    return null
  }

  return {
    id,
    name,
    key: normalizeText(raw.roleKey),
    description: normalizeText(raw.remark),
    isSystem: Boolean(raw.isSystem),
    isDisabled: Boolean(raw.isDisabled),
    menuIds: Array.isArray(raw.menuIds)
      ? raw.menuIds
          .map((item) => toNumber(item))
          .filter((item): item is number => item !== null)
          .map((item) => String(item))
      : []
  }
}

export const fetchRoleList = async (): Promise<SystemRoleItem[]> => {
  const list = unwrapEnvelope<RoleRaw[]>(await getRoleListRaw()) || []
  return list.map(normalizeRole).filter((item): item is SystemRoleItem => item !== null)
}

export const fetchRoleDetail = async (id: number): Promise<SystemRoleItem | null> => {
  return normalizeRole(unwrapEnvelope<RoleRaw>(await getRoleDetailRaw(id)) || {})
}

export interface RoleSavePayload {
  roleName: string
  roleKey?: string
  remark?: string
}

export const createRole = async (payload: RoleSavePayload): Promise<number | null> => {
  const result = unwrapEnvelope<number>(
    await createRoleRaw({
      roleName: payload.roleName,
      roleKey: payload.roleKey || '',
      remark: payload.remark
    })
  )
  return typeof result === 'number' ? result : null
}

export const updateRole = async (id: number, payload: RoleSavePayload) => {
  await updateRoleRaw(id, payload)
}

export const deleteRole = async (id: number) => {
  await deleteRoleRaw(id)
}

const normalizePermissionTree = (raw: MenuWithChildrenRaw): SystemRolePermissionNode | null => {
  const id = toNumber(raw.id)
  const label = normalizeText(raw.menuName)
  if (id === null || !label) {
    return null
  }
  return {
    id: String(id),
    label,
    children: Array.isArray(raw.children)
      ? raw.children
          .map(normalizePermissionTree)
          .filter((item): item is SystemRolePermissionNode => item !== null)
      : []
  }
}

export const fetchRolePermissionTree = async (): Promise<SystemRolePermissionNode[]> => {
  const list = unwrapEnvelope<MenuWithChildrenRaw[]>(await getMenuTreeRaw()) || []
  return list.map(normalizePermissionTree).filter((item): item is SystemRolePermissionNode => item !== null)
}

export const saveRoleMenus = async (id: number, menuIds: string[]) => {
  await saveRoleMenusRaw(id, {
    menuIds: menuIds
      .map((item) => Number(item))
      .filter((item) => Number.isFinite(item))
  })
}
