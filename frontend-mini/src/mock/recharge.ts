import { mockDelay } from './index'
import type { RechargeInitResponse } from '@/types/recharge'

export const mockDemandRechargeInit: RechargeInitResponse = {
  electricAccountType: 0,
  accountName: '星河家园 2 栋住户账',
  accountBalance: 328.6,
  serviceFeeRate: 2,
  selectedMeterId: 'meter-101',
  meterOptionList: [
    {
      meterId: 'meter-101',
      meterName: '1 单元 101 室',
      meterNo: 'EM2026050101',
      meterBalance: 86.5
    },
    {
      meterId: 'meter-102',
      meterName: '1 单元 102 室',
      meterNo: 'EM2026050102',
      meterBalance: 120.3
    },
    {
      meterId: 'meter-201',
      meterName: '2 单元 201 室',
      meterNo: 'EM2026050201',
      meterBalance: 121.8
    }
  ]
}

export const mockMergedRechargeInit: RechargeInitResponse = {
  electricAccountType: 2,
  accountName: '星河家园 2 栋住户账',
  accountBalance: 328.6,
  serviceFeeRate: 2
}

export const getMockRechargeInit = async (): Promise<RechargeInitResponse> => {
  await mockDelay()

  return mockDemandRechargeInit
}
