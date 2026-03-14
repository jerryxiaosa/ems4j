import {
  addGatewayRaw,
  addElectricMeterRaw,
  deleteGatewayRaw,
  deleteElectricMeterRaw,
  getElectricMeterDetailRaw,
  getElectricMeterPageRaw,
  getDeviceModelListRaw,
  getDeviceModelPageRaw,
  getGatewayDetailRaw,
  getGatewayPageRaw,
  getGatewayListRaw,
  getDeviceTypeTreeRaw,
  getLatestPowerRecordRaw,
  updateElectricMeterCtRaw,
  updateElectricMeterProtectRaw,
  updateElectricMeterSwitchRaw,
  updateElectricMeterRaw,
  updateGatewayRaw
} from '@/api/raw/device'
import type {
  ElectricMeterCtPayload,
  ElectricMeterCreatePayload,
  ElectricMeterDetailItem,
  ElectricMeterPageItem,
  ElectricMeterPageQuery,
  ElectricMeterProtectPayload,
  ElectricMeterPageResult,
  ElectricMeterSwitchPayload,
  ElectricMeterUpdatePayload
} from '@/modules/devices/electric-meters/types'
import type {
  ElectricMeterRaw,
  DeviceModelRaw,
  DevicePageResultRaw,
  GatewayDetailRaw,
  GatewayMeterRaw,
  DeviceTypeTreeRaw,
  GatewayRaw,
  LatestPowerRecordRaw
} from '@/api/raw/device'
import { normalizePageResult, unwrapEnvelope } from '@/api/raw/types'
import type {
  DeviceModelListQuery,
  DeviceModelItem,
  DeviceModelPageQuery,
  DeviceModelPageResult,
  DeviceTypeTreeItem,
  GatewayCreatePayload,
  GatewayDetailItem,
  GatewayMeterItem,
  GatewayItem,
  GatewayListQuery,
  GatewayPageQuery,
  GatewayPageResult,
  GatewayUpdatePayload,
  LatestPowerRecord
} from '@/types/device'

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

const toBoolean = (value: unknown): boolean | undefined => {
  if (typeof value === 'boolean') {
    return value
  }

  if (typeof value === 'number') {
    if (value === 1) {
      return true
    }
    if (value === 0) {
      return false
    }
  }

  if (typeof value === 'string') {
    const source = value.trim().toLowerCase()
    if (['true', '1', 'yes', 'y'].includes(source)) {
      return true
    }
    if (['false', '0', 'no', 'n'].includes(source)) {
      return false
    }
  }

  return undefined
}

const normalizePowerValue = (value: unknown): string | undefined => {
  if (typeof value === 'number' && Number.isFinite(value)) {
    return value.toFixed(2)
  }

  if (typeof value === 'string') {
    const source = value.trim()
    if (!source) {
      return undefined
    }

    const normalized = source.replace(/,/g, '').replace(/[^\d.-]/g, '')
    const parsed = Number(normalized)
    if (Number.isFinite(parsed)) {
      return parsed.toFixed(2)
    }

    return source
  }

  return undefined
}

const normalizeLatestPowerRecord = (raw: LatestPowerRecordRaw): LatestPowerRecord => {
  return {
    recordTime: (raw.recordTime || '').trim() || undefined,
    power: normalizePowerValue(raw.power),
    powerHigher: normalizePowerValue(raw.powerHigher),
    powerHigh: normalizePowerValue(raw.powerHigh),
    powerLow: normalizePowerValue(raw.powerLow),
    powerLower: normalizePowerValue(raw.powerLower),
    powerDeepLow: normalizePowerValue(raw.powerDeepLow)
  }
}

const extractModelPropertyValue = (
  modelProperty: Record<string, unknown> | undefined,
  key: string
) => {
  if (!modelProperty || typeof modelProperty !== 'object') {
    return undefined
  }

  const source = modelProperty[key]
  if (source == null) {
    return undefined
  }

  if (typeof source !== 'object') {
    return source
  }

  const objectSource = source as Record<string, unknown>
  return (
    objectSource.value ??
    objectSource.defaultValue ??
    objectSource.code ??
    objectSource.name ??
    objectSource.label
  )
}

const normalizeDeviceTypeTreeItem = (raw: DeviceTypeTreeRaw): DeviceTypeTreeItem | null => {
  const id = toNumber(raw.id)
  const typeName = (raw.typeName || '').trim()
  if (id === undefined || !typeName) {
    return null
  }

  return {
    id,
    pid: toNumber(raw.pid) ?? 0,
    typeName,
    typeKey: (raw.typeKey || '').trim(),
    level: toNumber(raw.level)
  }
}

