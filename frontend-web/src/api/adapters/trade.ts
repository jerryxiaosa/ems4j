import {
  getAccountConsumePageRaw,
  createEnergyTopUpOrderRaw,
  getDefaultServiceRateRaw,
  getMeterConsumeDetailRaw,
  getMeterConsumePageRaw,
  getOrderDetailRaw,
  getOrderPageRaw,
  updateDefaultServiceRateRaw
} from '@/api/raw/trade'
import { normalizePageResult, unwrapEnvelope } from '@/api/raw/types'
import type {
  AccountConsumeItemRaw,
  CreateEnergyTopUpOrderPayload,
  MeterConsumeDetailRaw,
  MeterConsumeItemRaw,
  OrderCreationResponseRaw,
  OrderDetailRaw,
  OrderItemRaw,
  PageResultRaw
} from '@/api/raw/trade'

const toNumber = (value: unknown): number | null => {
  if (typeof value === 'number' && Number.isFinite(value)) {
    return value
  }
  if (typeof value === 'string' && value.trim()) {
    const parsed = Number(value)
    if (Number.isFinite(parsed)) {
      return parsed
    }
  }
  return null
}

export const fetchDefaultServiceRate = async (): Promise<number | null> => {
  const payload = unwrapEnvelope<{ defaultServiceRate?: number | string }>(
    await getDefaultServiceRateRaw()
  )
  return toNumber(payload?.defaultServiceRate)
}

export const updateDefaultServiceRate = async (serviceRate: number): Promise<void> => {
  await unwrapEnvelope<void>(
    await updateDefaultServiceRateRaw({
      defaultServiceRate: serviceRate
    })
  )
}

export interface OrderCreationResult {
  orderSn?: string
  orderType?: number
  paymentChannel?: string
  orderPayStopTime?: string
}

export interface OrderPageQuery {
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

export interface OrderPageItem {
  orderSn: string
  thirdPartySn: string
  ownerName: string
  orderType?: number
  orderTypeName: string
  orderAmount?: number | string
  meterName: string
  deviceNo: string
  beginBalance?: number | string
  endBalance?: number | string
  serviceAmount?: number | string
  serviceRate?: number | string
  orderCreateTime: string
  paymentChannel?: string
  paymentChannelName: string
  userRealName: string
  orderStatus: string
}

export interface OrderPageResult {
  list: OrderPageItem[]
  total: number
  pageNum?: number
  pageSize?: number
}

export interface OrderDetail {
  orderSn: string
  userRealName: string
  userPhone: string
  thirdPartyUserId: string
  thirdPartySn: string
  meterName: string
  deviceNo: string
  accountId: string
  ownerId: string
  ownerType: string
  ownerName: string
  orderType: string
  orderTypeName: string
  orderAmount: string
  currency: string
  serviceRate: string
  serviceAmount: string
  userPayAmount: string
  paymentChannel: string
  paymentChannelName: string
  orderStatus: string
  orderCreateTime: string
  orderPayStopTime: string
  orderSuccessTime: string
  remark: string
  ticketNo: string
  beginBalance: string
  endBalance: string
}

export interface MeterConsumePageQuery {
  searchKey?: string
  spaceNameLike?: string
  beginTime?: string
  endTime?: string
  pageNum: number
  pageSize: number
}

export interface MeterConsumePageItem {
  id: number
  meterId?: number
  meterName: string
  deviceNo: string
  spaceName: string
  beginBalance?: number | string
  consumeAmount?: number | string
  endBalance?: number | string
  electricAccountType?: number
  electricAccountTypeText: string
  meterType?: number
  meterTypeName: string
  consumeTime: string
}

export interface MeterConsumePageResult {
  list: MeterConsumePageItem[]
  total: number
  pageNum?: number
  pageSize?: number
}

export interface MeterConsumeDetail {
  id: number
  consumeNo: string
  ownerName: string
  meterName: string
  deviceNo: string
  spaceName: string
  consumeTime: string
  processTime: string
  beginBalance: string
  consumeAmount: string
  endBalance: string
  consumeAmountHigher: string
  consumeAmountHigh: string
  consumeAmountLow: string
  consumeAmountLower: string
  consumeAmountDeepLow: string
  beginRecordTime: string
  endRecordTime: string
  beginPower: string
  consumePower: string
  endPower: string
  endPowerHigher: string
  endPowerHigh: string
  endPowerLow: string
  endPowerLower: string
  endPowerDeepLow: string
  consumePowerHigher: string
  consumePowerHigh: string
  consumePowerLow: string
  consumePowerLower: string
  consumePowerDeepLow: string
  stepStartValue: string
  historyPowerOffset: string
  stepRate: string
  priceHigher: string
  priceHigh: string
  priceLow: string
  priceLower: string
  priceDeepLow: string
}

export interface AccountConsumePageQuery {
  accountId?: number
  accountNameLike?: string
  consumeTimeStart?: string
  consumeTimeEnd?: string
  pageNum: number
  pageSize: number
}

export interface AccountConsumePageItem {
  id: number
  accountId?: number
  ownerName: string
  ownerType?: number
  ownerTypeName: string
  consumeType?: number
  consumeTypeName: string
  contactName: string
  contactPhone: string
  consumeNo: string
  payAmount?: number | string
  beginBalance?: number | string
  endBalance?: number | string
  consumeTime: string
  createTime: string
}

export interface AccountConsumePageResult {
  list: AccountConsumePageItem[]
  total: number
  pageNum?: number
  pageSize?: number
}

const normalizeText = (value: unknown): string => {
  if (value === undefined || value === null) {
    return '--'
  }
  const text = String(value).trim()
  if (!text) {
    return '--'
  }
  return text
}

const normalizeNumberText = (value: unknown): string => {
  if (value === undefined || value === null) {
    return '--'
  }
  if (typeof value === 'number') {
    return Number.isFinite(value) ? String(value) : '--'
  }
  const text = String(value).trim()
  return text || '--'
}

const normalizePercentText = (value: unknown): string => {
  const parsed = toNumber(value)
  if (parsed === null) {
    return normalizeNumberText(value)
  }

  return (parsed * 100).toFixed(6).replace(/\.?0+$/, '')
}

const formatDateTime = (value: unknown): string => {
  const text = normalizeText(value)
  if (text === '--') {
    return text
  }

  if (!text.includes('T')) {
    return text.length >= 19 ? text.slice(0, 19) : text
  }

  const date = new Date(text)
  if (Number.isNaN(date.getTime())) {
    const fallback = text.replace('T', ' ')
    return fallback.length >= 19 ? fallback.slice(0, 19) : fallback
  }

  const pad = (num: number) => String(num).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(
    date.getHours()
  )}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}

