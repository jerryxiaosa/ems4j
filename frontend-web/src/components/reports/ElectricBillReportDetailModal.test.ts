import { mount } from '@vue/test-utils'
import ElectricBillReportDetailModal from '@/components/reports/ElectricBillReportDetailModal.vue'
import type { ElectricBillReportDetailResult } from '@/types/report'

const createDetail = (): ElectricBillReportDetailResult => ({
  accountInfo: {
    accountId: 101,
    accountName: '企业A',
    contactName: '张三',
    contactPhone: '13800000000',
    electricAccountType: 0,
    electricAccountTypeName: '按需计费',
    monthlyPayAmountText: null,
    accountBalanceText: '100.00',
    meterCount: 1,
    periodConsumePowerText: '200',
    periodElectricChargeAmountText: '300.00',
    periodRechargeAmountText: '10.00',
    periodCorrectionAmountText: '0.00',
    dateRangeText: '2026-04-01~2026-04-08'
  },
  meterList: [
    {
      meterId: 1,
      deviceNo: 'EM001',
      meterName: '一号表',
      consumePowerHigherText: '0',
      consumePowerHighText: '10',
      consumePowerLowText: '20',
      consumePowerLowerText: '30',
      consumePowerDeepLowText: '0',
      displayPriceHigherText: '0.1000',
      displayPriceHighText: '0.2000',
      displayPriceLowText: '0.3000',
      displayPriceLowerText: '0.4000',
      displayPriceDeepLowText: '0.5000',
      electricChargeAmountHigherText: '0.00',
      electricChargeAmountHighText: '1.00',
      electricChargeAmountLowText: '2.00',
      electricChargeAmountLowerText: '3.00',
      electricChargeAmountDeepLowText: '0.00',
      totalConsumePowerText: '60',
      totalElectricChargeAmountText: '6.00',
      totalRechargeAmountText: '1.00',
      totalCorrectionAmountText: '0.00'
    }
  ]
})

describe('ElectricBillReportDetailModal', () => {
  test('testStickyRightOffset_WhenRenderTotalConsumeColumn_ShouldMatchTrailingColumnWidths', () => {
    const wrapper = mount(ElectricBillReportDetailModal, {
      props: {
        modelValue: true,
        detail: createDetail()
      },
      global: {
        stubs: {
          UiEmptyState: {
            template: '<div />'
          },
          UiLoadingState: {
            template: '<div />'
          }
        }
      }
    })

    const headerCell = wrapper
      .findAll('th')
      .find((item) => item.text() === '总电量(kWh)')

    expect(headerCell?.attributes('style')).toContain('right: 324px;')
  })
})
