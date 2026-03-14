import {
  appendAccountMetersRaw,
  cancelAccountRaw,
  getAccountOptionsRaw,
  getAccountDetailRaw,
  getAccountPageRaw,
  getCancelDetailRaw,
  getCancelRecordPageRaw,
  getOwnerAccountStatusRaw,
  getOwnerCandidateMetersRaw,
  openAccountRaw
} from '@/api/raw/account'
import { normalizePageResult, unwrapEnvelope } from '@/api/raw/types'
import type {
  AppendAccountMetersPayload,
  AccountItem,
  AccountMeter,
  AccountPageQuery,
  AccountPageResult,
  CancelAccountPayload,
  CancelAccountResult,
  CancelDetail,
  CancelRecordPageResult,
  CancelRecordQuery,
  CanceledMeterItem,
  OpenAccountPayload,
  OwnerAccountStatus,
  OwnerAccountStatusQuery,
  OwnerCandidateMeter,
  OwnerCandidateMeterQuery
} from '@/types/account'
import type {
  AccountRaw,
  AccountMeterRaw,
  CancelDetailRaw,
  CancelRecordRaw,
  CanceledMeterRaw,
  OwnerAccountStatusRaw,
  OwnerCandidateMeterRaw,
  PageResultRaw
} from '@/api/raw/account'
import type { OrganizationOption } from '@/api/adapters/organization'

const normalizeAccountMeter = (raw: AccountMeterRaw): AccountMeter => {
  return {
    id: raw.id,
    meterName: raw.meterName,
    deviceNo: raw.deviceNo,
    spaceId: raw.spaceId,
    spaceName: raw.spaceName,
    spaceParentNames: raw.spaceParentNames,
    offlineDurationText: raw.offlineDurationText,
    meterType: raw.meterType,
    meterTypeName: raw.meterTypeName,
    ct: raw.ct,
    meterBalanceAmountText: raw.meterBalanceAmountText,
    balance: raw.balance,
    pricePlanId: raw.pricePlanId,
    warnPlanId: raw.warnPlanId,
    warnType: raw.warnType,
    warnTypeName: raw.warnTypeName,
    isOnline: raw.isOnline
  }
}

const normalizeAccount = (raw: AccountRaw): AccountItem => {
  return {
    id: raw.id,
    ownerType: raw.ownerType,
    ownerTypeName: raw.ownerTypeName,
    ownerId: raw.ownerId,
    ownerName: raw.ownerName,
    contactName: raw.contactName,
    contactPhone: raw.contactPhone,
    electricAccountType: raw.electricAccountType,
    electricAccountTypeName: raw.electricAccountTypeName,
    electricPricePlanName: raw.electricPricePlanName,
    electricBalanceAmountText: raw.electricBalanceAmountText,
    openedMeterCount: raw.openedMeterCount,
    totalOpenableMeterCount: raw.totalOpenableMeterCount,
    monthlyPayAmount: raw.monthlyPayAmount,
    warnPlanId: raw.warnPlanId,
    warnPlanName: raw.warnPlanName,
    electricWarnType: raw.electricWarnType,
    meterList: (raw.meterList || []).map(normalizeAccountMeter)
  }
}

const normalizeCanceledMeter = (raw: CanceledMeterRaw): CanceledMeterItem => {
  return {
    spaceName: raw.spaceName,
    spaceParentNames: raw.spaceParentNames,
    meterName: raw.meterName,
    deviceNo: raw.deviceNo,
    meterType: raw.meterType,
    balance: raw.balance,
    power: raw.power,
    powerHigher: raw.powerHigher,
    powerHigh: raw.powerHigh,
    powerLow: raw.powerLow,
    powerLower: raw.powerLower,
    powerDeepLow: raw.powerDeepLow,
    showTime: raw.showTime,
    historyPowerTotal: raw.historyPowerTotal
  }
}

const normalizeCancelDetail = (raw: CancelDetailRaw): CancelDetail => {
  return {
    cancelNo: raw.cancelNo,
    ownerName: raw.ownerName,
    electricMeterAmount: raw.electricMeterAmount,
    cleanBalanceType: raw.cleanBalanceType,
    cleanBalanceAmountText: raw.cleanBalanceAmountText,
    cleanBalanceReal: raw.cleanBalanceReal,
    operatorName: raw.operatorName,
    cancelTime: raw.cancelTime,
    remark: raw.remark,
    meterList: (raw.meterList || []).map(normalizeCanceledMeter)
  }
}

