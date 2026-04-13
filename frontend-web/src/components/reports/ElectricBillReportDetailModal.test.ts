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
    monthlyPayAmount: null,
    monthlyPayAmountText: null,
    accountBalance: null,
    accountBalanceText: '100.00',
    meterCount: 1,
    periodConsumePower: null,
    periodConsumePowerText: '200',
    periodElectricChargeAmount: null,
    periodElectricChargeAmountText: '300.00',
    periodRechargeAmount: null,
    periodRechargeAmountText: '10.00',
    periodCorrectionAmount: null,
    periodCorrectionAmountText: '0.00',
    dateRangeText: '2026-04-01~2026-04-08'
  },
  meterList: [
    {
      meterId: 1,
      deviceNo: 'EM001',
      meterName: '一号表',
      consumePowerHigher: null,
      consumePowerHigherText: '0',
      consumePowerHigh: null,
      consumePowerHighText: '10',
      consumePowerLow: null,
      consumePowerLowText: '20',
      consumePowerLower: null,
      consumePowerLowerText: '30',
      consumePowerDeepLow: null,
      consumePowerDeepLowText: '0',
      displayPriceHigher: null,
      displayPriceHigherText: '0.1000',
      displayPriceHigh: null,
      displayPriceHighText: '0.2000',
      displayPriceLow: null,
      displayPriceLowText: '0.3000',
      displayPriceLower: null,
      displayPriceLowerText: '0.4000',
      displayPriceDeepLow: null,
      displayPriceDeepLowText: '0.5000',
      electricChargeAmountHigher: null,
      electricChargeAmountHigherText: '0.00',
      electricChargeAmountHigh: null,
      electricChargeAmountHighText: '1.00',
      electricChargeAmountLow: null,
      electricChargeAmountLowText: '2.00',
      electricChargeAmountLower: null,
      electricChargeAmountLowerText: '3.00',
      electricChargeAmountDeepLow: null,
      electricChargeAmountDeepLowText: '0.00',
      totalConsumePower: null,
      totalConsumePowerText: '60',
      totalElectricChargeAmount: null,
      totalElectricChargeAmountText: '6.00',
      totalRechargeAmount: null,
      totalRechargeAmountText: '1.00',
      totalCorrectionAmount: null,
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
