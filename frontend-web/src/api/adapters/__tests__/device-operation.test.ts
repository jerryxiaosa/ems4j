import {
  fetchDeviceOperationDetail,
  fetchDeviceOperationExecuteRecordList,
  fetchDeviceOperationPage
} from '@/api/adapters/device-operation'
import {
  getDeviceOperationDetailRaw,
  getDeviceOperationExecuteRecordListRaw,
  getDeviceOperationPageRaw
} from '@/api/raw/device-operation'
import { SUCCESS_CODE } from '@/api/raw/types'

vi.mock('@/api/raw/device-operation', () => ({
  getDeviceOperationPageRaw: vi.fn(),
  getDeviceOperationDetailRaw: vi.fn(),
  getDeviceOperationExecuteRecordListRaw: vi.fn()
}))

const mockedGetDeviceOperationPageRaw = vi.mocked(getDeviceOperationPageRaw)
const mockedGetDeviceOperationDetailRaw = vi.mocked(getDeviceOperationDetailRaw)
const mockedGetDeviceOperationExecuteRecordListRaw = vi.mocked(getDeviceOperationExecuteRecordListRaw)

describe('device-operation adapter', () => {
  beforeEach(() => {
    mockedGetDeviceOperationPageRaw.mockReset()
    mockedGetDeviceOperationDetailRaw.mockReset()
    mockedGetDeviceOperationExecuteRecordListRaw.mockReset()
  })

  test('testFetchDeviceOperationPage_WhenRawPayloadReturned_ShouldNormalizePage', async () => {
    mockedGetDeviceOperationPageRaw.mockResolvedValue({
      success: true,
      code: SUCCESS_CODE,
      data: {
        pageNum: 2,
        pageSize: 20,
        total: 1,
        list: [
          {
            id: '10',
            deviceNo: ' EM-1001 ',
            deviceName: ' 一层主表 ',
            deviceType: 'electricMeter',
            spaceName: ' 一层配电房 ',
            commandType: 3,
            success: true,
            operateUserName: ' admin ',
            createTime: '2026-03-16 10:00:00'
          }
        ]
      }
    } as never)

    const result = await fetchDeviceOperationPage({
      pageNum: 2,
      pageSize: 20
    })

    expect(result).toEqual({
      pageNum: 2,
      pageSize: 20,
      total: 1,
      list: [
        {
          id: 10,
          deviceNo: 'EM-1001',
          deviceName: '一层主表',
          deviceType: 'electricMeter',
          deviceTypeName: '电表',
          spaceName: '一层配电房',
          commandType: 3,
          commandTypeName: '下发尖峰平谷时间段',
          success: true,
          successName: '成功',
          operateUserName: 'admin',
          createTime: '2026-03-16 10:00:00'
        }
      ]
    })
  })

  test('testFetchDeviceOperationDetail_WhenEnumObjectReturned_ShouldNormalizeDetail', async () => {
    mockedGetDeviceOperationDetailRaw.mockResolvedValue({
      success: true,
      code: SUCCESS_CODE,
      data: {
        id: 18,
        deviceId: 101,
        deviceIotId: 'iot-101',
        deviceNo: 'EM-101',
        deviceName: '主表',
        deviceType: {
          key: 'electricMeter'
        },
        commandType: {
          code: 5,
          info: '设置CT变比'
        },
        commandSource: {
          code: 1,
          info: '用户命令'
        },
        commandData: '{"ct":120}',
        success: false,
        lastExecTime: '2026-03-16 11:11:11',
        ensureSuccess: true,
        executeTimes: 3,
        operateUserName: 'Jerry',
        createTime: '2026-03-16 09:00:00'
      }
    } as never)

    const result = await fetchDeviceOperationDetail(18)

    expect(result).toMatchObject({
      id: 18,
      deviceId: 101,
      deviceIotId: 'iot-101',
      deviceNo: 'EM-101',
      deviceType: 'electricMeter',
      deviceTypeName: '电表',
      commandType: 5,
      commandTypeName: '设置CT变比',
      commandSource: 1,
      commandSourceName: '用户命令',
      commandData: '{"ct":120}',
      success: false,
      successName: '失败',
      lastExecuteTime: '2026-03-16 11:11:11',
      ensureSuccess: true,
      executeTimes: 3,
      operateUserName: 'Jerry'
    })
  })

  test('testFetchDeviceOperationExecuteRecordList_WhenRawPayloadReturned_ShouldNormalizeList', async () => {
    mockedGetDeviceOperationExecuteRecordListRaw.mockResolvedValue({
      success: true,
      code: SUCCESS_CODE,
      data: [
        {
          id: 1,
          commandId: 18,
          commandSource: 0,
          success: true,
          runTime: '2026-03-16 10:00:01'
        },
        {
          id: 2,
          commandId: 18,
          commandSource: 1,
          success: false,
          reason: '设备离线',
          runTime: '2026-03-16 10:05:01'
        }
      ]
    } as never)

    const result = await fetchDeviceOperationExecuteRecordList(18)

    expect(result).toEqual([
      {
        id: 1,
        commandId: 18,
        commandSource: 0,
        commandSourceName: '系统命令',
        success: true,
        successName: '成功',
        reason: '--',
        runTime: '2026-03-16 10:00:01'
      },
      {
        id: 2,
        commandId: 18,
        commandSource: 1,
        commandSourceName: '用户命令',
        success: false,
        successName: '失败',
        reason: '设备离线',
        runTime: '2026-03-16 10:05:01'
      }
    ])
  })
})
