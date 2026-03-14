import type {
  ElectricPriceStandardPrice,
  ElectricPricePlanFormValue,
  ElectricPricePlanItem
} from '@/types/electric-price-plan'

export const defaultStandardElectricPrices: ElectricPriceStandardPrice = {
  priceHigher: 5,
  priceHigh: 4,
  priceLow: 3,
  priceLower: 2,
  priceDeepLow: 1
}

export const electricPricePlanRows: ElectricPricePlanItem[] = [
  {
    id: 1,
    name: '默认电价方案',
    priceHigher: 1.25,
    priceHigh: 1.02,
    priceLow: 0.78,
    priceLower: 0.56,
    priceDeepLow: 0.42,
    ratioHigher: null,
    ratioHigh: null,
    ratioLow: null,
    ratioLower: null,
    ratioDeepLow: null,
    isCustomPrice: true,
    hasStepPrice: false,
    step1End: null,
    step1Ratio: null,
    step2End: null,
    step2Ratio: null,
    step3End: null,
    step3Ratio: null,
    createTime: '2026-03-01 09:20:00'
  },
  {
    id: 2,
    name: '园区峰谷方案',
    priceHigher: 1.32,
    priceHigh: 1.08,
    priceLow: 0.8,
    priceLower: 0.6,
    priceDeepLow: 0.48,
    ratioHigher: 0.264,
    ratioHigh: 0.27,
    ratioLow: 0.267,
    ratioLower: 0.3,
    ratioDeepLow: 0.48,
    isCustomPrice: false,
    hasStepPrice: true,
    step1End: 1000,
    step1Ratio: 1,
    step2End: 2000,
    step2Ratio: 1.2,
    step3End: null,
    step3Ratio: 1.3,
    createTime: '2026-03-02 11:15:00'
  }
]

export const createDefaultElectricPricePlanForm = (): ElectricPricePlanFormValue => ({
  name: '',
  isCustomPrice: 'false',
  priceHigher: '',
  priceHigh: '',
  priceLow: '',
  priceLower: '',
  priceDeepLow: '',
  ratioHigher: '',
  ratioHigh: '',
  ratioLow: '',
  ratioLower: '',
  ratioDeepLow: '',
  hasStepPrice: 'false',
  step1End: '',
  step1Ratio: '1',
  step2End: '',
  step2Ratio: '1.2',
  step3End: '',
  step3Ratio: '1.3'
})
