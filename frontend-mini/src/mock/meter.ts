import { mockDelay } from './index'
import type { MeterDetailResponse, MeterListItem, MeterListResponse, MeterTodayUsageResponse } from '@/types/meter'

const mockMeterList: MeterListItem[] = [
  {
    meterId: 101,
    meterName: '客厅电表',
    meterNo: '01234567890123456789',
    location: '1 单元 101 室',
    balance: 86.5,
    balanceText: '86.50',
    balanceSource: 'meter',
    isOnline: true
  },
  {
    meterId: 102,
    meterName: '卧室电表',
    meterNo: '98765432109876543210',
    location: '1 单元 202 室',
    balance: 120.3,
    balanceText: '120.30',
    balanceSource: 'meter',
    isOnline: true
  },
  {
    meterId: 201,
    meterName: '商铺电表',
    meterNo: '11223344556677889900',
    location: '2 单元 301 室',
    balance: 121.8,
    balanceText: '121.80',
    balanceSource: 'meter',
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
    balanceText: '86.50',
    balanceSource: 'meter',
    updateTime: '2026-05-12 09:41:00',
    sharpEnergy: 12.45,
    sharpEnergyText: '12.45',
    peakEnergy: 25.68,
    peakEnergyText: '25.68',
    flatEnergy: 38.76,
    flatEnergyText: '38.76',
    valleyEnergy: 7.89,
    valleyEnergyText: '7.89',
    deepValleyEnergy: 2.43,
    deepValleyEnergyText: '2.43',
    totalEnergy: 87.21,
    totalEnergyText: '87.21'
  },
  102: {
    meterId: 102,
    meterName: '卧室电表',
    meterNo: '98765432109876543210',
    location: '1 单元 202 室',
    isOnline: true,
    balance: 120.3,
    balanceText: '120.30',
    balanceSource: 'meter',
    updateTime: '2026-05-12 09:41:00',
    sharpEnergy: 5.24,
    sharpEnergyText: '5.24',
    peakEnergy: 12.68,
    peakEnergyText: '12.68',
    flatEnergy: 18.42,
    flatEnergyText: '18.42',
    valleyEnergy: 4.56,
    valleyEnergyText: '4.56',
    deepValleyEnergy: 1.28,
    deepValleyEnergyText: '1.28',
    totalEnergy: 42.18,
    totalEnergyText: '42.18'
  },
  201: {
    meterId: 201,
    meterName: '商铺电表',
    meterNo: '11223344556677889900',
    location: '2 单元 301 室',
    isOnline: false,
    balance: 121.8,
    balanceText: '121.80',
    balanceSource: 'meter',
    updateTime: '2026-05-12 09:41:00',
    sharpEnergy: 2.15,
    sharpEnergyText: '2.15',
    peakEnergy: 4.68,
    peakEnergyText: '4.68',
    flatEnergy: 7.12,
    flatEnergyText: '7.12',
    valleyEnergy: 2.06,
    valleyEnergyText: '2.06',
    deepValleyEnergy: 0.79,
    deepValleyEnergyText: '0.79',
    totalEnergy: 16.8,
    totalEnergyText: '16.80'
  }
}

const mockMeterTodayUsageMap: Record<number, MeterTodayUsageResponse> = {
  101: {
    meterId: 101,
    todayEnergy: 2.36,
    todayEnergyText: '2.36',
    todaySharpEnergy: 1234.56,
    todaySharpEnergyText: '1234.56',
    todayPeakEnergy: 2345.67,
    todayPeakEnergyText: '2345.67',
    todayFlatEnergy: 3456.78,
    todayFlatEnergyText: '3456.78',
    todayValleyEnergy: 4567.89,
    todayValleyEnergyText: '4567.89',
    todayDeepValleyEnergy: 5678.9,
    todayDeepValleyEnergyText: '5678.90',
    updateTime: '2026-05-12 09:41:00',
    updateClock: '09:41',
    trendList: [
      { timeLabel: '0', value: 0.06 },
      { timeLabel: '4', value: 0.48 },
      { timeLabel: '8', value: 1.72 },
      { timeLabel: '12', value: 2.36 },
      { timeLabel: '16', value: 2.36 },
      { timeLabel: '20', value: 2.36 },
      { timeLabel: '24', value: 2.36 }
    ]
  },
  102: {
    meterId: 102,
    todayEnergy: 1.64,
    todayEnergyText: '1.64',
    todaySharpEnergy: 0.18,
    todaySharpEnergyText: '0.18',
    todayPeakEnergy: 0.46,
    todayPeakEnergyText: '0.46',
    todayFlatEnergy: 0.72,
    todayFlatEnergyText: '0.72',
    todayValleyEnergy: 0.21,
    todayValleyEnergyText: '0.21',
    todayDeepValleyEnergy: 0.07,
    todayDeepValleyEnergyText: '0.07',
    updateTime: '2026-05-12 09:41:00',
    updateClock: '09:41',
    trendList: [
      { timeLabel: '0', value: 0.04 },
      { timeLabel: '4', value: 0.33 },
      { timeLabel: '8', value: 1.12 },
      { timeLabel: '12', value: 1.64 },
      { timeLabel: '16', value: 1.64 },
      { timeLabel: '20', value: 1.64 },
      { timeLabel: '24', value: 1.64 }
    ]
  },
  201: {
    meterId: 201,
    todayEnergy: 0.58,
    todayEnergyText: '0.58',
    todaySharpEnergy: 0.07,
    todaySharpEnergyText: '0.07',
    todayPeakEnergy: 0.16,
    todayPeakEnergyText: '0.16',
    todayFlatEnergy: 0.25,
    todayFlatEnergyText: '0.25',
    todayValleyEnergy: 0.07,
    todayValleyEnergyText: '0.07',
    todayDeepValleyEnergy: 0.03,
    todayDeepValleyEnergyText: '0.03',
    updateTime: '2026-05-12 09:41:00',
    updateClock: '09:41',
    trendList: [
      { timeLabel: '0', value: 0.02 },
      { timeLabel: '4', value: 0.13 },
      { timeLabel: '8', value: 0.41 },
      { timeLabel: '12', value: 0.58 },
      { timeLabel: '16', value: 0.58 },
      { timeLabel: '20', value: 0.58 },
      { timeLabel: '24', value: 0.58 }
    ]
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
    balanceSource: 'none',
    updateTime: '-',
    sharpEnergy: 0,
    peakEnergy: 0,
    flatEnergy: 0,
    valleyEnergy: 0,
    deepValleyEnergy: 0,
    totalEnergy: 0
  }
}

export const getMockMeterTodayUsage = async (meterId: number): Promise<MeterTodayUsageResponse> => {
  await mockDelay()

  return mockMeterTodayUsageMap[meterId] ?? {
    meterId,
    todayEnergy: 0,
    todaySharpEnergy: 0,
    todayPeakEnergy: 0,
    todayFlatEnergy: 0,
    todayValleyEnergy: 0,
    todayDeepValleyEnergy: 0,
    updateTime: '-',
    updateClock: '-',
    trendList: ['0', '4', '8', '12', '16', '20', '24'].map((timeLabel) => ({
      timeLabel,
      value: 0
    }))
  }
}

export const getMockEmptyMeterList = async (): Promise<MeterListResponse> => {
  await mockDelay()

  return {
    list: []
  }
}
