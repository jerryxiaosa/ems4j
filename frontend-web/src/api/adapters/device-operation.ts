import {
  getDeviceOperationDetailRaw,
  getDeviceOperationExecuteRecordListRaw,
  getDeviceOperationPageRaw,
  postDeviceOperationRetryRaw,
  type DeviceOperationEnumRaw,
  type DeviceOperationExecuteRecordRaw,
  type DeviceOperationRaw
} from '@/api/raw/device-operation'
import { normalizePageResult, unwrapEnvelope } from '@/api/raw/types'
import type {
  DeviceOperationDetail,
  DeviceOperationExecuteRecord,
  DeviceOperationItem,
  DeviceOperationPageQuery,
  DeviceOperationPageResult
} from '@/types/device-operation'

const DEFAULT_TEXT = '--'

const COMMAND_TYPE_NAME_MAP: Record<number, string> = {
  1: '电表充值自动合闸',
  2: '电表欠费自动断闸',
  3: '下发尖峰平谷时间段',
  4: '下发指定日期电价方案',
  5: '设置CT变比'
}

const COMMAND_SOURCE_NAME_MAP: Record<number, string> = {
  0: '系统命令',
  1: '用户命令'
}

const DEVICE_TYPE_NAME_MAP: Record<string, string> = {
  electricMeter: '电表',
  waterMeter: '水表',
  gateway: '网关'
}

const toNumber = (value: unknown): number | undefined => {
  if (typeof value === 'number' && Number.isFinite(value)) {
    return value
  }
  if (typeof value === 'string' && value.trim()) {
    const parsed = Number(value)
    if (Number.isFinite(parsed)) {
      return parsed
    }
  }
  return undefined
}

const toText = (value: unknown): string => {
  if (value === undefined || value === null) {
    return DEFAULT_TEXT
  }
  const source = String(value).trim()
  return source || DEFAULT_TEXT
}

const toBool = (value: unknown): boolean | undefined => {
  if (typeof value === 'boolean') {
    return value
  }
  if (typeof value === 'number') {
    if (value === 1) {
      return true
    }
    if (value === 0) {
      return false
    }
  }
  if (typeof value === 'string') {
    const source = value.trim().toLowerCase()
    if (['true', '1'].includes(source)) {
      return true
    }
    if (['false', '0'].includes(source)) {
      return false
    }
  }
  return undefined
}

const isEnumObject = (value: unknown): value is DeviceOperationEnumRaw => {
  return !!value && typeof value === 'object'
}

const resolveEnumCode = (value: unknown): number | undefined => {
  if (isEnumObject(value)) {
    return toNumber(value.code)
  }
  return toNumber(value)
}

const resolveEnumInfo = (value: unknown): string | undefined => {
  if (isEnumObject(value)) {
    const infoText = toText(value.info)
    return infoText === DEFAULT_TEXT ? undefined : infoText
  }
  return undefined
}

const resolveDeviceTypeKey = (value: unknown): string => {
  if (typeof value === 'string') {
    const key = value.trim()
    return key || DEFAULT_TEXT
  }
  if (isEnumObject(value) && typeof value.key === 'string' && value.key.trim()) {
    return value.key.trim()
  }
  return DEFAULT_TEXT
}

const resolveCommandTypeName = (raw: DeviceOperationRaw): string => {
  const explicit = toText(raw.commandTypeName)
  if (explicit !== DEFAULT_TEXT) {
    return explicit
  }

  const enumInfo = resolveEnumInfo(raw.commandType)
  if (enumInfo) {
    return enumInfo
  }

  const code = resolveEnumCode(raw.commandType)
  if (code !== undefined && COMMAND_TYPE_NAME_MAP[code]) {
    return COMMAND_TYPE_NAME_MAP[code]
  }

  return DEFAULT_TEXT
}

const resolveCommandSourceName = (
  commandSource: DeviceOperationExecuteRecordRaw['commandSource'] | DeviceOperationRaw['commandSource'],
  commandSourceName: string | undefined
): string => {
  const explicit = toText(commandSourceName)
  if (explicit !== DEFAULT_TEXT) {
    return explicit
  }

  const enumInfo = resolveEnumInfo(commandSource)
  if (enumInfo) {
    return enumInfo
  }

  const code = resolveEnumCode(commandSource)
  if (code !== undefined && COMMAND_SOURCE_NAME_MAP[code]) {
    return COMMAND_SOURCE_NAME_MAP[code]
  }

  return DEFAULT_TEXT
}

