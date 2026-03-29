import { nextTick, onBeforeUnmount, type Ref } from 'vue'
import * as echarts from 'echarts'
import type { ECharts, EChartsOption } from 'echarts'
import type { ElectricMeterPowerConsumeTrendPoint } from '@/types/device'

const TREND_COLOR = '#16a34a'
const MAX_DATE_LABEL_COUNT = 8
const SECONDS_PER_DAY = 24 * 60 * 60

type TrendSeriesPoint = {
  value: [number, number | null]
  meterConsumeTime: string
  beginRecordTime?: string
  endRecordTime?: string
}

const toNumber = (value: string | undefined) => {
  if (!value) {
    return null
  }

  const parsed = Number(value)
  return Number.isFinite(parsed) ? parsed : null
}

const toDateLabel = (value: string) => {
  const datePart = value.split(' ')[0] || value
  const segments = datePart.split('-')
  if (segments.length >= 3) {
    return `${segments[1]}-${segments[2]}`
  }

  return datePart
}

const toDateKey = (value: string) => value.split(' ')[0] || value

const toDayFraction = (value: string) => {
  const timePart = value.split(' ')[1] || ''
  const [hourText = '0', minuteText = '0', secondText = '0'] = timePart.split(':')
  const hour = Number(hourText)
  const minute = Number(minuteText)
  const second = Number(secondText)
  if (![hour, minute, second].every((item) => Number.isFinite(item))) {
    return 0
  }

  const totalSeconds = hour * 60 * 60 + minute * 60 + second
  return Math.min(Math.max(totalSeconds / SECONDS_PER_DAY, 0), 0.99999)
}

const pickDateLabelPositions = (dateCount: number) => {
  if (dateCount <= MAX_DATE_LABEL_COUNT) {
    return new Set(Array.from({ length: dateCount }, (_unused, index) => index))
  }

  const selectedPositions = new Set<number>()
  const step = Math.ceil(dateCount / MAX_DATE_LABEL_COUNT)

  for (let position = 0; position < dateCount; position += step) {
    selectedPositions.add(position)
  }

  return selectedPositions
}

const buildTimeAxisModel = (points: ElectricMeterPowerConsumeTrendPoint[]) => {
  const uniqueDateKeys: string[] = []
  const datePositionMap = new Map<string, number>()

  points.forEach((item) => {
    const dateKey = toDateKey(item.meterConsumeTime || '--')
    if (!datePositionMap.has(dateKey)) {
      datePositionMap.set(dateKey, uniqueDateKeys.length)
      uniqueDateKeys.push(dateKey)
    }
  })

  const dateLabelPositions = pickDateLabelPositions(uniqueDateKeys.length)
  const seriesData: TrendSeriesPoint[] = points.map((item) => {
    const meterConsumeTime = item.meterConsumeTime || '--'
    const dateKey = toDateKey(meterConsumeTime)
    const datePosition = datePositionMap.get(dateKey) ?? 0

    return {
      value: [datePosition + toDayFraction(meterConsumeTime), toNumber(item.consumePower)],
      meterConsumeTime,
      beginRecordTime: item.beginRecordTime,
      endRecordTime: item.endRecordTime
    }
  })

  return {
    uniqueDateKeys,
    dateLabelPositions,
    seriesData,
    axisMax: Math.max(uniqueDateKeys.length, 1)
  }
}

const createTrendOption = (points: ElectricMeterPowerConsumeTrendPoint[]): EChartsOption => {
  const showSymbol = points.length <= 24
  const { uniqueDateKeys, dateLabelPositions, seriesData, axisMax } = buildTimeAxisModel(points)

  return {
    color: [TREND_COLOR],
    animation: false,
    grid: {
      top: 36,
      right: 20,
      bottom: 56,
      left: 72,
      containLabel: false
    },
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(15, 23, 42, 0.92)',
      borderWidth: 0,
      padding: [10, 12],
      textStyle: {
        color: '#fff',
        fontSize: 12
      },
      formatter: (params: unknown) => {
        const source = Array.isArray(params) ? params[0] : params
        const point =
          typeof source === 'object' && source && 'data' in source ? source.data : undefined
        const meterConsumeTime =
          point && typeof point === 'object' && 'meterConsumeTime' in point
            ? String(point.meterConsumeTime || '--')
            : '--'
        const beginRecordTime =
          point && typeof point === 'object' && 'beginRecordTime' in point
            ? String(point.beginRecordTime || '')
            : ''
        const endRecordTime =
          point && typeof point === 'object' && 'endRecordTime' in point
            ? String(point.endRecordTime || '')
            : ''
        const dataValue =
          point &&
          typeof point === 'object' &&
          'value' in point &&
          Array.isArray(point.value) &&
          point.value.length > 1
            ? point.value[1]
            : '--'

        const intervalText =
          beginRecordTime && endRecordTime ? `<br/>区间：${beginRecordTime} ~ ${endRecordTime}` : ''

        return `${meterConsumeTime}${intervalText}<br/>消费电量：${dataValue ?? '--'} kWh`
      }
    },
    xAxis: {
      type: 'value',
      min: 0,
      max: axisMax,
      interval: 1,
      axisLabel: {
        color: '#64748b',
        fontSize: 12,
        hideOverlap: false,
        formatter: (value: number | string) => {
          const numericValue = Number(value)
          if (!Number.isFinite(numericValue)) {
            return ''
          }

          const position = Math.round(numericValue)
          if (Math.abs(numericValue - position) > 0.0001) {
            return ''
          }

          if (!dateLabelPositions.has(position)) {
            return ''
          }

          return toDateLabel(uniqueDateKeys[position] || '')
        }
      },
      axisLine: {
        lineStyle: {
          color: '#cbd5e1'
        }
      }
    },
    yAxis: {
      type: 'value',
      name: '消费电量(kWh)',
      nameLocation: 'middle',
      nameGap: 52,
      nameRotate: 90,
      nameTextStyle: {
        color: '#475569',
        fontSize: 12
      },
      axisLabel: {
        color: '#64748b',
        fontSize: 12
      },
      splitLine: {
        lineStyle: {
          color: '#e2e8f0'
        }
      }
    },
    series: [
      {
        type: 'line',
        data: seriesData,
        smooth: false,
        showSymbol,
        symbol: 'circle',
        symbolSize: 7,
        lineStyle: {
          width: 2,
          color: TREND_COLOR
        },
        itemStyle: {
          color: TREND_COLOR
        }
      }
    ]
  }
}

export const useElectricMeterTrendChart = (chartRef: Ref<HTMLDivElement | null>) => {
  let chartInstance: ECharts | null = null
  let chartElement: HTMLDivElement | null = null

  const handleResize = () => {
    chartInstance?.resize()
  }

  const ensureChart = () => {
    const currentElement = chartRef.value
    if (!currentElement) {
      return null
    }

    if (chartInstance && chartElement !== currentElement) {
      disposeChart()
    }

    if (!chartInstance) {
      chartInstance = echarts.init(currentElement)
      chartElement = currentElement
      window.addEventListener('resize', handleResize)
    }

    return chartInstance
  }

  const renderChart = async (points: ElectricMeterPowerConsumeTrendPoint[]) => {
    await nextTick()
    const instance = ensureChart()
    if (!instance) {
      return
    }

    instance.setOption(createTrendOption(points), true)
    instance.resize()
  }

  const disposeChart = () => {
    window.removeEventListener('resize', handleResize)
    chartInstance?.dispose()
    chartInstance = null
    chartElement = null
  }

  onBeforeUnmount(() => {
    disposeChart()
  })

  return {
    renderChart,
    disposeChart
  }
}
