import {
  addElectricPricePlanRaw,
  addWarnPlanRaw,
  deleteElectricPricePlanRaw,
  deleteWarnPlanRaw,
  getDefaultElectricPriceRaw,
  getDefaultStepPriceRaw,
  getDefaultElectricTimeRaw,
  getElectricPricePlanDetailRaw,
  getElectricPricePlanListRaw,
  getWarnPlanDetailRaw,
  getWarnPlanListRaw,
  updateDefaultElectricPriceRaw,
  updateDefaultElectricTimeRaw,
  updateElectricPricePlanRaw,
  updateWarnPlanRaw,
  type ElectricPricePlanRaw,
  type ElectricPriceTimeSettingRaw,
  type ElectricPriceTypeRaw,
  type StepPriceRaw,
  type WarnPlanRaw
} from '@/api/raw/plan'
import { unwrapEnvelope } from '@/api/raw/types'
import type { EnumOption } from '@/api/adapters/system'
import type {
  ElectricPriceDefaultStepPrice,
  ElectricPricePlanItem,
  ElectricPricePlanQuery,
  ElectricPricePlanSavePayload,
  ElectricPriceStandardPrice,
  ElectricPriceTimeSettingItem
} from '@/types/electric-price-plan'
import type { WarnPlanItem, WarnPlanQuery, WarnPlanSavePayload } from '@/types/plan'

const toOptionValue = (value: unknown): string => {
  if (typeof value === 'number' && Number.isFinite(value)) {
    return String(value)
  }
  if (typeof value === 'string' && value.trim()) {
    return value.trim()
  }
  return ''
}

