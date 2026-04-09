import { fetchElectricBillReportDetail, fetchElectricBillReportPage } from '@/api/adapters/report'
import {
  getElectricBillReportDetailRaw,
  getElectricBillReportPageRaw
} from '@/api/raw/report'
import { SUCCESS_CODE } from '@/api/raw/types'

vi.mock('@/api/raw/report', async () => {
  const actual = await vi.importActual('@/api/raw/report')
  return {
    ...actual,
    getElectricBillReportPageRaw: vi.fn(),
    getElectricBillReportDetailRaw: vi.fn()
  }
})

const mockedGetElectricBillReportPageRaw = vi.mocked(getElectricBillReportPageRaw)
const mockedGetElectricBillReportDetailRaw = vi.mocked(getElectricBillReportDetailRaw)

describe('report adapter', () => {
  beforeEach(() => {
    mockedGetElectricBillReportPageRaw.mockReset()
    mockedGetElectricBillReportDetailRaw.mockReset()
  })

  test('testFetchElectricBillReportPage_WhenResponseSuccess_ShouldNormalizePageResult', async () => {
    mockedGetElectricBillReportPageRaw.mockResolvedValue({
      success: true,
      code: SUCCESS_CODE,
      data: {
        total: 1,
        pageNum: 2,
        pageSize: 20,
        list: [
          {
            accountId: 101,
            accountName: '企业A',
            electricAccountType: 0,
            electricAccountTypeName: '按需计费',
            meterCount: 3,
            periodConsumePowerText: '100',
            periodElectricChargeAmountText: '200.00',
            periodRechargeAmountText: '50.00',
            periodCorrectionAmountText: '-10.00',
            totalDebitAmountText: '210.00'
          }
        ]
      }
    } as never)

    const result = await fetchElectricBillReportPage({
      accountNameLike: '企业',
      startDate: '2026-04-01',
      endDate: '2026-04-08',
      pageNum: 2,
      pageSize: 20
    })

    expect(mockedGetElectricBillReportPageRaw).toHaveBeenCalledWith({
      accountNameLike: '企业',
      startDate: '2026-04-01',
      endDate: '2026-04-08',
      pageNum: 2,
      pageSize: 20
    })
    expect(result.total).toBe(1)
    expect(result.pageNum).toBe(2)
    expect(result.pageSize).toBe(20)
    expect(result.list[0]?.accountName).toBe('企业A')
  })

  test('testFetchElectricBillReportDetail_WhenResponseSuccess_ShouldReturnDetailPayload', async () => {
    mockedGetElectricBillReportDetailRaw.mockResolvedValue({
      success: true,
      code: SUCCESS_CODE,
      data: {
        accountInfo: {
          accountId: 101,
          accountName: '企业A',
          contactName: '张三',
          contactPhone: '13800000000',
          electricAccountType: 2,
          electricAccountTypeName: '合并计费',
          monthlyPayAmountText: null,
          accountBalanceText: '500.00',
          meterCount: 2,
          periodConsumePowerText: '120',
          periodElectricChargeAmountText: '300.00',
          periodRechargeAmountText: '0.00',
          periodCorrectionAmountText: '20.00',
          dateRangeText: '2026-04-01~2026-04-08'
        },
        meterList: [
          {
            meterId: 201,
            deviceNo: 'EM001',
            meterName: '一号表',
            consumePowerHigherText: '0',
            consumePowerHighText: '10',
            consumePowerLowText: '20',
            consumePowerLowerText: '30',
            consumePowerDeepLowText: '0',
            displayPriceHigherText: null,
            displayPriceHighText: null,
            displayPriceLowText: null,
            displayPriceLowerText: null,
            displayPriceDeepLowText: null,
            electricChargeAmountHigherText: null,
            electricChargeAmountHighText: null,
            electricChargeAmountLowText: null,
            electricChargeAmountLowerText: null,
            electricChargeAmountDeepLowText: null,
            totalConsumePowerText: '60',
            totalElectricChargeAmountText: null,
            totalRechargeAmountText: null,
            totalCorrectionAmountText: null
          }
        ]
      }
    } as never)

    const result = await fetchElectricBillReportDetail(101, {
      startDate: '2026-04-01',
      endDate: '2026-04-08'
    })

    expect(mockedGetElectricBillReportDetailRaw).toHaveBeenCalledWith(101, {
      startDate: '2026-04-01',
      endDate: '2026-04-08'
    })
    expect(result.accountInfo.accountName).toBe('企业A')
    expect(result.meterList[0]?.meterName).toBe('一号表')
  })
})
