import type { ElectricAccountType } from './common'

export type CurrentMiniUserResponse = {
  userPhone: string
  electricAccountId: string
  electricAccountName: string
  electricAccountType: ElectricAccountType
  balance: number
  meterCount: number
}

export type MeProfileResponse = CurrentMiniUserResponse
