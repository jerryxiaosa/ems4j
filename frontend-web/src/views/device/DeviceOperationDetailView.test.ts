import { flushPromises, mount } from '@vue/test-utils'
import DeviceOperationDetailView from '@/views/device/DeviceOperationDetailView.vue'
import {
  fetchDeviceOperationDetail,
  fetchDeviceOperationExecuteRecordList
} from '@/api/adapters/device-operation'
import type {
  DeviceOperationDetail,
  DeviceOperationExecuteRecord
} from '@/types/device-operation'

const mockPush = vi.fn()

vi.mock('vue-router', () => ({
  useRoute: () => ({
    query: {
      id: '18'
    }
  }),
  useRouter: () => ({
    push: mockPush
  })
}))

vi.mock('@/api/adapters/device-operation', () => ({
  fetchDeviceOperationDetail: vi.fn(),
  fetchDeviceOperationExecuteRecordList: vi.fn()
}))

const mockedFetchDeviceOperationDetail = vi.mocked(fetchDeviceOperationDetail)
const mockedFetchDeviceOperationExecuteRecordList = vi.mocked(fetchDeviceOperationExecuteRecordList)

const createDetail = (overrides: Partial<DeviceOperationDetail> = {}): DeviceOperationDetail => ({
  id: 18,
  deviceId: 9,
  deviceIotId: 'iot-18',
  deviceNo: 'EM-001',
  deviceName: '1号电表',
  deviceType: 'electricMeter',
  deviceTypeName: '电表',
  spaceName: 'A-101',
  areaId: 6,
  accountId: 15,
  commandType: 1,
  commandTypeName: '合闸',
  commandSource: 1,
  commandSourceName: '平台下发',
  commandData: '{"switch":"on"}',
  success: true,
  successName: '成功',
  successTime: '2026-03-16 10:00:00',
  lastExecuteTime: '2026-03-16 10:01:00',
  ensureSuccess: true,
  executeTimes: 2,
  operateUserName: '张三',
  createTime: '2026-03-16 09:59:00',
  remark: '备注',
  ...overrides
})

const createExecuteRecord = (
  overrides: Partial<DeviceOperationExecuteRecord> = {}
): DeviceOperationExecuteRecord => ({
  id: 1,
  commandId: 18,
  commandSource: 1,
  commandSourceName: '平台下发',
  success: true,
  successName: '成功',
  reason: '--',
  runTime: '2026-03-16 10:00:00',
  ...overrides
})

const mountComponent = () =>
  mount(DeviceOperationDetailView, {
    global: {
      stubs: {
        UiEmptyState: {
          props: ['text'],
          template: '<div data-test="empty">{{ text || "empty" }}</div>'
        },
        UiLoadingState: {
          template: '<div data-test="loading">loading</div>'
        }
      }
    }
  })

describe('DeviceOperationDetailView', () => {
  test('testStatusAndExecuteRecordModal_WhenOpen_ShouldRenderSuccessTagAndLazyLoadRecords', async () => {
    mockedFetchDeviceOperationDetail.mockResolvedValue(createDetail())
    mockedFetchDeviceOperationExecuteRecordList.mockResolvedValue([createExecuteRecord()])

    const wrapper = mountComponent()
    await flushPromises()

    expect(mockedFetchDeviceOperationDetail).toHaveBeenCalledWith(18)
    expect(mockedFetchDeviceOperationExecuteRecordList).not.toHaveBeenCalled()
    expect(wrapper.get('[data-test="operation-status"]').classes()).toContain('detail-value-status')
    expect(wrapper.get('[data-test="operation-status"]').classes()).toContain(
      'detail-value-status-success'
    )
    expect(wrapper.get('[data-test="execute-times-button"]').classes()).toContain(
      'detail-value-button'
    )
    expect(wrapper.get('[data-test="execute-times-button"]').classes()).toContain(
      'detail-value-button-full'
    )
    expect(wrapper.get('[data-test="execute-times-button"]').text()).toBe('2次')
    expect(wrapper.find('[data-test="execute-record-modal"]').exists()).toBe(false)

    await wrapper.get('[data-test="execute-times-button"]').trigger('click')
    await flushPromises()

    expect(mockedFetchDeviceOperationExecuteRecordList).toHaveBeenCalledWith(18)
    expect(wrapper.get('[data-test="execute-record-modal"]').text()).toContain('执行/重试记录')
    expect(wrapper.get('[data-test="execute-record-modal"]').text()).toContain('平台下发')
  })

  test('testStatus_WhenFailed_ShouldRenderFailedTag', async () => {
    mockedFetchDeviceOperationDetail.mockResolvedValue(
      createDetail({
        success: false,
        successName: '失败'
      })
    )
    mockedFetchDeviceOperationExecuteRecordList.mockResolvedValue([])

    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.get('[data-test="operation-status"]').classes()).toContain(
      'detail-value-status-failed'
    )
  })

  test('testDetailFieldOrder_WhenRender_ShouldPlaceExecuteStatusAndRemarkAtExpectedPosition', async () => {
    mockedFetchDeviceOperationDetail.mockResolvedValue(createDetail())
    mockedFetchDeviceOperationExecuteRecordList.mockResolvedValue([])

    const wrapper = mountComponent()
    await flushPromises()

    const labels = wrapper
      .findAll('.detail-label')
      .map((item) => item.text())

    expect(labels.slice(-4)).toEqual(['执行状态', '操作状态', '执行次数', '备注'])
  })
})