const flattenDeviceTypeTree = (items: DeviceTypeTreeRaw[]): DeviceTypeTreeItem[] => {
  const result: DeviceTypeTreeItem[] = []

  const walk = (nodes: DeviceTypeTreeRaw[], parentId?: number) => {
    nodes.forEach((node) => {
      const normalized = normalizeDeviceTypeTreeItem({
        ...node,
        pid: node.pid ?? parentId ?? 0
      })

      if (!normalized) {
        return
      }

      result.push(normalized)

      if (Array.isArray(node.children) && node.children.length > 0) {
        walk(node.children, normalized.id)
      }
    })
  }

  walk(items)
  return result
}

export const fetchLatestPowerRecord = async (id: number): Promise<LatestPowerRecord> => {
  const payload = unwrapEnvelope<LatestPowerRecordRaw>(await getLatestPowerRecordRaw(id)) || {}
  return normalizeLatestPowerRecord(payload)
}

export const fetchDeviceTypeTree = async (): Promise<DeviceTypeTreeItem[]> => {
  const payload = unwrapEnvelope<DeviceTypeTreeRaw[]>(await getDeviceTypeTreeRaw()) || []
  return flattenDeviceTypeTree(payload)
}

const normalizeDeviceModel = (raw: DeviceModelRaw): DeviceModelItem | null => {
  const id = toNumber(raw.id)
  if (id === undefined) {
    return null
  }

  const modelProperty =
    raw.modelProperty && typeof raw.modelProperty === 'object' ? raw.modelProperty : undefined
  const communicateModel =
    (raw.communicateModel || '').trim() ||
    (typeof extractModelPropertyValue(modelProperty, 'communicateModel') === 'string'
      ? String(extractModelPropertyValue(modelProperty, 'communicateModel')).trim()
      : '') ||
    undefined
  const normalizedCommunicateModel = communicateModel?.toLowerCase()
  const isNb =
    toBoolean(raw.isNb) ??
    toBoolean(extractModelPropertyValue(modelProperty, 'isNb')) ??
    (normalizedCommunicateModel === 'nb'
      ? true
      : normalizedCommunicateModel === 'tcp'
      ? false
      : undefined)
  const isCt = toBoolean(raw.isCt) ?? toBoolean(extractModelPropertyValue(modelProperty, 'isCt'))
  const isPrepay =
    toBoolean(raw.isPrepay) ?? toBoolean(extractModelPropertyValue(modelProperty, 'isPrepay'))

  return {
    id,
    typeId: toNumber(raw.typeId),
    typeKey: (raw.typeKey || '').trim() || undefined,
    manufacturerName: (raw.manufacturerName || '').trim() || undefined,
    modelName: (raw.modelName || '').trim() || undefined,
    productCode: (raw.productCode || '').trim() || undefined,
    communicateModel,
    isNb,
    isCt,
    isPrepay,
    modelProperty
  }
}

const normalizeGateway = (raw: GatewayRaw): GatewayItem | null => {
  const id = toNumber(raw.id)
  if (id === undefined) {
    return null
  }

  return {
    id,
    spaceId: toNumber(raw.spaceId),
    spaceName: (raw.spaceName || '').trim() || undefined,
    spaceParentNames: normalizeStringArray(raw.spaceParentNames),
    deviceNo: (raw.deviceNo || '').trim() || undefined,
    gatewayName: (raw.gatewayName || '').trim() || undefined,
    modelId: toNumber(raw.modelId),
    modelName: (raw.modelName || '').trim() || undefined,
    productCode: (raw.productCode || '').trim() || undefined,
    communicateModel: (raw.communicateModel || '').trim() || undefined,
    sn: (raw.sn || '').trim() || undefined,
    imei: (raw.imei || '').trim() || undefined,
    iotId: (raw.iotId || '').trim() || undefined,
    isOnline: raw.isOnline,
    configInfo: (raw.configInfo || '').trim() || undefined,
    remark: (raw.remark || '').trim() || undefined,
    ownAreaId: toNumber(raw.ownAreaId)
  }
}

