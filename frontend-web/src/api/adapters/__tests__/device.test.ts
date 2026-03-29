import {
  fetchElectricMeterPowerConsumeTrend,
  fetchElectricMeterPowerTrend
} from '@/api/adapters/device'
import {
  getElectricMeterPowerConsumeTrendRaw,
  getElectricMeterPowerTrendRaw
} from '@/api/raw/device'
import { SUCCESS_CODE } from '@/api/raw/types'

vi.mock('@/api/raw/device', async () => {
  const actual = await vi.importActual('@/api/raw/device')
  return {
    ...actual,
    getElectricMeterPowerTrendRaw: vi.fn(),
    getElectricMeterPowerConsumeTrendRaw: vi.fn()
  }
})

const mockedGetElectricMeterPowerTrendRaw = vi.mocked(getElectricMeterPowerTrendRaw)
const mockedGetElectricMeterPowerConsumeTrendRaw = vi.mocked(getElectricMeterPowerConsumeTrendRaw)

describe('device adapter', () => {
  beforeEach(() => {
    mockedGetElectricMeterPowerTrendRaw.mockReset()
    mockedGetElectricMeterPowerConsumeTrendRaw.mockReset()
    vi.unstubAllEnvs()
  })

  test('testFetchElectricMeterPowerTrend_WhenDefaultSource_ShouldRequestApiAndNormalizeTrendPoints', async () => {
    mockedGetElectricMeterPowerTrendRaw.mockResolvedValue({
      success: true,
      code: SUCCESS_CODE,
      data: [
        {
          recordTime: '2026-03-22T08:00:00',
          power: 120.2
        }
      ]
    } as never)

    const result = await fetchElectricMeterPowerTrend(3, {
      beginTime: '2026-03-22 00:00:00',
      endTime: '2026-03-28 23:59:59'
    })

    expect(mockedGetElectricMeterPowerTrendRaw).toHaveBeenCalledWith(3, {
      beginTime: '2026-03-22 00:00:00',
      endTime: '2026-03-28 23:59:59'
    })
    expect(result).toEqual([
      {
        recordTime: '2026-03-22 08:00:00',
        power: '120.20',
        powerHigher: undefined,
        powerHigh: undefined,
        powerLow: undefined,
        powerLower: undefined,
        powerDeepLow: undefined
      }
    ])
  })

  test('testFetchElectricMeterPowerTrend_WhenMockSourceEnabled_ShouldReturnMockPointsWithoutRequest', async () => {
    vi.stubEnv('VITE_ELECTRIC_METER_TREND_SOURCE', 'mock')

    const result = await fetchElectricMeterPowerTrend(3, {
      beginTime: '2026-03-22 00:00:00',
      endTime: '2026-03-28 23:59:59'
    })

    expect(mockedGetElectricMeterPowerTrendRaw).not.toHaveBeenCalled()
    expect(result.length).toBeGreaterThanOrEqual(8)
    expect(result[0]).toMatchObject({
      recordTime: expect.stringMatching(/^2026-03-22 \d{2}:\d{2}:\d{2}$/),
      power: expect.any(String)
    })
  })

  test('testFetchElectricMeterPowerConsumeTrend_WhenDefaultSource_ShouldRequestApiAndNormalizeTrendPoints', async () => {
    mockedGetElectricMeterPowerConsumeTrendRaw.mockResolvedValue({
      success: true,
      code: SUCCESS_CODE,
      data: [
        {
          beginRecordTime: '2026-03-22T08:00:00',
          endRecordTime: '2026-03-22T10:00:00',
          meterConsumeTime: '2026-03-22T10:00:00',
          consumePower: 3.2
        }
      ]
    } as never)

    const result = await fetchElectricMeterPowerConsumeTrend(3, {
      beginTime: '2026-03-22 00:00:00',
      endTime: '2026-03-28 23:59:59'
    })

    expect(mockedGetElectricMeterPowerConsumeTrendRaw).toHaveBeenCalledWith(3, {
      beginTime: '2026-03-22 00:00:00',
      endTime: '2026-03-28 23:59:59'
    })
    expect(result).toEqual([
      {
        beginRecordTime: '2026-03-22 08:00:00',
        endRecordTime: '2026-03-22 10:00:00',
        meterConsumeTime: '2026-03-22 10:00:00',
        consumePower: '3.20',
        consumePowerHigher: undefined,
        consumePowerHigh: undefined,
        consumePowerLow: undefined,
        consumePowerLower: undefined,
        consumePowerDeepLow: undefined
      }
    ])
  })

  test('testFetchElectricMeterPowerConsumeTrend_WhenMockSourceEnabled_ShouldReturnMockPointsWithoutRequest', async () => {
    vi.stubEnv('VITE_ELECTRIC_METER_TREND_SOURCE', 'mock')

    const result = await fetchElectricMeterPowerConsumeTrend(3, {
      beginTime: '2026-03-22 00:00:00',
      endTime: '2026-03-28 23:59:59'
    })

    expect(mockedGetElectricMeterPowerConsumeTrendRaw).not.toHaveBeenCalled()
    expect(result.length).toBeGreaterThanOrEqual(8)
    expect(result[0]).toMatchObject({
      meterConsumeTime: expect.stringMatching(/^2026-03-22 \d{2}:\d{2}:\d{2}$/),
      consumePower: expect.any(String)
    })
  })
})
