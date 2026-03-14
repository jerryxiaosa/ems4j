import { unwrapEnvelope } from '@/api/raw/types'
import { getCurrentUserMenusRaw, type CurrentUserMenuRaw } from '@/api/raw/permission'
import type { CurrentUserMenuItem } from '@/types/permission'

const normalizeText = (value: unknown): string => {
  if (value === undefined || value === null) {
    return ''
  }
  return String(value).trim()
}

const toNumber = (value: unknown): number | undefined => {
  if (typeof value === "number" && Number.isFinite(value)) {
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

const normalizeCurrentUserMenu = (raw: CurrentUserMenuRaw): CurrentUserMenuItem | null => {
  const id = toNumber(raw.id)
  if (id === undefined) {
    return null
  }

  const parentId = toNumber(raw.pid)

  return {
    id,
    parentId: parentId && parentId > 0 ? parentId : null,
    menuName: normalizeText(raw.menuName),
    menuKey: normalizeText(raw.menuKey),
    path: normalizeText(raw.path),
    sortNum: toNumber(raw.sortNum) ?? 0,
    menuType: toNumber(raw.menuType) ?? 0,
    icon: normalizeText(raw.icon),
    hidden: Boolean(raw.hidden),
    menuSource: toNumber(raw.menuSource) ?? 0
  }
}

export const fetchCurrentUserMenus = async (source = 1): Promise<CurrentUserMenuItem[]> => {
  const payload = unwrapEnvelope<CurrentUserMenuRaw[]>(await getCurrentUserMenusRaw(source)) || []

  return payload
    .map(normalizeCurrentUserMenu)
    .filter((item): item is CurrentUserMenuItem => item !== null)
}
