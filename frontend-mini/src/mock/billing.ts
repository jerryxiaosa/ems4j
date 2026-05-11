import { mockDelay } from './index'
import type { BillDayItem, BillDayListResponse, BillDayQuery, BillMonthListResponse } from '@/types/billing'

const mockBillDayList: BillDayItem[] = [
  {
    date: '04-30',
    tipEnergy: 1.2,
    peakEnergy: 3.6,
    flatEnergy: 8.4,
    valleyEnergy: 5.3,
    deepValleyEnergy: 1.1,
    totalEnergy: 19.6,
    tipFee: 1.08,
    peakFee: 3.02,
    flatFee: 5.38,
    valleyFee: 2.44,
    deepValleyFee: 0.36,
    totalFee: 12.28
  },
  {
    date: '04-29',
    tipEnergy: 1.4,
    peakEnergy: 4.1,
    flatEnergy: 8.9,
    valleyEnergy: 5.8,
    deepValleyEnergy: 1.3,
    totalEnergy: 21.5,
    tipFee: 1.26,
    peakFee: 3.44,
    flatFee: 5.7,
    valleyFee: 2.67,
    deepValleyFee: 0.42,
    totalFee: 13.49
  }
]

export const getMockBillMonthList = async (): Promise<BillMonthListResponse> => {
  await mockDelay()

  return {
    list: [
      {
        month: '2026-05',
        monthLabel: '本月',
        isCurrentMonth: true,
        hasMonthlySummary: false,
        tip: '查看本月数据'
      },
      {
        month: '2026-04',
        monthLabel: '2026年04月',
        isCurrentMonth: false,
        hasMonthlySummary: true,
        monthEnergy: 286.4,
        monthFee: 173.8
      },
      {
        month: '2026-03',
        monthLabel: '2026年03月',
        isCurrentMonth: false,
        hasMonthlySummary: true,
        monthEnergy: 264.9,
        monthFee: 160.3
      }
    ]
  }
}

export const getMockBillDayList = async (query: BillDayQuery): Promise<BillDayListResponse> => {
  await mockDelay()

  if (query.month === '2026-05') {
    return {
      month: query.month,
      monthLabel: '本月',
      tip: '当月暂无记录',
      list: []
    }
  }

  return {
    month: query.month,
    monthLabel: query.month === '2026-04' ? '2026年04月' : '2026年03月',
    list: mockBillDayList
  }
}
