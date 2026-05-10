<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(
  defineProps<{
    canvasId?: string
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

const chartInset = {
  top: 18,
  right: 20,
  bottom: 58,
  left: 70
}

const chartStyle = computed(() => ({
  width: `${props.widthRpx}rpx`,
  height: `${props.heightRpx}rpx`
}))

const plotWidth = computed(() => props.widthRpx - chartInset.left - chartInset.right)
const plotHeight = computed(() => props.heightRpx - chartInset.top - chartInset.bottom)
const plotBottom = computed(() => chartInset.top + plotHeight.value)

const maxValue = computed(() => {
  if (typeof props.max === 'number' && props.max > 0) {
    return props.max
  }

  return Math.max(...props.values, 1)
})

const yTicks = computed(() => {
  const splitCount = 4

  return Array.from({ length: splitCount + 1 }, (_, index) => {
    const ratio = index / splitCount
    const value = Math.round(maxValue.value * (1 - ratio))
    const y = chartInset.top + plotHeight.value * ratio

    return {
      value,
      label: String(value),
      style: {
        top: `${y}rpx`
      }
    }
  })
})

const chartPoints = computed(() => {
  const lastIndex = Math.max(props.values.length - 1, 1)

  return props.values.map((value, index) => {
    const ratio = Math.max(0, Math.min(value / maxValue.value, 1))
    const x = chartInset.left + plotWidth.value * (index / lastIndex)
    const y = chartInset.top + plotHeight.value * (1 - ratio)

    return {
      x,
      y,
      value,
      category: props.categories[index] ?? '',
      columnStyle: {
        left: `${x - 7}rpx`,
        top: `${y}rpx`,
        height: `${plotBottom.value - y}rpx`
      },
      labelStyle: {
        left: `${x}rpx`
      }
    }
  })
})

const lineSegments = computed(() => {
  return chartPoints.value.slice(0, -1).map((point, index) => {
    const nextPoint = chartPoints.value[index + 1]
    const deltaX = nextPoint.x - point.x
    const deltaY = nextPoint.y - point.y
    const width = Math.sqrt(deltaX * deltaX + deltaY * deltaY)
    const angle = Math.atan2(deltaY, deltaX) * (180 / Math.PI)

    return {
      style: {
        left: `${point.x}rpx`,
        top: `${point.y}rpx`,
        width: `${width}rpx`,
        transform: `rotate(${angle}deg)`,
        background: props.color
      }
    }
  })
})
</script>

<template>
  <view class="trend-line-chart-wrap" :style="chartStyle">
    <view
      v-for="tick in yTicks"
      :key="tick.label"
      class="chart-grid-line"
      :style="tick.style"
    ></view>
    <view
      v-for="tick in yTicks"
      :key="`label-${tick.label}`"
      class="y-axis-label"
      :style="tick.style"
    >
      {{ tick.label }}
    </view>

    <view class="y-axis-line"></view>
    <view class="x-axis-line"></view>

    <view
      v-for="point in chartPoints"
      :key="`column-${point.category}`"
      class="chart-column"
      :style="point.columnStyle"
    ></view>

    <view
      v-for="(line, index) in lineSegments"
      :key="`line-${index}`"
      class="chart-line"
      :style="line.style"
    ></view>

    <view
      v-for="point in chartPoints"
      :key="`label-${point.category}`"
      class="x-axis-label"
      :style="point.labelStyle"
    >
      {{ point.category }}
    </view>
  </view>
</template>

<style scoped lang="scss">
.trend-line-chart-wrap {
  position: relative;
  overflow: visible;
}

.chart-grid-line {
  position: absolute;
  right: 20rpx;
  left: 70rpx;
  height: 1rpx;
  background: #f1f5f9;
  transform: translateY(-0.5rpx);
}

.y-axis-label {
  position: absolute;
  left: 0;
  width: 56rpx;
  color: #6a7a8f;
  font-size: 20rpx;
  line-height: 20rpx;
  text-align: right;
  transform: translateY(-10rpx);
}

.y-axis-line {
  position: absolute;
  top: 18rpx;
  bottom: 58rpx;
  left: 70rpx;
  width: 2rpx;
  background: rgba(106, 122, 143, 0.22);
}

.x-axis-line {
  position: absolute;
  right: 20rpx;
  bottom: 58rpx;
  left: 70rpx;
  height: 1rpx;
  background: #f1f5f9;
}

.chart-column {
  position: absolute;
  width: 14rpx;
  min-height: 12rpx;
  background: rgba(0, 74, 198, 0.12);
  border-radius: 999rpx 999rpx 0 0;
}

.chart-line {
  position: absolute;
  height: 5rpx;
  border-radius: 999rpx;
  transform-origin: left center;
}

.x-axis-label {
  position: absolute;
  bottom: 16rpx;
  color: #6a7a8f;
  font-size: 22rpx;
  line-height: 24rpx;
  transform: translateX(-50%);
}
</style>
