import { flushPromises, mount } from '@vue/test-utils'
import WarnPlanView from '@/views/plans/WarnPlanView.vue'
import { fetchWarnPlanDetail, fetchWarnPlanList } from '@/api/adapters/plan'

vi.mock('@/api/adapters/plan', () => ({
  fetchWarnPlanList: vi.fn(),
  fetchWarnPlanDetail: vi.fn(),
  addWarnPlan: vi.fn(),
  updateWarnPlan: vi.fn(),
  deleteWarnPlan: vi.fn()
}))

const mockedFetchWarnPlanList = vi.mocked(fetchWarnPlanList)
const mockedFetchWarnPlanDetail = vi.mocked(fetchWarnPlanDetail)

const mountComponent = () => {
  return mount(WarnPlanView, {
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
        WarnPlanEditModal: {
          props: ['modelValue', 'mode', 'plan'],
          template:
            '<div data-test="warn-plan-edit-modal-stub" :data-visible="String(modelValue)" :data-mode="mode" :data-plan-name="plan?.name || \'\'" />'
        },
        WarnPlanDetailModal: {
          props: ['modelValue', 'plan'],
          template:
            '<div data-test="warn-plan-detail-modal-stub" :data-visible="String(modelValue)" :data-plan-name="plan?.name || \'\'" />'
        },
        Transition: {
          template: '<div><slot /></div>'
        }
      }
    }
  })
}

describe('WarnPlanView', () => {
  beforeEach(() => {
    mockedFetchWarnPlanList.mockReset()
    mockedFetchWarnPlanDetail.mockReset()
  })

  test('testLoadWarnPlans_OnMounted_ShouldFetchListAndRenderRows', async () => {
    mockedFetchWarnPlanList.mockResolvedValue([
      {
        id: 1,
        name: '标准预警方案',
        firstLevel: 100,
        secondLevel: 50,
        autoClose: true,
        createTime: '2026-04-14 09:00:00',
        remark: '默认方案'
      }
    ])

    const wrapper = mountComponent()
    await flushPromises()

    expect(mockedFetchWarnPlanList).toHaveBeenCalledTimes(1)
    expect(mockedFetchWarnPlanList).toHaveBeenCalledWith({})
    expect(wrapper.text()).toContain('标准预警方案')
    expect(wrapper.text()).toContain('默认方案')
    expect(wrapper.text()).toContain('是')
  })

  test('testOpenDetailModal_WhenClickDetail_ShouldFetchDetailAndOpenModal', async () => {
    mockedFetchWarnPlanList.mockResolvedValue([
      {
        id: 2,
        name: '企业预警方案',
        firstLevel: 200,
        secondLevel: 80,
        autoClose: false,
        createTime: '2026-04-14 10:00:00',
        remark: '企业用'
      }
    ])
    mockedFetchWarnPlanDetail.mockResolvedValue({
      id: 2,
      name: '企业预警方案',
      firstLevel: 200,
      secondLevel: 80,
      autoClose: false,
      createUser: '管理员',
      createTime: '2026-04-14 10:00:00',
      updateUser: '管理员',
      updateTime: '2026-04-14 10:05:00',
      remark: '企业用'
    })

    const wrapper = mountComponent()
    await flushPromises()

    await wrapper.get('.btn-link').trigger('click')
    await flushPromises()

    expect(mockedFetchWarnPlanDetail).toHaveBeenCalledWith(2)
    const modal = wrapper.get('[data-test="warn-plan-detail-modal-stub"]')
    expect(modal.attributes('data-visible')).toBe('true')
    expect(modal.attributes('data-plan-name')).toBe('企业预警方案')
  })
})