const normalizePlanOption = (raw: ElectricPricePlanRaw | WarnPlanRaw): EnumOption | null => {
  const value = toOptionValue(raw?.id)
  if (!value) {
    return null
  }

  const label = (raw?.name || '').trim() || value
  return { value, label }
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

const parseStepPriceList = (
  value: unknown
): Array<{ start?: number | null; end?: number | null; value?: number | null }> => {
  const normalize = (source: unknown[]) =>
    source
      .map((item) => {
        const record = item as Record<string, unknown>
        return {
          start: toNumber(record.start) ?? null,
          end: toNumber(record.end) ?? null,
          value: toNumber(record.value) ?? null
        }
      })
      .filter((item) => item.start !== null || item.end !== null || item.value !== null)

  if (Array.isArray(value)) {
    return normalize(value)
  }

  if (typeof value === 'string' && value.trim()) {
    try {
      const parsed = JSON.parse(value)
      return Array.isArray(parsed) ? normalize(parsed) : []
    } catch {
      return []
    }
  }

  return []
}

const normalizeElectricPricePlan = (raw: ElectricPricePlanRaw): ElectricPricePlanItem | null => {
  const id = toNumber(raw.id)
  if (id === undefined) {
    return null
  }

  const stepPrices = parseStepPriceList(raw.stepPrice)

  return {
    id,
    name: (raw.name || '').trim() || '--',
    priceHigher: toNumber(raw.priceHigher) ?? 0,
    priceHigh: toNumber(raw.priceHigh) ?? 0,
    priceLow: toNumber(raw.priceLow) ?? 0,
    priceLower: toNumber(raw.priceLower) ?? 0,
    priceDeepLow: toNumber(raw.priceDeepLow) ?? 0,
    ratioHigher: toNumber(raw.priceHigherMultiply) ?? null,
    ratioHigh: toNumber(raw.priceHighMultiply) ?? null,
    ratioLow: toNumber(raw.priceLowMultiply) ?? null,
    ratioLower: toNumber(raw.priceLowerMultiply) ?? null,
    ratioDeepLow: toNumber(raw.priceDeepLowMultiply) ?? null,
    isCustomPrice: raw.isCustomPrice === true,
    hasStepPrice: raw.isStep === true,
    step1End: stepPrices[0]?.end ?? null,
    step1Ratio: stepPrices[0]?.value ?? null,
    step2End: stepPrices[1]?.end ?? null,
    step2Ratio: stepPrices[1]?.value ?? null,
    step3End: stepPrices[2]?.end ?? null,
    step3Ratio: stepPrices[2]?.value ?? null,
    createUser: (raw.createUserName || '').trim() || undefined,
    createTime: raw.createTime || undefined,
    updateUser: (raw.updateUserName || '').trim() || undefined,
    updateTime: raw.updateTime || undefined
  }
}

const normalizeDefaultElectricPrice = (
  list: ElectricPriceTypeRaw[]
): ElectricPriceStandardPrice => {
  const priceMap = new Map<number, number>()
  list.forEach((item) => {
    const type = toNumber(item.type)
    const price = toNumber(item.price)
    if (type !== undefined && price !== undefined) {
      priceMap.set(type, price)
    }
  })

  return {
    priceHigher: priceMap.get(1) ?? 0,
    priceHigh: priceMap.get(2) ?? 0,
    priceLow: priceMap.get(3) ?? 0,
    priceLower: priceMap.get(4) ?? 0,
    priceDeepLow: priceMap.get(5) ?? 0
  }
}

const electricTimeTypeMap: Record<number, ElectricPriceTimeSettingItem['type']> = {
  1: 'higher',
  2: 'high',
  3: 'low',
  4: 'lower',
  5: 'deepLow'
}

const electricTimeTypeCodeMap: Record<Exclude<ElectricPriceTimeSettingItem['type'], ''>, number> = {
  higher: 1,
  high: 2,
  low: 3,
  lower: 4,
  deepLow: 5
}

const normalizeLocalTime = (value: unknown): string => {
  if (typeof value !== 'string' || !value.trim()) {
    return ''
  }

  const match = value.trim().match(/^(\d{2}):(\d{2})/)
  if (!match) {
    return ''
  }
  return `${match[1]}:${match[2]}`
}

const normalizeWarnPlan = (raw: WarnPlanRaw): WarnPlanItem | null => {
  const id = toNumber(raw.id)
  if (id === undefined) {
    return null
  }

  return {
    id,
    name: (raw.name || '').trim() || '--',
    firstLevel: toNumber(raw.firstLevel) ?? 0,
    secondLevel: toNumber(raw.secondLevel) ?? 0,
    autoClose: raw.autoClose === true,
    createUser: (raw.createUserName || '').trim() || undefined,
    createTime: raw.createTime || undefined,
    updateUser: (raw.updateUserName || '').trim() || undefined,
    updateTime: raw.updateTime || undefined,
    remark: (raw.remark || '').trim() || undefined
  }
}

export const fetchElectricPricePlanOptions = async (): Promise<EnumOption[]> => {
  const list = unwrapEnvelope<ElectricPricePlanRaw[]>(await getElectricPricePlanListRaw()) || []
  return list.map(normalizePlanOption).filter((item): item is EnumOption => item !== null)
}

export const fetchElectricPricePlanList = async (
  query: ElectricPricePlanQuery = {}
): Promise<ElectricPricePlanItem[]> => {
  const list =
    unwrapEnvelope<ElectricPricePlanRaw[]>(await getElectricPricePlanListRaw(query)) || []
  return list
    .map(normalizeElectricPricePlan)
    .filter((item): item is ElectricPricePlanItem => item !== null)
}

export const fetchElectricPricePlanDetail = async (id: number): Promise<ElectricPricePlanItem> => {
  const payload =
    unwrapEnvelope<ElectricPricePlanRaw>(await getElectricPricePlanDetailRaw(id)) || {}
  const item = normalizeElectricPricePlan(payload)
  if (!item) {
    throw new Error('电价方案详情数据无效')
  }
  return item
}

export const fetchDefaultElectricPrices = async (): Promise<ElectricPriceStandardPrice> => {
  const list = unwrapEnvelope<ElectricPriceTypeRaw[]>(await getDefaultElectricPriceRaw()) || []
  return normalizeDefaultElectricPrice(list)
}

export const updateDefaultElectricPrices = async (
  prices: ElectricPriceStandardPrice
): Promise<void> => {
  const payload: ElectricPriceTypeRaw[] = [
    { type: 1, price: prices.priceHigher },
    { type: 2, price: prices.priceHigh },
    { type: 3, price: prices.priceLow },
    { type: 4, price: prices.priceLower },
    { type: 5, price: prices.priceDeepLow }
  ]

  await unwrapEnvelope<void>(await updateDefaultElectricPriceRaw(payload))
}

export const fetchDefaultElectricTimes = async (): Promise<ElectricPriceTimeSettingItem[]> => {
  const list =
    unwrapEnvelope<ElectricPriceTimeSettingRaw[]>(await getDefaultElectricTimeRaw()) || []
  const result: ElectricPriceTimeSettingItem[] = []

  list.forEach((item) => {
    const typeCode = toNumber(item.type)
    const type = typeCode !== undefined ? electricTimeTypeMap[typeCode] : undefined
    const time = normalizeLocalTime(item.start)
    if (!type || !time) {
      return
    }
    result.push({ type, time })
  })

  return result
}

export const updateDefaultElectricTimes = async (
  rows: ElectricPriceTimeSettingItem[]
): Promise<void> => {
  const payload: ElectricPriceTimeSettingRaw[] = rows
    .filter((row) => row.type && row.time)
    .map((row) => ({
      type: electricTimeTypeCodeMap[row.type as Exclude<ElectricPriceTimeSettingItem['type'], ''>],
      start: row.time
    }))

  await unwrapEnvelope<void>(await updateDefaultElectricTimeRaw(payload))
}

export const fetchDefaultStepPrices = async (): Promise<ElectricPriceDefaultStepPrice> => {
  const list = unwrapEnvelope<StepPriceRaw[]>(await getDefaultStepPriceRaw()) || []

  return {
    step1End: list[0]?.end != null ? String(list[0].end) : '',
    step1Ratio: list[0]?.value != null ? String(list[0].value) : '',
    step2End: list[1]?.end != null ? String(list[1].end) : '',
    step2Ratio: list[1]?.value != null ? String(list[1].value) : '',
    step3End: list[2]?.end != null ? String(list[2].end) : '',
    step3Ratio: list[2]?.value != null ? String(list[2].value) : ''
  }
}

export const addElectricPricePlan = async (data: ElectricPricePlanSavePayload): Promise<number> => {
  const result = unwrapEnvelope<number>(await addElectricPricePlanRaw(data))
  if (typeof result !== 'number') {
    throw new Error('电价方案添加成功但未返回方案 ID')
  }
  return result
}

export const updateElectricPricePlan = async (
  id: number,
  data: ElectricPricePlanSavePayload
): Promise<void> => {
  await unwrapEnvelope<void>(await updateElectricPricePlanRaw(id, data))
}

export const deleteElectricPricePlan = async (id: number): Promise<void> => {
  await unwrapEnvelope<void>(await deleteElectricPricePlanRaw(id))
}

export const fetchWarnPlanOptions = async (): Promise<EnumOption[]> => {
  const list = unwrapEnvelope<WarnPlanRaw[]>(await getWarnPlanListRaw()) || []
  return list.map(normalizePlanOption).filter((item): item is EnumOption => item !== null)
}

export const fetchWarnPlanList = async (query: WarnPlanQuery = {}): Promise<WarnPlanItem[]> => {
  const list = unwrapEnvelope<WarnPlanRaw[]>(await getWarnPlanListRaw(query)) || []
  return list.map(normalizeWarnPlan).filter((item): item is WarnPlanItem => item !== null)
}

export const fetchWarnPlanDetail = async (id: number): Promise<WarnPlanItem> => {
  const payload = unwrapEnvelope<WarnPlanRaw>(await getWarnPlanDetailRaw(id)) || {}
  const item = normalizeWarnPlan(payload)
  if (!item) {
    throw new Error('预警方案详情数据无效')
  }
  return item
}

export const addWarnPlan = async (data: WarnPlanSavePayload): Promise<number> => {
  const result = unwrapEnvelope<number>(await addWarnPlanRaw(data))
  if (typeof result !== 'number') {
    throw new Error('预警方案添加成功但未返回方案 ID')
  }
  return result
}

export const updateWarnPlan = async (id: number, data: WarnPlanSavePayload): Promise<void> => {
  await unwrapEnvelope<void>(await updateWarnPlanRaw(id, data))
}

export const deleteWarnPlan = async (id: number): Promise<void> => {
  await unwrapEnvelope<void>(await deleteWarnPlanRaw(id))
}
