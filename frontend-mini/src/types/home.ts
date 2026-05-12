import type { ElectricAccountType } from './common'

export type HomeLatestRechargeOrder = {
  orderSn: string
  payAmount: number
  payAmountText?: string
  topUpAmount?: number
  topUpAmountText?: string
  serviceFeeAmount?: number
  serviceFeeAmountText?: string
  status: string
  statusName: string
  createTime: string
}

export type HomeSummaryResponse = {
  electricAccountName: string
  electricAccountType: ElectricAccountType
  balance: number
  balanceText?: string
  meterCount: number
  lastMonthEnergy?: number
  lastMonthEnergyText?: string
  lastMonthFee?: number
  lastMonthFeeText?: string
  latestRechargeOrder?: HomeLatestRechargeOrder
}

export type HomeTrendMetric = 'fee' | 'energy'

export type HomeTrendQuery = {
  metric: HomeTrendMetric
}

export type HomeTrendItem = {
  date: string
  value: number
}

export type HomeTrendResponse = {
  metric: HomeTrendMetric
  unit: string
  list: HomeTrendItem[]
  tip?: string
}
