import { flushPromises, mount } from '@vue/test-utils'
import ElectricPricePlanView from '@/views/plans/ElectricPricePlanView.vue'
import {
  fetchDefaultElectricPrices,
  fetchDefaultElectricTimes,
  fetchDefaultStepPrices,
  fetchElectricPricePlanDetail,
  fetchElectricPricePlanList
} from '@/api/adapters/plan'

vi.mock('@/api/adapters/plan', () => ({
  fetchElectricPricePlanList: vi.fn(),
  fetchElectricPricePlanDetail: vi.fn(),
  fetchDefaultElectricPrices: vi.fn(),
  fetchDefaultElectricTimes: vi.fn(),
  fetchDefaultStepPrices: vi.fn(),
  addElectricPricePlan: vi.fn(),
  updateElectricPricePlan: vi.fn(),
  deleteElectricPricePlan: vi.fn(),
  updateDefaultElectricPrices: vi.fn(),
  updateDefaultElectricTimes: vi.fn()
}))

const mockedFetchElectricPricePlanList = vi.mocked(fetchElectricPricePlanList)
const mockedFetchElectricPricePlanDetail = vi.mocked(fetchElectricPricePlanDetail)
const mockedFetchDefaultElectricPrices = vi.mocked(fetchDefaultElectricPrices)
const mockedFetchDefaultElectricTimes = vi.mocked(fetchDefaultElectricTimes)
const mockedFetchDefaultStepPrices = vi.mocked(fetchDefaultStepPrices)

const mountComponent = () => {
  return mount(ElectricPricePlanView, {
    global: {
      directives: {
        menuPermission: {
          mounted() {},
          updated() {}
        }
      },
      stubs: {
        UiTableStateOverlay: {
          props: ['loading', 'empty'],
          template:
            '<div data-test="table-overlay-stub" :data-loading="String(loading)" :data-empty="String(empty)" />'
        },
        ElectricPricePlanDetailModal: {
          props: ['modelValue', 'plan'],
          template:
            '<div data-test="electric-price-detail-modal-stub" :data-visible="String(modelValue)" :data-plan-name="plan?.name || \'\'" />'
        },
        ElectricPricePlanEditModal: {
          props: ['modelValue', 'mode', 'plan'],
          template:
            '<div data-test="electric-price-edit-modal-stub" :data-visible="String(modelValue)" :data-mode="mode" :data-plan-name="plan?.name || \'\'" />'
        },
        ElectricPricePlanStandardModal: {
          props: ['modelValue'],
          template:
            '<div data-test="electric-price-standard-modal-stub" :data-visible="String(modelValue)" />'
        },
        ElectricPricePlanTimeModal: {
          props: ['modelValue'],
          template:
            '<div data-test="electric-price-time-modal-stub" :data-visible="String(modelValue)" />'
        },
        Transition: {
          template: '<div><slot /></div>'
        }
      }
    }
  })
}

describe('ElectricPricePlanView', () => {
  beforeEach(() => {
    mockedFetchElectricPricePlanList.mockReset()
    mockedFetchElectricPricePlanDetail.mockReset()
    mockedFetchDefaultElectricPrices.mockReset()
    mockedFetchDefaultElectricTimes.mockReset()
    mockedFetchDefaultStepPrices.mockReset()

    mockedFetchDefaultElectricPrices.mockResolvedValue({
      priceHigher: 1.25,
      priceHigh: 1.02,
      priceLow: 0.78,
      priceLower: 0.56,
      priceDeepLow: 0.42
    })
    mockedFetchDefaultElectricTimes.mockResolvedValue([
      { type: 'lower', time: '00:00' },
      { type: 'low', time: '06:00' }
    ])
    mockedFetchDefaultStepPrices.mockResolvedValue({
      step1End: '',
      step1Ratio: '1',
      step2End: '',
      step2Ratio: '1.2',
      step3End: '',
      step3Ratio: '1.3'
    })
  })

  test('testLoadElectricPricePlans_OnMounted_ShouldFetchListAndRenderRows', async () => {
    mockedFetchElectricPricePlanList.mockResolvedValue([
      {
        id: 1,
        name: '默认电价方案',
        priceHigher: 1.25,
        priceHigh: 1.02,
        priceLow: 0.78,
        priceLower: 0.56,
        priceDeepLow: 0.42,
        isCustomPrice: true,
        hasStepPrice: false,
        createTime: '2026-04-14 09:00:00'
      }
    ])

    const wrapper = mountComponent()
    await flushPromises()

    expect(mockedFetchDefaultElectricPrices).toHaveBeenCalledTimes(1)
    expect(mockedFetchDefaultElectricTimes).toHaveBeenCalledTimes(1)
    expect(mockedFetchDefaultStepPrices).toHaveBeenCalledTimes(1)
    expect(mockedFetchElectricPricePlanList).toHaveBeenCalledTimes(1)
    expect(mockedFetchElectricPricePlanList).toHaveBeenCalledWith({})
    expect(wrapper.text()).toContain('默认电价方案')
    expect(wrapper.text()).toContain('1.25')
    expect(wrapper.text()).toContain('否')
  })

  test('testOpenDetailModal_WhenClickDetail_ShouldFetchDetailAndOpenModal', async () => {
    mockedFetchElectricPricePlanList.mockResolvedValue([
      {
        id: 2,
        name: '园区峰谷方案',
        priceHigher: 1.32,
        priceHigh: 1.08,
        priceLow: 0.8,
        priceLower: 0.6,
        priceDeepLow: 0.48,
        ratioHigher: 0.264,
        ratioHigh: 0.27,
        ratioLow: 0.267,
        ratioLower: 0.3,
        ratioDeepLow: 0.48,
        isCustomPrice: false,
        hasStepPrice: true,
        step1End: 1000,
        step1Ratio: 1,
        step2End: 2000,
        step2Ratio: 1.2,
        step3End: null,
        step3Ratio: 1.3,
        createTime: '2026-04-14 10:00:00'
      }
    ])
    mockedFetchElectricPricePlanDetail.mockResolvedValue({
      id: 2,
      name: '园区峰谷方案',
      priceHigher: 1.32,
      priceHigh: 1.08,
      priceLow: 0.8,
      priceLower: 0.6,
      priceDeepLow: 0.48,
      ratioHigher: 0.264,
      ratioHigh: 0.27,
      ratioLow: 0.267,
      ratioLower: 0.3,
      ratioDeepLow: 0.48,
      isCustomPrice: false,
      hasStepPrice: true,
      step1End: 1000,
      step1Ratio: 1,
      step2End: 2000,
      step2Ratio: 1.2,
      step3End: null,
      step3Ratio: 1.3,
      createUser: '管理员',
      createTime: '2026-04-14 10:00:00',
      updateUser: '管理员',
      updateTime: '2026-04-14 10:05:00'
    })

    const wrapper = mountComponent()
    await flushPromises()

    await wrapper.get('.btn-link').trigger('click')
    await flushPromises()

    expect(mockedFetchElectricPricePlanDetail).toHaveBeenCalledWith(2)
    const modal = wrapper.get('[data-test="electric-price-detail-modal-stub"]')
    expect(modal.attributes('data-visible')).toBe('true')
    expect(modal.attributes('data-plan-name')).toBe('园区峰谷方案')
  })
})
