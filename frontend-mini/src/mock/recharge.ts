import { mockDelay } from './index'
import type { RechargeInitResponse } from '@/types/recharge'

export const mockDemandRechargeInit: RechargeInitResponse = {
  electricAccountType: 0,
  electricAccountName: '星河家园 2 栋住户账',
  accountBalance: 328.6,
  serviceFeeRate: 0.02,
  selectedMeterId: 101,
  meterOptionList: [
    {
      meterId: 101,
      meterName: '客厅电表',
      location: '1 单元 101 室',
      meterNo: '01234567890123456789',
      meterBalance: 86.5
    },
    {
      meterId: 102,
      meterName: '卧室电表',
      location: '1 单元 102 室',
      meterNo: '01234567890123456790',
      meterBalance: 120.3
    },
    {
      meterId: 201,
      meterName: '商铺电表',
      location: '2 单元 201 室',
      meterNo: '01234567890123456791',
      meterBalance: 121.8
    }
  ]
}

export const mockMergedRechargeInit: RechargeInitResponse = {
  electricAccountType: 2,
  electricAccountName: '星河家园 2 栋住户账',
  accountBalance: 328.6,
  serviceFeeRate: 0.02
}

export const getMockRechargeInit = async (): Promise<RechargeInitResponse> => {
  await mockDelay()

  return mockDemandRechargeInit
}
