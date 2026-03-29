import { requestV1 } from '@/api/http'
import type { ApiEnvelope } from '@/types/http'

export interface ServiceRateRaw {
  defaultServiceRate?: number | string
}

export interface UpdateServiceRatePayload {
  defaultServiceRate: number
}

export interface EnergyTopUpDetailPayload {
  accountId: number
  balanceType: number
  ownerType: number
  ownerId: number
  ownerName: string
  electricAccountType: number
  meterId?: number
  meterType?: number
  meterName?: string
  deviceNo?: string
  spaceId?: number
}

export interface CreateEnergyTopUpOrderPayload {
  userId: number
  userPhone: string
  userRealName: string
  thirdPartyUserId: string
  orderAmount: number
  paymentChannel: string
  energyTopUp: EnergyTopUpDetailPayload
}

export interface OrderCreationResponseRaw {
  orderSn?: string
  orderType?: number
  paymentChannel?: string
  orderPayStopTime?: string
}

export interface OrderPageQueryRaw {
  orderType?: number
  orderStatus?: string
  orderSnLike?: string
  thirdPartySnLike?: string
  enterpriseNameLike?: string
  createStartTime?: string
  createEndTime?: string
  paymentChannel?: string
  pageNum: number
  pageSize: number
}

export interface MeterConsumePageQueryRaw {
  searchKey?: string
  spaceNameLike?: string
  beginTime?: string
  endTime?: string
  pageNum: number
  pageSize: number
}

export interface MeterConsumeItemRaw {
  id?: number
  meterId?: number
  meterName?: string
  deviceNo?: string
  spaceName?: string
  beginBalance?: number | string
  consumeAmount?: number | string
  endBalance?: number | string
  electricAccountType?: number
  electricAccountTypeText?: string
  meterType?: number
  meterTypeName?: string
  consumeTime?: string
}

export interface MeterConsumeDetailRaw {
  id?: number
  meterConsumeRecordId?: number
  consumeNo?: string
  accountId?: number
  meterId?: number
  meterName?: string
  deviceNo?: string
  spaceName?: string
  ownerId?: number
  ownerType?: number
  ownerName?: string
  beginBalance?: number | string
  consumeAmount?: number | string
  consumeAmountHigher?: number | string
  consumeAmountHigh?: number | string
  consumeAmountLow?: number | string
  consumeAmountLower?: number | string
  consumeAmountDeepLow?: number | string
  endBalance?: number | string
  stepStartValue?: number | string
  historyPowerOffset?: number | string
  stepRate?: number | string
  priceHigher?: number | string
  priceHigh?: number | string
  priceLow?: number | string
  priceLower?: number | string
  priceDeepLow?: number | string
  beginPower?: number | string
  endPower?: number | string
  consumePower?: number | string
  beginPowerHigher?: number | string
  beginPowerHigh?: number | string
  beginPowerLow?: number | string
  beginPowerLower?: number | string
  beginPowerDeepLow?: number | string
  endPowerHigher?: number | string
  endPowerHigh?: number | string
  endPowerLow?: number | string
  endPowerLower?: number | string
  endPowerDeepLow?: number | string
  consumePowerHigher?: number | string
  consumePowerHigh?: number | string
  consumePowerLow?: number | string
  consumePowerLower?: number | string
  consumePowerDeepLow?: number | string
  beginRecordTime?: string
  endRecordTime?: string
  consumeTime?: string
  processTime?: string
}

export interface AccountConsumePageQueryRaw {
  accountId?: number
  accountNameLike?: string
  consumeTimeStart?: string
  consumeTimeEnd?: string
  pageNum: number
  pageSize: number
}

export interface AccountConsumeItemRaw {
  id?: number
  accountId?: number
  ownerName?: string
  ownerType?: number
  ownerTypeName?: string
  consumeType?: number
  consumeTypeName?: string
  contactName?: string
  contactPhone?: string
  consumeNo?: string
  payAmount?: number | string
  beginBalance?: number | string
  endBalance?: number | string
  consumeTime?: string
  createTime?: string
}

