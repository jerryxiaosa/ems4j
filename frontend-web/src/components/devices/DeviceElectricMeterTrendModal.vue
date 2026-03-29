<script setup lang="ts">
import { computed, nextTick, reactive, ref, watch } from 'vue'
import { fetchElectricMeterPowerConsumeTrend } from '@/api/adapters/device'
import UiEmptyState from '@/components/common/UiEmptyState.vue'
import UiLoadingState from '@/components/common/UiLoadingState.vue'
import type { ElectricMeterItem } from '@/components/devices/electric-meter.mock'
import type { ElectricMeterPowerConsumeTrendPoint } from '@/types/device'
import { useElectricMeterTrendChart } from '@/modules/devices/electric-meters/composables/useElectricMeterTrendChart'

const props = defineProps<{
  modelValue: boolean
  meter: ElectricMeterItem | null
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
}>()

const chartRef = ref<HTMLDivElement | null>(null)
const trendPoints = ref<ElectricMeterPowerConsumeTrendPoint[]>([])
const loading = ref(false)
const errorText = ref('')
let latestRequestToken = 0
const queryForm = reactive({
  startDate: '',
  endDate: ''
})

const { renderChart, disposeChart } = useElectricMeterTrendChart(chartRef)

const summaryFields = computed(() => {
  const meter = props.meter
  if (!meter) {
    return []
  }

  return [
    { label: '电表名称', value: meter.meterName || '--', secondary: false },
    { label: '电表编号', value: meter.deviceNo || '--', secondary: true },
    { label: '所在位置', value: meter.spaceName || '--', secondary: false },
    { label: '通讯方式', value: meter.communicateModel || '--', secondary: true }
  ]
})

