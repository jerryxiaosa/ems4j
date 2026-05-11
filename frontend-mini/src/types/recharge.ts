import type { ElectricAccountType } from './common'

export type RechargeMeterOption = {
  meterId: string
  meterName: string
  meterNo?: string
  meterBalance: number
}

export type RechargeInitResponse = {
  electricAccountType: ElectricAccountType
  accountName: string
  accountBalance: number
  serviceFeeRate: number
  selectedMeterId?: string
  meterOptionList?: RechargeMeterOption[]
}
