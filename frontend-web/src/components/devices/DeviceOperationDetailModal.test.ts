import { flushPromises, mount } from '@vue/test-utils'
import DeviceOperationDetailModal from '@/components/devices/DeviceOperationDetailModal.vue'
import {
  fetchDeviceOperationDetail,
  fetchDeviceOperationExecuteRecordList,
  retryDeviceOperation
} from '@/api/adapters/device-operation'

vi.mock('@/api/adapters/device-operation', () => ({
  fetchDeviceOperationDetail: vi.fn(),
  fetchDeviceOperationExecuteRecordList: vi.fn(),
  retryDeviceOperation: vi.fn()
}))

const mockedFetchDeviceOperationDetail = vi.mocked(fetchDeviceOperationDetail)
const mockedFetchDeviceOperationExecuteRecordList = vi.mocked(fetchDeviceOperationExecuteRecordList)
const mockedRetryDeviceOperation = vi.mocked(retryDeviceOperation)

const mountComponent = () => {
  return mount(DeviceOperationDetailModal, {
    props: {
      modelValue: true,
      operationId: 18
    },
    global: {
      stubs: {
        UiEmptyState: {
          template: '<div data-test="empty-state-stub"><slot /></div>'
        },
        UiLoadingState: {
          template: '<div data-test="loading-state-stub"><slot /></div>'
        }
      }
    }
  })
}

describe('DeviceOperationDetailModal', () => {
  beforeEach(() => {
    mockedFetchDeviceOperationDetail.mockReset()
    mockedFetchDeviceOperationExecuteRecordList.mockReset()
    mockedRetryDeviceOperation.mockReset()
  })

  test('testRenderRetryButton_WhenOperationIsRetryable_ShouldRenderInlineActionInsteadOfHeaderButton', async () => {
    mockedFetchDeviceOperationDetail.mockResolvedValue({
      id: 18,
      deviceIotId: 'iot-18',
      deviceNo: 'EM-018',
      deviceName: '测试电表',
      deviceType: 'electricMeter',
      deviceTypeName: '电表',
      spaceName: 'A-101',
      commandType: 5,
      commandTypeName: '设置CT变比',
      commandSource: 1,
      commandSourceName: '用户命令',
      commandData: '{}',
      success: false,
      isRunning: false,
      successName: '失败',
      successTime: '--',
      lastExecuteTime: '2026-04-14 10:00:00',
      ensureSuccess: true,
      executeTimes: 1,
      maxExecuteTimes: 3,
      operateUserName: '张三',
      createTime: '2026-04-14 09:00:00',
      remark: '--'
    })
    mockedFetchDeviceOperationExecuteRecordList.mockResolvedValue([])

    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.get('.modal-actions').text()).not.toContain('重试')
    expect(wrapper.find('[data-test="detail-retry-button"]').exists()).toBe(true)
    expect(wrapper.get('[data-test="detail-retry-button"]').text()).toBe('重试')
  })

  test('testHideRetryButton_WhenOperationIsNotRetryable_ShouldNotRenderRetryAction', async () => {
    mockedFetchDeviceOperationDetail.mockResolvedValue({
      id: 19,
      deviceIotId: 'iot-19',
      deviceNo: 'GW-019',
      deviceName: '测试网关',
      deviceType: 'gateway',
      deviceTypeName: '网关',
      spaceName: 'A-102',
      commandType: 1,
      commandTypeName: '电表充值自动合闸',
      commandSource: 1,
      commandSourceName: '用户命令',
      commandData: '{}',
      success: true,
      isRunning: false,
      successName: '成功',
      successTime: '2026-04-14 11:00:00',
      lastExecuteTime: '2026-04-14 11:00:00',
      ensureSuccess: false,
      executeTimes: 1,
      maxExecuteTimes: 1,
      operateUserName: '李四',
      createTime: '2026-04-14 10:50:00',
      remark: '--'
    })
    mockedFetchDeviceOperationExecuteRecordList.mockResolvedValue([])

    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.find('[data-test="detail-retry-button"]').exists()).toBe(false)
  })

  test('testHandleRetry_WhenRetryFails_ShouldShowErrorNotice', async () => {
    mockedFetchDeviceOperationDetail.mockResolvedValue({
      id: 20,
      deviceIotId: 'iot-20',
      deviceNo: 'EM-020',
      deviceName: '测试电表',
      deviceType: 'electricMeter',
      deviceTypeName: '电表',
      spaceName: 'A-103',
      commandType: 5,
      commandTypeName: '设置CT变比',
      commandSource: 1,
      commandSourceName: '用户命令',
      commandData: '{}',
      success: false,
      isRunning: false,
      successName: '失败',
      successTime: '--',
      lastExecuteTime: '2026-04-14 12:00:00',
      ensureSuccess: true,
      executeTimes: 1,
      maxExecuteTimes: 3,
      operateUserName: '王五',
      createTime: '2026-04-14 11:50:00',
      remark: '--'
    })
    mockedFetchDeviceOperationExecuteRecordList.mockResolvedValue([])
    mockedRetryDeviceOperation.mockRejectedValue(new Error('设备操作重试失败'))

    const wrapper = mountComponent()
    await flushPromises()

    await wrapper.get('[data-test="detail-retry-button"]').trigger('click')
    await flushPromises()

    expect(mockedRetryDeviceOperation).toHaveBeenCalledWith(20)
    expect(wrapper.text()).toContain('设备操作重试失败')
  })

  test('testRenderCommandSection_WhenCommandDataIsLong_ShouldUseRemainingHeightLayout', async () => {
    mockedFetchDeviceOperationDetail.mockResolvedValue({
      id: 21,
      deviceIotId: 'iot-21',
      deviceNo: 'EM-021',
      deviceName: '测试电表',
      deviceType: 'electricMeter',
      deviceTypeName: '电表',
      spaceName: 'A-104',
      commandType: 5,
      commandTypeName: '设置CT变比',
      commandSource: 1,
      commandSourceName: '用户命令',
      commandData: '{"payload":"' + 'x'.repeat(4000) + '"}',
      success: false,
      isRunning: false,
      successName: '失败',
      successTime: '--',
      lastExecuteTime: '2026-04-14 13:00:00',
      ensureSuccess: true,
      executeTimes: 1,
      maxExecuteTimes: 3,
      operateUserName: '赵六',
      createTime: '2026-04-14 12:50:00',
      remark: '--'
    })
    mockedFetchDeviceOperationExecuteRecordList.mockResolvedValue([])

    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.find('.detail-layout').exists()).toBe(true)
    expect(wrapper.find('.section-card-command').exists()).toBe(true)
    expect(wrapper.find('.command-box-wrap').exists()).toBe(true)
  })
})
