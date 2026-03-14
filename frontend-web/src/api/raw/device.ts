import { requestV1 } from '@/api/http'
import type {
  ElectricMeterCtPayload,
  ElectricMeterCreatePayload,
  ElectricMeterPageQuery,
  ElectricMeterProtectPayload,
  ElectricMeterSwitchPayload,
  ElectricMeterUpdatePayload
} from '@/modules/devices/electric-meters/types'
import type { ApiEnvelope } from '@/types/http'
import type {
  DeviceModelListQuery,
  DeviceModelPageQuery,
  GatewayCreatePayload,
  GatewayUpdatePayload,
  GatewayPageQuery,
  GatewayListQuery
} from '@/types/device'

export interface LatestPowerRecordRaw {
  recordTime?: string
  power?: number | string
  powerHigher?: number | string
  powerHigh?: number | string
  powerLow?: number | string
  powerLower?: number | string
  powerDeepLow?: number | string
}

export interface DeviceTypeTreeRaw {
  id?: number | string
  pid?: number | string
  typeName?: string
  typeKey?: string
  level?: number | string
  children?: DeviceTypeTreeRaw[]
}

export interface DeviceModelRaw {
  id?: number | string
  typeId?: number | string
  typeKey?: string
  manufacturerName?: string
  modelName?: string
  productCode?: string
  communicateModel?: string
  isNb?: boolean | string | number
  isCt?: boolean | string | number
  isPrepay?: boolean | string | number
  modelProperty?: Record<string, unknown>
}

export interface GatewayRaw {
  id?: number | string
  spaceId?: number | string
  spaceName?: string
  spaceParentNames?: string[] | string
  deviceNo?: string
  gatewayName?: string
  modelId?: number | string
  modelName?: string
  productCode?: string
  communicateModel?: string
  sn?: string
  imei?: string
  iotId?: string
  isOnline?: boolean
  configInfo?: string
  remark?: string
  ownAreaId?: number | string
}

export interface GatewayMeterRaw {
  id?: number | string
  meterName?: string
  deviceNo?: string
  isOnline?: boolean
  portNo?: number | string
  meterAddress?: number | string
}

export interface GatewayDetailRaw extends GatewayRaw {
  meterList?: GatewayMeterRaw[]
}

export interface ElectricMeterRaw {
  id?: number | string
  spaceId?: number | string
  spaceName?: string
  spaceParentNames?: string[] | string
  meterName?: string
  deviceNo?: string
  modelId?: number | string
  modelName?: string
  productCode?: string
  communicateModel?: string
  gatewayId?: number | string
  gatewayName?: string
  portNo?: number | string
  meterAddress?: number | string
  imei?: string
  isOnline?: boolean
  offlineDurationText?: string
  isCutOff?: boolean
  remark?: string
  iotId?: string
  isCalculate?: boolean
  calculateType?: number | string
  isPrepay?: boolean
  protectedModel?: boolean
  pricePlanId?: number | string
  pricePlanName?: string
  warnPlanId?: number | string
  warnPlanName?: string
  warnType?: string
  electricWarnTypeName?: string
  accountId?: number | string
  ct?: number | string
  ownAreaId?: number | string
  latestPowerRecord?: LatestPowerRecordRaw
}

export interface DevicePageResultRaw<T> {
  pageNum?: number
  pageSize?: number
  total?: number
  list?: T[]
}

export const getLatestPowerRecordRaw = (id: number) => {
  return requestV1<ApiEnvelope<LatestPowerRecordRaw>>({
    method: 'GET',
    url: `/device/electric-meters/${id}/latest-power-record`
  })
}

export const getDeviceTypeTreeRaw = () => {
  return requestV1<ApiEnvelope<DeviceTypeTreeRaw[]>>({
    method: 'GET',
    url: '/device/device-types/tree'
  })
}

export const getDeviceModelPageRaw = (query: DeviceModelPageQuery) => {
  return requestV1<ApiEnvelope<DevicePageResultRaw<DeviceModelRaw>>>({
    method: 'GET',
    url: '/device/device-models/page',
    params: {
      typeIds: query.typeIds,
      typeKey: query.typeKey,
      manufacturerName: query.manufacturerName,
      modelName: query.modelName,
      productCode: query.productCode,
      pageNum: query.pageNum,
      pageSize: query.pageSize
    }
  })
}

