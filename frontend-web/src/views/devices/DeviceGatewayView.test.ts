import { flushPromises, mount } from '@vue/test-utils'
import DeviceGatewayView from '@/views/devices/DeviceGatewayView.vue'

const { fetchGatewayPage } = vi.hoisted(() => ({
  fetchGatewayPage: vi.fn()
}))

vi.mock('@/api/adapters/device', () => ({
  fetchGatewayPage,
  fetchGatewayDetail: vi.fn(),
  createGateway: vi.fn(),
  updateGateway: vi.fn(),
  removeGateway: vi.fn()
}))

vi.mock('@/components/devices/gateway.mock', () => ({
  gatewayOnlineStatusOptions: []
}))

describe('DeviceGatewayView', () => {
  test('testEmptyState_WhenNoRows_ShouldRenderCenteredOverlay', async () => {
    fetchGatewayPage.mockResolvedValueOnce({
      list: [],
      total: 0,
      pageNum: 1,
      pageSize: 10
    })

    const wrapper = mount(DeviceGatewayView, {
      global: {
        directives: {
          menuPermission: () => undefined
        },
        stubs: {
          CommonPagination: {
            template: '<div data-test="pager" />'
          },
          UiTableStateOverlay: {
            props: ['loading', 'empty'],
            template:
              '<div v-if="loading" data-test="loading-overlay" /><div v-else-if="empty" class="table-empty-overlay"><div data-test="empty" /></div>'
          },
          DeviceGatewayDetailModal: {
            template: '<div />'
          },
          DeviceGatewayEditModal: {
            template: '<div />'
          }
        }
      }
    })

    await flushPromises()

    expect(wrapper.find('.table-empty-overlay').exists()).toBe(true)
    expect(wrapper.find('[data-test="empty"]').exists()).toBe(true)
  })
})
