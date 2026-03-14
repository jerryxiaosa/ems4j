import { fetchEnumOptionsByKey } from '@/api/adapters/system'
import { unwrapEnvelope } from '@/api/raw/types'
import {
  createSpaceRaw,
  deleteSpaceRaw,
  getSpaceDetailRaw,
  getSpaceTreeRaw,
  updateSpaceRaw,
  type SpaceDetailRaw,
  type SpaceRaw,
  type SpaceSavePayloadRaw
} from '@/api/raw/space-manage'
import type { SystemSpaceItem } from '@/modules/system/spaces/types'

export interface SpaceSavePayload {
  name: string
  pid: number
  type: number
  area: number
  sortIndex?: number
}

const normalizeText = (value: unknown): string => {
  if (value === undefined || value === null) {
    return ''
  }
  return String(value).trim()
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

const buildTypeLabelMap = async () => {
  const options = await fetchEnumOptionsByKey('spaceType')
  return new Map(options.map((item) => [item.value, item.label]))
}

const normalizeSpaceTreeNode = (
  raw: SpaceRaw,
  typeLabelMap: Map<string, string>
): SystemSpaceItem => {
  const typeValue = (() => {
    const value = toNumber(raw.type)
    return value === undefined ? '' : String(value)
  })()

  return {
    id: toNumber(raw.id) ?? 0,
    parentId: toNumber(raw.pid) ?? null,
    name: normalizeText(raw.name),
    typeValue,
    typeName: typeLabelMap.get(typeValue) || '--',
    area: toNumber(raw.area) ?? 0,
    sortNum: toNumber(raw.sortIndex) ?? 0,
    children: Array.isArray(raw.children)
      ? raw.children.map((item) => normalizeSpaceTreeNode(item, typeLabelMap))
      : []
  }
}

const normalizeSpaceDetail = async (raw: SpaceDetailRaw): Promise<SystemSpaceItem> => {
  const typeLabelMap = await buildTypeLabelMap()
  const typeValue = (() => {
    const value = toNumber(raw.type)
    return value === undefined ? '' : String(value)
  })()

  return {
    id: toNumber(raw.id) ?? 0,
    parentId: toNumber(raw.pid) ?? null,
    name: normalizeText(raw.name),
    typeValue,
    typeName: typeLabelMap.get(typeValue) || '--',
    area: toNumber(raw.area) ?? 0,
    sortNum: toNumber(raw.sortIndex) ?? 0,
    children: []
  }
}

export const fetchSpaceTree = async (): Promise<SystemSpaceItem[]> => {
  const [list, typeLabelMap] = await Promise.all([
    Promise.resolve(unwrapEnvelope<SpaceRaw[]>(await getSpaceTreeRaw())),
    buildTypeLabelMap()
  ])

  return Array.isArray(list) ? list.map((item) => normalizeSpaceTreeNode(item, typeLabelMap)) : []
}

export const fetchSpaceDetail = async (id: number): Promise<SystemSpaceItem> => {
  return normalizeSpaceDetail(unwrapEnvelope<SpaceDetailRaw>(await getSpaceDetailRaw(id)))
}

export const createSpace = async (payload: SpaceSavePayload): Promise<number> => {
  const requestPayload: SpaceSavePayloadRaw = {
    name: payload.name,
    pid: payload.pid,
    type: payload.type,
    area: payload.area,
    sortIndex: payload.sortIndex
  }
  return unwrapEnvelope<number>(await createSpaceRaw(requestPayload))
}

export const updateSpace = async (id: number, payload: SpaceSavePayload) => {
  const requestPayload: SpaceSavePayloadRaw = {
    name: payload.name,
    pid: payload.pid,
    type: payload.type,
    area: payload.area,
    sortIndex: payload.sortIndex
  }
  await unwrapEnvelope<void>(await updateSpaceRaw(id, requestPayload))
}

export const deleteSpace = async (id: number) => {
  await unwrapEnvelope<void>(await deleteSpaceRaw(id))
}