export const getDeviceModelListRaw = (query: DeviceModelListQuery) => {
  return requestV1<ApiEnvelope<DeviceModelRaw[]>>({
    method: 'GET',
    url: '/device/device-models',
    params: {
      typeIds: query.typeIds,
      typeKey: query.typeKey,
      manufacturerName: query.manufacturerName,
      modelName: query.modelName,
      productCode: query.productCode
    }
  })
}

export const getGatewayListRaw = (query: GatewayListQuery = {}) => {
  return requestV1<ApiEnvelope<GatewayRaw[]>>({
    method: 'GET',
    url: '/device/gateways',
    params: {
      searchKey: query.searchKey,
      sn: query.sn,
      isOnline: query.isOnline,
      iotId: query.iotId,
      spaceIds: query.spaceIds
    }
  })
}

export const getGatewayPageRaw = (query: GatewayPageQuery) => {
  return requestV1<ApiEnvelope<DevicePageResultRaw<GatewayRaw>>>({
    method: 'GET',
    url: '/device/gateways/page',
    params: {
      searchKey: query.searchKey,
      sn: query.sn,
      isOnline: query.isOnline,
      iotId: query.iotId,
      spaceIds: query.spaceIds,
      pageNum: query.pageNum,
      pageSize: query.pageSize
    }
  })
}

export const getGatewayDetailRaw = (id: number) => {
  return requestV1<ApiEnvelope<GatewayDetailRaw>>({
    method: 'GET',
    url: `/device/gateways/${id}`
  })
}

export const addGatewayRaw = (data: GatewayCreatePayload) => {
  return requestV1<ApiEnvelope<number>>({
    method: 'POST',
    url: '/device/gateways',
    data
  })
}

export const updateGatewayRaw = (id: number, data: GatewayUpdatePayload) => {
  return requestV1<ApiEnvelope<void>>({
    method: 'PUT',
    url: `/device/gateways/${id}`,
    data
  })
}

export const deleteGatewayRaw = (id: number) => {
  return requestV1<ApiEnvelope<void>>({
    method: 'DELETE',
    url: `/device/gateways/${id}`
  })
}

export const getElectricMeterPageRaw = (query: ElectricMeterPageQuery) => {
  return requestV1<ApiEnvelope<DevicePageResultRaw<ElectricMeterRaw>>>({
    method: 'GET',
    url: '/device/electric-meters/page',
    params: {
      searchKey: query.searchKey,
      isOnline: query.isOnline,
      isCutOff: query.isCutOff,
      isPrepay: query.isPrepay,
      pageNum: query.pageNum,
      pageSize: query.pageSize
    }
  })
}

export const getElectricMeterDetailRaw = (id: number) => {
  return requestV1<ApiEnvelope<ElectricMeterRaw>>({
    method: 'GET',
    url: `/device/electric-meters/${id}`
  })
}

export const addElectricMeterRaw = (data: ElectricMeterCreatePayload) => {
  return requestV1<ApiEnvelope<number>>({
    method: 'POST',
    url: '/device/electric-meters',
    data
  })
}

export const updateElectricMeterCtRaw = (data: ElectricMeterCtPayload) => {
  return requestV1<ApiEnvelope<number>>({
    method: 'PUT',
    url: '/device/electric-meters/ct',
    data
  })
}

export const updateElectricMeterSwitchRaw = (data: ElectricMeterSwitchPayload) => {
  return requestV1<ApiEnvelope<void>>({
    method: 'PUT',
    url: '/device/electric-meters/switch',
    data
  })
}

export const updateElectricMeterProtectRaw = (data: ElectricMeterProtectPayload) => {
  return requestV1<ApiEnvelope<void>>({
    method: 'PUT',
    url: '/device/electric-meters/protect',
    data
  })
}

export const updateElectricMeterRaw = (id: number, data: ElectricMeterUpdatePayload) => {
  return requestV1<ApiEnvelope<void>>({
    method: 'PUT',
    url: `/device/electric-meters/${id}`,
    data
  })
}

export const deleteElectricMeterRaw = (id: number) => {
  return requestV1<ApiEnvelope<void>>({
    method: 'DELETE',
    url: `/device/electric-meters/${id}`
  })
}
