import { requestV1 } from '@/api/http'
import type { ApiEnvelope } from '@/types/http'

export interface ElectricBillReportQueryRaw {
  accountNameLike?: string
  startDate: string
  endDate: string
  pageNum: number
  pageSize: number
}

export interface ElectricBillReportPageItemRaw {
  accountId?: number
  accountName?: string
  electricAccountType?: number
  electricAccountTypeName?: string
  meterCount?: number
  periodConsumePowerText?: string
  periodElectricChargeAmountText?: string
  periodRechargeAmountText?: string
  periodCorrectionAmountText?: string
  totalDebitAmountText?: string
}

export interface ElectricBillReportAccountDetailRaw {
  accountId?: number
  accountName?: string
  contactName?: string | null
  contactPhone?: string | null
  electricAccountType?: number
  electricAccountTypeName?: string
  monthlyPayAmountText?: string | null
  accountBalanceText?: string | null
  meterCount?: number
  periodConsumePowerText?: string
  periodElectricChargeAmountText?: string
  periodRechargeAmountText?: string
  periodCorrectionAmountText?: string
  dateRangeText?: string
}

export interface ElectricBillReportMeterDetailRaw {
  meterId?: number
  deviceNo?: string | null
  meterName?: string | null
  consumePowerHigherText?: string | null
  consumePowerHighText?: string | null
  consumePowerLowText?: string | null
  consumePowerLowerText?: string | null
  consumePowerDeepLowText?: string | null
  displayPriceHigherText?: string | null
  displayPriceHighText?: string | null
  displayPriceLowText?: string | null
  displayPriceLowerText?: string | null
  displayPriceDeepLowText?: string | null
  electricChargeAmountHigherText?: string | null
  electricChargeAmountHighText?: string | null
  electricChargeAmountLowText?: string | null
  electricChargeAmountLowerText?: string | null
  electricChargeAmountDeepLowText?: string | null
  totalConsumePowerText?: string | null
  totalElectricChargeAmountText?: string | null
  totalRechargeAmountText?: string | null
  totalCorrectionAmountText?: string | null
}

export interface ElectricBillReportDetailRaw {
  accountInfo?: ElectricBillReportAccountDetailRaw
  meterList?: ElectricBillReportMeterDetailRaw[]
}

export interface PageResultRaw<T> {
  pageNum?: number
  pageSize?: number
  total?: number
  list?: T[]
}

export const getElectricBillReportPageRaw = (query: ElectricBillReportQueryRaw) => {
  return requestV1<ApiEnvelope<PageResultRaw<ElectricBillReportPageItemRaw>>>({
    method: 'GET',
    url: '/report/electric-bill/page',
    params: {
      accountNameLike: query.accountNameLike,
      startDate: query.startDate,
      endDate: query.endDate,
      pageNum: query.pageNum,
      pageSize: query.pageSize
    }
  })
}

export const getElectricBillReportDetailRaw = (
  accountId: number,
  query: Pick<ElectricBillReportQueryRaw, 'startDate' | 'endDate'>
) => {
  return requestV1<ApiEnvelope<ElectricBillReportDetailRaw>>({
    method: 'GET',
    url: `/report/electric-bill/${accountId}/detail`,
    params: {
      startDate: query.startDate,
      endDate: query.endDate
    }
  })
}
