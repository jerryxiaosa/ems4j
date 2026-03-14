export interface RechargeMeterItem {
  id: number
  roomNo: string
  regionName: string
  meterName: string
  deviceNo: string
  spaceId?: number
  meterType?: number | string
  meterTypeName?: string
  balanceAmountText: string
  lastMonthAmountText: string
}

export interface RechargeEnterpriseDetail {
  id: number
  accountId?: number
  ownerId?: number
  ownerType?: number
  ownerTypeName?: string
  name: string
  contactName: string
  contactPhone: string
  electricAccountType?: number
  electricBalanceAmountText: string
  electricAccountTypeName: string
  meterCount: number
  meters: RechargeMeterItem[]
}

export const defaultServiceRate = 10
