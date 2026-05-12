import type { ElectricAccountType } from './common'

export type RechargeMeterOption = {
  meterId: number
  meterName: string
  meterNo?: string
  location?: string
  meterBalance: number
}

export type RechargeInitResponse = {
  electricAccountType: ElectricAccountType
  electricAccountName: string
  accountBalance: number
  serviceFeeRate: number
  selectedMeterId?: number
  meterOptionList?: RechargeMeterOption[]
}
