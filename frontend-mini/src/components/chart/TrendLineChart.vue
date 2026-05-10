<script setup lang="ts">
import { computed, getCurrentInstance, nextTick, onMounted, watch } from 'vue'
import UCharts from '@qiun/ucharts'

const props = withDefaults(
  defineProps<{
    canvasId: string
    categories: string[]
    values: number[]
    seriesName: string
    unit?: string
    color?: string
    widthRpx?: number
    heightRpx?: number
    max?: number
  }>(),
  {
    color: '#004AC6',
    widthRpx: 646,
    heightRpx: 421
  }
)

let chart: UCharts | null = null

const componentInstance = getCurrentInstance()

const chartWidthPx = computed(() => rpxToPx(props.widthRpx))
const chartHeightPx = computed(() => rpxToPx(props.heightRpx))
const canvasStyle = computed(() => ({
  width: `${props.widthRpx}rpx`,
  height: `${props.heightRpx}rpx`
}))

const rpxToPx = (rpx: number) => {
  const systemInfo = uni.getSystemInfoSync()
  return Math.round((systemInfo.windowWidth / 750) * rpx)
}

const findMaxValue = () => {
  if (typeof props.max === 'number') {
    return props.max
  }

  const maxValue = Math.max(...props.values, 0)
  if (maxValue <= 10) {
    return Math.ceil(maxValue / 2) * 2 || 10
  }

  return Math.ceil(maxValue / 100) * 100
}

const buildChartOptions = () => ({
  type: 'mix',
  context: uni.createCanvasContext(props.canvasId, componentInstance?.proxy),
  canvasId: props.canvasId,
  width: chartWidthPx.value,
  height: chartHeightPx.value,
  categories: props.categories,
  series: [
    {
      name: props.seriesName,
      type: 'column',
      data: props.values,
      color: 'rgba(0, 74, 198, 0.12)',
      disableLegend: true
    },
    {
      name: props.seriesName,
      type: 'line',
      data: props.values,
      color: props.color,
      pointShape: 'circle'
    }
  ],
  animation: true,
  background: '#ffffff',
  color: [props.color],
  padding: [12, 24, 6, 4],
  fontSize: 10,
  fontColor: '#6A7A8F',
  dataLabel: false,
  dataPointShape: true,
  enableScroll: false,
  legend: {
    show: false
  },
  xAxis: {
    disableGrid: true,
    axisLine: false,
    boundaryGap: 'justify',
    fontColor: '#6A7A8F',
    fontSize: 10,
    lineHeight: 16,
    marginTop: 8
  },
  yAxis: {
    splitNumber: 4,
    gridType: 'solid',
    gridColor: '#F1F5F9',
    dashLength: 0,
    fontColor: '#6A7A8F',
    fontSize: 10,
    padding: 8,
    data: [
      {
        min: 0,
        max: findMaxValue(),
        unit: '',
        tofix: 0
      }
    ]
  },
  extra: {
    mix: {
      column: {
        width: 8,
        barBorderCircle: true,
        seriesGap: 0,
        categoryGap: 6
      },
      line: {
        width: 2.5
      }
    },
    line: {
      type: 'curve',
      width: 2.5,
      activeType: 'hollow',
      linearType: 'none',
      onShadow: false
    },
    tooltip: {
      showBox: true,
      bgColor: '#FFFFFF',
      bgOpacity: 1,
      borderColor: '#E7E7F3',
      borderWidth: 1,
      fontColor: '#152234',
      fontSize: 11,
      legendShape: 'circle'
    }
  }
})

const renderChart = () => {
  if (!props.categories.length || !props.values.length) {
    return
  }

  chart = new UCharts(buildChartOptions())
}

const updateChart = () => {
  if (!chart) {
    renderChart()
    return
  }

  chart.updateData({
    categories: props.categories,
    series: [
      {
        name: props.seriesName,
        type: 'column',
        data: props.values,
        color: 'rgba(0, 74, 198, 0.12)',
        disableLegend: true
      },
      {
        name: props.seriesName,
        type: 'line',
        data: props.values,
        color: props.color,
        pointShape: 'circle'
      }
    ],
    yAxis: buildChartOptions().yAxis
  })
}

const handleTouchStart = (event: unknown) => {
  chart?.showToolTip(event)
}

onMounted(() => {
  nextTick(renderChart)
})

watch(
  () => [props.categories, props.values, props.seriesName, props.unit, props.color, props.max],
  () => {
    nextTick(updateChart)
  },
  { deep: true }
)
</script>

<template>
  <view class="trend-line-chart-wrap" :style="canvasStyle">
    <canvas
      class="trend-line-chart"
      :canvas-id="canvasId"
      :id="canvasId"
      :style="canvasStyle"
      @touchstart="handleTouchStart"
      @touchmove="handleTouchStart"
    ></canvas>
  </view>
</template>

<style scoped lang="scss">
.trend-line-chart-wrap {
  position: relative;
}

.trend-line-chart {
  display: block;
}
</style>
