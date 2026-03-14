export interface ElectricPriceStandardPrice {
  priceHigher: number
  priceHigh: number
  priceLow: number
  priceLower: number
  priceDeepLow: number
}

export interface ElectricPriceTimeSettingItem {
  type: '' | 'higher' | 'high' | 'low' | 'lower' | 'deepLow'
  time: string
}

export interface ElectricPriceDefaultStepPrice {
  step1End: string
  step1Ratio: string
  step2End: string
  step2Ratio: string
  step3End: string
  step3Ratio: string
}

export interface ElectricPricePlanStepPricePayload {
  start: number
  end?: number
  value: number
}

export interface ElectricPricePlanItem {
  id: number
  name: string
  priceHigher: number
  priceHigh: number
  priceLow: number
  priceLower: number
  priceDeepLow: number
  ratioHigher?: number | null
  ratioHigh?: number | null
  ratioLow?: number | null
  ratioLower?: number | null
  ratioDeepLow?: number | null
  isCustomPrice: boolean
  hasStepPrice: boolean
  step1End?: number | null
  step1Ratio?: number | null
  step2End?: number | null
  step2Ratio?: number | null
  step3End?: number | null
  step3Ratio?: number | null
  createUser?: string
  createTime?: string
  updateUser?: string
  updateTime?: string
}

export interface ElectricPricePlanFormValue {
  id?: number
  name: string
  isCustomPrice: 'true' | 'false'
  priceHigher: string
  priceHigh: string
  priceLow: string
  priceLower: string
  priceDeepLow: string
  ratioHigher: string
  ratioHigh: string
  ratioLow: string
  ratioLower: string
  ratioDeepLow: string
  hasStepPrice: 'true' | 'false'
  step1End: string
  step1Ratio: string
  step2End: string
  step2Ratio: string
  step3End: string
  step3Ratio: string
}

export interface ElectricPricePlanQuery {
  name?: string
}

export interface ElectricPricePlanSavePayload {
  name: string
  priceHigher: number
  priceHigh: number
  priceLow: number
  priceLower: number
  priceDeepLow: number
  isStep: boolean
  stepPrices?: ElectricPricePlanStepPricePayload[]
  isCustomPrice: boolean
  priceHigherMultiply?: number
  priceHighMultiply?: number
  priceLowMultiply?: number
  priceLowerMultiply?: number
  priceDeepLowMultiply?: number
}
