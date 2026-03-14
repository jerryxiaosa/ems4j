import type { PageResult } from '@/types/http'

export interface LatestPowerRecord {
  recordTime?: string
  power?: string
  powerHigher?: string
  powerHigh?: string
  powerLow?: string
  powerLower?: string
  powerDeepLow?: string
}

export interface DeviceTypeTreeItem {
  id: number
  pid: number
  typeName: string
  typeKey: string
  level?: number
}

export interface DeviceModelPageQuery {
  typeIds?: number[]
  typeKey?: string
  manufacturerName?: string
  modelName?: string
  productCode?: string
  pageNum: number
  pageSize: number
}

export interface DeviceModelListQuery {
  typeIds?: number[]
  typeKey?: string
  manufacturerName?: string
  modelName?: string
  productCode?: string
}

export interface GatewayListQuery {
  searchKey?: string
  sn?: string
  isOnline?: boolean
  iotId?: string
  spaceIds?: number[]
}

export interface GatewayPageQuery extends GatewayListQuery {
  pageNum: number
  pageSize: number
}

export interface GatewayCreatePayload {
  spaceId: number
  gatewayName: string
  modelId: number
  deviceNo: string
  sn?: string
  imei?: string
  configInfo: string
  remark?: string
}

export interface GatewayUpdatePayload {
  id: number
  spaceId: number
  gatewayName: string
  modelId: number
  deviceNo: string
  sn?: string
  imei?: string
  configInfo?: string
  remark?: string
}

export interface DeviceModelItem {
  id: number
  typeId?: number
  typeKey?: string
  manufacturerName?: string
  modelName?: string
  productCode?: string
  communicateModel?: string
  isNb?: boolean
  isCt?: boolean
  isPrepay?: boolean
  modelProperty?: Record<string, unknown>
}

export type DeviceModelPageResult = PageResult<DeviceModelItem>

export interface GatewayItem {
  id: number
  spaceId?: number
  spaceName?: string
  spaceParentNames?: string[] | string
  deviceNo?: string
  gatewayName?: string
  modelId?: number
  modelName?: string
  productCode?: string
  communicateModel?: string
  sn?: string
  imei?: string
  iotId?: string
  isOnline?: boolean
  configInfo?: string
  remark?: string
  ownAreaId?: number
}

export type GatewayPageResult = PageResult<GatewayItem>

export interface GatewayMeterItem {
  id: number
  meterName?: string
  deviceNo?: string
  isOnline?: boolean
  portNo?: number
  meterAddress?: number
}

export interface GatewayDetailItem extends GatewayItem {
  meterList?: GatewayMeterItem[]
}
