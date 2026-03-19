import type { PageResult } from '@/types/http'

export interface DeviceOperationPageQuery {
  operateUserName?: string
  commandType?: number
  success?: boolean
  deviceType?: string
  deviceNo?: string
  deviceName?: string
  spaceName?: string
  pageNum: number
  pageSize: number
}

export interface DeviceOperationItem {
  id: number
  deviceNo: string
  deviceName: string
  deviceType: string
  deviceTypeName: string
  spaceName: string
  commandType?: number
  commandTypeName: string
  success?: boolean
  isRunning?: boolean
  successName: string
  executeTimes?: number
  maxExecuteTimes?: number
  operateUserName: string
  createTime: string
}

export type DeviceOperationPageResult = PageResult<DeviceOperationItem>

export interface DeviceOperationDetail {
  id: number
  deviceId?: number
  deviceIotId: string
  deviceNo: string
  deviceName: string
  deviceType: string
  deviceTypeName: string
  spaceName: string
  areaId?: number
  accountId?: number
  commandType?: number
  commandTypeName: string
  commandSource?: number
  commandSourceName: string
  commandData: string
  success?: boolean
  isRunning?: boolean
  successName: string
  successTime: string
  lastExecuteTime: string
  ensureSuccess?: boolean
  executeTimes?: number
  maxExecuteTimes?: number
  operateUserName: string
  createTime: string
  remark: string
}

export interface DeviceOperationExecuteRecord {
  id: number
  commandId?: number
  commandSource?: number
  commandSourceName: string
  success?: boolean
  successName: string
  reason: string
  runTime: string
}