const formatDateInput = (date: Date) => {
  const pad = (value: number) => String(value).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`
}

const formatDateTimeRange = (date: string, isEnd: boolean) => {
  return `${date} ${isEnd ? '23:59:59' : '00:00:00'}`
}

const createDefaultRange = () => {
  const endDate = new Date()
  const startDate = new Date(endDate)
  startDate.setDate(startDate.getDate() - 1)

  queryForm.startDate = formatDateInput(startDate)
  queryForm.endDate = formatDateInput(endDate)
}

const chartReady = computed(() => trendPoints.value.length > 0 && !loading.value && !errorText.value)

const invalidateTrendRequest = () => {
  latestRequestToken += 1
}

const resetTrendState = () => {
  loading.value = false
  errorText.value = ''
  trendPoints.value = []
  disposeChart()
}

const isLatestRequest = (
  requestToken: number,
  meterId: number,
  startDate: string,
  endDate: string
) => {
  return (
    requestToken === latestRequestToken &&
    props.modelValue &&
    props.meter?.id === meterId &&
    queryForm.startDate === startDate &&
    queryForm.endDate === endDate
  )
}

const close = () => {
  emit('update:modelValue', false)
}

const loadTrend = async () => {
  const meterId = props.meter?.id
  const startDate = queryForm.startDate
  const endDate = queryForm.endDate

  if (!meterId || !startDate || !endDate) {
    invalidateTrendRequest()
    resetTrendState()
    return
  }

  const requestToken = ++latestRequestToken
  loading.value = true
  errorText.value = ''

  try {
    const result = await fetchElectricMeterPowerConsumeTrend(meterId, {
      beginTime: formatDateTimeRange(startDate, false),
      endTime: formatDateTimeRange(endDate, true)
    })

    if (!isLatestRequest(requestToken, meterId, startDate, endDate)) {
      return
    }

    trendPoints.value = result
  } catch (error) {
    if (!isLatestRequest(requestToken, meterId, startDate, endDate)) {
      return
    }

    trendPoints.value = []
    errorText.value = error instanceof Error && error.message ? error.message : '趋势数据查询失败'
  }

  if (!isLatestRequest(requestToken, meterId, startDate, endDate)) {
    return
  }

  loading.value = false
  await nextTick()

  if (!isLatestRequest(requestToken, meterId, startDate, endDate)) {
    return
  }

  if (errorText.value || trendPoints.value.length === 0) {
    disposeChart()
    return
  }

  await renderChart(trendPoints.value)

  if (!isLatestRequest(requestToken, meterId, startDate, endDate)) {
    disposeChart()
  }
}

const handleSearch = async () => {
  await loadTrend()
}

const handleReset = async () => {
  createDefaultRange()
  await loadTrend()
}

watch(
  () => [props.modelValue, props.meter?.id] as const,
  async ([visible, meterId], previousValue) => {
    const [previousVisible, previousMeterId] = previousValue ?? [false, undefined]

    if (!visible) {
      invalidateTrendRequest()
      resetTrendState()
      return
    }

    if (!meterId) {
      invalidateTrendRequest()
      resetTrendState()
      return
    }

    if (!previousVisible || previousMeterId !== meterId) {
      createDefaultRange()
      await nextTick()
      await loadTrend()
    }
  },
  { immediate: true }
)
</script>

<template>
  <Transition name="meter-trend-fade" appear>
    <div v-if="modelValue" class="modal-mask" @click.self.prevent>
      <div class="modal-panel meter-trend-modal">
        <div class="modal-head">
          <h3 class="modal-title">用电趋势</h3>
          <button class="icon-btn" type="button" @click="close">关闭</button>
        </div>

        <div class="modal-body">
          <div class="summary-grid summary-grid-relaxed">
            <div
              v-for="field in summaryFields"
              :key="field.label"
              :class="['summary-item', { 'summary-item-secondary': field.secondary }]"
            >
              <span class="summary-label">{{ field.label }}</span>
              <span class="summary-value">{{ field.value }}</span>
            </div>
          </div>

          <div class="toolbar-grid">
            <div class="toolbar-cell">
              <label class="toolbar-field-grid">
                <span class="field-label-inline">开始时间</span>
                <input
                  v-model="queryForm.startDate"
                  class="field-input"
                  type="date"
                  :disabled="loading"
                />
              </label>
            </div>
            <div class="toolbar-cell toolbar-cell-end">
              <label class="toolbar-field-grid toolbar-field-grid-secondary">
                <span class="field-label-inline">结束时间</span>
                <input
                  v-model="queryForm.endDate"
                  class="field-input"
                  type="date"
                  :disabled="loading"
                />
              </label>
              <div class="toolbar-actions">
                <button
                  class="btn btn-primary"
                  type="button"
                  :disabled="loading"
                  @click="handleSearch"
                >
                  查询
                </button>
                <button
                  class="btn btn-secondary"
                  type="button"
                  :disabled="loading"
                  @click="handleReset"
                >
                  重置
                </button>
              </div>
            </div>
          </div>

          <div v-if="loading" class="chart-state">
            <UiLoadingState :size="20" :thickness="2" :min-height="280" />
          </div>
          <div v-else-if="errorText" class="chart-state chart-state-error">
            <p class="error-text">{{ errorText }}</p>
            <button class="btn btn-secondary" type="button" @click="handleSearch">重试</button>
          </div>
          <div v-else-if="!chartReady" class="chart-state">
            <UiEmptyState text="该时间范围暂无用电记录" :min-height="280" />
          </div>
          <div v-else class="chart-panel">
            <div ref="chartRef" class="chart-canvas" data-test="trend-chart" />
          </div>
        </div>
      </div>
    </div>
  </Transition>
</template>

<style scoped>
.modal-mask {
  position: fixed;
  inset: 0;
  z-index: 42;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  background: rgb(15 23 42 / 38%);
}

.meter-trend-modal {
  display: flex;
  width: min(980px, calc(100vw - 32px));
  max-height: calc(100vh - 40px);
  overflow: hidden;
  background: var(--es-color-bg-elevated);
  border: 1px solid var(--es-color-border);
  border-radius: 5px;
  box-shadow: var(--es-shadow-floating);
  flex-direction: column;
}

.modal-head {
  display: flex;
  padding: 12px 16px;
  background: #f8fbff;
  border-bottom: 1px solid var(--es-color-border);
  align-items: center;
  justify-content: space-between;
}

.modal-title {
  margin: 0;
  font-size: var(--es-font-size-md);
  font-weight: 600;
  color: var(--es-color-text-primary);
}

.icon-btn {
  height: 36px;
  min-width: 64px;
  padding: 0 12px;
  font-size: var(--es-font-size-sm);
  color: var(--es-color-text-secondary);
  background: #fff;
  border: 1px solid var(--es-color-border-strong);
  border-radius: 5px;
}

.modal-body {
  display: grid;
  min-height: 0;
  padding: 20px;
  overflow: auto;
  gap: 16px;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px 12px;
}

.summary-grid-relaxed {
  row-gap: 18px;
}

.summary-item {
  display: grid;
  grid-template-columns: 72px minmax(0, 1fr);
  gap: 8px;
  align-items: center;
}

.summary-item-secondary {
  margin-left: -20px;
}

.summary-label {
  font-size: var(--es-font-size-sm);
  color: var(--es-color-text-secondary);
}

.summary-value {
  min-width: 0;
  font-size: var(--es-font-size-sm);
  color: var(--es-color-text-primary);
  word-break: break-all;
}

.toolbar-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px 12px;
  align-items: center;
}

.toolbar-cell {
  min-width: 0;
}

.toolbar-cell-end {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 16px;
  align-items: center;
}

.toolbar-field-grid {
  display: grid;
  grid-template-columns: 72px minmax(0, 1fr);
  gap: 8px;
  align-items: center;
  min-width: 0;
}

.toolbar-field-grid-secondary {
  margin-left: -20px;
}

.field-label-inline {
  white-space: nowrap;
  font-size: var(--es-font-size-sm);
  color: var(--es-color-text-secondary);
}

.field-input {
  width: 176px;
  height: 36px;
  padding: 0 10px;
  font-size: var(--es-font-size-sm);
  color: var(--es-color-text-primary);
  background: #fff;
  border: 1px solid var(--es-color-border-strong);
  border-radius: 5px;
}

.toolbar-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
  justify-content: flex-end;
}

.btn {
  display: inline-flex;
  height: 36px;
  min-width: 72px;
  padding: 0 12px;
  font-size: var(--es-font-size-sm);
  border: 1px solid transparent;
  border-radius: 5px;
  transition: background-color 0.2s ease, border-color 0.2s ease, color 0.2s ease;
  align-items: center;
  justify-content: center;
}

.btn-primary {
  color: #fff;
  background: var(--es-color-primary);
}

.btn-primary:hover {
  background: var(--es-color-primary-hover);
  border-color: var(--es-color-primary-hover);
}

.btn-secondary {
  color: var(--es-color-text-secondary);
  background: #f8fbff;
  border-color: var(--es-color-border-strong);
}

.btn-secondary:hover {
  color: var(--es-color-primary);
  background: #eff6ff;
  border-color: #93c5fd;
}

.chart-panel,
.chart-state {
  min-height: 480px;
  border: 1px solid var(--es-color-border);
  border-radius: 5px;
  background: #fff;
}

.chart-state {
  display: flex;
  align-items: center;
  justify-content: center;
}

.chart-state-error {
  gap: 12px;
  flex-direction: column;
}

.error-text {
  margin: 0;
  font-size: var(--es-font-size-sm);
  color: var(--es-color-danger);
}

.chart-canvas {
  width: 100%;
  height: 480px;
}

.meter-trend-fade-enter-active,
.meter-trend-fade-leave-active {
  transition: opacity 0.2s ease;
}

.meter-trend-fade-enter-from,
.meter-trend-fade-leave-to {
  opacity: 0;
}

.meter-trend-fade-enter-from .meter-trend-modal,
.meter-trend-fade-leave-to .meter-trend-modal {
  transform: translateY(8px);
}

.icon-btn:focus-visible,
.field-input:focus-visible,
.btn:focus-visible {
  outline: 2px solid var(--es-color-primary);
  outline-offset: 2px;
}

button:disabled {
  cursor: not-allowed;
  opacity: 0.6;
}

@media (width <= 900px) {
  .summary-grid {
    grid-template-columns: minmax(0, 1fr);
  }

  .summary-item {
    grid-template-columns: 72px minmax(0, 1fr);
  }

  .toolbar-grid {
    grid-template-columns: minmax(0, 1fr);
  }

  .toolbar-cell-end {
    gap: 12px;
    width: 100%;
    grid-template-columns: minmax(0, 1fr);
  }

  .toolbar-field-grid {
    width: 100%;
  }

  .summary-item-secondary,
  .toolbar-field-grid-secondary {
    margin-left: 0;
  }

  .field-input {
    width: 100%;
  }

  .toolbar-actions {
    width: 100%;
    justify-content: flex-end;
  }
}
</style>
