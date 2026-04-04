import { createEnergyTopUpOrder, fetchOrderDetail, fetchOrderPage } from '@/api/adapters/trade'
import { createEnergyTopUpOrderRaw, getOrderDetailRaw, getOrderPageRaw } from '@/api/raw/trade'
import { SUCCESS_CODE } from '@/api/raw/types'

vi.mock('@/api/raw/trade', async () => {
  const actual = await vi.importActual('@/api/raw/trade')
  return {
    ...actual,
    createEnergyTopUpOrderRaw: vi.fn(),
    getOrderPageRaw: vi.fn(),
    getOrderDetailRaw: vi.fn()
  }
})

const mockedCreateEnergyTopUpOrderRaw = vi.mocked(createEnergyTopUpOrderRaw)
const mockedGetOrderPageRaw = vi.mocked(getOrderPageRaw)
const mockedGetOrderDetailRaw = vi.mocked(getOrderDetailRaw)

describe('trade adapter', () => {
  beforeEach(() => {
    mockedCreateEnergyTopUpOrderRaw.mockReset()
    mockedGetOrderPageRaw.mockReset()
    mockedGetOrderDetailRaw.mockReset()
  })

  test('testCreateEnergyTopUpOrder_WhenServiceRateProvided_ShouldPassThroughEnergyTopUpServiceRate', async () => {
    mockedCreateEnergyTopUpOrderRaw.mockResolvedValue({
      success: true,
      code: SUCCESS_CODE,
      data: {
        orderSn: 'ORDER-001',
        paymentChannel: 'OFFLINE'
      }
    } as never)

    await createEnergyTopUpOrder({
      userId: 1,
      userPhone: '13800000000',
      userRealName: '张三',
      thirdPartyUserId: 'third-user',
      orderAmount: 100,
      paymentChannel: 'OFFLINE',
      energyTopUp: {
        accountId: 1,
        balanceType: 0,
        ownerType: 0,
        ownerId: 1001,
        ownerName: '企业A',
        electricAccountType: 0,
        serviceRate: 0.12,
        meterId: 101
      }
    })

    expect(mockedCreateEnergyTopUpOrderRaw).toHaveBeenCalledWith(
      expect.objectContaining({
        energyTopUp: expect.objectContaining({
          serviceRate: 0.12
        })
      })
    )
  })

  test('testFetchOrderPage_WhenServiceRateIsRatio_ShouldConvertToPercentText', async () => {
    mockedGetOrderPageRaw.mockResolvedValue({
      success: true,
      code: SUCCESS_CODE,
      data: {
        pageNum: 1,
        pageSize: 10,
        total: 1,
        list: [
          {
            orderSn: 'ORDER-001',
            serviceRate: 0.12
          }
        ]
      }
    } as never)

    const result = await fetchOrderPage({
      pageNum: 1,
      pageSize: 10
    })

    expect(result.list[0]?.serviceRate).toBe('12')
  })

  test('testFetchOrderDetail_WhenServiceRateIsRatio_ShouldConvertToPercentText', async () => {
    mockedGetOrderDetailRaw.mockResolvedValue({
      success: true,
      code: SUCCESS_CODE,
      data: {
        orderSn: 'ORDER-001',
        serviceRate: 0.12
      }
    } as never)

    const result = await fetchOrderDetail('ORDER-001')

    expect(result.serviceRate).toBe('12')
  })

  test('testFetchOrderPage_WhenServiceRateHasPrecision_ShouldKeepStablePercentText', async () => {
    mockedGetOrderPageRaw.mockResolvedValue({
      success: true,
      code: SUCCESS_CODE,
      data: {
        pageNum: 1,
        pageSize: 10,
        total: 1,
        list: [
          {
            orderSn: 'ORDER-002',
            serviceRate: '0.12345678'
          }
        ]
      }
    } as never)

    const result = await fetchOrderPage({
      pageNum: 1,
      pageSize: 10
    })

    expect(result.list[0]?.serviceRate).toBe('12.345678')
  })

  test('testFetchOrderDetail_WhenServiceRateMissing_ShouldReturnPlaceholder', async () => {
    mockedGetOrderDetailRaw.mockResolvedValue({
      success: true,
      code: SUCCESS_CODE,
      data: {
        orderSn: 'ORDER-003',
        serviceRate: null
      }
    } as never)

    const result = await fetchOrderDetail('ORDER-003')

    expect(result.serviceRate).toBe('--')
  })
})
