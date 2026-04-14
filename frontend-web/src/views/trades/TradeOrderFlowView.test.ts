import { flushPromises, mount } from '@vue/test-utils'
import TradeOrderFlowView from '@/views/trades/TradeOrderFlowView.vue'
import { fetchOrderDetail, fetchOrderPage } from '@/api/adapters/trade'
import { fetchEnumOptionsByKey } from '@/api/adapters/system'

vi.mock('@/api/adapters/trade', () => ({
  fetchOrderPage: vi.fn(),
  fetchOrderDetail: vi.fn()
}))

vi.mock('@/api/adapters/system', () => ({
  fetchEnumOptionsByKey: vi.fn()
}))

const mockedFetchOrderPage = vi.mocked(fetchOrderPage)
const mockedFetchOrderDetail = vi.mocked(fetchOrderDetail)
const mockedFetchEnumOptionsByKey = vi.mocked(fetchEnumOptionsByKey)

const mountComponent = () => {
  return mount(TradeOrderFlowView, {
    global: {
      directives: {
        menuPermission: {
          mounted() {},
          updated() {}
        }
      },
      stubs: {
        CommonPagination: {
          template: '<div data-test="pagination-stub" />'
        },
        UiTableStateOverlay: {
          props: ['loading', 'empty'],
          template:
            '<div data-test="table-overlay-stub" :data-loading="String(loading)" :data-empty="String(empty)" />'
        },
        OrderDetailModal: {
          props: ['modelValue', 'detail', 'loading', 'errorText'],
          template:
            '<div data-test="order-detail-modal-stub" :data-visible="String(modelValue)" :data-loading="String(loading)" :data-order-sn="detail?.orderSn || \'\'" :data-error="errorText || \'\'" />'
        }
      }
    }
  })
}

describe('TradeOrderFlowView', () => {
  beforeEach(() => {
    mockedFetchOrderPage.mockReset()
    mockedFetchOrderDetail.mockReset()
    mockedFetchEnumOptionsByKey.mockReset()
    mockedFetchEnumOptionsByKey.mockImplementation(async (key: string) => {
      if (key === 'orderType') {
        return [{ label: '电费充值', value: '1' }]
      }
      if (key === 'paymentChannel') {
        return [{ label: '现金支付', value: 'OFFLINE' }]
      }
      return []
    })
  })

  test('testLoadOrders_OnMounted_ShouldFetchPageAndRenderRows', async () => {
    mockedFetchOrderPage.mockResolvedValue({
      total: 1,
      pageNum: 1,
      pageSize: 10,
      list: [
        {
          orderSn: 'ORDER-001',
          thirdPartySn: 'FLOW-001',
          ownerName: '测试企业A',
          orderType: 1,
          orderTypeName: '电费充值',
          orderAmount: '100.00',
          meterName: '1号表',
          deviceNo: 'EM-001',
          beginBalance: '50.00',
          endBalance: '150.00',
          serviceAmount: '2.00',
          serviceRate: '2',
          orderCreateTime: '2026-04-14 10:00:00',
          paymentChannel: 'OFFLINE',
          paymentChannelName: '现金支付',
          userRealName: '张三',
          orderStatus: 'SUCCESS'
        }
      ]
    })

    const wrapper = mountComponent()
    await flushPromises()

    expect(mockedFetchOrderPage).toHaveBeenCalledTimes(1)
    expect(mockedFetchOrderPage).toHaveBeenCalledWith(
      expect.objectContaining({
        pageNum: 1,
        pageSize: 10
      })
    )
    expect(wrapper.text()).toContain('ORDER-001')
    expect(wrapper.text()).toContain('测试企业A')
    expect(wrapper.text()).toContain('支付成功')
  })

  test('testOpenOrderDetail_WhenClickDetail_ShouldFetchDetailAndOpenModal', async () => {
    mockedFetchOrderPage.mockResolvedValue({
      total: 1,
      pageNum: 1,
      pageSize: 10,
      list: [
        {
          orderSn: 'ORDER-002',
          thirdPartySn: 'FLOW-002',
          ownerName: '测试企业B',
          orderType: 1,
          orderTypeName: '电费充值',
          orderAmount: '200.00',
          meterName: '2号表',
          deviceNo: 'EM-002',
          beginBalance: '80.00',
          endBalance: '280.00',
          serviceAmount: '4.00',
          serviceRate: '2',
          orderCreateTime: '2026-04-14 11:00:00',
          paymentChannel: 'OFFLINE',
          paymentChannelName: '现金支付',
          userRealName: '李四',
          orderStatus: 'SUCCESS'
        }
      ]
    })
    mockedFetchOrderDetail.mockResolvedValue({
      orderSn: 'ORDER-002',
      userRealName: '李四',
      userPhone: '13800000000',
      thirdPartyUserId: '1',
      thirdPartySn: 'FLOW-002',
      meterName: '2号表',
      deviceNo: 'EM-002',
      accountId: '88',
      ownerId: '1001',
      ownerType: '0',
      ownerName: '测试企业B',
      orderType: '1',
      orderTypeName: '电费充值',
      orderAmount: '200.00',
      currency: 'CNY',
      serviceRate: '2',
      serviceAmount: '4.00',
      userPayAmount: '200.00',
      paymentChannel: 'OFFLINE',
      paymentChannelName: '现金支付',
      orderStatus: 'SUCCESS',
      orderCreateTime: '2026-04-14 11:00:00',
      orderPayStopTime: '--',
      orderSuccessTime: '2026-04-14 11:01:00',
      remark: '--',
      ticketNo: '--',
      beginBalance: '80.00',
      endBalance: '280.00'
    })

    const wrapper = mountComponent()
    await flushPromises()

    await wrapper.get('.table-link').trigger('click')
    await flushPromises()

    expect(mockedFetchOrderDetail).toHaveBeenCalledWith('ORDER-002')
    const modal = wrapper.get('[data-test="order-detail-modal-stub"]')
    expect(modal.attributes('data-visible')).toBe('true')
    expect(modal.attributes('data-order-sn')).toBe('ORDER-002')
  })
})
