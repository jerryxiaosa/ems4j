import { flushPromises, mount } from '@vue/test-utils'
import TradeConsumptionRecordsView from '@/views/trades/TradeConsumptionRecordsView.vue'
import {
  fetchAccountConsumePage,
  fetchMeterConsumeDetail,
  fetchMeterConsumePage
} from '@/api/adapters/trade'

vi.mock('@/api/adapters/trade', () => ({
  fetchMeterConsumePage: vi.fn(),
  fetchMeterConsumeDetail: vi.fn(),
  fetchAccountConsumePage: vi.fn()
}))

const mockedFetchMeterConsumePage = vi.mocked(fetchMeterConsumePage)
const mockedFetchMeterConsumeDetail = vi.mocked(fetchMeterConsumeDetail)
const mockedFetchAccountConsumePage = vi.mocked(fetchAccountConsumePage)

const mountComponent = () => {
  return mount(TradeConsumptionRecordsView, {
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
        MeterConsumeDetailModal: {
          props: ['modelValue', 'detail', 'loading', 'errorText'],
          template:
            '<div data-test="meter-detail-modal-stub" :data-visible="String(modelValue)" :data-loading="String(loading)" :data-consume-no="detail?.consumeNo || \'\'" :data-error="errorText || \'\'" />'
        }
      }
    }
  })
}

describe('TradeConsumptionRecordsView', () => {
  beforeEach(() => {
    mockedFetchMeterConsumePage.mockReset()
    mockedFetchMeterConsumeDetail.mockReset()
    mockedFetchAccountConsumePage.mockReset()
  })

  test('testLoadMeterRecords_OnMounted_ShouldFetchPageAndRenderRows', async () => {
    mockedFetchMeterConsumePage.mockResolvedValue({
      total: 1,
      pageNum: 1,
      pageSize: 10,
      list: [
        {
          id: 11,
          meterName: '1号表',
          deviceNo: 'EM-001',
          meterTypeName: '智能电表',
          spaceName: 'A-101',
          beginBalance: '100.00',
          consumeAmount: '6.88',
          endBalance: '93.12',
          electricAccountTypeText: '按需计费',
          consumeTime: '2026-04-14 08:30:00'
        }
      ]
    })
    mockedFetchAccountConsumePage.mockResolvedValue({
      total: 0,
      pageNum: 1,
      pageSize: 10,
      list: []
    })

    const wrapper = mountComponent()
    await flushPromises()

    expect(mockedFetchMeterConsumePage).toHaveBeenCalledTimes(1)
    expect(mockedFetchMeterConsumePage).toHaveBeenCalledWith(
      expect.objectContaining({
        pageNum: 1,
        pageSize: 10
      })
    )
    expect(wrapper.text()).toContain('1号表')
    expect(wrapper.text()).toContain('EM-001')
    expect(wrapper.text()).toContain('按需计费')
  })

  test('testLoadMonthlyRecords_WhenSwitchTab_ShouldFetchPageAndRenderRows', async () => {
    mockedFetchMeterConsumePage.mockResolvedValue({
      total: 0,
      pageNum: 1,
      pageSize: 10,
      list: []
    })
    mockedFetchAccountConsumePage.mockResolvedValue({
      total: 1,
      pageNum: 1,
      pageSize: 10,
      list: [
        {
          id: 21,
          ownerName: '测试企业A',
          ownerTypeName: '企业',
          contactName: '张三',
          contactPhone: '13800000000',
          consumeNo: 'MONTHLY-001',
          beginBalance: '300.00',
          payAmount: '88.00',
          endBalance: '212.00',
          consumeTypeName: '包月扣费',
          consumeTime: '2026-04-14 09:00:00',
          createTime: '2026-04-14 09:00:01'
        }
      ]
    })

    const wrapper = mountComponent()
    await flushPromises()

    await wrapper.findAll('.tab-btn')[1]!.trigger('click')
    await flushPromises()

    expect(mockedFetchAccountConsumePage).toHaveBeenCalledTimes(1)
    expect(mockedFetchAccountConsumePage).toHaveBeenCalledWith(
      expect.objectContaining({
        pageNum: 1,
        pageSize: 10
      })
    )
    expect(wrapper.text()).toContain('测试企业A')
    expect(wrapper.text()).toContain('包月扣费')
  })

  test('testOpenMeterDetail_WhenClickDetail_ShouldFetchDetailAndOpenModal', async () => {
    mockedFetchMeterConsumePage.mockResolvedValue({
      total: 1,
      pageNum: 1,
      pageSize: 10,
      list: [
        {
          id: 31,
          meterName: '2号表',
          deviceNo: 'EM-002',
          meterTypeName: '智能电表',
          spaceName: 'B-201',
          beginBalance: '200.00',
          consumeAmount: '12.00',
          endBalance: '188.00',
          electricAccountTypeText: '按需计费',
          consumeTime: '2026-04-14 10:00:00'
        }
      ]
    })
    mockedFetchMeterConsumeDetail.mockResolvedValue({
      id: 31,
      consumeNo: 'CONSUME-001',
      ownerName: '测试企业B',
      meterName: '2号表',
      deviceNo: 'EM-002',
      spaceName: 'B-201',
      consumeTime: '2026-04-14 10:00:00',
      processTime: '2026-04-14 10:00:05',
      beginBalance: '200.00',
      endBalance: '188.00',
      consumeAmount: '12.00',
      consumeAmountHigher: '1.00',
      consumeAmountHigh: '2.00',
      consumeAmountLow: '3.00',
      consumeAmountLower: '4.00',
      consumeAmountDeepLow: '2.00',
      beginRecordTime: '2026-04-14 09:55:00',
      endRecordTime: '2026-04-14 10:00:00',
      beginPower: '1000',
      endPower: '1012',
      endPowerHigher: '1001',
      endPowerHigh: '1003',
      endPowerLow: '1006',
      endPowerLower: '1010',
      endPowerDeepLow: '1012',
      consumePower: '12',
      consumePowerHigher: '1',
      consumePowerHigh: '2',
      consumePowerLow: '3',
      consumePowerLower: '4',
      consumePowerDeepLow: '2',
      stepStartValue: '0',
      historyPowerOffset: '0',
      stepRate: '1',
      priceHigher: '1.0000',
      priceHigh: '1.0000',
      priceLow: '1.0000',
      priceLower: '1.0000',
      priceDeepLow: '1.0000'
    })

    const wrapper = mountComponent()
    await flushPromises()

    await wrapper.get('.table-link').trigger('click')
    await flushPromises()

    expect(mockedFetchMeterConsumeDetail).toHaveBeenCalledWith(31)
    const modal = wrapper.get('[data-test="meter-detail-modal-stub"]')
    expect(modal.attributes('data-visible')).toBe('true')
    expect(modal.attributes('data-consume-no')).toBe('CONSUME-001')
  })
})
