import type { PageResult } from '@/types/http'

export interface AccountMeter {
  id?: number
  meterName?: string
  deviceNo?: string
  spaceId?: number
  spaceName?: string
  spaceParentNames?: string | string[]
  offlineDurationText?: string
  meterType?: number | string
  meterTypeName?: string
  ct?: number | string
  meterBalanceAmountText?: string
  balance?: number | string
  pricePlanId?: number
  warnPlanId?: number
  warnType?: string
  warnTypeName?: string
  isOnline?: boolean
}

export interface AccountItem {
  id?: number
  ownerType?: number
  ownerTypeName?: string
  ownerId?: number
  ownerName?: string
  contactName?: string
  contactPhone?: string
  electricAccountType?: number
  electricAccountTypeName?: string
  electricPricePlanName?: string
  electricBalanceAmountText?: string
  openedMeterCount?: number
  totalOpenableMeterCount?: number
  monthlyPayAmount?: number
  warnPlanId?: number
  warnPlanName?: string
  electricWarnType?: string
  meterList: AccountMeter[]
}

export interface AccountPageQuery {
  ownerName?: string
  electricAccountType?: number
  pageNum: number
  pageSize: number
}

export type AccountPageResult = PageResult<AccountItem>

export interface OpenAccountMeter {
  meterId: number
}

export interface AppendAccountMetersPayload {
  electricMeterList: OpenAccountMeter[]
  inheritHistoryPower?: boolean
}

export interface OpenAccountPayload {
  ownerId: number
  ownerType: number
  ownerName: string
  contactName?: string
  contactPhone?: string
  electricAccountType: number
  monthlyPayAmount?: number
  electricPricePlanId?: number
  warnPlanId?: number
  inheritHistoryPower?: boolean
  electricMeterList: OpenAccountMeter[]
}

export interface OwnerAccountStatusQuery {
  ownerType: number
  ownerId: number
}

export interface OwnerAccountStatus {
  ownerType?: number
  ownerId?: number
  hasAccount?: boolean
  accountId?: number
  electricAccountType?: number
  electricPricePlanId?: number
  warnPlanId?: number
  monthlyPayAmountText?: string
}

export interface OwnerCandidateMeterQuery {
  ownerType: number
  ownerId: number
  spaceNameLike?: string
}

export interface OwnerCandidateMeter {
  id: number
  meterName?: string
  deviceNo?: string
  spaceId?: number
  spaceName?: string
  spaceParentNames?: string[] | string
  isOnline?: boolean
  isPrepay?: boolean
  meterType?: number | string
  meterTypeName?: string
}

export interface CancelAccountMeter {
  meterId: number
  powerHigher?: number
  powerHigh?: number
  powerLow?: number
  powerLower?: number
  powerDeepLow?: number
}

export interface CancelAccountPayload {
  accountId: number
  remark?: string
  meterList: CancelAccountMeter[]
}

export interface CancelAccountResult {
  cancelNo?: string
  cleanBalanceType?: number
  cleanBalanceAmountText?: string
  amount?: number
}

export interface CancelRecordItem {
  cancelNo?: string
  ownerName?: string
  electricMeterAmount?: number
  cleanBalanceType?: number
  cleanBalanceAmountText?: string
  cleanBalanceReal?: number
  operatorName?: string
  cancelTime?: string
  remark?: string
}

export interface CancelRecordQuery {
  ownerName?: string
  cleanBalanceType?: number
  pageNum: number
  pageSize: number
}

export type CancelRecordPageResult = PageResult<CancelRecordItem>

export interface CanceledMeterItem {
  spaceName?: string
  spaceParentNames?: string
  meterName?: string
  deviceNo?: string
  meterType?: number
  balance?: number
  power?: number
  powerHigher?: number
  powerHigh?: number
  powerLow?: number
  powerLower?: number
  powerDeepLow?: number
  showTime?: string
  historyPowerTotal?: number
}

export interface CancelDetail {
  cancelNo?: string
  ownerName?: string
  electricMeterAmount?: number
  cleanBalanceType?: number
  cleanBalanceAmountText?: string
  cleanBalanceReal?: number
  operatorName?: string
  cancelTime?: string
  remark?: string
  meterList: CanceledMeterItem[]
}
