import { flushPromises, mount } from '@vue/test-utils'
import ElectricBillReportView from '@/views/reports/ElectricBillReportView.vue'
import { fetchElectricBillReportDetail, fetchElectricBillReportPage } from '@/api/adapters/report'

vi.mock('@/api/adapters/report', () => ({
  fetchElectricBillReportPage: vi.fn(),
  fetchElectricBillReportDetail: vi.fn()
}))

const mockedFetchElectricBillReportPage = vi.mocked(fetchElectricBillReportPage)
const mockedFetchElectricBillReportDetail = vi.mocked(fetchElectricBillReportDetail)

const mountComponent = () => {
  return mount(ElectricBillReportView, {
    global: {
      stubs: {
        CommonPagination: {
          template: '<div data-test="pagination-stub" />'
        },
        UiTableStateOverlay: {
          props: ['loading', 'empty'],
          template:
            '<div data-test="table-overlay-stub" :data-loading="String(loading)" :data-empty="String(empty)" />'
        },
        ElectricBillReportDetailModal: {
          props: ['modelValue', 'detail', 'loading'],
          template:
            '<div data-test="detail-modal-stub" :data-visible="String(modelValue)" :data-loading="String(loading)" :data-account-name="detail?.accountInfo?.accountName || \'\'"></div>'
        }
      }
    }
  })
}

describe('ElectricBillReportView', () => {
  beforeEach(() => {
    vi.useRealTimers()
    mockedFetchElectricBillReportPage.mockReset()
    mockedFetchElectricBillReportDetail.mockReset()
  })

  test('testLoadList_OnMounted_ShouldFetchPageAndRenderRows', async () => {
    mockedFetchElectricBillReportPage.mockResolvedValue({
      total: 1,
      pageNum: 1,
      pageSize: 10,
      list: [
        {
          accountId: 101,
          accountName: '美特富精密拉深技术',
          electricAccountType: 2,
          electricAccountTypeName: '合并计费',
          meterCount: 2,
          periodConsumePowerText: '14023.6',
          periodElectricChargeAmountText: '12454.25',
          periodRechargeAmountText: '0.00',
          periodCorrectionAmountText: '0.00',
          totalDebitAmountText: '12454.25'
        }
      ]
    })
    mockedFetchElectricBillReportDetail.mockResolvedValue({
      accountInfo: {
        accountId: 101,
        accountName: '美特富精密拉深技术',
        contactName: '张三',
        contactPhone: '13800000000',
        electricAccountType: 2,
        electricAccountTypeName: '合并计费',
        monthlyPayAmountText: '0.00',
        accountBalanceText: '100.00',
        meterCount: 2,
        periodConsumePowerText: '14023.6',
        periodElectricChargeAmountText: '12454.25',
        periodRechargeAmountText: '0.00',
        periodCorrectionAmountText: '0.00',
        dateRangeText: '2026-04-13~2026-04-13'
      },
      meterList: []
    })

    const wrapper = mountComponent()
    await flushPromises()

    expect(mockedFetchElectricBillReportPage).toHaveBeenCalledTimes(1)
    const query = mockedFetchElectricBillReportPage.mock.calls[0][0]
    expect(query.pageNum).toBe(1)
    expect(query.pageSize).toBe(10)
    expect(query.startDate).toBe(query.endDate)

    expect(wrapper.text()).toContain('美特富精密拉深技术')
    expect(wrapper.text()).toContain('合并计费')
    expect(wrapper.text()).toContain('12454.25')
  })

  test('testOpenDetail_WhenClickDetail_ShouldFetchDetailAndOpenModal', async () => {
    mockedFetchElectricBillReportPage.mockResolvedValue({
      total: 1,
      pageNum: 1,
      pageSize: 10,
      list: [
        {
          accountId: 202,
          accountName: '富采思特汽车零部件',
          electricAccountType: 0,
          electricAccountTypeName: '按需计费',
          meterCount: 4,
          periodConsumePowerText: '29406.97',
          periodElectricChargeAmountText: '23342.10',
          periodRechargeAmountText: '3000.00',
          periodCorrectionAmountText: '0.00',
          totalDebitAmountText: '23342.10'
        }
      ]
    })
    mockedFetchElectricBillReportDetail.mockResolvedValue({
      accountInfo: {
        accountId: 202,
        accountName: '富采思特汽车零部件',
        contactName: '李四',
        contactPhone: '13900000000',
        electricAccountType: 0,
        electricAccountTypeName: '按需计费',
        monthlyPayAmountText: null,
        accountBalanceText: '500.00',
        meterCount: 4,
        periodConsumePowerText: '29406.97',
        periodElectricChargeAmountText: '23342.10',
        periodRechargeAmountText: '3000.00',
        periodCorrectionAmountText: '0.00',
        dateRangeText: '2026-04-13~2026-04-13'
      },
      meterList: []
    })

    const wrapper = mountComponent()
    await flushPromises()

    await wrapper.get('.btn-link').trigger('click')
    await flushPromises()

    expect(mockedFetchElectricBillReportDetail).toHaveBeenCalledTimes(1)
    expect(mockedFetchElectricBillReportDetail).toHaveBeenCalledWith(
      202,
      expect.objectContaining({
        startDate: expect.any(String),
        endDate: expect.any(String)
      })
    )

    const modal = wrapper.get('[data-test="detail-modal-stub"]')
    expect(modal.attributes('data-visible')).toBe('true')
    expect(modal.attributes('data-account-name')).toBe('富采思特汽车零部件')
  })

  test('testSearch_WhenDateRangeExceedsLimit_ShouldShowNoticeAndSkipSecondFetch', async () => {
    vi.useFakeTimers()
    vi.setSystemTime(new Date('2026-04-14T08:00:00'))
    mockedFetchElectricBillReportPage.mockResolvedValue({
      total: 0,
      pageNum: 1,
      pageSize: 10,
      list: []
    })

    const wrapper = mountComponent()
    await flushPromises()

    expect(mockedFetchElectricBillReportPage).toHaveBeenCalledTimes(1)

    const dateInputs = wrapper.findAll('input[type="date"]')
    await dateInputs[0]!.setValue('2026-02-01')
    await dateInputs[1]!.setValue('2026-04-13')
    await wrapper.get('button.btn-primary').trigger('click')
    await flushPromises()

    expect(mockedFetchElectricBillReportPage).toHaveBeenCalledTimes(1)
    expect(wrapper.text()).toContain('统计日期跨度不能超过 65 天')
  })

  test('testLoadList_WhenPageResultIsEmpty_ShouldRenderEmptyOverlay', async () => {
    mockedFetchElectricBillReportPage.mockResolvedValue({
      total: 0,
      pageNum: 1,
      pageSize: 10,
      list: []
    })
    mockedFetchElectricBillReportDetail.mockResolvedValue({
      accountInfo: {
        accountId: 0,
        accountName: '',
        contactName: '',
        contactPhone: '',
        electricAccountType: 0,
        electricAccountTypeName: '',
        monthlyPayAmountText: null,
        accountBalanceText: null,
        meterCount: 0,
        periodConsumePowerText: '0',
        periodElectricChargeAmountText: '0.00',
        periodRechargeAmountText: '0.00',
        periodCorrectionAmountText: '0.00',
        dateRangeText: ''
      },
      meterList: []
    })

    const wrapper = mountComponent()
    await flushPromises()

    const overlay = wrapper.get('[data-test="table-overlay-stub"]')
    expect(overlay.attributes('data-loading')).toBe('false')
    expect(overlay.attributes('data-empty')).toBe('true')
    expect(wrapper.findAll('.btn-link')).toHaveLength(0)
  })
})