const normalizeGatewayMeter = (raw: GatewayMeterRaw): GatewayMeterItem | null => {
  const id = toNumber(raw.id)
  if (id === undefined) {
    return null
  }

  return {
    id,
    meterName: (raw.meterName || '').trim() || undefined,
    deviceNo: (raw.deviceNo || '').trim() || undefined,
    isOnline: raw.isOnline,
    portNo: toNumber(raw.portNo),
    meterAddress: toNumber(raw.meterAddress)
  }
}

const normalizeStringArray = (value: unknown): string[] | string | undefined => {
  if (Array.isArray(value)) {
    return value
      .map((item) => (typeof item === 'string' ? item.trim() : ''))
      .filter((item) => item.length > 0)
  }

  if (typeof value === 'string') {
    const source = value.trim()
    return source ? source : undefined
  }

  return undefined
}

const normalizeElectricMeter = (raw: ElectricMeterRaw): ElectricMeterPageItem | null => {
  const id = toNumber(raw.id)
  if (id === undefined) {
    return null
  }

  return {
    id,
    spaceId: toNumber(raw.spaceId),
    spaceName: (raw.spaceName || '').trim() || undefined,
    spaceParentNames: normalizeStringArray(raw.spaceParentNames),
    meterName: (raw.meterName || '').trim() || undefined,
    deviceNo: (raw.deviceNo || '').trim() || undefined,
    modelId: toNumber(raw.modelId),
    modelName: (raw.modelName || '').trim() || undefined,
    productCode: (raw.productCode || '').trim() || undefined,
    communicateModel: (raw.communicateModel || '').trim() || undefined,
    gatewayId: toNumber(raw.gatewayId),
    gatewayName: (raw.gatewayName || '').trim() || undefined,
    portNo: toNumber(raw.portNo),
    meterAddress: toNumber(raw.meterAddress),
    imei: (raw.imei || '').trim() || undefined,
    isOnline: raw.isOnline,
    offlineDurationText: (raw.offlineDurationText || '').trim() || undefined,
    isCutOff: raw.isCutOff,
    remark: (raw.remark || '').trim() || undefined,
    iotId: (raw.iotId || '').trim() || undefined,
    isCalculate: raw.isCalculate,
    calculateType: toNumber(raw.calculateType),
    isPrepay: raw.isPrepay,
    protectedModel: raw.protectedModel,
    pricePlanId: toNumber(raw.pricePlanId),
    pricePlanName: (raw.pricePlanName || '').trim() || undefined,
    warnPlanId: toNumber(raw.warnPlanId),
    warnPlanName: (raw.warnPlanName || '').trim() || undefined,
    warnType: (raw.warnType || '').trim() || undefined,
    electricWarnTypeName: (raw.electricWarnTypeName || '').trim() || undefined,
    accountId: toNumber(raw.accountId),
    ct: toNumber(raw.ct),
    ownAreaId: toNumber(raw.ownAreaId)
  }
}

const normalizeElectricMeterDetail = (raw: ElectricMeterRaw): ElectricMeterDetailItem | null => {
  const normalized = normalizeElectricMeter(raw)
  if (!normalized) {
    return null
  }

  const latestPowerRecord = raw.latestPowerRecord
    ? normalizeLatestPowerRecord(raw.latestPowerRecord)
    : undefined

  return {
    ...normalized,
    createUser: toNumber((raw as ElectricMeterRaw & { createUser?: unknown }).createUser),
    createUserName:
      ((raw as ElectricMeterRaw & { createUserName?: string }).createUserName || '').trim() ||
      undefined,
    createTime:
      ((raw as ElectricMeterRaw & { createTime?: string }).createTime || '').trim() || undefined,
    updateUser: toNumber((raw as ElectricMeterRaw & { updateUser?: unknown }).updateUser),
    updateUserName:
      ((raw as ElectricMeterRaw & { updateUserName?: string }).updateUserName || '').trim() ||
      undefined,
    updateTime:
      ((raw as ElectricMeterRaw & { updateTime?: string }).updateTime || '').trim() || undefined,
    latestReportPower: latestPowerRecord?.power,
    latestReportTime: latestPowerRecord?.recordTime,
    latestReportHigherPower: latestPowerRecord?.powerHigher,
    latestReportHighPower: latestPowerRecord?.powerHigh,
    latestReportLowPower: latestPowerRecord?.powerLow,
    latestReportLowerPower: latestPowerRecord?.powerLower,
    latestReportDeepLowPower: latestPowerRecord?.powerDeepLow
  }
}

