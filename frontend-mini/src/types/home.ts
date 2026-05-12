import type { ElectricAccountType } from './common'
import type { OrderRecordItem } from './order'

export type HomeSummaryResponse = {
  electricAccountName: string
  electricAccountType: ElectricAccountType
  balance: number
  meterCount: number
  lastMonthEnergy?: number
  lastMonthFee?: number
  latestRechargeOrder?: OrderRecordItem
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
