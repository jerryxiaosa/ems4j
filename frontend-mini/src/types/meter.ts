import type { ListResponse } from './common'

export type MeterListItem = {
  meterId: string
  meterName: string
  balance: number
  isOnline?: boolean
}

export type MeterListResponse = ListResponse<MeterListItem>

export type MeterDetailResponse = {
  meterId: string
  meterName: string
  isOnline?: boolean
  balance: number
  tipEnergy: number
  peakEnergy: number
  flatEnergy: number
  valleyEnergy: number
  deepValleyEnergy: number
  totalEnergy: number
}
