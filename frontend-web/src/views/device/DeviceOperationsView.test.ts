import { flushPromises, mount } from '@vue/test-utils'
import DeviceOperationsView from '@/views/device/DeviceOperationsView.vue'
import { fetchEnumOptionsByKey } from '@/api/adapters/system'
import { fetchDeviceOperationPage } from '@/api/adapters/device-operation'

vi.mock('@/api/adapters/system', () => ({
  fetchEnumOptionsByKey: vi.fn()
}))

vi.mock('@/api/adapters/device-operation', () => ({
  fetchDeviceOperationPage: vi.fn(),
  retryDeviceOperation: vi.fn()
}))

const mockedFetchEnumOptionsByKey = vi.mocked(fetchEnumOptionsByKey)
const mockedFetchDeviceOperationPage = vi.mocked(fetchDeviceOperationPage)

const mountComponent = () => {
  return mount(DeviceOperationsView, {
    global: {
      stubs: {
        CommonPagination: {
          template: '<div data-test="pagination-stub" />'
        },
        UiTableStateOverlay: {
          template: '<div data-test="table-overlay-stub" />'
        },
        DeviceOperationDetailModal: {
          props: ['modelValue', 'operationId'],
          template:
            '<div data-test="detail-modal-stub" :data-visible="String(modelValue)" :data-operation-id="String(operationId ?? \'\')"></div>'
        }
      }
    }
  })
}

describe('DeviceOperationsView', () => {
  test('testOpenDetail_WhenClickDetail_ShouldOpenDetailModal', async () => {
    mockedFetchEnumOptionsByKey.mockResolvedValue([])
    mockedFetchDeviceOperationPage.mockResolvedValue({
      list: [
        {
          id: 18,
          deviceNo: 'EM-001',
          deviceName: '1号电表',
          deviceType: 'electricMeter',
          deviceTypeName: '电表',
          spaceName: 'A-101',
          commandType: 1,
          commandTypeName: '合闸',
          success: false,
          ensureSuccess: true,
          successName: '失败',
          operateUserName: '张三',
          createTime: '2026-04-13 10:00:00'
        }
      ],
      total: 1,
      pageNum: 1,
      pageSize: 10
    })

    const wrapper = mountComponent()
    await flushPromises()

    await wrapper.get('.btn-link').trigger('click')
    await flushPromises()

    const modal = wrapper.get('[data-test="detail-modal-stub"]')
    expect(modal.attributes('data-visible')).toBe('true')
    expect(modal.attributes('data-operation-id')).toBe('18')
  })

  test('testRenderRetryAction_WhenRowIsNotRetryable_ShouldOnlyShowRetryForRetryableRow', async () => {
    mockedFetchEnumOptionsByKey.mockResolvedValue([])
    mockedFetchDeviceOperationPage.mockResolvedValue({
      list: [
        {
          id: 11,
          deviceNo: 'EM-001',
          deviceName: '1号电表',
          deviceType: 'electricMeter',
          deviceTypeName: '电表',
          spaceName: 'A-101',
          commandType: 1,
          commandTypeName: '合闸',
          success: true,
          ensureSuccess: true,
          successName: '成功',
          operateUserName: '张三',
          createTime: '2026-04-13 10:00:00'
        },
        {
          id: 12,
          deviceNo: 'EM-002',
          deviceName: '2号电表',
          deviceType: 'electricMeter',
          deviceTypeName: '电表',
          spaceName: 'A-102',
          commandType: 2,
          commandTypeName: '断闸',
          success: false,
          ensureSuccess: false,
          successName: '失败',
          operateUserName: '李四',
          createTime: '2026-04-13 11:00:00'
        },
        {
          id: 13,
          deviceNo: 'EM-003',
          deviceName: '3号电表',
          deviceType: 'electricMeter',
          deviceTypeName: '电表',
          spaceName: 'A-103',
          commandType: 5,
          commandTypeName: '设置CT变比',
          success: false,
          ensureSuccess: true,
          successName: '失败',
          operateUserName: '王五',
          createTime: '2026-04-13 12:00:00'
        }
      ],
      total: 3,
      pageNum: 1,
      pageSize: 10
    })

    const wrapper = mountComponent()
    await flushPromises()

    const actionButtons = wrapper.findAll('.action-buttons .btn-link')
    const actionTexts = actionButtons.map((button) => button.text())

    expect(actionTexts.filter((text) => text === '详情')).toHaveLength(3)
    expect(actionTexts.filter((text) => text === '重试')).toHaveLength(1)
    expect(actionTexts).not.toContain('已成功')
    expect(actionTexts).not.toContain('不可重试')
  })
})
