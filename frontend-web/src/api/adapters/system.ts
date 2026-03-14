import { getSystemApiOptionsRaw, getSystemEnumsRaw } from '@/api/raw/system'
import type { ApiOptionRaw, EnumItemRaw } from '@/api/raw/system'
import { unwrapEnvelope } from '@/api/raw/types'

export interface EnumOption {
  label: string
  value: string
}

const toOptionValue = (value: unknown): string => {
  if (typeof value === 'number' || typeof value === 'string') {
    return String(value)
  }
  if (typeof value === 'boolean') {
    return value ? 'true' : 'false'
  }
  return ''
}

const toOptionLabel = (item: EnumItemRaw, value: string): string => {
  const info = (item.info || '').trim()
  if (info) {
    return info
  }
  return value || '--'
}

let enumMapCache: Record<string, EnumItemRaw[]> | null = null
let enumMapLoadingPromise: Promise<Record<string, EnumItemRaw[]>> | null = null
let apiOptionsCache: EnumOption[] | null = null
let apiOptionsLoadingPromise: Promise<EnumOption[]> | null = null

const loadEnumMap = async (): Promise<Record<string, EnumItemRaw[]>> => {
  if (enumMapCache) {
    return enumMapCache
  }
  if (enumMapLoadingPromise) {
    return enumMapLoadingPromise
  }

  enumMapLoadingPromise = (async () => {
    const enumMap = unwrapEnvelope(await getSystemEnumsRaw()) || {}
    enumMapCache = enumMap
    return enumMap
  })()

  try {
    return await enumMapLoadingPromise
  } finally {
    enumMapLoadingPromise = null
  }
}

export const clearSystemEnumCache = () => {
  enumMapCache = null
  enumMapLoadingPromise = null
  apiOptionsCache = null
  apiOptionsLoadingPromise = null
}

export const fetchEnumOptionsByKey = async (key: string): Promise<EnumOption[]> => {
  const enumMap = await loadEnumMap()
  const items = Array.isArray(enumMap[key]) ? enumMap[key] : []

  return items
    .map((item) => {
      const value = toOptionValue(item?.value)
      if (value === '') {
        return null
      }
      return {
        value,
        label: toOptionLabel(item || {}, value)
      }
    })
    .filter((item): item is EnumOption => item !== null)
}

const normalizeApiOption = (item: ApiOptionRaw): EnumOption | null => {
  const value = (item.permissionCode || '').trim()
  if (!value) {
    return null
  }

  const label = (item.key || '').trim() || value
  return {
    value,
    label
  }
}

export const fetchSystemApiOptions = async (): Promise<EnumOption[]> => {
  if (apiOptionsCache) {
    return apiOptionsCache
  }
  if (apiOptionsLoadingPromise) {
    return apiOptionsLoadingPromise
  }

  apiOptionsLoadingPromise = (async () => {
    const list = unwrapEnvelope<ApiOptionRaw[]>(await getSystemApiOptionsRaw()) || []
    apiOptionsCache = list
      .map(normalizeApiOption)
      .filter((item): item is EnumOption => item !== null)
    return apiOptionsCache
  })()

  try {
    return await apiOptionsLoadingPromise
  } finally {
    apiOptionsLoadingPromise = null
  }
}