const normalizeOrderItem = (raw: OrderItemRaw): OrderPageItem => {
  return {
    orderSn: normalizeText(raw.orderSn),
    thirdPartySn: normalizeText(raw.thirdPartySn),
    ownerName: normalizeText(raw.ownerName),
    orderType: raw.orderType,
    orderTypeName: normalizeText(raw.orderTypeName),
    orderAmount: raw.orderAmount,
    meterName: normalizeText(raw.meterName),
    deviceNo: normalizeText(raw.deviceNo),
    beginBalance: raw.beginBalance,
    endBalance: raw.endBalance,
    serviceAmount: raw.serviceAmount,
    serviceRate: normalizePercentText(raw.serviceRate),
    orderCreateTime: normalizeText(raw.orderCreateTime),
    paymentChannel: raw.paymentChannel,
    paymentChannelName: normalizeText(raw.paymentChannelName),
    userRealName: normalizeText(raw.userRealName),
    orderStatus: normalizeText(raw.orderStatus)
  }
}

const normalizeOrderDetail = (raw: OrderDetailRaw): OrderDetail => {
  return {
    orderSn: normalizeText(raw.orderSn),
    userRealName: normalizeText(raw.userRealName),
    userPhone: normalizeText(raw.userPhone),
    thirdPartyUserId: normalizeText(raw.thirdPartyUserId),
    thirdPartySn: normalizeText(raw.thirdPartySn),
    meterName: normalizeText(raw.meterName),
    deviceNo: normalizeText(raw.deviceNo),
    accountId: normalizeNumberText(raw.accountId),
    ownerId: normalizeNumberText(raw.ownerId),
    ownerType: normalizeNumberText(raw.ownerType),
    ownerName: normalizeText(raw.ownerName),
    orderType: normalizeNumberText(raw.orderType),
    orderTypeName: normalizeText(raw.orderTypeName),
    orderAmount: normalizeNumberText(raw.orderAmount),
    currency: normalizeText(raw.currency),
    serviceRate: normalizePercentText(raw.serviceRate),
    serviceAmount: normalizeNumberText(raw.serviceAmount),
    userPayAmount: normalizeNumberText(raw.userPayAmount),
    paymentChannel: normalizeText(raw.paymentChannel),
    paymentChannelName: normalizeText(raw.paymentChannelName),
    orderStatus: normalizeText(raw.orderStatus),
    orderCreateTime: formatDateTime(raw.orderCreateTime),
    orderPayStopTime: formatDateTime(raw.orderPayStopTime),
    orderSuccessTime: formatDateTime(raw.orderSuccessTime),
    remark: normalizeText(raw.remark),
    ticketNo: normalizeText(raw.ticketNo),
    beginBalance: normalizeNumberText(raw.beginBalance),
    endBalance: normalizeNumberText(raw.endBalance)
  }
}

