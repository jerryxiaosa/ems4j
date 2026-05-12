import type { ListResponse } from './common'

export type MeterBalanceSource = 'meter' | 'account' | 'none'

export type MeterListItem = {
  meterId: number
  meterName: string
  meterNo?: string
  location?: string
  balance: number | null
  balanceText?: string
  balanceSource?: MeterBalanceSource
  isOnline?: boolean
}

export type MeterListResponse = ListResponse<MeterListItem>

export type MeterDetailResponse = {
  meterId: number
  meterName: string
  meterNo?: string
  location?: string
  isOnline?: boolean
  balance: number | null
  balanceText?: string
  balanceSource?: MeterBalanceSource
  updateTime?: string
  sharpEnergy: number
  sharpEnergyText?: string
  peakEnergy: number
  peakEnergyText?: string
  flatEnergy: number
  flatEnergyText?: string
  valleyEnergy: number
  valleyEnergyText?: string
  deepValleyEnergy: number
  deepValleyEnergyText?: string
  totalEnergy: number
  totalEnergyText?: string
}

export type MeterTodayUsageTrendItem = {
  timeLabel: string
  value: number
}

export type MeterTodayUsageResponse = {
  meterId: number
  todayEnergy: number
  todayEnergyText?: string
  todaySharpEnergy: number
  todaySharpEnergyText?: string
  todayPeakEnergy: number
  todayPeakEnergyText?: string
  todayFlatEnergy: number
  todayFlatEnergyText?: string
  todayValleyEnergy: number
  todayValleyEnergyText?: string
  todayDeepValleyEnergy: number
  todayDeepValleyEnergyText?: string
  updateTime?: string
  updateClock?: string
  trendList: MeterTodayUsageTrendItem[]
}
