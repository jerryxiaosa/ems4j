import { requestV1 } from '@/api/http'
import type { ApiEnvelope } from '@/types/http'
import type {
  ElectricPricePlanQuery,
  ElectricPricePlanSavePayload
} from '@/types/electric-price-plan'
import type { WarnPlanQuery, WarnPlanSavePayload } from '@/types/plan'

export interface ElectricPricePlanRaw {
  id?: number | string
  name?: string
  priceHigher?: number | string
  priceHigh?: number | string
  priceLow?: number | string
  priceLower?: number | string
  priceDeepLow?: number | string
  isStep?: boolean
  stepPrice?: string
  isCustomPrice?: boolean
  priceHigherBase?: number | string
  priceHighBase?: number | string
  priceLowBase?: number | string
  priceLowerBase?: number | string
  priceDeepLowBase?: number | string
  priceHigherMultiply?: number | string
  priceHighMultiply?: number | string
  priceLowMultiply?: number | string
  priceLowerMultiply?: number | string
  priceDeepLowMultiply?: number | string
  createUser?: number | string
  createUserName?: string
  createTime?: string
  updateUser?: number | string
  updateUserName?: string
  updateTime?: string
}

export interface ElectricPriceTypeRaw {
  type?: number | string
  price?: number | string
}

export interface ElectricPriceTimeSettingRaw {
  type?: number | string
  start?: string
}

export interface StepPriceRaw {
  start?: number | string
  end?: number | string
  value?: number | string
}

export interface WarnPlanRaw {
  id?: number | string
  name?: string
  firstLevel?: number | string
  secondLevel?: number | string
  autoClose?: boolean
  remark?: string
  createUser?: number | string
  createUserName?: string
  createTime?: string
  updateUser?: number | string
  updateUserName?: string
  updateTime?: string
}

export const getElectricPricePlanListRaw = (query: ElectricPricePlanQuery = {}) => {
  return requestV1<ApiEnvelope<ElectricPricePlanRaw[]>>({
    method: 'GET',
    url: '/plan/electric-price-plans',
    params: {
      name: query.name
    }
  })
}

export const getElectricPricePlanDetailRaw = (id: number) => {
  return requestV1<ApiEnvelope<ElectricPricePlanRaw>>({
    method: 'GET',
    url: `/plan/electric-price-plans/${id}`
  })
}

export const addElectricPricePlanRaw = (data: ElectricPricePlanSavePayload) => {
  return requestV1<ApiEnvelope<number>>({
    method: 'POST',
    url: '/plan/electric-price-plans',
    data
  })
}

export const updateElectricPricePlanRaw = (id: number, data: ElectricPricePlanSavePayload) => {
  return requestV1<ApiEnvelope<void>>({
    method: 'PUT',
    url: `/plan/electric-price-plans/${id}`,
    data
  })
}

export const deleteElectricPricePlanRaw = (id: number) => {
  return requestV1<ApiEnvelope<void>>({
    method: 'DELETE',
    url: `/plan/electric-price-plans/${id}`
  })
}

export const getDefaultElectricPriceRaw = () => {
  return requestV1<ApiEnvelope<ElectricPriceTypeRaw[]>>({
    method: 'GET',
    url: '/plan/electric-price-plans/default/price'
  })
}

export const updateDefaultElectricPriceRaw = (data: ElectricPriceTypeRaw[]) => {
  return requestV1<ApiEnvelope<void>>({
    method: 'PUT',
    url: '/plan/electric-price-plans/default/price',
    data
  })
}

export const getDefaultElectricTimeRaw = () => {
  return requestV1<ApiEnvelope<ElectricPriceTimeSettingRaw[]>>({
    method: 'GET',
    url: '/plan/electric-price-plans/default/time'
  })
}

export const updateDefaultElectricTimeRaw = (data: ElectricPriceTimeSettingRaw[]) => {
  return requestV1<ApiEnvelope<void>>({
    method: 'PUT',
    url: '/plan/electric-price-plans/default/time',
    data
  })
}

export const getDefaultStepPriceRaw = () => {
  return requestV1<ApiEnvelope<StepPriceRaw[]>>({
    method: 'GET',
    url: '/plan/electric-price-plans/default/step-price'
  })
}

export const getWarnPlanListRaw = (query: WarnPlanQuery = {}) => {
  return requestV1<ApiEnvelope<WarnPlanRaw[]>>({
    method: 'GET',
    url: '/plan/warn-plans',
    params: {
      name: query.name
    }
  })
}

export const getWarnPlanDetailRaw = (id: number) => {
  return requestV1<ApiEnvelope<WarnPlanRaw>>({
    method: 'GET',
    url: `/plan/warn-plans/${id}`
  })
}

export const addWarnPlanRaw = (data: WarnPlanSavePayload) => {
  return requestV1<ApiEnvelope<number>>({
    method: 'POST',
    url: '/plan/warn-plans',
    data
  })
}

export const updateWarnPlanRaw = (id: number, data: WarnPlanSavePayload) => {
  return requestV1<ApiEnvelope<void>>({
    method: 'PUT',
    url: `/plan/warn-plans/${id}`,
    data
  })
}

export const deleteWarnPlanRaw = (id: number) => {
  return requestV1<ApiEnvelope<void>>({
    method: 'DELETE',
    url: `/plan/warn-plans/${id}`
  })
}