export const fetchDeviceModelPage = async (
  query: DeviceModelPageQuery
): Promise<DeviceModelPageResult> => {
  const payload =
    unwrapEnvelope<DevicePageResultRaw<DeviceModelRaw>>(await getDeviceModelPageRaw(query)) || {}
  const page = normalizePageResult(payload)

  return {
    ...page,
    list: page.list
      .map(normalizeDeviceModel)
      .filter((item): item is DeviceModelItem => item !== null)
  }
}

export const fetchDeviceModelList = async (
  query: DeviceModelListQuery
): Promise<DeviceModelItem[]> => {
  const payload = unwrapEnvelope<DeviceModelRaw[]>(await getDeviceModelListRaw(query)) || []
  return payload.map(normalizeDeviceModel).filter((item): item is DeviceModelItem => item !== null)
}

export const fetchGatewayList = async (query: GatewayListQuery = {}): Promise<GatewayItem[]> => {
  const payload = unwrapEnvelope<GatewayRaw[]>(await getGatewayListRaw(query)) || []
  return payload.map(normalizeGateway).filter((item): item is GatewayItem => item !== null)
}

export const fetchGatewayPage = async (query: GatewayPageQuery): Promise<GatewayPageResult> => {
  const payload =
    unwrapEnvelope<DevicePageResultRaw<GatewayRaw>>(await getGatewayPageRaw(query)) || {}
  const page = normalizePageResult(payload)

  return {
    ...page,
    list: page.list.map(normalizeGateway).filter((item): item is GatewayItem => item !== null)
  }
}

export const fetchGatewayDetail = async (id: number): Promise<GatewayDetailItem | null> => {
  const payload = unwrapEnvelope<GatewayDetailRaw>(await getGatewayDetailRaw(id))
  if (!payload) {
    return null
  }

  const gateway = normalizeGateway(payload)
  if (!gateway) {
    return null
  }

  return {
    ...gateway,
    meterList: Array.isArray(payload.meterList)
      ? payload.meterList
          .map(normalizeGatewayMeter)
          .filter((item): item is GatewayMeterItem => item !== null)
      : []
  }
}

export const createGateway = async (payload: GatewayCreatePayload): Promise<number | undefined> => {
  const result = unwrapEnvelope<number>(await addGatewayRaw(payload))
  return typeof result === 'number' ? result : undefined
}

export const updateGateway = async (id: number, payload: GatewayUpdatePayload): Promise<void> => {
  await updateGatewayRaw(id, payload)
}

export const removeGateway = async (id: number): Promise<void> => {
  await deleteGatewayRaw(id)
}

export const fetchElectricMeterPage = async (
  query: ElectricMeterPageQuery
): Promise<ElectricMeterPageResult> => {
  const payload =
    unwrapEnvelope<DevicePageResultRaw<ElectricMeterRaw>>(await getElectricMeterPageRaw(query)) ||
    {}
  const page = normalizePageResult(payload)

  return {
    ...page,
    list: page.list
      .map(normalizeElectricMeter)
      .filter((item): item is ElectricMeterPageItem => item !== null)
  }
}

export const fetchElectricMeterDetail = async (
  id: number
): Promise<ElectricMeterDetailItem | null> => {
  const payload = unwrapEnvelope<ElectricMeterRaw>(await getElectricMeterDetailRaw(id))
  if (!payload) {
    return null
  }

  return normalizeElectricMeterDetail(payload)
}

export const createElectricMeter = async (payload: ElectricMeterCreatePayload): Promise<number> => {
  return unwrapEnvelope<number>(await addElectricMeterRaw(payload))
}

export const updateElectricMeter = async (
  id: number,
  payload: ElectricMeterUpdatePayload
): Promise<void> => {
  await unwrapEnvelope<void>(await updateElectricMeterRaw(id, payload))
}

export const updateElectricMeterCt = async (
  payload: ElectricMeterCtPayload
): Promise<number | undefined> => {
  return unwrapEnvelope<number>(await updateElectricMeterCtRaw(payload))
}

export const updateElectricMeterSwitch = async (
  payload: ElectricMeterSwitchPayload
): Promise<void> => {
  await unwrapEnvelope<void>(await updateElectricMeterSwitchRaw(payload))
}

export const updateElectricMeterProtect = async (
  payload: ElectricMeterProtectPayload
): Promise<void> => {
  await unwrapEnvelope<void>(await updateElectricMeterProtectRaw(payload))
}

export const removeElectricMeter = async (id: number): Promise<void> => {
  await unwrapEnvelope<void>(await deleteElectricMeterRaw(id))
}
