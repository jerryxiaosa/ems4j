import { unwrapEnvelope } from '@/api/raw/types'
import {
  createMenuRaw,
  deleteMenuRaw,
  getMenuDetailRaw,
  getMenuTreeRaw,
  updateMenuRaw,
  type MenuCreatePayloadRaw,
  type MenuRaw,
  type MenuUpdatePayloadRaw
} from '@/api/raw/menu-manage'
import type { SystemMenuItem } from '@/modules/system/menus/types'

export interface MenuSavePayload {
  menuName: string
  menuKey?: string
  pid?: number
  sortNum?: number
  path?: string
  menuSource: number
  menuType: number
  icon?: string
  remark?: string
  hidden?: boolean
  permissionCodes?: string[]
}

const normalizeText = (value: unknown): string => {
  if (value === undefined || value === null) {
    return ''
  }
  return String(value).trim()
}

const toOptionalText = (value: unknown): string | undefined => {
  const normalized = normalizeText(value)
  return normalized || undefined
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

const normalizeMenuTypeName = (value: string): string => {
  if (value === '1') {
    return '菜单'
  }
  if (value === '2') {
    return '按钮'
  }
  if (value === '3') {
    return '子系统'
  }
  return '--'
}

const normalizeMenuSourceName = (value: string): string => {
  if (value === '1') {
    return '后台'
  }
  if (value === '2') {
    return '移动端'
  }
  return '--'
}

const normalizeMenu = (raw: MenuRaw): SystemMenuItem | null => {
  const id = toNumber(raw.id)
  if (id === undefined) {
    return null
  }

  const parentId = toNumber(raw.pid)
  const menuTypeValue = String(toNumber(raw.menuType) ?? '')
  const menuSourceValue = String(toNumber(raw.menuSource) ?? '')
  const permissionCodes = Array.isArray(raw.permissionCodes)
    ? raw.permissionCodes
        .map((item) => normalizeText(item))
        .filter(Boolean)
    : []

  return {
    id,
    parentId: parentId && parentId > 0 ? parentId : null,
    name: normalizeText(raw.menuName),
    key: normalizeText(raw.menuKey),
    routePath: normalizeText(raw.path),
    backendApi: permissionCodes[0] || '',
    permissionCodes,
    categoryValue: menuTypeValue,
    categoryName: normalizeMenuTypeName(menuTypeValue),
    platformValue: menuSourceValue,
    platformName: normalizeMenuSourceName(menuSourceValue),
    hiddenValue: raw.hidden ? 'true' : 'false',
    hiddenName: raw.hidden ? '是' : '否',
    icon: normalizeText(raw.icon),
    sortNum: toNumber(raw.sortNum) ?? 0,
    remark: normalizeText(raw.remark),
    children: Array.isArray(raw.children)
      ? raw.children.map(normalizeMenu).filter((item): item is SystemMenuItem => item !== null)
      : []
  }
}

export const fetchMenuTree = async (menuSource?: number): Promise<SystemMenuItem[]> => {
  const payload =
    unwrapEnvelope<MenuRaw[]>(
      await getMenuTreeRaw({
        menuSource: menuSource ?? undefined
      })
    ) || []
  return payload.map(normalizeMenu).filter((item): item is SystemMenuItem => item !== null)
}

export const fetchMenuDetail = async (id: number): Promise<SystemMenuItem | null> => {
  return normalizeMenu(unwrapEnvelope<MenuRaw>(await getMenuDetailRaw(id)) || {})
}

export const createMenu = async (payload: MenuSavePayload) => {
  const requestPayload: MenuCreatePayloadRaw = {
    menuName: payload.menuName,
    menuKey: payload.menuKey || '',
    pid: payload.pid ?? 0,
    sortNum: payload.sortNum,
    path: toOptionalText(payload.path),
    menuSource: payload.menuSource,
    menuType: payload.menuType,
    icon: payload.icon || undefined,
    remark: payload.remark || undefined,
    hidden: payload.hidden,
    permissionCodes: payload.permissionCodes?.length ? payload.permissionCodes : undefined
  }
  await unwrapEnvelope<void>(await createMenuRaw(requestPayload))
}

export const updateMenu = async (id: number, payload: MenuSavePayload) => {
  const requestPayload: MenuUpdatePayloadRaw = {
    menuName: payload.menuName,
    menuKey: payload.menuKey || '',
    sortNum: payload.sortNum,
    path: toOptionalText(payload.path),
    menuSource: payload.menuSource,
    menuType: payload.menuType,
    icon: payload.icon || undefined,
    remark: payload.remark || undefined,
    hidden: payload.hidden,
    permissionCodes: payload.permissionCodes?.length ? payload.permissionCodes : undefined
  }
  await unwrapEnvelope<void>(await updateMenuRaw(id, requestPayload))
}

export const removeMenu = async (id: number) => {
  await unwrapEnvelope<void>(await deleteMenuRaw(id))
}
