import type { PageResult } from '@/types/http'

export interface ElectricMeterPageQuery {
  searchKey?: string
  isOnline?: boolean
  isCutOff?: boolean
  isPrepay?: boolean
  pageNum: number
  pageSize: number
}

export interface ElectricMeterCreatePayload {
  spaceId: number
  meterName: string
  deviceNo?: string
  isCalculate: boolean
  calculateType?: number
  isPrepay: boolean
  modelId: number
  gatewayId?: number
  portNo?: number
  meterAddress?: number
  imei?: string
  ct?: number
}

export interface ElectricMeterUpdatePayload {
  spaceId: number
  meterName: string
  isCalculate: boolean
  isPrepay: boolean
}

export interface ElectricMeterCtPayload {
  meterId: number
  ct: number
}

export interface ElectricMeterSwitchPayload {
  id: number
  switchStatus: 0 | 1
}

export interface ElectricMeterProtectPayload {
  meterIds: number[]
  protect: boolean
}

export interface ElectricMeterPageItem {
  id: number
  spaceId?: number
  spaceName?: string
  spaceParentNames?: string[] | string
  meterName?: string
  deviceNo?: string
  modelId?: number
  modelName?: string
  productCode?: string
  communicateModel?: string
  gatewayId?: number
  gatewayName?: string
  portNo?: number
  meterAddress?: number
  imei?: string
  isOnline?: boolean
  offlineDurationText?: string
  isCutOff?: boolean
  remark?: string
  iotId?: string
  isCalculate?: boolean
  calculateType?: number
  isPrepay?: boolean
  protectedModel?: boolean
  pricePlanId?: number
  pricePlanName?: string
  warnPlanId?: number
  warnPlanName?: string
  warnType?: string
  electricWarnTypeName?: string
  accountId?: number
  ct?: number
  ownAreaId?: number
}

export type ElectricMeterPageResult = PageResult<ElectricMeterPageItem>

export interface ElectricMeterDetailItem extends ElectricMeterPageItem {
  createUser?: number
  createUserName?: string
  createTime?: string
  updateUser?: number
  updateUserName?: string
  updateTime?: string
  latestReportPower?: string
  latestReportTime?: string
  latestReportHigherPower?: string
  latestReportHighPower?: string
  latestReportLowPower?: string
  latestReportLowerPower?: string
  latestReportDeepLowPower?: string
}
