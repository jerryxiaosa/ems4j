import { flushPromises, mount } from '@vue/test-utils'
import DeviceOperationDetailModal from '@/components/devices/DeviceOperationDetailModal.vue'
import { fetchDeviceOperationDetail, fetchDeviceOperationExecuteRecordList } from '@/api/adapters/device-operation'

vi.mock('@/api/adapters/device-operation', () => ({
  fetchDeviceOperationDetail: vi.fn(),
  fetchDeviceOperationExecuteRecordList: vi.fn(),
  retryDeviceOperation: vi.fn()
}))

const mockedFetchDeviceOperationDetail = vi.mocked(fetchDeviceOperationDetail)
const mockedFetchDeviceOperationExecuteRecordList = vi.mocked(fetchDeviceOperationExecuteRecordList)

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
})
