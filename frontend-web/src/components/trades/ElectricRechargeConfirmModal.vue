<script setup lang="ts">
const props = defineProps<{
  modelValue: boolean
  meterName: string
  meterDeviceNo: string
  amountText: string
  submitting?: boolean
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  confirm: []
}>()

const close = () => {
  if (props.submitting) {
    return
  }
  emit('update:modelValue', false)
}

const handleConfirm = () => {
  if (props.submitting) {
    return
  }
  emit('confirm')
}
</script>

<template>
  <Transition name="recharge-confirm-fade" appear>
    <div v-if="modelValue" class="modal-mask" @click.self.prevent>
      <div class="modal-panel">
        <div class="modal-head">
          <h3 class="modal-title">请确认充值的电表</h3>
          <button class="icon-btn" type="button" :disabled="submitting" @click="close">关闭</button>
        </div>

        <div class="modal-body">
          <div class="detail-grid">
            <div class="detail-item">
              <span class="detail-label es-detail-label">充值电表</span>
              <span class="detail-value es-detail-value-box">{{ meterName }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label es-detail-label">电表编号</span>
              <span class="detail-value es-detail-value-box">{{ meterDeviceNo }}</span>
            </div>
          </div>
          <div class="detail-grid">
            <div class="detail-item">
              <span class="detail-label es-detail-label">充值金额（元）</span>
              <span class="detail-value es-detail-value-box">{{ amountText }}</span>
            </div>
          </div>
        </div>

        <div class="modal-actions">
          <button class="btn btn-secondary" type="button" :disabled="submitting" @click="close"
            >取消</button
          >
          <button
            class="btn btn-primary"
            type="button"
            :disabled="submitting"
            @click="handleConfirm"
            >确定充值</button
          >
        </div>
      </div>
    </div>
  </Transition>
</template>

<style scoped>
.modal-mask {
  position: fixed;
  inset: 0;
  z-index: 43;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  background: rgb(15 23 42 / 38%);
}

.modal-panel {
  width: min(640px, calc(100vw - 32px));
  max-height: calc(100vh - 40px);
  overflow: hidden;
  background: var(--es-color-bg-elevated);
  border: 1px solid var(--es-color-border);
  border-radius: 5px;
  box-shadow: var(--es-shadow-floating);
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

.icon-btn:disabled {
  cursor: not-allowed;
  opacity: 0.65;
}

.modal-body {
  padding: 20px;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px 18px;
}

.detail-grid + .detail-grid {
  margin-top: 12px;
}

.detail-item {
  display: grid;
  grid-template-columns: 88px minmax(0, 1fr);
  gap: 2px;
  align-items: center;
}

.detail-label {
  white-space: nowrap;
}

.detail-value {
  width: 100%;
}

.modal-actions {
  display: flex;
  padding: 16px 20px 20px;
  background: #fff;
  border-top: 1px solid var(--es-color-border);
  justify-content: flex-end;
  gap: 12px;
}

.btn {
  height: 36px;
  min-width: 72px;
  padding: 0 16px;
  font-size: var(--es-font-size-sm);
  border: 1px solid transparent;
  border-radius: 5px;
}

.btn-primary {
  color: #fff;
  background: var(--es-color-primary);
}

.btn-secondary {
  color: var(--es-color-text-secondary);
  background: #fff;
  border-color: var(--es-color-border-strong);
}

.btn:disabled {
  cursor: not-allowed;
  opacity: 0.65;
}

.recharge-confirm-fade-enter-active,
.recharge-confirm-fade-leave-active {
  transition: opacity 0.22s ease, transform 0.22s ease;
}

.recharge-confirm-fade-enter-from,
.recharge-confirm-fade-leave-to {
  opacity: 0;
}

.recharge-confirm-fade-enter-from .modal-panel,
.recharge-confirm-fade-leave-to .modal-panel {
  transform: translateY(10px) scale(0.98);
}
</style>
