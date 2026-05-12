import type { ListResponse } from './common'

export type MeterListItem = {
  meterId: number
  meterName: string
  meterNo: string
  location: string
  balance: number | null
  isOnline: boolean
}

export type MeterListResponse = ListResponse<MeterListItem>

export type MeterDetailResponse = {
  meterId: number
  meterName: string
  meterNo: string
  location: string
  isOnline: boolean
  balance: number | null
  sharpEnergy: number
  peakEnergy: number
  flatEnergy: number
  valleyEnergy: number
  deepValleyEnergy: number
  totalEnergy: number
  todaySharpEnergy: number
  todayPeakEnergy: number
  todayFlatEnergy: number
  todayValleyEnergy: number
  todayDeepValleyEnergy: number
  todayTotalEnergy: number
  todayUsageTrend: number[]
  updateTime: string
}
