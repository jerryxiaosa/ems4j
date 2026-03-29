import { flushPromises, mount } from '@vue/test-utils'
import type { ElectricMeterItem } from '@/components/devices/electric-meter.mock'
import DeviceElectricMeterTrendModal from '@/components/devices/DeviceElectricMeterTrendModal.vue'
import { fetchElectricMeterPowerConsumeTrend } from '@/api/adapters/device'
import type { ElectricMeterPowerConsumeTrendPoint } from '@/types/device'

const { initMock } = vi.hoisted(() => ({
  initMock: vi.fn()
}))
const chartInstance = {
  setOption: vi.fn(),
  resize: vi.fn(),
  dispose: vi.fn()
}

vi.mock(
  'echarts',
  () => ({
    init: initMock
  })
)

vi.mock('@/api/adapters/device', async () => {
  const actual = await vi.importActual('@/api/adapters/device')
  return {
    ...actual,
    fetchElectricMeterPowerConsumeTrend: vi.fn()
  }
})

const mockedFetchElectricMeterPowerConsumeTrend = vi.mocked(fetchElectricMeterPowerConsumeTrend)

const createDeferred = <T>() => {
  let resolve!: (value: T | PromiseLike<T>) => void
  let reject!: (reason?: unknown) => void
  const promise = new Promise<T>((innerResolve, innerReject) => {
    resolve = innerResolve
    reject = innerReject
  })
  return { promise, resolve, reject }
}

const createMeter = (overrides: Partial<ElectricMeterItem> = {}): ElectricMeterItem => ({
  id: 1,
  meterName: 'A1-101总表',
  deviceNo: 'EM-001',
  meterAddress: '11',
  modelId: 102,
  modelName: 'DTSD1888',
  communicateModel: 'RS485',
  isCt: true,
  ct: '150',
  gatewayId: 1,
  gatewayName: '1号网关',
  gatewayDeviceNo: 'GW-001',
  gatewaySn: 'GW-SN-001',
  portNo: '1',
  imei: '',
  payType: 1,
  isCalculate: true,
  calculateType: '2',
  calculateTypeName: '阶梯计量',
  spaceId: '9',
  spaceName: '101',
  spacePath: 'A区 / 1号楼 / 101',
  onlineStatus: 1,
  onlineStatusName: '在线',
  offlineDuration: '',
  status: 0,
  statusName: '合闸',
  protectedModel: false,
  pricePlanName: '居民电价',
  warnPlanName: '默认预警',
  electricWarnTypeName: '一级预警',
  accountId: 99,
  ...overrides
})

const mountComponent = () =>
  mount(DeviceElectricMeterTrendModal, {
    props: {
      modelValue: true,
      meter: createMeter()
    },
    global: {
      stubs: {
        UiEmptyState: {
          props: ['text'],
          template: '<div data-test="empty">{{ text }}</div>'
        },
        UiLoadingState: {
          template: '<div data-test="loading">loading</div>'
        }
      }
    }
  })