const normalizeMeterConsumeDetail = (raw: MeterConsumeDetailRaw): MeterConsumeDetail => {
  return {
    id: typeof raw.id === 'number' ? raw.id : 0,
    consumeNo: normalizeText(raw.consumeNo),
    ownerName: normalizeText(raw.ownerName),
    meterName: normalizeText(raw.meterName),
    deviceNo: normalizeText(raw.deviceNo),
    spaceName: normalizeText(raw.spaceName),
    consumeTime: formatDateTime(raw.consumeTime),
    processTime: formatDateTime(raw.processTime ?? raw.consumeTime),
    beginBalance: normalizeNumberText(raw.beginBalance),
    consumeAmount: normalizeNumberText(raw.consumeAmount),
    endBalance: normalizeNumberText(raw.endBalance),
    consumeAmountHigher: normalizeNumberText(raw.consumeAmountHigher),
    consumeAmountHigh: normalizeNumberText(raw.consumeAmountHigh),
    consumeAmountLow: normalizeNumberText(raw.consumeAmountLow),
    consumeAmountLower: normalizeNumberText(raw.consumeAmountLower),
    consumeAmountDeepLow: normalizeNumberText(raw.consumeAmountDeepLow),
    beginRecordTime: formatDateTime(raw.beginRecordTime),
    endRecordTime: formatDateTime(raw.endRecordTime),
    beginPower: normalizeNumberText(raw.beginPower),
    consumePower: normalizeNumberText(raw.consumePower),
    endPower: normalizeNumberText(raw.endPower),
    endPowerHigher: normalizeNumberText(raw.endPowerHigher),
    endPowerHigh: normalizeNumberText(raw.endPowerHigh),
    endPowerLow: normalizeNumberText(raw.endPowerLow),
    endPowerLower: normalizeNumberText(raw.endPowerLower),
    endPowerDeepLow: normalizeNumberText(raw.endPowerDeepLow),
    consumePowerHigher: normalizeNumberText(raw.consumePowerHigher),
    consumePowerHigh: normalizeNumberText(raw.consumePowerHigh),
    consumePowerLow: normalizeNumberText(raw.consumePowerLow),
    consumePowerLower: normalizeNumberText(raw.consumePowerLower),
    consumePowerDeepLow: normalizeNumberText(raw.consumePowerDeepLow),
    stepStartValue: normalizeNumberText(raw.stepStartValue),
    historyPowerOffset: normalizeNumberText(raw.historyPowerOffset),
    stepRate: normalizeNumberText(raw.stepRate),
    priceHigher: normalizeNumberText(raw.priceHigher),
    priceHigh: normalizeNumberText(raw.priceHigh),
    priceLow: normalizeNumberText(raw.priceLow),
    priceLower: normalizeNumberText(raw.priceLower),
    priceDeepLow: normalizeNumberText(raw.priceDeepLow)
  }
}

const normalizeMeterConsumeItem = (raw: MeterConsumeItemRaw): MeterConsumePageItem => {
  return {
    id: typeof raw.id === 'number' ? raw.id : 0,
    meterId: raw.meterId,
    meterName: normalizeText(raw.meterName),
    deviceNo: normalizeText(raw.deviceNo),
    spaceName: normalizeText(raw.spaceName),
    beginBalance: raw.beginBalance,
    consumeAmount: raw.consumeAmount,
    endBalance: raw.endBalance,
    electricAccountType: raw.electricAccountType,
    electricAccountTypeText: normalizeText(raw.electricAccountTypeText),
    meterType: raw.meterType,
    meterTypeName: normalizeText(raw.meterTypeName),
    consumeTime: formatDateTime(raw.consumeTime)
  }
}

