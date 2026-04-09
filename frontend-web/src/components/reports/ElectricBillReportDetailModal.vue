<script setup lang="ts">
import { computed } from 'vue'
import UiEmptyState from '@/components/common/UiEmptyState.vue'
import UiLoadingState from '@/components/common/UiLoadingState.vue'
import type { ElectricBillReportDetailResult } from '@/types/report'

const props = withDefaults(
  defineProps<{
    modelValue: boolean
    detail: ElectricBillReportDetailResult | null
    loading?: boolean
  }>(),
  {
    loading: false
  }
)

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
}>()

const accountInfo = computed(() => props.detail?.accountInfo ?? null)
const meterList = computed(() => props.detail?.meterList ?? [])

const close = () => {
  emit('update:modelValue', false)
}

const displayText = (value: string | number | null | undefined) => {
  if (typeof value === 'number' && Number.isFinite(value)) {
    return String(value)
  }
  if (typeof value === 'string' && value.trim()) {
    return value
  }
  return '—'
}

const accountSummaryRows = computed(() => {
  const detail = accountInfo.value
  if (!detail) {
    return []
  }

  return [
    [
      { label: '账户名称', value: detail.accountName },
      { label: '联系人', value: displayText(detail.contactName) },
      { label: '联系方式', value: displayText(detail.contactPhone) }
    ],
    [
      { label: '电价计费类型', value: detail.electricAccountTypeName },
      { label: '包月费用', value: displayText(detail.monthlyPayAmountText) },
      { label: '账户余额', value: displayText(detail.accountBalanceText) }
    ],
    [
      { label: '电表数量', value: String(detail.meterCount) },
      { label: '本期电量(kWh)', value: detail.periodConsumePowerText },
      { label: '本期电费', value: detail.periodElectricChargeAmountText }
    ],
    [
      { label: '本期充值', value: detail.periodRechargeAmountText },
      { label: '本期补正', value: detail.periodCorrectionAmountText },
      { label: '统计日期', value: detail.dateRangeText }
    ]
  ]
})

type MeterColumn = {
  key: keyof (typeof meterList.value)[number]
  label: string
  width: number
  stickyLeft?: number
  stickyRight?: number
}

const meterColumns: MeterColumn[] = [
  { key: 'deviceNo', label: '电表编号', width: 140, stickyLeft: 0 },
  { key: 'meterName', label: '电表名称', width: 140, stickyLeft: 140 },
  { key: 'consumePowerHigherText', label: '尖电量(kWh)', width: 108 },
  { key: 'consumePowerHighText', label: '峰电量(kWh)', width: 108 },
  { key: 'consumePowerLowText', label: '平电量(kWh)', width: 108 },
  { key: 'consumePowerLowerText', label: '谷电量(kWh)', width: 108 },
  { key: 'consumePowerDeepLowText', label: '深谷电量(kWh)', width: 116 },
  { key: 'displayPriceHigherText', label: '尖单价', width: 86 },
  { key: 'displayPriceHighText', label: '峰单价', width: 86 },
  { key: 'displayPriceLowText', label: '平单价', width: 86 },
  { key: 'displayPriceLowerText', label: '谷单价', width: 86 },
  { key: 'displayPriceDeepLowText', label: '深谷单价', width: 86 },
  { key: 'electricChargeAmountHigherText', label: '尖电费', width: 96 },
  { key: 'electricChargeAmountHighText', label: '峰电费', width: 96 },
  { key: 'electricChargeAmountLowText', label: '平电费', width: 96 },
  { key: 'electricChargeAmountLowerText', label: '谷电费', width: 96 },
  { key: 'electricChargeAmountDeepLowText', label: '深谷电费', width: 96 },
  { key: 'totalConsumePowerText', label: '总电量(kWh)', width: 118, stickyRight: 334 },
  { key: 'totalElectricChargeAmountText', label: '总电费', width: 108, stickyRight: 216 },
  { key: 'totalRechargeAmountText', label: '总充值', width: 108, stickyRight: 108 },
  { key: 'totalCorrectionAmountText', label: '总补正', width: 108, stickyRight: 0 }
]

const getColumnStyle = (column: MeterColumn) => {
  const style: Record<string, string> = {
    width: `${column.width}px`,
    minWidth: `${column.width}px`
  }

  if (typeof column.stickyLeft === 'number') {
    style.left = `${column.stickyLeft}px`
  }

  if (typeof column.stickyRight === 'number') {
    style.right = `${column.stickyRight}px`
  }

  return style
}

const getColumnClass = (column: MeterColumn) => ({
  'is-sticky-left': typeof column.stickyLeft === 'number',
  'is-sticky-right': typeof column.stickyRight === 'number'
})
</script>