describe('DeviceElectricMeterTrendModal', () => {
  beforeEach(() => {
    vi.useFakeTimers()
    vi.setSystemTime(new Date('2026-03-28T10:00:00'))
    mockedFetchElectricMeterPowerConsumeTrend.mockReset()
    initMock.mockClear()
    initMock.mockReturnValue(chartInstance as never)
    chartInstance.setOption.mockReset()
    chartInstance.resize.mockReset()
    chartInstance.dispose.mockReset()
  })

  afterEach(() => {
    vi.useRealTimers()
  })

  test('testDefaultRangeAndInfo_WhenModalOpened_ShouldQueryRecentSevenDays', async () => {
    mockedFetchElectricMeterPowerConsumeTrend.mockResolvedValue([
      {
        beginRecordTime: '2026-03-28 06:00:00',
        endRecordTime: '2026-03-28 08:00:00',
        meterConsumeTime: '2026-03-28 08:00:00',
        consumePower: '3.45',
        consumePowerHigher: '0.80',
        consumePowerHigh: undefined,
        consumePowerLow: undefined,
        consumePowerLower: undefined,
        consumePowerDeepLow: undefined
      }
    ])

    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.get('.modal-title').text()).toBe('用电趋势')
    expect(wrapper.text()).toContain('A1-101总表')
    expect(wrapper.text()).toContain('EM-001')
    expect(wrapper.text()).toContain('101')
    expect(wrapper.text()).toContain('RS485')
    expect(wrapper.findAll('.summary-grid .summary-item')).toHaveLength(4)
    expect(wrapper.find('.summary-grid-relaxed').exists()).toBe(true)
    expect(wrapper.findAll('.summary-item-secondary')).toHaveLength(2)
    expect(wrapper.find('.summary-item-full').exists()).toBe(false)
    expect(wrapper.find('.toolbar-grid').exists()).toBe(true)
    expect(wrapper.findAll('.toolbar-cell')).toHaveLength(2)
    expect(wrapper.find('.toolbar-cell-end').exists()).toBe(true)
    expect(wrapper.findAll('.toolbar-field-grid')).toHaveLength(2)
    expect(wrapper.find('.toolbar-field-grid-secondary').exists()).toBe(true)

    const dateInputs = wrapper.findAll('input[type="date"]')
    expect((dateInputs[0]!.element as HTMLInputElement).value).toBe('2026-03-27')
    expect((dateInputs[1]!.element as HTMLInputElement).value).toBe('2026-03-28')

    expect(mockedFetchElectricMeterPowerConsumeTrend).toHaveBeenCalledWith(1, {
      beginTime: '2026-03-27 00:00:00',
      endTime: '2026-03-28 23:59:59'
    })
  })

  test('testDateInputsDisabled_WhenLoadingTrend_ShouldPreventEditing', async () => {
    const deferred = createDeferred<ElectricMeterPowerConsumeTrendPoint[]>()
    mockedFetchElectricMeterPowerConsumeTrend.mockReturnValue(deferred.promise)

    const wrapper = mountComponent()
    await flushPromises()

    const dateInputs = wrapper.findAll('input[type="date"]')
    expect((dateInputs[0]!.element as HTMLInputElement).disabled).toBe(true)
    expect((dateInputs[1]!.element as HTMLInputElement).disabled).toBe(true)

    deferred.resolve([])
    await flushPromises()
  })

  test('testChartLifecycle_WhenTrendLoadedAndClosed_ShouldInitOptionAndDispose', async () => {
    mockedFetchElectricMeterPowerConsumeTrend.mockResolvedValue([
      {
        beginRecordTime: '2026-03-28 06:00:00',
        endRecordTime: '2026-03-28 08:00:00',
        meterConsumeTime: '2026-03-28 08:00:00',
        consumePower: '3.45',
        consumePowerHigher: undefined,
        consumePowerHigh: undefined,
        consumePowerLow: undefined,
        consumePowerLower: undefined,
        consumePowerDeepLow: undefined
      },
      {
        beginRecordTime: '2026-03-28 08:00:00',
        endRecordTime: '2026-03-28 09:00:00',
        meterConsumeTime: '2026-03-28 09:00:00',
        consumePower: '4.10',
        consumePowerHigher: undefined,
        consumePowerHigh: undefined,
        consumePowerLow: undefined,
        consumePowerLower: undefined,
        consumePowerDeepLow: undefined
      }
    ])

    const wrapper = mountComponent()
    await flushPromises()

    expect(chartInstance.setOption).toHaveBeenCalled()
    const chartOption = chartInstance.setOption.mock.calls[0]?.[0]
    expect(chartOption?.xAxis?.type).toBe('value')
    expect(chartOption?.xAxis?.min).toBe(0)
    expect(chartOption?.xAxis?.max).toBe(1)
    expect(chartOption?.xAxis?.interval).toBe(1)
    expect(chartOption?.xAxis?.axisLabel?.hideOverlap).toBe(false)
    expect(chartOption?.xAxis?.axisLabel?.formatter?.(0)).toBe('03-28')
    expect(chartOption?.xAxis?.axisLabel?.formatter?.(1)).toBe('')
    expect(chartOption?.yAxis).toMatchObject({
      name: '消费电量(kWh)',
      nameLocation: 'middle'
    })
    expect(chartOption?.grid).toMatchObject({
      left: 72,
      bottom: 56
    })
    expect(chartOption?.series?.[0]?.areaStyle).toBeUndefined()
    expect(chartOption?.series?.[0]?.data?.[0]?.value?.[0]).toBeCloseTo(8 / 24, 4)
    expect(chartOption?.series?.[0]?.data?.[1]?.value?.[0]).toBeCloseTo(9 / 24, 4)

    await wrapper.unmount()

    expect(chartInstance.dispose).toHaveBeenCalled()
  })

  test('testEmptyState_WhenNoTrendData_ShouldRenderEmptyState', async () => {
    mockedFetchElectricMeterPowerConsumeTrend.mockResolvedValue([])

    const wrapper = mountComponent()
    await flushPromises()

    expect(wrapper.get('[data-test="empty"]').text()).toContain('该时间范围暂无用电记录')
  })

  test('testDateLabels_WhenTrendSpansMultipleDays_ShouldDistributeAcrossWholeRange', async () => {
    const trendPoints: ElectricMeterPowerConsumeTrendPoint[] = []
    for (let day = 1; day <= 12; day += 1) {
      const dayText = String(day).padStart(2, '0')
      trendPoints.push({
        beginRecordTime: `2026-02-${dayText} 06:00:00`,
        endRecordTime: `2026-02-${dayText} 08:00:00`,
        meterConsumeTime: `2026-02-${dayText} 08:00:00`,
        consumePower: `${(1.5 + day / 10).toFixed(2)}`,
        consumePowerHigher: undefined,
        consumePowerHigh: undefined,
        consumePowerLow: undefined,
        consumePowerLower: undefined,
        consumePowerDeepLow: undefined
      })
      trendPoints.push({
        beginRecordTime: `2026-02-${dayText} 16:00:00`,
        endRecordTime: `2026-02-${dayText} 18:00:00`,
        meterConsumeTime: `2026-02-${dayText} 18:00:00`,
        consumePower: `${(1.8 + day / 10).toFixed(2)}`,
        consumePowerHigher: undefined,
        consumePowerHigh: undefined,
        consumePowerLow: undefined,
        consumePowerLower: undefined,
        consumePowerDeepLow: undefined
      })
    }
    mockedFetchElectricMeterPowerConsumeTrend.mockResolvedValue(trendPoints)

    const wrapper = mountComponent()
    await flushPromises()

    const chartOption = chartInstance.setOption.mock.calls[0]?.[0]
    expect(chartOption?.xAxis?.type).toBe('value')
    expect(chartOption?.xAxis?.min).toBe(0)
    expect(chartOption?.xAxis?.max).toBe(12)
    expect(chartOption?.xAxis?.interval).toBe(1)
    expect(chartOption?.xAxis?.axisLabel?.formatter?.(0)).toBe('02-01')
    expect(chartOption?.xAxis?.axisLabel?.formatter?.(1)).toBe('')
    expect(chartOption?.xAxis?.axisLabel?.formatter?.(2)).toBe('02-03')
    expect(chartOption?.xAxis?.axisLabel?.formatter?.(10)).toBe('02-11')
    expect(chartOption?.xAxis?.axisLabel?.formatter?.(11)).toBe('')
  })

  test('testChartReinit_WhenSearchRunsAgain_ShouldRenderOnLatestCanvas', async () => {
    mockedFetchElectricMeterPowerConsumeTrend
      .mockResolvedValueOnce([
        {
          beginRecordTime: '2026-03-27 06:00:00',
          endRecordTime: '2026-03-27 08:00:00',
          meterConsumeTime: '2026-03-27 08:00:00',
          consumePower: '3.45',
          consumePowerHigher: undefined,
          consumePowerHigh: undefined,
          consumePowerLow: undefined,
          consumePowerLower: undefined,
          consumePowerDeepLow: undefined
        }
      ])
      .mockResolvedValueOnce([
        {
          beginRecordTime: '2026-03-28 08:00:00',
          endRecordTime: '2026-03-28 10:00:00',
          meterConsumeTime: '2026-03-28 10:00:00',
          consumePower: '6.80',
          consumePowerHigher: undefined,
          consumePowerHigh: undefined,
          consumePowerLow: undefined,
          consumePowerLower: undefined,
          consumePowerDeepLow: undefined
        }
      ])

    const wrapper = mountComponent()
    await flushPromises()

    expect(initMock).toHaveBeenCalledTimes(1)
    expect(chartInstance.setOption).toHaveBeenCalledTimes(1)

    const dateInputs = wrapper.findAll('input[type="date"]')
    await dateInputs[0]!.setValue('2026-03-26')
    await wrapper.get('.btn.btn-primary').trigger('click')
    await flushPromises()

    expect(initMock).toHaveBeenCalledTimes(2)
    expect(chartInstance.setOption).toHaveBeenCalledTimes(2)
  })

  test('testLatestRequestWins_WhenMeterChanges_ShouldIgnoreStaleTrendResponse', async () => {
    const firstDeferred = createDeferred<ElectricMeterPowerConsumeTrendPoint[]>()
    const secondDeferred = createDeferred<ElectricMeterPowerConsumeTrendPoint[]>()
    mockedFetchElectricMeterPowerConsumeTrend
      .mockReturnValueOnce(firstDeferred.promise)
      .mockReturnValueOnce(secondDeferred.promise)

    const wrapper = mountComponent()
    await flushPromises()
    expect(mockedFetchElectricMeterPowerConsumeTrend).toHaveBeenCalledTimes(1)

    await wrapper.setProps({
      meter: createMeter({
        id: 2,
        meterName: 'B1-201分表',
        deviceNo: 'EM-002'
      })
    })
    await flushPromises()
    expect(mockedFetchElectricMeterPowerConsumeTrend).toHaveBeenCalledTimes(2)

    secondDeferred.resolve([
      {
        beginRecordTime: '2026-03-28 10:00:00',
        endRecordTime: '2026-03-28 11:00:00',
        meterConsumeTime: '2026-03-28 11:00:00',
        consumePower: '2.20',
        consumePowerHigher: undefined,
        consumePowerHigh: undefined,
        consumePowerLow: undefined,
        consumePowerLower: undefined,
        consumePowerDeepLow: undefined
      }
    ])
    await flushPromises()

    expect(chartInstance.setOption).toHaveBeenCalledTimes(1)
    const latestOption = chartInstance.setOption.mock.calls[0]?.[0]
    expect(latestOption?.series?.[0]?.data?.[0]?.value?.[1]).toBe(2.2)

    firstDeferred.resolve([
      {
        beginRecordTime: '2026-03-28 06:00:00',
        endRecordTime: '2026-03-28 08:00:00',
        meterConsumeTime: '2026-03-28 08:00:00',
        consumePower: '3.45',
        consumePowerHigher: undefined,
        consumePowerHigh: undefined,
        consumePowerLow: undefined,
        consumePowerLower: undefined,
        consumePowerDeepLow: undefined
      }
    ])
    await flushPromises()

    expect(chartInstance.setOption).toHaveBeenCalledTimes(1)
    expect(wrapper.text()).toContain('B1-201分表')
    expect(wrapper.text()).not.toContain('趋势数据查询失败')
  })
})
