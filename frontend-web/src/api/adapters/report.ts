import {
  getElectricBillReportDetailRaw,
  getElectricBillReportPageRaw
} from '@/api/raw/report'
import { normalizePageResult, unwrapEnvelope } from '@/api/raw/types'
import type {
  ElectricBillReportAccountInfo,
  ElectricBillReportDetailResult,
  ElectricBillReportMeterDetail,
  ElectricBillReportPageItem,
  ElectricBillReportPageResult,
  ElectricBillReportQuery
} from '@/types/report'
import type {
  ElectricBillReportAccountDetailRaw,
  ElectricBillReportDetailRaw,
  ElectricBillReportMeterDetailRaw,
  ElectricBillReportPageItemRaw,
  PageResultRaw
} from '@/api/raw/report'

const normalizePageItem = (raw: ElectricBillReportPageItemRaw): ElectricBillReportPageItem => {
  return {
    accountId: raw.accountId ?? 0,
    accountName: raw.accountName || '',
    electricAccountType: raw.electricAccountType ?? 0,
    electricAccountTypeName: raw.electricAccountTypeName || '',
    meterCount: raw.meterCount ?? 0,
    periodConsumePowerText: raw.periodConsumePowerText || '',
    periodElectricChargeAmountText: raw.periodElectricChargeAmountText || '',
    periodRechargeAmountText: raw.periodRechargeAmountText || '',
    periodCorrectionAmountText: raw.periodCorrectionAmountText || '',
    totalDebitAmountText: raw.totalDebitAmountText || ''
  }
}

const normalizeAccountInfo = (
  raw: ElectricBillReportAccountDetailRaw | undefined
): ElectricBillReportAccountInfo => {
  return {
    accountId: raw?.accountId ?? 0,
    accountName: raw?.accountName || '',
    contactName: raw?.contactName ?? null,
    contactPhone: raw?.contactPhone ?? null,
    electricAccountType: raw?.electricAccountType ?? 0,
    electricAccountTypeName: raw?.electricAccountTypeName || '',
    monthlyPayAmountText: raw?.monthlyPayAmountText ?? null,
    accountBalanceText: raw?.accountBalanceText ?? null,
    meterCount: raw?.meterCount ?? 0,
    periodConsumePowerText: raw?.periodConsumePowerText || '',
    periodElectricChargeAmountText: raw?.periodElectricChargeAmountText || '',
    periodRechargeAmountText: raw?.periodRechargeAmountText || '',
    periodCorrectionAmountText: raw?.periodCorrectionAmountText || '',
    dateRangeText: raw?.dateRangeText || ''
  }
}

const normalizeMeterDetail = (raw: ElectricBillReportMeterDetailRaw): ElectricBillReportMeterDetail => {
  return {
    meterId: raw.meterId ?? 0,
    deviceNo: raw.deviceNo ?? null,
    meterName: raw.meterName ?? null,
    consumePowerHigherText: raw.consumePowerHigherText ?? null,
    consumePowerHighText: raw.consumePowerHighText ?? null,
    consumePowerLowText: raw.consumePowerLowText ?? null,
    consumePowerLowerText: raw.consumePowerLowerText ?? null,
    consumePowerDeepLowText: raw.consumePowerDeepLowText ?? null,
    displayPriceHigherText: raw.displayPriceHigherText ?? null,
    displayPriceHighText: raw.displayPriceHighText ?? null,
    displayPriceLowText: raw.displayPriceLowText ?? null,
    displayPriceLowerText: raw.displayPriceLowerText ?? null,
    displayPriceDeepLowText: raw.displayPriceDeepLowText ?? null,
    electricChargeAmountHigherText: raw.electricChargeAmountHigherText ?? null,
    electricChargeAmountHighText: raw.electricChargeAmountHighText ?? null,
    electricChargeAmountLowText: raw.electricChargeAmountLowText ?? null,
    electricChargeAmountLowerText: raw.electricChargeAmountLowerText ?? null,
    electricChargeAmountDeepLowText: raw.electricChargeAmountDeepLowText ?? null,
    totalConsumePowerText: raw.totalConsumePowerText ?? null,
    totalElectricChargeAmountText: raw.totalElectricChargeAmountText ?? null,
    totalRechargeAmountText: raw.totalRechargeAmountText ?? null,
    totalCorrectionAmountText: raw.totalCorrectionAmountText ?? null
  }
}

export const fetchElectricBillReportPage = async (
  query: ElectricBillReportQuery
): Promise<ElectricBillReportPageResult> => {
  const payload = unwrapEnvelope<PageResultRaw<ElectricBillReportPageItemRaw>>(
    await getElectricBillReportPageRaw(query)
  )
  const pageResult = normalizePageResult(payload)
  return {
    total: pageResult.total,
    pageNum: pageResult.pageNum ?? query.pageNum,
    pageSize: pageResult.pageSize ?? query.pageSize,
    list: pageResult.list.map(normalizePageItem)
  }
}

export const fetchElectricBillReportDetail = async (
  accountId: number,
  query: Pick<ElectricBillReportQuery, 'startDate' | 'endDate'>
): Promise<ElectricBillReportDetailResult> => {
  const payload = unwrapEnvelope<ElectricBillReportDetailRaw>(
    await getElectricBillReportDetailRaw(accountId, query)
  )
  return {
    accountInfo: normalizeAccountInfo(payload?.accountInfo),
    meterList: (payload?.meterList || []).map(normalizeMeterDetail)
  }
}
