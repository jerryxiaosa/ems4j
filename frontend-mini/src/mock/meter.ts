import { mockDelay } from './index'
import type { MeterDetailResponse, MeterListItem, MeterListResponse } from '@/types/meter'

const mockMeterList: MeterListItem[] = [
  {
    meterId: 'meter-101',
    meterName: '1 单元 101 室',
    balance: 86.5,
    isOnline: true
  },
  {
    meterId: 'meter-102',
    meterName: '1 单元 102 室',
    balance: 120.3,
    isOnline: true
  },
  {
    meterId: 'meter-201',
    meterName: '2 单元 201 室',
    balance: 121.8,
    isOnline: false
  }
]

const mockMeterDetailMap: Record<string, MeterDetailResponse> = {
  'meter-101': {
    meterId: 'meter-101',
    meterName: '1 单元 101 室',
    isOnline: true,
    balance: 86.5,
    tipEnergy: 3.2,
    peakEnergy: 7.8,
    flatEnergy: 18.6,
    valleyEnergy: 9.4,
    deepValleyEnergy: 2.1,
    totalEnergy: 41.1
  },
  'meter-102': {
    meterId: 'meter-102',
    meterName: '1 单元 102 室',
    isOnline: true,
    balance: 120.3,
    tipEnergy: 2.9,
    peakEnergy: 6.9,
    flatEnergy: 17.2,
    valleyEnergy: 8.8,
    deepValleyEnergy: 1.8,
    totalEnergy: 37.6
  }
}

export const getMockMeterList = async (): Promise<MeterListResponse> => {
  await mockDelay()

  return {
    list: mockMeterList
  }
}

export const getMockMeterDetail = async (meterId: string): Promise<MeterDetailResponse> => {
  await mockDelay()

  return mockMeterDetailMap[meterId] ?? {
    meterId,
    meterName: '未知电表',
    isOnline: false,
    balance: 0,
    tipEnergy: 0,
    peakEnergy: 0,
    flatEnergy: 0,
    valleyEnergy: 0,
    deepValleyEnergy: 0,
    totalEnergy: 0
  }
}

export const getMockEmptyMeterList = async (): Promise<MeterListResponse> => {
  await mockDelay()

  return {
    list: []
  }
}
