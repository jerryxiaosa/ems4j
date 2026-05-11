import { mockDelay } from './index'
import type { HomeSummaryResponse, HomeTrendMetric, HomeTrendResponse } from '@/types/home'

export const getMockHomeSummary = async (): Promise<HomeSummaryResponse> => {
  await mockDelay()

  return {
    accountName: '星河家园 2 栋住户账',
    electricAccountType: 2,
    balance: 328.6,
    meterCount: 6,
    lastMonthEnergy: 286.4,
    lastMonthFee: 173.8,
    latestRechargeOrder: {
      orderSn: 'RC202605110001',
      amount: 200,
      arrivalAmount: 196,
      serviceFeeAmount: 4,
      status: 'PAID',
      statusName: '支付成功',
      createTime: '2026-05-10 18:24'
    }
  }
}

export const getMockHomeTrend = async (metric: HomeTrendMetric): Promise<HomeTrendResponse> => {
  await mockDelay()

  const energyValues = [18.2, 21.3, 19.8, 22.1, 20.4, 23.5, 24.2]
  const feeValues = [11.5, 13.6, 12.3, 14.2, 12.9, 15.1, 15.8]

  return {
    metric,
    unit: metric === 'energy' ? 'kWh' : '元',
    list: ['05-05', '05-06', '05-07', '05-08', '05-09', '05-10', '05-11'].map((date, index) => ({
      date,
      value: metric === 'energy' ? energyValues[index] : feeValues[index]
    }))
  }
}
