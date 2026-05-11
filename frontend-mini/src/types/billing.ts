import type { ListResponse } from './common'

export type BillMonthItem = {
  month: string
  monthLabel: string
  isCurrentMonth: boolean
  hasMonthlySummary: boolean
  monthEnergy?: number
  monthFee?: number
  tip?: string
}

export type BillMonthListResponse = ListResponse<BillMonthItem>

export type BillDayQuery = {
  month: string
}

export type BillDayItem = {
  date: string
  tipEnergy: number
  peakEnergy: number
  flatEnergy: number
  valleyEnergy: number
  deepValleyEnergy: number
  totalEnergy: number
  tipFee: number
  peakFee: number
  flatFee: number
  valleyFee: number
  deepValleyFee: number
  totalFee: number
}

export type BillDayListResponse = {
  month: string
  monthLabel: string
  tip?: string
  list: BillDayItem[]
}
