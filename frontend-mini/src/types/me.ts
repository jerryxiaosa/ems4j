import type { ElectricAccountType } from './common'

export type CurrentMiniUserResponse = {
  userPhone: string
  electricAccountId: number
  electricAccountName: string
  electricAccountType: ElectricAccountType
  balance: number
  meterCount: number
}

export type MeProfileResponse = CurrentMiniUserResponse
