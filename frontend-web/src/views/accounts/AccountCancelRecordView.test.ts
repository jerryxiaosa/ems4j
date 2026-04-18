import { flushPromises, mount } from '@vue/test-utils'
import AccountCancelRecordView from '@/views/accounts/AccountCancelRecordView.vue'
import { fetchCancelDetail, fetchCancelRecordPage } from '@/api/adapters/account'
import { fetchEnumOptionsByKey } from '@/api/adapters/system'

vi.mock('@/api/adapters/account', () => ({
  fetchCancelRecordPage: vi.fn(),
  fetchCancelDetail: vi.fn()
}))

vi.mock('@/api/adapters/system', () => ({
  fetchEnumOptionsByKey: vi.fn()
}))

const mockedFetchCancelRecordPage = vi.mocked(fetchCancelRecordPage)
const mockedFetchCancelDetail = vi.mocked(fetchCancelDetail)
const mockedFetchEnumOptionsByKey = vi.mocked(fetchEnumOptionsByKey)

const mountComponent = () => {
  return mount(AccountCancelRecordView, {
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
        UiLoadingState: {
          template: '<div data-test="loading-state-stub" />'
        },
        UiEmptyState: {
          props: ['text'],
          template: '<div data-test="empty-state-stub">{{ text }}</div>'
        }
      }
    }
  })
}

describe('AccountCancelRecordView', () => {
  beforeEach(() => {
    mockedFetchCancelRecordPage.mockReset()
    mockedFetchCancelDetail.mockReset()
    mockedFetchEnumOptionsByKey.mockReset()
    mockedFetchEnumOptionsByKey.mockResolvedValue([{ label: '退费', value: '1' }])
  })

  test('testLoadCancelRecords_OnMounted_ShouldFetchPageAndRenderRows', async () => {
    mockedFetchCancelRecordPage.mockResolvedValue({
      total: 1,
      pageNum: 1,
      pageSize: 10,
      list: [
        {
          cancelNo: 'CANCEL-001',
          ownerName: '测试企业A',
          electricMeterAmount: 2,
          cleanBalanceType: 1,
          cleanBalanceAmountText: '50.00',
          operatorName: '张三',
          cancelTime: '2026-04-14 09:00:00',
          remark: '--'
        }
      ]
    })

    const wrapper = mountComponent()
    await flushPromises()

    expect(mockedFetchCancelRecordPage).toHaveBeenCalledWith(
      expect.objectContaining({
        ownerName: undefined,
        pageNum: 1,
        pageSize: 10
      })
    )
    expect(wrapper.text()).toContain('CANCEL-001')
    expect(wrapper.text()).toContain('测试企业A')
    expect(wrapper.text()).toContain('50.00')
  })

  test('testOpenDetail_WhenClickDetail_ShouldFetchCancelDetailAndRenderModal', async () => {
    mockedFetchCancelRecordPage.mockResolvedValue({
      total: 1,
      pageNum: 1,
      pageSize: 10,
      list: [
        {
          cancelNo: 'CANCEL-002',
          ownerName: '测试企业B',
          electricMeterAmount: 1,
          cleanBalanceType: 1,
          cleanBalanceAmountText: '30.00',
          operatorName: '李四',
          cancelTime: '2026-04-14 10:00:00',
          remark: '--'
        }
      ]
    })
    mockedFetchCancelDetail.mockResolvedValue({
      cancelNo: 'CANCEL-002',
      ownerName: '测试企业B',
      electricMeterAmount: 1,
      cleanBalanceType: 1,
      cleanBalanceAmountText: '30.00',
      operatorName: '李四',
      cancelTime: '2026-04-14 10:00:00',
      remark: '销户完成',
      meterList: [
        {
          spaceName: '101',
          meterName: '1号表',
          deviceNo: 'EM-001',
          balance: 0,
          power: 123.4,
          showTime: '2026-04-14 09:55:00'
        }
      ]
    })

    const wrapper = mountComponent()
    await flushPromises()

    await wrapper.get('.btn-link').trigger('click')
    await flushPromises()

    expect(mockedFetchCancelDetail).toHaveBeenCalledWith('CANCEL-002')
    expect(wrapper.text()).toContain('销户详情')
    expect(wrapper.text()).toContain('销户完成')
    expect(wrapper.text()).toContain('1号表')
  })
})