const normalizeCancelRecord = (raw: CancelRecordRaw) => {
  return {
    cancelNo: raw.cancelNo,
    ownerName: raw.ownerName,
    electricMeterAmount: raw.electricMeterAmount,
    cleanBalanceType: raw.cleanBalanceType,
    cleanBalanceAmountText: raw.cleanBalanceAmountText,
    cleanBalanceReal: raw.cleanBalanceReal,
    operatorName: raw.operatorName,
    cancelTime: raw.cancelTime,
    remark: raw.remark
  }
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

const normalizeOwnerAccountStatus = (raw: OwnerAccountStatusRaw): OwnerAccountStatus => {
  return {
    ownerType: raw.ownerType,
    ownerId: raw.ownerId,
    hasAccount: raw.hasAccount,
    accountId: raw.accountId,
    electricAccountType: raw.electricAccountType,
    electricPricePlanId: raw.electricPricePlanId,
    warnPlanId: raw.warnPlanId,
    monthlyPayAmountText: raw.monthlyPayAmountText
  }
}

const normalizeOwnerCandidateMeter = (raw: OwnerCandidateMeterRaw): OwnerCandidateMeter | null => {
  const id = toNumber(raw.id)
  if (id === undefined) {
    return null
  }

  return {
    id,
    meterName: raw.meterName,
    deviceNo: raw.deviceNo,
    spaceId: raw.spaceId,
    spaceName: raw.spaceName,
    spaceParentNames: raw.spaceParentNames,
    isOnline: raw.isOnline,
    isPrepay: raw.isPrepay,
    meterType: raw.meterType,
    meterTypeName: raw.meterTypeName
  }
}

const normalizeAccountOption = (raw: {
  id?: number | string
  accountId?: number | string
  ownerId?: number | string
  name?: string
  ownerName?: string
  contactName?: string
  contactPhone?: string
  managerName?: string
  managerPhone?: string
}): OrganizationOption | null => {
  const id = toNumber(raw.id ?? raw.accountId ?? raw.ownerId)
  const name = (raw.ownerName || raw.name || '').trim()
  if (id === undefined || !name) {
    return null
  }

  return {
    id,
    name,
    managerName: (raw.contactName || raw.managerName || '').trim(),
    managerPhone: (raw.contactPhone || raw.managerPhone || '').trim()
  }
}

export const fetchAccountPage = async (query: AccountPageQuery): Promise<AccountPageResult> => {
  const payload = unwrapEnvelope<PageResultRaw<AccountRaw>>(await getAccountPageRaw(query))
  const page = normalizePageResult(payload)

  return {
    ...page,
    list: page.list.map(normalizeAccount)
  }
}

export const searchAccountOptions = async (keyword: string): Promise<OrganizationOption[]> => {
  const payload =
    unwrapEnvelope<
      {
        id?: number | string
        accountId?: number | string
        ownerId?: number | string
        name?: string
        ownerName?: string
        contactName?: string
        contactPhone?: string
        managerName?: string
        managerPhone?: string
      }[]
    >(await getAccountOptionsRaw(keyword.trim() || undefined)) || []

  return payload
    .map(normalizeAccountOption)
    .filter((item): item is OrganizationOption => item !== null)
}

export const fetchAccountDetail = async (id: number): Promise<AccountItem> => {
  const payload = unwrapEnvelope<AccountRaw>(await getAccountDetailRaw(id)) || {}
  return normalizeAccount(payload)
}

export const openAccount = async (data: OpenAccountPayload): Promise<number> => {
  const accountId = unwrapEnvelope<number>(await openAccountRaw(data))
  if (typeof accountId !== 'number') {
    throw new Error('开户成功但未返回账户 ID')
  }
  return accountId
}

export const fetchOwnerAccountStatus = async (
  query: OwnerAccountStatusQuery
): Promise<OwnerAccountStatus> => {
  const payload = unwrapEnvelope<OwnerAccountStatusRaw>(await getOwnerAccountStatusRaw(query)) || {}
  return normalizeOwnerAccountStatus(payload)
}

export const fetchOwnerCandidateMeters = async (
  query: OwnerCandidateMeterQuery
): Promise<OwnerCandidateMeter[]> => {
  const payload =
    unwrapEnvelope<OwnerCandidateMeterRaw[]>(await getOwnerCandidateMetersRaw(query)) || []
  return payload
    .map(normalizeOwnerCandidateMeter)
    .filter((item): item is OwnerCandidateMeter => item !== null)
}

export const appendAccountMeters = async (
  id: number,
  data: AppendAccountMetersPayload
): Promise<void> => {
  await unwrapEnvelope<void>(await appendAccountMetersRaw(id, data))
}

export const fetchCancelRecordPage = async (
  query: CancelRecordQuery
): Promise<CancelRecordPageResult> => {
  const payload = unwrapEnvelope<PageResultRaw<CancelRecordRaw>>(
    await getCancelRecordPageRaw(query)
  )
  const page = normalizePageResult(payload)
  return {
    ...page,
    list: page.list.map(normalizeCancelRecord)
  }
}

export const fetchCancelDetail = async (cancelNo: string): Promise<CancelDetail> => {
  const payload = unwrapEnvelope<CancelDetailRaw>(await getCancelDetailRaw(cancelNo)) || {}
  return normalizeCancelDetail(payload)
}

export const cancelAccount = async (data: CancelAccountPayload): Promise<CancelAccountResult> => {
  return unwrapEnvelope<CancelAccountResult>(await cancelAccountRaw(data)) || {}
}