const resolveDeviceTypeName = (raw: DeviceOperationRaw): string => {
  const explicit = toText(raw.deviceTypeName)
  if (explicit !== DEFAULT_TEXT) {
    return explicit
  }

  const key = resolveDeviceTypeKey(raw.deviceType)
  if (key !== DEFAULT_TEXT && DEVICE_TYPE_NAME_MAP[key]) {
    return DEVICE_TYPE_NAME_MAP[key]
  }

  return key === DEFAULT_TEXT ? DEFAULT_TEXT : key
}

const resolveSuccessName = (success: boolean | undefined): string => {
  if (success === true) {
    return '成功'
  }
  if (success === false) {
    return '失败'
  }
  return '待执行'
}

const normalizeOperationItem = (raw: DeviceOperationRaw): DeviceOperationItem => {
  const success = toBool(raw.success)
  return {
    id: toNumber(raw.id) ?? 0,
    deviceNo: toText(raw.deviceNo),
    deviceName: toText(raw.deviceName),
    deviceType: resolveDeviceTypeKey(raw.deviceType),
    deviceTypeName: resolveDeviceTypeName(raw),
    spaceName: toText(raw.spaceName),
    commandType: resolveEnumCode(raw.commandType),
    commandTypeName: resolveCommandTypeName(raw),
    success,
    isRunning: toBool(raw.isRunning),
    successName: resolveSuccessName(success),
    executeTimes: toNumber(raw.executeTimes),
    maxExecuteTimes: toNumber(raw.maxExecuteTimes),
    operateUserName: toText(raw.operateUserName),
    createTime: toText(raw.createTime)
  }
}

const normalizeOperationDetail = (raw: DeviceOperationRaw): DeviceOperationDetail => {
  const success = toBool(raw.success)
  const lastExecuteTime = toText(raw.lastExecTime || raw.lastExecuteTime)

  return {
    id: toNumber(raw.id) ?? 0,
    deviceId: toNumber(raw.deviceId),
    deviceIotId: toText(raw.deviceIotId),
    deviceNo: toText(raw.deviceNo),
    deviceName: toText(raw.deviceName),
    deviceType: resolveDeviceTypeKey(raw.deviceType),
    deviceTypeName: resolveDeviceTypeName(raw),
    spaceName: toText(raw.spaceName),
    areaId: toNumber(raw.areaId),
    accountId: toNumber(raw.accountId),
    commandType: resolveEnumCode(raw.commandType),
    commandTypeName: resolveCommandTypeName(raw),
    commandSource: resolveEnumCode(raw.commandSource),
    commandSourceName: resolveCommandSourceName(raw.commandSource, raw.commandSourceName),
    commandData: toText(raw.commandData),
    success,
    isRunning: toBool(raw.isRunning),
    successName: resolveSuccessName(success),
    successTime: toText(raw.successTime),
    lastExecuteTime,
    ensureSuccess: toBool(raw.ensureSuccess),
    executeTimes: toNumber(raw.executeTimes),
    maxExecuteTimes: toNumber(raw.maxExecuteTimes),
    operateUserName: toText(raw.operateUserName),
    createTime: toText(raw.createTime),
    remark: toText(raw.remark)
  }
}

const normalizeExecuteRecord = (raw: DeviceOperationExecuteRecordRaw): DeviceOperationExecuteRecord => {
  const success = toBool(raw.success)
  return {
    id: toNumber(raw.id) ?? 0,
    commandId: toNumber(raw.commandId),
    commandSource: resolveEnumCode(raw.commandSource),
    commandSourceName: resolveCommandSourceName(raw.commandSource, raw.commandSourceName),
    success,
    successName: resolveSuccessName(success),
    reason: toText(raw.reason),
    runTime: toText(raw.runTime)
  }
}

export const fetchDeviceOperationPage = async (
  query: DeviceOperationPageQuery
): Promise<DeviceOperationPageResult> => {
  const payload = unwrapEnvelope(await getDeviceOperationPageRaw(query))
  const page = normalizePageResult<DeviceOperationRaw>(payload)
  return {
    ...page,
    list: page.list.map(normalizeOperationItem)
  }
}

export const fetchDeviceOperationDetail = async (id: number): Promise<DeviceOperationDetail> => {
  const payload = unwrapEnvelope<DeviceOperationRaw>(await getDeviceOperationDetailRaw(id)) || {}
  return normalizeOperationDetail(payload)
}

export const fetchDeviceOperationExecuteRecordList = async (
  id: number
): Promise<DeviceOperationExecuteRecord[]> => {
  const payload = unwrapEnvelope<DeviceOperationExecuteRecordRaw[]>(
    await getDeviceOperationExecuteRecordListRaw(id)
  )
  const sourceList = Array.isArray(payload) ? payload : []
  return sourceList.map(normalizeExecuteRecord)
}

export const retryDeviceOperation = async (id: number): Promise<void> => {
  unwrapEnvelope(await postDeviceOperationRetryRaw(id))
}
