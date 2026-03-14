import type { ElectricMeterItem } from '@/components/devices/electric-meter.mock'
import type {
  ElectricMeterDetailItem,
  ElectricMeterPageItem
} from '@/modules/devices/electric-meters/types'

export const DEFAULT_ELECTRIC_METER_PAGE_SIZE = 10
export const electricMeterPermissionKeys = {
  create: 'device_management_electric_meter_create',
  detail: 'device_management_electric_meter_detail',
  edit: 'device_management_electric_meter_edit',
  delete: 'device_management_electric_meter_delete',
  switchOn: 'device_management_electric_meter_switch_on',
  switchOff: 'device_management_electric_meter_switch_off',
  setCt: 'device_management_electric_meter_set_ct',
  enableProtection: 'device_management_electric_meter_enable_protection',
  disableProtection: 'device_management_electric_meter_disable_protection',
  batchSwitchOn: 'device_management_electric_meter_batch_switch_on',
  batchSwitchOff: 'device_management_electric_meter_batch_switch_off',
  batchEnableProtection: 'device_management_electric_meter_batch_enable_protection',
  batchDisableProtection: 'device_management_electric_meter_batch_disable_protection'
} as const

export const getElectricMeterErrorMessage = (error: unknown): string => {
  if (error instanceof Error && error.message) {
    return error.message
  }

  return '请稍后重试'
}

export const formatCommunicateModel = (value: string | undefined) => {
  const source = (value || '').trim()
  const normalized = source.toLowerCase()
  if (!source) {
    return '--'
  }
  if (normalized === 'tcp') {
    return 'TCP'
  }
  if (normalized === 'nb') {
    return 'NB'
  }
  return source
}

export const getOnlineStatusClass = (row: Pick<ElectricMeterItem, 'onlineStatus'>) => {
  if (row.onlineStatus === 1) {
    return 'meter-online-status meter-online-status-online'
  }
  if (row.onlineStatus === 0) {
    return 'meter-online-status meter-online-status-offline'
  }
  return 'meter-online-status meter-online-status-unknown'
}

export const getSpaceRegion = (row: Pick<ElectricMeterItem, 'spacePath' | 'spaceName'>) => {
  const segments = row.spacePath
    .split(' / ')
    .map((item) => item.trim())
    .filter(Boolean)

  if (segments.length <= 1) {
    return row.spacePath || '--'
  }

  if (segments[segments.length - 1] === row.spaceName) {
    return segments.slice(0, -1).join(' > ') || '--'
  }

  return segments.slice(0, -1).join(' > ') || row.spacePath || '--'
}

const normalizeSpaceParentNames = (value: string[] | string | undefined): string[] => {
  if (Array.isArray(value)) {
    return value.map((item) => item.trim()).filter(Boolean)
  }

  if (typeof value === 'string') {
    return value
      .split(/[>/,]/)
      .map((item) => item.trim())
      .filter(Boolean)
  }

  return []
}

export const normalizeElectricMeterRow = (item: ElectricMeterPageItem): ElectricMeterItem => {
  const parentNames = normalizeSpaceParentNames(item.spaceParentNames)
  const spaceName = item.spaceName || '--'
  const spacePath = [...parentNames, spaceName].filter(Boolean).join(' / ') || spaceName
  const onlineStatus = item.isOnline === true ? 1 : item.isOnline === false ? 0 : null
  const isCutOff = item.isCutOff === true

  return {
    id: item.id,
    meterName: item.meterName || '--',
    deviceNo: item.deviceNo || '--',
    meterAddress: item.meterAddress != null ? String(item.meterAddress) : '',
    modelId: item.modelId || 0,
    modelName: item.modelName || '--',
    communicateModel: formatCommunicateModel(item.communicateModel),
    isCt: item.ct != null,
    ct: item.ct != null ? String(item.ct) : '',
    gatewayId: item.gatewayId,
    gatewayName: item.gatewayName || '',
    gatewayDeviceNo: undefined,
    gatewaySn: undefined,
    portNo: item.portNo != null ? String(item.portNo) : '',
    imei: item.imei || '',
    payType: item.isPrepay === true ? 1 : 0,
    isCalculate: item.isCalculate === true,
    calculateType: item.calculateType != null ? String(item.calculateType) : '',
    calculateTypeName: item.calculateType != null ? String(item.calculateType) : '--',
    spaceId: item.spaceId != null ? String(item.spaceId) : '',
    spaceName,
    spacePath,
    onlineStatus,
    onlineStatusName: onlineStatus === 1 ? '在线' : onlineStatus === 0 ? '离线' : '--',
    offlineDuration: item.offlineDurationText || '',
    status: isCutOff ? 1 : 0,
    statusName: isCutOff ? '断闸' : '合闸',
    protectedModel: item.protectedModel === true,
    pricePlanName: item.pricePlanName || '',
    warnPlanName: item.warnPlanName || '',
    electricWarnTypeName: item.electricWarnTypeName || '',
    accountId: item.accountId ?? null,
    latestReportPower: undefined,
    latestReportTime: undefined,
    latestReportHigherPower: undefined,
    latestReportHighPower: undefined,
    latestReportLowPower: undefined,
    latestReportLowerPower: undefined,
    latestReportDeepLowPower: undefined
  }
}

export const normalizeElectricMeterDetailRow = (
  item: ElectricMeterDetailItem,
  fallback?: ElectricMeterItem | null
): ElectricMeterItem => {
  const base = normalizeElectricMeterRow(item)

  return {
    ...base,
    gatewayDeviceNo: fallback?.gatewayDeviceNo,
    latestReportPower: item.latestReportPower ?? fallback?.latestReportPower,
    latestReportTime: item.latestReportTime ?? fallback?.latestReportTime,
    latestReportHigherPower: item.latestReportHigherPower ?? fallback?.latestReportHigherPower,
    latestReportHighPower: item.latestReportHighPower ?? fallback?.latestReportHighPower,
    latestReportLowPower: item.latestReportLowPower ?? fallback?.latestReportLowPower,
    latestReportLowerPower: item.latestReportLowerPower ?? fallback?.latestReportLowerPower,
    latestReportDeepLowPower: item.latestReportDeepLowPower ?? fallback?.latestReportDeepLowPower
  }
}

export const toOptionalNumber = (value: string | undefined) => {
  if (!value || !value.trim()) {
    return undefined
  }

  const parsed = Number(value.trim())
  return Number.isFinite(parsed) ? parsed : undefined
}

export const showNumberForCreate = (isNb: boolean, value: string | undefined) => {
  if (isNb) {
    return undefined
  }

  return toOptionalNumber(value)
}
