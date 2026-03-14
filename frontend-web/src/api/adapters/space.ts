import { getSpaceTreeRaw, type SpaceTreeRaw } from '@/api/raw/space'
import { unwrapEnvelope } from '@/api/raw/types'
import type { SpaceTreeItem } from '@/types/space'

const toNumber = (value: unknown, fallback = 0): number => {
  if (typeof value === 'number' && Number.isFinite(value)) {
    return value
  }

  if (typeof value === 'string' && value.trim()) {
    const parsed = Number(value)
    if (Number.isFinite(parsed)) {
      return parsed
    }
  }

  return fallback
}

const normalizeParentsNames = (raw: SpaceTreeRaw, inheritedParents: string[]) => {
  if (Array.isArray(raw.parentsNames) && raw.parentsNames.length > 0) {
    return raw.parentsNames.map((item) => item.trim()).filter(Boolean)
  }

  if (typeof raw.fullPath === 'string' && raw.fullPath.trim()) {
    const segments = raw.fullPath
      .split(',')
      .map((item) => item.trim())
      .filter(Boolean)

    if (segments.length > 1) {
      return segments.slice(0, -1)
    }
  }

  return inheritedParents
}

const normalizeSpaceTreeNode = (
  raw: SpaceTreeRaw,
  inheritedParents: string[] = []
): SpaceTreeItem | null => {
  const id = toNumber(raw.id, NaN)
  const name = (raw.name || '').trim()
  if (!Number.isFinite(id) || !name) {
    return null
  }

  const parentsNames = normalizeParentsNames(raw, inheritedParents)
  const currentPath = [...parentsNames, name].join(' / ')
  const children = Array.isArray(raw.children)
    ? raw.children
        .map((item) => normalizeSpaceTreeNode(item, [...parentsNames, name]))
        .filter((item): item is SpaceTreeItem => item !== null)
    : []

  return {
    id,
    pid: toNumber(raw.pid),
    name,
    fullPath: (raw.fullPath || '').trim() || undefined,
    type: Number.isFinite(toNumber(raw.type, NaN)) ? toNumber(raw.type, NaN) : undefined,
    sortIndex: Number.isFinite(toNumber(raw.sortIndex, NaN))
      ? toNumber(raw.sortIndex, NaN)
      : undefined,
    ownAreaId: Number.isFinite(toNumber(raw.ownAreaId, NaN))
      ? toNumber(raw.ownAreaId, NaN)
      : undefined,
    parentsIds: Array.isArray(raw.parentsIds)
      ? raw.parentsIds.map((item) => toNumber(item, NaN)).filter((item) => Number.isFinite(item))
      : undefined,
    parentsNames,
    pathLabel: currentPath,
    children: children.length > 0 ? children : undefined
  }
}

export const fetchSpaceTree = async (): Promise<SpaceTreeItem[]> => {
  const payload = unwrapEnvelope<SpaceTreeRaw[]>(await getSpaceTreeRaw()) || []
  return payload
    .map((item) => normalizeSpaceTreeNode(item))
    .filter((item): item is SpaceTreeItem => item !== null)
}
