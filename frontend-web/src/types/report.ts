export interface ElectricBillReportQuery {
  accountNameLike?: string
  startDate: string
  endDate: string
  pageNum: number
  pageSize: number
}

export interface ElectricBillReportPageItem {
  accountId: number
  accountName: string
  electricAccountType: number
  electricAccountTypeName: string
  meterCount: number
  periodConsumePowerText: string
  periodElectricChargeAmountText: string
  periodRechargeAmountText: string
  periodCorrectionAmountText: string
  totalDebitAmountText: string
}

export interface ElectricBillReportPageResult {
  list: ElectricBillReportPageItem[]
  total: number
  pageNum: number
  pageSize: number
}

export interface ElectricBillReportAccountInfo {
  accountId: number
  accountName: string
  contactName: string | null
  contactPhone: string | null
  electricAccountType: number
  electricAccountTypeName: string
  monthlyPayAmountText: string | null
  accountBalanceText: string | null
  meterCount: number
  periodConsumePowerText: string
  periodElectricChargeAmountText: string
  periodRechargeAmountText: string
  periodCorrectionAmountText: string
  dateRangeText: string
}

export interface ElectricBillReportMeterDetail {
  meterId: number
  deviceNo: string | null
  meterName: string | null
  consumePowerHigherText: string | null
  consumePowerHighText: string | null
  consumePowerLowText: string | null
  consumePowerLowerText: string | null
  consumePowerDeepLowText: string | null
  displayPriceHigherText: string | null
  displayPriceHighText: string | null
  displayPriceLowText: string | null
  displayPriceLowerText: string | null
  displayPriceDeepLowText: string | null
  electricChargeAmountHigherText: string | null
  electricChargeAmountHighText: string | null
  electricChargeAmountLowText: string | null
  electricChargeAmountLowerText: string | null
  electricChargeAmountDeepLowText: string | null
  totalConsumePowerText: string | null
  totalElectricChargeAmountText: string | null
  totalRechargeAmountText: string | null
  totalCorrectionAmountText: string | null
}

export interface ElectricBillReportDetailResult {
  accountInfo: ElectricBillReportAccountInfo
  meterList: ElectricBillReportMeterDetail[]
}
