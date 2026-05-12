export type BillDateRange = 'all' | 'currentYear' | 'last6Months' | 'last3Months'

export type BillMonthListQuery = {
  range?: BillDateRange
}

export type BillMonthItem = {
  month: string
  monthLabel: string
  isCurrentMonth: boolean
  hasMonthlySummary: boolean
  monthEnergy?: number
  monthFee?: number
  tip?: string
}

export type BillMonthListResponse = {
  list: BillMonthItem[]
  totalEnergy: number
  totalFee: number
}

export type BillDayQuery = {
  month: string
}

export type BillDayItem = {
  date: string
  weekday: string
  sharpEnergy: number
  peakEnergy: number
  flatEnergy: number
  valleyEnergy: number
  deepValleyEnergy: number
  totalEnergy: number
  sharpFee: number
  peakFee: number
  flatFee: number
  valleyFee: number
  deepValleyFee: number
  totalFee: number
}

export type BillDayListResponse = {
  month: string
  monthLabel: string
  monthEnergy: number
  monthFee: number
  averageDailyEnergy: number
  averageDailyFee: number
  tip?: string
  list: BillDayItem[]
}
