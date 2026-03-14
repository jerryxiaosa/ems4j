<script setup lang="ts">
import { reactive, watch } from 'vue'

const props = defineProps<{
  modelValue: boolean
  serviceRate: string
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  submit: [serviceRate: string]
}>()

const form = reactive({
  serviceRate: ''
})

const errors = reactive({
  serviceRate: ''
})

watch(
  () => props.modelValue,
  (visible) => {
    if (!visible) {
      return
    }
    form.serviceRate = props.serviceRate
    errors.serviceRate = ''
  }
)

const close = () => {
  emit('update:modelValue', false)
}

const validate = () => {
  const source = form.serviceRate.trim()
  if (!source) {
    errors.serviceRate = '请输入服务费比例'
    return false
  }

  const value = Number(source)
  if (!Number.isFinite(value) || value < 0) {
    errors.serviceRate = '服务费比例需为大于等于 0 的数字'
    return false
  }

  errors.serviceRate = ''
  return true
}

const handleSubmit = () => {
  if (!validate()) {
    return
  }
  emit('submit', form.serviceRate.trim())
}
</script>

<template>
  <Transition name="service-rate-fade" appear>
    <div v-if="modelValue" class="modal-mask" @click.self.prevent>
      <div class="modal-panel">
        <div class="modal-head">
          <h3 class="modal-title">电费服务费设置</h3>
          <button class="icon-btn" type="button" @click="close">关闭</button>
        </div>

        <div class="modal-body">
          <label class="form-field">
            <span class="field-label search-label-required">服务费比例（%）</span>
            <input
              v-model="form.serviceRate"
              class="form-control"
              type="text"
              placeholder="请输入服务费比例"
            />
            <span v-if="errors.serviceRate" class="field-error">{{ errors.serviceRate }}</span>
          </label>

          <p class="tip-text">*缴电费时需要扣除缴费总金额比例的服务费</p>
        </div>

        <div class="modal-actions">
          <button class="btn btn-secondary" type="button" @click="close">取消</button>
          <button class="btn btn-primary" type="button" @click="handleSubmit">确定</button>
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

.modal-body {
  padding: 24px 20px 12px;
}

.form-field {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.field-label {
  font-size: var(--es-font-size-md);
  font-weight: 600;
  color: var(--es-color-text-secondary);
}

.search-label-required::before {
  margin-right: 4px;
  color: var(--es-color-danger);
  content: '*';
}

.form-control {
  width: 100%;
  height: 36px;
  padding: 0 10px;
  font-size: var(--es-font-size-sm);
  color: var(--es-color-text-primary);
  background: #fff;
  border: 1px solid var(--es-color-border);
  border-radius: 5px;
}

.field-error {
  font-size: 12px;
  color: var(--es-color-danger);
}

.tip-text {
  margin: 22px 0 0;
  font-size: var(--es-font-size-md);
  font-weight: 600;
  color: #334155;
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

.service-rate-fade-enter-active,
.service-rate-fade-leave-active {
  transition: opacity 0.22s ease, transform 0.22s ease;
}

.service-rate-fade-enter-from,
.service-rate-fade-leave-to {
  opacity: 0;
}

.service-rate-fade-enter-from .modal-panel,
.service-rate-fade-leave-to .modal-panel {
  transform: translateY(10px) scale(0.98);
}
</style>