export interface OrderItemRaw {
  orderSn?: string
  userId?: number
  userRealName?: string
  userPhone?: string
  thirdPartyUserId?: string
  thirdPartySn?: string
  meterName?: string
  deviceNo?: string
  accountId?: number
  ownerId?: number
  ownerType?: number
  ownerName?: string
  orderType?: number
  orderTypeName?: string
  orderAmount?: number | string
  currency?: string
  serviceRate?: number | string
  serviceAmount?: number | string
  userPayAmount?: number | string
  paymentChannel?: string
  paymentChannelName?: string
  orderStatus?: string
  orderCreateTime?: string
  orderPayStopTime?: string
  orderSuccessTime?: string
  remark?: string
  ticketNo?: string
  beginBalance?: number | string
  endBalance?: number | string
}

export interface OrderDetailRaw {
  orderSn?: string
  userId?: number
  userRealName?: string
  userPhone?: string
  thirdPartyUserId?: string
  thirdPartySn?: string
  meterName?: string
  deviceNo?: string
  accountId?: number
  ownerId?: number
  ownerType?: number
  ownerName?: string
  orderType?: number
  orderTypeName?: string
  orderAmount?: number | string
  currency?: string
  serviceRate?: number | string
  serviceAmount?: number | string
  userPayAmount?: number | string
  paymentChannel?: string
  paymentChannelName?: string
  orderStatus?: string
  orderCreateTime?: string
  orderPayStopTime?: string
  orderSuccessTime?: string
  remark?: string
  ticketNo?: string
  beginBalance?: number | string
  endBalance?: number | string
}

export interface PageResultRaw<T> {
  pageNum?: number
  pageSize?: number
  total?: number
  list?: T[]
}

export const getDefaultServiceRateRaw = () => {
  return requestV1<ApiEnvelope<ServiceRateRaw>>({
    method: 'GET',
    url: '/orders/service-rate/default'
  })
}

export const updateDefaultServiceRateRaw = (data: UpdateServiceRatePayload) => {
  return requestV1<ApiEnvelope<void>>({
    method: 'PUT',
    url: '/orders/service-rate/default',
    data
  })
}

export const createEnergyTopUpOrderRaw = (data: CreateEnergyTopUpOrderPayload) => {
  return requestV1<ApiEnvelope<OrderCreationResponseRaw>>({
    method: 'POST',
    url: '/orders/energy-top-up',
    data
  })
}

export const getOrderPageRaw = (params: OrderPageQueryRaw) => {
  return requestV1<ApiEnvelope<PageResultRaw<OrderItemRaw>>>({
    method: 'GET',
    url: '/orders',
    params: {
      orderType: params.orderType,
      orderStatus: params.orderStatus,
      orderSnLike: params.orderSnLike,
      thirdPartySnLike: params.thirdPartySnLike,
      enterpriseNameLike: params.enterpriseNameLike,
      createStartTime: params.createStartTime,
      createEndTime: params.createEndTime,
      paymentChannel: params.paymentChannel,
      pageNum: params.pageNum,
      pageSize: params.pageSize
    }
  })
}

export const getOrderDetailRaw = (orderSn: string) => {
  return requestV1<ApiEnvelope<OrderDetailRaw>>({
    method: 'GET',
    url: `/orders/${encodeURIComponent(orderSn)}`
  })
}

export const getMeterConsumePageRaw = (params: MeterConsumePageQueryRaw) => {
  return requestV1<ApiEnvelope<PageResultRaw<MeterConsumeItemRaw>>>({
    method: 'GET',
    url: '/finance/meter-billings',
    params: {
      searchKey: params.searchKey,
      spaceNameLike: params.spaceNameLike,
      beginTime: params.beginTime,
      endTime: params.endTime,
      pageNum: params.pageNum,
      pageSize: params.pageSize
    }
  })
}

export const getMeterConsumeDetailRaw = (id: number) => {
  return requestV1<ApiEnvelope<MeterConsumeDetailRaw>>({
    method: 'GET',
    url: `/finance/meter-billings/${id}`
  })
}

export const getAccountConsumePageRaw = (params: AccountConsumePageQueryRaw) => {
  return requestV1<ApiEnvelope<PageResultRaw<AccountConsumeItemRaw>>>({
    method: 'GET',
    url: '/finance/account-consumes',
    params: {
      accountId: params.accountId,
      accountNameLike: params.accountNameLike,
      consumeTimeStart: params.consumeTimeStart,
      consumeTimeEnd: params.consumeTimeEnd,
      pageNum: params.pageNum,
      pageSize: params.pageSize
    }
  })
}
