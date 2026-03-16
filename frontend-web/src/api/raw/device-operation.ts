import { requestV1 } from '@/api/http'
import type { ApiEnvelope } from '@/types/http'
import type { DeviceOperationPageQuery } from '@/types/device-operation'

export interface DeviceOperationEnumRaw {
  code?: number | string
  info?: string
  key?: string
}

export interface DeviceOperationRaw {
  id?: number | string
  deviceId?: number | string
  deviceIotId?: string
  deviceNo?: string
  deviceName?: string
  deviceType?: string | DeviceOperationEnumRaw
  deviceTypeName?: string
  spaceName?: string
  areaId?: number | string
  accountId?: number | string
  commandType?: number | string | DeviceOperationEnumRaw
  commandTypeName?: string
  commandSource?: number | string | DeviceOperationEnumRaw
  commandSourceName?: string
  commandData?: string
  success?: boolean
  successTime?: string
  lastExecTime?: string
  lastExecuteTime?: string
  ensureSuccess?: boolean
  executeTimes?: number | string
  operateUserName?: string
  createTime?: string
  remark?: string
}

export interface DeviceOperationExecuteRecordRaw {
  id?: number | string
  commandId?: number | string
  commandSource?: number | string | DeviceOperationEnumRaw
  commandSourceName?: string
  success?: boolean
  reason?: string
  runTime?: string
}

export interface DeviceOperationPageResultRaw<T> {
  pageNum?: number
  pageSize?: number
  total?: number
  list?: T[]
}

export const getDeviceOperationPageRaw = (query: DeviceOperationPageQuery) => {
  return requestV1<ApiEnvelope<DeviceOperationPageResultRaw<DeviceOperationRaw>>>({
    method: 'GET',
    url: '/device/operations/page',
    params: {
      operateUserName: query.operateUserName,
      commandType: query.commandType,
      success: query.success,
      deviceType: query.deviceType,
      deviceNo: query.deviceNo,
      deviceName: query.deviceName,
      spaceName: query.spaceName,
      pageNum: query.pageNum,
      pageSize: query.pageSize
    }
  })
}

export const getDeviceOperationDetailRaw = (id: number) => {
  return requestV1<ApiEnvelope<DeviceOperationRaw>>({
    method: 'GET',
    url: `/device/operations/${id}`
  })
}

export const getDeviceOperationExecuteRecordListRaw = (id: number) => {
  return requestV1<ApiEnvelope<DeviceOperationExecuteRecordRaw[]>>({
    method: 'GET',
    url: `/device/operations/${id}/execute-records`
  })
}