const normalizeAccountConsumeItem = (raw: AccountConsumeItemRaw): AccountConsumePageItem => {
  return {
    id: typeof raw.id === 'number' ? raw.id : 0,
    accountId: raw.accountId,
    ownerName: normalizeText(raw.ownerName),
    ownerType: raw.ownerType,
    ownerTypeName: normalizeText(raw.ownerTypeName),
    consumeType: raw.consumeType,
    consumeTypeName: normalizeText(raw.consumeTypeName),
    contactName: normalizeText(raw.contactName),
    contactPhone: normalizeText(raw.contactPhone),
    consumeNo: normalizeText(raw.consumeNo),
    payAmount: raw.payAmount,
    beginBalance: raw.beginBalance,
    endBalance: raw.endBalance,
    consumeTime: formatDateTime(raw.consumeTime),
    createTime: formatDateTime(raw.createTime)
  }
}

export const createEnergyTopUpOrder = async (
  payload: CreateEnergyTopUpOrderPayload
): Promise<OrderCreationResult> => {
  const data =
    unwrapEnvelope<OrderCreationResponseRaw>(await createEnergyTopUpOrderRaw(payload)) || {}
  return {
    orderSn: data.orderSn,
    orderType: data.orderType,
    paymentChannel: data.paymentChannel,
    orderPayStopTime: data.orderPayStopTime
  }
}

export const fetchOrderPage = async (query: OrderPageQuery): Promise<OrderPageResult> => {
  const payload = unwrapEnvelope<PageResultRaw<OrderItemRaw>>(
    await getOrderPageRaw({
      orderType: query.orderType,
      orderStatus: query.orderStatus,
      orderSnLike: query.orderSnLike,
      thirdPartySnLike: query.thirdPartySnLike,
      enterpriseNameLike: query.enterpriseNameLike,
      createStartTime: query.createStartTime,
      createEndTime: query.createEndTime,
      paymentChannel: query.paymentChannel,
      pageNum: query.pageNum,
      pageSize: query.pageSize
    })
  )

  const page = normalizePageResult(payload)
  return {
    ...page,
    list: page.list.map(normalizeOrderItem)
  }
}

export const fetchOrderDetail = async (orderSn: string): Promise<OrderDetail> => {
  const payload = unwrapEnvelope<OrderDetailRaw>(await getOrderDetailRaw(orderSn)) || {}
  return normalizeOrderDetail(payload)
}

export const fetchMeterConsumePage = async (
  query: MeterConsumePageQuery
): Promise<MeterConsumePageResult> => {
  const payload = unwrapEnvelope<PageResultRaw<MeterConsumeItemRaw>>(
    await getMeterConsumePageRaw({
      searchKey: query.searchKey,
      spaceNameLike: query.spaceNameLike,
      beginTime: query.beginTime,
      endTime: query.endTime,
      pageNum: query.pageNum,
      pageSize: query.pageSize
    })
  )

  const page = normalizePageResult(payload)
  return {
    ...page,
    list: page.list.map(normalizeMeterConsumeItem)
  }
}

export const fetchMeterConsumeDetail = async (id: number): Promise<MeterConsumeDetail> => {
  const payload = unwrapEnvelope<MeterConsumeDetailRaw>(await getMeterConsumeDetailRaw(id)) || {}
  return normalizeMeterConsumeDetail(payload)
}

export const fetchAccountConsumePage = async (
  query: AccountConsumePageQuery
): Promise<AccountConsumePageResult> => {
  const payload = unwrapEnvelope<PageResultRaw<AccountConsumeItemRaw>>(
    await getAccountConsumePageRaw({
      accountId: query.accountId,
      accountNameLike: query.accountNameLike,
      consumeTimeStart: query.consumeTimeStart,
      consumeTimeEnd: query.consumeTimeEnd,
      pageNum: query.pageNum,
      pageSize: query.pageSize
    })
  )

  const page = normalizePageResult(payload)
  return {
    ...page,
    list: page.list.map(normalizeAccountConsumeItem)
  }
}
