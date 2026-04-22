<script setup lang="ts">
import { computed } from 'vue'
import UiEmptyState from '@/components/common/UiEmptyState.vue'
import UiErrorState from '@/components/common/UiErrorState.vue'
import UiLoadingState from '@/components/common/UiLoadingState.vue'
import type { MeterConsumeDetail } from '@/api/adapters/trade'

interface DetailCell {
  label: string
  value: string
}

const props = defineProps<{
  modelValue: boolean
  detail: MeterConsumeDetail | null
  loading?: boolean
  errorText?: string
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
}>()

const buildRows = (cells: DetailCell[]) => {
  const rows: Array<[DetailCell, DetailCell | null]> = []
  for (let index = 0; index < cells.length; index += 2) {
    rows.push([cells[index], cells[index + 1] ?? null])
  }
  return rows
}

const basicRows = computed(() => {
  const detail = props.detail
  if (!detail) {
    return []
  }
  return buildRows([
    { label: '消费编号', value: detail.consumeNo },
    { label: '账户名称', value: detail.ownerName },
    { label: '电表名称', value: detail.meterName },
    { label: '电表编号', value: detail.deviceNo },
    { label: '所在位置', value: detail.spaceName },
    { label: '消费时间', value: detail.consumeTime },
    { label: '处理时间', value: detail.processTime }
  ])
})

const amountRows = computed(() => {
  const detail = props.detail
  if (!detail) {
    return []
  }
  return buildRows([
    { label: '消费前余额（元）', value: detail.beginBalance },
    { label: '消费后余额（元）', value: detail.endBalance },
    { label: '消费总金额（元）', value: detail.consumeAmount },
    { label: '尖消费金额（元）', value: detail.consumeAmountHigher },
    { label: '峰消费金额（元）', value: detail.consumeAmountHigh },
    { label: '平消费金额（元）', value: detail.consumeAmountLow },
    { label: '谷消费金额（元）', value: detail.consumeAmountLower },
    { label: '深谷消费金额（元）', value: detail.consumeAmountDeepLow }
  ])
})

const powerRows = computed(() => {
  const detail = props.detail
  if (!detail) {
    return []
  }
  return buildRows([
    { label: '起始记录时间', value: detail.beginRecordTime },
    { label: '结束记录时间', value: detail.endRecordTime },
    { label: '开始总电量', value: detail.beginPower },
    { label: '结束总电量', value: detail.endPower },
    { label: '尖结束电量', value: detail.endPowerHigher },
    { label: '峰结束电量', value: detail.endPowerHigh },
    { label: '平结束电量', value: detail.endPowerLow },
    { label: '谷结束电量', value: detail.endPowerLower },
    { label: '深谷结束电量', value: detail.endPowerDeepLow },
    { label: '消费总电量', value: detail.consumePower },
    { label: '尖消耗电量', value: detail.consumePowerHigher },
    { label: '峰消耗电量', value: detail.consumePowerHigh },
    { label: '平消耗电量', value: detail.consumePowerLow },
    { label: '谷消耗电量', value: detail.consumePowerLower },
    { label: '深谷消耗电量', value: detail.consumePowerDeepLow }
  ])
})

const priceRows = computed(() => {
  const detail = props.detail
  if (!detail) {
    return []
  }
  return buildRows([
    { label: '阶梯起始值', value: detail.stepStartValue },
    { label: '历史电量偏移', value: detail.historyPowerOffset },
    { label: '阶梯倍率', value: detail.stepRate },
    { label: '尖单价（元）', value: detail.priceHigher },
    { label: '峰单价（元）', value: detail.priceHigh },
    { label: '平单价（元）', value: detail.priceLow },
    { label: '谷单价（元）', value: detail.priceLower },
    { label: '深谷单价（元）', value: detail.priceDeepLow }
  ])
})

const close = () => emit('update:modelValue', false)
</script>

