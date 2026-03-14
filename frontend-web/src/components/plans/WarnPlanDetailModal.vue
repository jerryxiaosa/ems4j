<script setup lang="ts">
import { computed } from 'vue'
import type { WarnPlanItem } from '@/types/plan'

const props = defineProps<{
  modelValue: boolean
  plan: WarnPlanItem | null
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
    { label: '预警方案名称', value: plan.name || '--' },
    { label: '第一预警余额', value: `${plan.firstLevel ?? '--'}` },
    { label: '第二预警余额', value: `${plan.secondLevel ?? '--'}` },
    { label: '欠费断闸', value: plan.autoClose ? '是' : '否' },
    { label: '创建人', value: plan.createUser || '--' },
    { label: '创建时间', value: plan.createTime || '--' },
    { label: '更新人', value: plan.updateUser || '--' },
    { label: '更新时间', value: plan.updateTime || '--' },
    { label: '备注', value: plan.remark || '--', fullWidth: true }
  ]
})

const close = () => emit('update:modelValue', false)
</script>

<template>
  <Transition name="warn-plan-detail-fade" appear>
    <div v-if="modelValue" class="modal-mask" @click.self.prevent>
      <div class="modal-panel warn-plan-detail-modal">
        <div class="modal-head">
          <h3 class="modal-title">预警方案详情</h3>
          <button class="icon-btn" type="button" @click="close">关闭</button>
        </div>

        <div class="modal-body">
          <div class="summary-grid">
            <div
              v-for="row in summaryRows"
              :key="row.label"
              :class="['summary-item', { 'summary-item-full': row.fullWidth }]"
            >
              <span class="summary-label es-detail-label">{{ row.label }}</span>
              <span
                :class="[
                  'summary-value',
                  'es-detail-value-box',
                  { 'summary-value-remark': row.fullWidth }
                ]"
              >
                {{ row.value }}
              </span>
            </div>
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

.warn-plan-detail-modal {
  display: flex;
  width: min(760px, calc(100vw - 32px));
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
}

.summary-item {
  display: grid;
  grid-template-columns: 96px minmax(0, 1fr);
  align-items: center;
  gap: 2px;
}

.summary-item-full {
  grid-column: 1 / -1;
}

.summary-value {
  width: 100%;
}

.summary-value-remark {
  align-items: flex-start;
  padding-top: 14px;
  padding-bottom: 14px;
  line-height: 1.75;
}

.warn-plan-detail-fade-enter-active,
.warn-plan-detail-fade-leave-active {
  transition: opacity 0.22s ease, transform 0.22s ease;
}

.warn-plan-detail-fade-enter-from,
.warn-plan-detail-fade-leave-to {
  opacity: 0;
}

.warn-plan-detail-fade-enter-from .warn-plan-detail-modal,
.warn-plan-detail-fade-leave-to .warn-plan-detail-modal {
  transform: translateY(10px) scale(0.98);
}
</style>
