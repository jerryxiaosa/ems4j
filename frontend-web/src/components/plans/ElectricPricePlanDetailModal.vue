<script setup lang="ts">
import { computed } from 'vue'
import type { ElectricPricePlanItem } from '@/types/electric-price-plan'

const props = defineProps<{
  modelValue: boolean
  plan: ElectricPricePlanItem | null
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
}>()

const summaryRows = computed(() => {
  const plan = props.plan
  if (!plan) {
    return []
  }

  return [
    { label: '电价方案名称', value: plan.name || '--' },
    { label: '尖', value: `${plan.priceHigher ?? '--'}` },
    { label: '峰', value: `${plan.priceHigh ?? '--'}` },
    { label: '平', value: `${plan.priceLow ?? '--'}` },
    { label: '谷', value: `${plan.priceLower ?? '--'}` },
    { label: '深谷', value: `${plan.priceDeepLow ?? '--'}` },
    { label: '创建时间', value: plan.createTime || '--' },
    { label: '创建人', value: plan.createUser || '--' },
    { label: '修改时间', value: plan.updateTime || '--' },
    { label: '修改人', value: plan.updateUser || '--' },
    { label: '是否阶梯电费', value: plan.hasStepPrice ? '是' : '否' }
  ]
})

const stepRows = computed(() => {
  const plan = props.plan
  if (!plan || !plan.hasStepPrice) {
    return []
  }

  return [
    {
      label: '第一阶梯',
      range: `0 ~ ${plan.step1End ?? '--'}`,
      ratio: plan.step1Ratio ?? '--'
    },
    {
      label: '第二阶梯',
      range: `${plan.step1End ?? '--'} ~ ${plan.step2End ?? '--'}`,
      ratio: plan.step2Ratio ?? '--'
    },
    {
      label: '第三阶梯',
      range: `${plan.step2End ?? '--'} ~ ${plan.step3End ?? '以上'}`,
      ratio: plan.step3Ratio ?? '--'
    }
  ]
})

const close = () => emit('update:modelValue', false)
</script>

<template>
  <Transition name="electric-plan-detail-fade" appear>
    <div v-if="modelValue" class="modal-mask" @click.self.prevent>
      <div class="modal-panel electric-plan-detail-modal">
        <div class="modal-head">
          <h3 class="modal-title">电价方案详情</h3>
          <button class="icon-btn" type="button" @click="close">关闭</button>
        </div>

        <div class="modal-body">
          <div class="summary-grid">
            <div v-for="row in summaryRows" :key="row.label" class="summary-item">
              <span class="summary-label es-detail-label">{{ row.label }}</span>
              <span class="summary-value es-detail-value-box">{{ row.value }}</span>
            </div>
          </div>

          <template v-if="stepRows.length">
            <h4 class="es-detail-section-title">阶梯电费</h4>
            <div class="es-detail-table-wrap">
              <table class="es-detail-table">
                <thead>
                  <tr>
                    <th class="table-col-step">阶梯</th>
                    <th>范围（度）</th>
                    <th>倍率</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="row in stepRows" :key="row.label">
                    <td>{{ row.label }}</td>
                    <td>{{ row.range }}</td>
                    <td>{{ row.ratio }}</td>
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

.electric-plan-detail-modal {
  display: flex;
  width: min(820px, calc(100vw - 32px));
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
  min-height: 0;
  padding: 20px 20px 32px;
  overflow: auto;
  flex: 1;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px 56px;
  margin-bottom: 28px;
}

.summary-item {
  display: grid;
  grid-template-columns: 84px minmax(0, 1fr);
  align-items: center;
  gap: 2px;
}

.summary-item-full {
  grid-column: 1 / -1;
}

.summary-value {
  width: 100%;
}

.table-col-step {
  width: 120px;
}

.electric-plan-detail-fade-enter-active,
.electric-plan-detail-fade-leave-active {
  transition: opacity 0.22s ease, transform 0.22s ease;
}

.electric-plan-detail-fade-enter-from,
.electric-plan-detail-fade-leave-to {
  opacity: 0;
}

.electric-plan-detail-fade-enter-from .electric-plan-detail-modal,
.electric-plan-detail-fade-leave-to .electric-plan-detail-modal {
  transform: translateY(10px) scale(0.98);
}
</style>