<template>
  <Transition name="consume-detail-fade" appear>
    <div v-if="modelValue" class="modal-mask" @click.self.prevent>
      <div class="modal-panel consume-detail-modal">
        <div class="modal-head">
          <h3 class="modal-title">消费记录详情</h3>
          <button class="icon-btn" type="button" @click="close">关闭</button>
        </div>

        <div v-if="loading" class="modal-body modal-body-loading">
          <UiLoadingState :size="20" :thickness="2" :min-height="180" />
        </div>
        <div v-else class="modal-body">
          <UiErrorState v-if="errorText" :text="errorText" :min-height="72" />
          <UiEmptyState v-else-if="!detail" text="暂无详情数据" :min-height="72" />
          <template v-else>
            <h4 class="section-title es-detail-section-title">基本信息</h4>
            <div class="summary-grid section-grid">
              <div
                v-for="[leftCell, rightCell] in basicRows"
                :key="leftCell.label"
                class="summary-row"
              >
                <div class="summary-item">
                  <span class="summary-label es-detail-label">{{ leftCell.label }}</span>
                  <span class="summary-value es-detail-value-box">{{ leftCell.value }}</span>
                </div>
                <div v-if="rightCell" class="summary-item">
                  <span class="summary-label es-detail-label">{{ rightCell.label }}</span>
                  <span class="summary-value es-detail-value-box">{{ rightCell.value }}</span>
                </div>
              </div>
            </div>

            <h4 class="section-title es-detail-section-title">余额与金额</h4>
            <div class="summary-grid section-grid">
              <div
                v-for="[leftCell, rightCell] in amountRows"
                :key="leftCell.label"
                class="summary-row"
              >
                <div class="summary-item">
                  <span class="summary-label es-detail-label">{{ leftCell.label }}</span>
                  <span class="summary-value es-detail-value-box">{{ leftCell.value }}</span>
                </div>
                <div v-if="rightCell" class="summary-item">
                  <span class="summary-label es-detail-label">{{ rightCell.label }}</span>
                  <span class="summary-value es-detail-value-box">{{ rightCell.value }}</span>
                </div>
              </div>
            </div>

            <h4 class="section-title es-detail-section-title">电量明细</h4>
            <div class="summary-grid section-grid">
              <div
                v-for="[leftCell, rightCell] in powerRows"
                :key="leftCell.label"
                class="summary-row"
              >
                <div class="summary-item">
                  <span class="summary-label es-detail-label">{{ leftCell.label }}</span>
                  <span class="summary-value es-detail-value-box">{{ leftCell.value }}</span>
                </div>
                <div v-if="rightCell" class="summary-item">
                  <span class="summary-label es-detail-label">{{ rightCell.label }}</span>
                  <span class="summary-value es-detail-value-box">{{ rightCell.value }}</span>
                </div>
              </div>
            </div>

            <h4 class="section-title es-detail-section-title">计价参数</h4>
            <div class="summary-grid latest-grid">
              <div
                v-for="[leftCell, rightCell] in priceRows"
                :key="leftCell.label"
                class="summary-row"
              >
                <div class="summary-item">
                  <span class="summary-label es-detail-label">{{ leftCell.label }}</span>
                  <span class="summary-value es-detail-value-box">{{ leftCell.value }}</span>
                </div>
                <div v-if="rightCell" class="summary-item">
                  <span class="summary-label es-detail-label">{{ rightCell.label }}</span>
                  <span class="summary-value es-detail-value-box">{{ rightCell.value }}</span>
                </div>
              </div>
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

.consume-detail-modal {
  display: flex;
  width: min(900px, calc(100vw - 32px));
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
  padding: 20px 20px 32px;
  overflow: auto;
}

.modal-body-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 160px;
}

.summary-grid {
  display: grid;
  gap: 10px;
}

.summary-row {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px 56px;
}

.summary-item {
  display: grid;
  grid-template-columns: 120px minmax(0, 1fr);
  align-items: center;
  gap: 2px;
}

.summary-label {
  white-space: nowrap;
}

.summary-value {
  width: 100%;
  font-size: var(--es-font-size-sm);
  color: var(--es-color-text-primary);
  word-break: break-all;
}

.section-title {
  margin: 0 0 14px;
  font-size: 14px;
  font-weight: 600;
  color: var(--es-color-text-primary);
}

.section-grid {
  margin-bottom: 28px;
}

.latest-grid {
  margin-bottom: 8px;
}

.consume-detail-fade-enter-active,
.consume-detail-fade-leave-active {
  transition: opacity 0.22s ease, transform 0.22s ease;
}

.consume-detail-fade-enter-from,
.consume-detail-fade-leave-to {
  opacity: 0;
}

.consume-detail-fade-enter-from .consume-detail-modal,
.consume-detail-fade-leave-to .consume-detail-modal {
  transform: translateY(10px) scale(0.98);
}
</style>
