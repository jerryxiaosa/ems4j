import { requestV1 } from '@/api/http'
import type { ApiEnvelope } from '@/types/http'
import type {
  AppendAccountMetersPayload,
  AccountPageQuery,
  CancelAccountPayload,
  CancelRecordQuery,
  OpenAccountPayload,
  OwnerAccountStatusQuery,
  OwnerCandidateMeterQuery
} from '@/types/account'

export interface AccountMeterRaw {
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

export interface AccountRaw {
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
  meterList?: AccountMeterRaw[]
}

export interface PageResultRaw<T> {
  pageNum?: number
  pageSize?: number
  total?: number
  list?: T[]
}

export interface CancelRecordRaw {
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

export interface CanceledMeterRaw {
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

export interface CancelDetailRaw {
  cancelNo?: string
  ownerName?: string
  electricMeterAmount?: number
  cleanBalanceType?: number
  cleanBalanceAmountText?: string
  cleanBalanceReal?: number
  operatorName?: string
  cancelTime?: string
  remark?: string
  meterList?: CanceledMeterRaw[]
}

export interface CancelResultRaw {
  cancelNo?: string
  cleanBalanceType?: number
  cleanBalanceAmountText?: string
  amount?: number
}

export interface OwnerAccountStatusRaw {
  ownerType?: number
  ownerId?: number
  hasAccount?: boolean
  accountId?: number
  electricAccountType?: number
  electricPricePlanId?: number
  warnPlanId?: number
  monthlyPayAmountText?: string
}

export interface OwnerCandidateMeterRaw {
  id?: number
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

export interface AccountOptionRaw {
  id?: number | string
  accountId?: number | string
  ownerId?: number | string
  name?: string
  ownerName?: string
  contactName?: string
  contactPhone?: string
  managerName?: string
  managerPhone?: string
}

export const getAccountPageRaw = (query: AccountPageQuery) => {
  return requestV1<ApiEnvelope<PageResultRaw<AccountRaw>>>({
    method: 'GET',
    url: '/accounts/page',
    params: {
      ownerNameLike: query.ownerName,
      electricAccountType: query.electricAccountType,
      pageNum: query.pageNum,
      pageSize: query.pageSize
    }
  })
}

export const getAccountOptionsRaw = (ownerNameLike?: string, limit = 10) => {
  return requestV1<ApiEnvelope<AccountOptionRaw[]>>({
    method: 'GET',
    url: '/accounts/options',
    params: {
      ownerNameLike,
      limit
    }
  })
}

export const getAccountDetailRaw = (id: number) => {
  return requestV1<ApiEnvelope<AccountRaw>>({
    method: 'GET',
    url: `/accounts/${id}`
  })
}

export const openAccountRaw = (data: OpenAccountPayload) => {
  return requestV1<ApiEnvelope<number>>({
    method: 'POST',
    url: '/accounts/open',
    data
  })
}

export const getOwnerAccountStatusRaw = (query: OwnerAccountStatusQuery) => {
  return requestV1<ApiEnvelope<OwnerAccountStatusRaw>>({
    method: 'GET',
    url: '/owner-accounts/status',
    params: {
      ownerType: query.ownerType,
      ownerId: query.ownerId
    }
  })
}

export const getOwnerCandidateMetersRaw = (query: OwnerCandidateMeterQuery) => {
  return requestV1<ApiEnvelope<OwnerCandidateMeterRaw[]>>({
    method: 'GET',
    url: '/owner-candidate-meters',
    params: {
      ownerType: query.ownerType,
      ownerId: query.ownerId,
      spaceNameLike: query.spaceNameLike
    }
  })
}

export const appendAccountMetersRaw = (id: number, data: AppendAccountMetersPayload) => {
  return requestV1<ApiEnvelope<void>>({
    method: 'POST',
    url: `/accounts/${id}/meters/open`,
    data
  })
}

export const getCancelRecordPageRaw = (query: CancelRecordQuery) => {
  return requestV1<ApiEnvelope<PageResultRaw<CancelRecordRaw>>>({
    method: 'GET',
    url: '/accounts/cancel/page',
    params: {
      ownerName: query.ownerName,
      cleanBalanceType: query.cleanBalanceType,
      pageNum: query.pageNum,
      pageSize: query.pageSize
    }
  })
}

export const getCancelDetailRaw = (cancelNo: string) => {
  return requestV1<ApiEnvelope<CancelDetailRaw>>({
    method: 'GET',
    url: `/accounts/cancel/${cancelNo}`
  })
}

export const cancelAccountRaw = (data: CancelAccountPayload) => {
  return requestV1<ApiEnvelope<CancelResultRaw>>({
    method: 'POST',
    url: '/accounts/cancel',
    data
  })
}
