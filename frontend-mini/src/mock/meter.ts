import { mockDelay } from './index'
import type { MeterDetailResponse, MeterListItem, MeterListResponse } from '@/types/meter'

const mockMeterList: MeterListItem[] = [
  {
    meterId: 101,
    meterName: '客厅电表',
    meterNo: '01234567890123456789',
    location: '1 单元 101 室',
    balance: 86.5,
    isOnline: true
  },
  {
    meterId: 102,
    meterName: '卧室电表',
    meterNo: '98765432109876543210',
    location: '1 单元 202 室',
    balance: 120.3,
    isOnline: true
  },
  {
    meterId: 201,
    meterName: '商铺电表',
    meterNo: '11223344556677889900',
    location: '2 单元 301 室',
    balance: 121.8,
    isOnline: false
  }
]

const mockMeterDetailMap: Record<number, MeterDetailResponse> = {
  101: {
    meterId: 101,
    meterName: '客厅电表',
    meterNo: '01234567890123456789',
    location: '1 单元 101 室',
    isOnline: true,
    balance: 86.5,
    sharpEnergy: 12.45,
    peakEnergy: 25.68,
    flatEnergy: 38.76,
    valleyEnergy: 7.89,
    deepValleyEnergy: 2.43,
    totalEnergy: 87.21,
    todaySharpEnergy: 1234.56,
    todayPeakEnergy: 2345.67,
    todayFlatEnergy: 3456.78,
    todayValleyEnergy: 4567.89,
    todayDeepValleyEnergy: 5678.9,
    todayTotalEnergy: 2.36,
    todayUsageTrend: [0.06, 0.48, 1.72, 2.36, 2.36, 2.36, 2.36],
    updateTime: '2026-05-12 09:41:00'
  },
  102: {
    meterId: 102,
    meterName: '卧室电表',
    meterNo: '98765432109876543210',
    location: '1 单元 202 室',
    isOnline: true,
    balance: 120.3,
    sharpEnergy: 5.24,
    peakEnergy: 12.68,
    flatEnergy: 18.42,
    valleyEnergy: 4.56,
    deepValleyEnergy: 1.28,
    totalEnergy: 42.18,
    todaySharpEnergy: 0.18,
    todayPeakEnergy: 0.46,
    todayFlatEnergy: 0.72,
    todayValleyEnergy: 0.21,
    todayDeepValleyEnergy: 0.07,
    todayTotalEnergy: 1.64,
    todayUsageTrend: [0.04, 0.33, 1.12, 1.64, 1.64, 1.64, 1.64],
    updateTime: '2026-05-12 09:41:00'
  },
  201: {
    meterId: 201,
    meterName: '商铺电表',
    meterNo: '11223344556677889900',
    location: '2 单元 301 室',
    isOnline: false,
    balance: 121.8,
    sharpEnergy: 2.15,
    peakEnergy: 4.68,
    flatEnergy: 7.12,
    valleyEnergy: 2.06,
    deepValleyEnergy: 0.79,
    totalEnergy: 16.8,
    todaySharpEnergy: 0.07,
    todayPeakEnergy: 0.16,
    todayFlatEnergy: 0.25,
    todayValleyEnergy: 0.07,
    todayDeepValleyEnergy: 0.03,
    todayTotalEnergy: 0.58,
    todayUsageTrend: [0.02, 0.13, 0.41, 0.58, 0.58, 0.58, 0.58],
    updateTime: '2026-05-12 09:41:00'
  }
}

export const getMockMeterList = async (): Promise<MeterListResponse> => {
  await mockDelay()

  return {
    list: mockMeterList
  }
}

export const getMockMeterDetail = async (meterId: number): Promise<MeterDetailResponse> => {
  await mockDelay()

  return mockMeterDetailMap[meterId] ?? {
    meterId,
    meterName: '未知电表',
    meterNo: '-',
    location: '-',
    isOnline: false,
    balance: null,
    sharpEnergy: 0,
    peakEnergy: 0,
    flatEnergy: 0,
    valleyEnergy: 0,
    deepValleyEnergy: 0,
    totalEnergy: 0,
    todaySharpEnergy: 0,
    todayPeakEnergy: 0,
    todayFlatEnergy: 0,
    todayValleyEnergy: 0,
    todayDeepValleyEnergy: 0,
    todayTotalEnergy: 0,
    todayUsageTrend: [0, 0, 0, 0, 0, 0, 0],
    updateTime: '-'
  }
}

export const getMockEmptyMeterList = async (): Promise<MeterListResponse> => {
  await mockDelay()

  return {
    list: []
  }
}
