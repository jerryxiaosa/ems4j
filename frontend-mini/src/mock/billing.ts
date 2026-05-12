import { mockDelay } from './index'
import type {
  BillDayItem,
  BillDayListResponse,
  BillDayQuery,
  BillMonthItem,
  BillMonthListQuery,
  BillMonthListResponse
} from '@/types/billing'

const mockBillDayList: BillDayItem[] = [
  {
    date: '04-30',
    weekday: '周四',
    sharpEnergy: 1.2,
    peakEnergy: 3.6,
    flatEnergy: 8.4,
    valleyEnergy: 5.3,
    deepValleyEnergy: 1.1,
    totalEnergy: 19.6,
    sharpFee: 1.08,
    peakFee: 3.02,
    flatFee: 5.38,
    valleyFee: 2.44,
    deepValleyFee: 0.36,
    totalFee: 12.28
  },
  {
    date: '04-29',
    weekday: '周三',
    sharpEnergy: 1.4,
    peakEnergy: 4.1,
    flatEnergy: 8.9,
    valleyEnergy: 5.8,
    deepValleyEnergy: 1.3,
    totalEnergy: 21.5,
    sharpFee: 1.26,
    peakFee: 3.44,
    flatFee: 5.7,
    valleyFee: 2.67,
    deepValleyFee: 0.42,
    totalFee: 13.49
  }
]

const allBillMonths: BillMonthItem[] = [
  {
    month: '2026-05',
    monthLabel: '2026年05月',
    isCurrentMonth: true,
    hasMonthlySummary: false,
    tip: '本月结算中'
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
  },
  {
    month: '2026-02',
    monthLabel: '2026年02月',
    isCurrentMonth: false,
    hasMonthlySummary: true,
    monthEnergy: 244.7,
    monthFee: 148.6
  }
]

const getFilteredBillMonths = (query?: BillMonthListQuery) => {
  if (query?.range === 'last3Months') {
    return allBillMonths.slice(0, 3)
  }

  return allBillMonths
}

export const getMockBillMonthList = async (query?: BillMonthListQuery): Promise<BillMonthListResponse> => {
  await mockDelay()

  const list = getFilteredBillMonths(query)
  const settledList = list.filter((item) => item.hasMonthlySummary)

  return {
    list,
    totalEnergy: Number(settledList.reduce((sum, item) => sum + (item.monthEnergy ?? 0), 0).toFixed(2)),
    totalFee: Number(settledList.reduce((sum, item) => sum + (item.monthFee ?? 0), 0).toFixed(2))
  }
}

export const getMockBillDayList = async (query: BillDayQuery): Promise<BillDayListResponse> => {
  await mockDelay()

  if (query.month === '2026-05') {
    return {
      month: query.month,
      monthLabel: '2026年05月',
      monthEnergy: null,
      monthFee: null,
      averageDailyEnergy: null,
      averageDailyFee: null,
      tip: '本月结算中',
      list: []
    }
  }

  const monthEnergy = Number(mockBillDayList.reduce((sum, item) => sum + item.totalEnergy, 0).toFixed(2))
  const monthFee = Number(mockBillDayList.reduce((sum, item) => sum + item.totalFee, 0).toFixed(2))

  return {
    month: query.month,
    monthLabel: query.month === '2026-04' ? '2026年04月' : '2026年03月',
    monthEnergy,
    monthFee,
    averageDailyEnergy: Number((monthEnergy / mockBillDayList.length).toFixed(2)),
    averageDailyFee: Number((monthFee / mockBillDayList.length).toFixed(2)),
    list: mockBillDayList
  }
}