<template>
  <Transition name="report-detail-fade" appear>
    <div v-if="modelValue" class="modal-mask" @click.self.prevent>
      <div class="modal-panel report-detail-modal">
        <div class="modal-head">
          <h3 class="modal-title">电费报表详情</h3>
          <button class="icon-btn" type="button" @click="close">关闭</button>
        </div>

        <div v-if="loading" class="modal-body modal-body-loading">
          <UiLoadingState :size="20" :thickness="2" :min-height="180" />
        </div>
        <div v-else class="modal-body">
          <UiEmptyState v-if="!detail" text="暂无详情数据" :min-height="120" />
          <template v-else>
            <h4 class="section-title es-detail-section-title">账户信息</h4>
            <div class="summary-grid">
              <div
                v-for="(row, rowIndex) in accountSummaryRows"
                :key="rowIndex"
                class="summary-row summary-row-three"
              >
                <div v-for="cell in row" :key="cell.label" class="summary-item">
                  <span class="summary-label es-detail-label">{{ cell.label }}</span>
                  <span class="summary-value es-detail-value-box">{{ cell.value }}</span>
                </div>
              </div>
            </div>

            <div class="meter-header">
              <h4 class="section-title es-detail-section-title">电表信息</h4>
              <p class="meter-tip">单价展示为统计结束日有效单价，金额按统计区间内每日实际结算累计。</p>
            </div>

            <div class="table-wrap meter-table-wrap">
              <table class="meter-table">
                <thead>
                  <tr>
                    <th
                      v-for="column in meterColumns"
                      :key="column.key"
                      :class="getColumnClass(column)"
                      :style="getColumnStyle(column)"
                    >
                      {{ column.label }}
                    </th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-if="meterList.length === 0">
                    <td :colspan="meterColumns.length" class="empty">
                      <UiEmptyState :min-height="72" />
                    </td>
                  </tr>
                  <tr v-for="item in meterList" :key="item.meterId">
                    <td
                      v-for="column in meterColumns"
                      :key="column.key"
                      :class="getColumnClass(column)"
                      :style="getColumnStyle(column)"
                    >
                      {{ displayText(item[column.key]) }}
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </template>
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

.report-detail-modal {
  display: flex;
  width: min(1160px, calc(100vw - 32px));
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
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: #f8fbff;
  border-bottom: 1px solid var(--es-color-border);
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
  flex: 1;
  min-height: 0;
  padding: 20px 20px 28px;
  overflow: auto;
}

.modal-body-loading {
  display: flex;
  align-items: center;
  justify-content: center;
}

.summary-grid {
  display: grid;
  gap: 10px;
  margin-bottom: 22px;
}

.summary-row {
  display: grid;
  gap: 20px;
}

.summary-row-three {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.summary-item {
  display: grid;
  grid-template-columns: 108px minmax(0, 1fr);
  align-items: center;
  gap: 2px;
  min-width: 0;
}

.summary-label {
  color: var(--es-color-text-secondary);
  white-space: nowrap;
}

.summary-value {
  color: var(--es-color-text-primary);
  word-break: break-all;
}

.section-title {
  margin: 0 0 10px;
  font-size: var(--es-font-size-md);
  font-weight: 600;
  color: var(--es-color-text-primary);
}

.meter-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 10px;
}

.meter-tip {
  margin: 0;
  font-size: var(--es-font-size-xs);
  color: var(--es-color-text-placeholder);
  text-align: right;
}

.table-wrap {
  overflow: auto;
  border: 1px solid var(--es-color-border);
  border-radius: 5px;
}

.meter-table {
  width: 100%;
  min-width: 2050px;
  border-collapse: collapse;
}

.meter-table th,
.meter-table td {
  padding: 10px;
  font-size: var(--es-font-size-sm);
  color: var(--es-color-text-secondary);
  text-align: left;
  border-bottom: 1px solid var(--es-color-border);
  border-right: 1px solid var(--es-color-border);
  white-space: nowrap;
}

.meter-table th:last-child,
.meter-table td:last-child {
  border-right: none;
}

.meter-table thead th {
  font-weight: 500;
  color: #1e3a8a;
  background: var(--es-color-table-header-bg);
}

.meter-table th.is-sticky-left,
.meter-table td.is-sticky-left,
.meter-table th.is-sticky-right,
.meter-table td.is-sticky-right {
  position: sticky;
  z-index: 1;
  background: #fff;
}

.meter-table thead th.is-sticky-left,
.meter-table thead th.is-sticky-right {
  z-index: 3;
  background: var(--es-color-table-header-bg);
}

.meter-table th.is-sticky-left,
.meter-table td.is-sticky-left {
  box-shadow: 1px 0 0 var(--es-color-border);
}

.meter-table th.is-sticky-right,
.meter-table td.is-sticky-right {
  box-shadow: -1px 0 0 var(--es-color-border);
}

.empty {
  padding: 0;
}

.report-detail-fade-enter-active,
.report-detail-fade-leave-active {
  transition: opacity 0.22s ease, transform 0.22s ease;
}

.report-detail-fade-enter-from,
.report-detail-fade-leave-to {
  opacity: 0;
}

.report-detail-fade-enter-from .report-detail-modal,
.report-detail-fade-leave-to .report-detail-modal {
  transform: translateY(10px) scale(0.98);
}

@media (width <= 1100px) {
  .summary-row-three {
    grid-template-columns: minmax(0, 1fr);
  }

  .meter-header {
    align-items: flex-start;
    flex-direction: column;
  }

  .meter-tip {
    text-align: left;
  }
}
</style>
