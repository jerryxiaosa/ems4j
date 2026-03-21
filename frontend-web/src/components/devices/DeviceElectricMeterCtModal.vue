<script setup lang="ts">
import { computed, reactive, watch } from 'vue'
import { type ElectricMeterItem } from '@/components/devices/electric-meter.mock'

const props = defineProps<{
  modelValue: boolean
  meter: ElectricMeterItem | null
  submitting?: boolean
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  submit: [payload: { id: number; ct: string }]
}>()

const form = reactive({
  ct: ''
})
const error = reactive({
  ct: ''
})

watch(
  () => [props.modelValue, props.meter?.id],
  () => {
    if (!props.modelValue) {
      return
    }
    form.ct = props.meter?.ct || ''
    error.ct = ''
  }
)

const close = () => {
  if (props.submitting) {
    return
  }
  emit('update:modelValue', false)
}

const getSpaceRegion = (spacePath: string | undefined, spaceName: string | undefined) => {
  const segments = (spacePath || '')
    .split(' / ')
    .map((item) => item.trim())
    .filter(Boolean)

  if (segments.length <= 1) {
    return '—'
  }

  if (spaceName && segments[segments.length - 1] === spaceName) {
    return segments.slice(0, -1).join(' > ') || '—'
  }

  return segments.slice(0, -1).join(' > ') || '—'
}

const detailRows = computed(() => {
  const meter = props.meter
  if (!meter) {
    return []
  }

  return [
    { label: '电表名称', value: meter.meterName },
    { label: '电表编号', value: meter.deviceNo },
    { label: '所在位置', value: meter.spaceName || '—' },
    { label: '所属区域', value: getSpaceRegion(meter.spacePath, meter.spaceName) },
    { label: '电表型号', value: meter.modelName },
    { label: '通讯模式', value: meter.communicateModel },
    { label: '是否预付费', value: meter.payType === 1 ? '是' : '否' },
    { label: '是否计量', value: meter.isCalculate ? '是' : '否' }
  ]
})

const handleSubmit = () => {
  if (!props.meter) {
    return
  }
  if (!form.ct.trim()) {
    error.ct = '请输入 CT 变比'
    return
  }
  if (!/^[1-9][0-9]*$/.test(form.ct.trim()) || Number(form.ct.trim()) >= 65535) {
    error.ct = 'CT 变比需为小于 65535 的正整数'
    return
  }
  error.ct = ''
  emit('submit', { id: props.meter.id, ct: form.ct.trim() })
}
</script>

<template>
  <Transition name="meter-modal-fade" appear>
    <div v-if="modelValue" class="modal-mask" @click.self.prevent>
      <div class="modal-panel meter-ct-modal">
        <div class="modal-head">
          <h3 class="modal-title">设置CT变比</h3>
          <button class="icon-btn" type="button" :disabled="submitting" @click="close">关闭</button>
        </div>

        <div class="modal-body">
          <div class="ct-form-block">
            <label class="field">
              <span class="field-label">CT变比（请输入整数值）</span>
              <input
                v-model="form.ct"
                class="form-control"
                type="text"
                :disabled="submitting"
                placeholder="请输入 CT 变比"
              />
              <span v-if="error.ct" class="field-error">{{ error.ct }}</span>
            </label>
          </div>

          <div class="detail-panel">
            <div class="detail-grid">
              <div v-for="row in detailRows" :key="row.label" class="detail-item">
                <span class="detail-label es-detail-label">{{ row.label }}</span>
                <span class="detail-value es-detail-value-box">{{ row.value || '—' }}</span>
              </div>
            </div>
          </div>
        </div>

        <div class="modal-actions">
          <button class="btn btn-secondary" type="button" :disabled="submitting" @click="close">
            取消
          </button>
          <button class="btn btn-primary" type="button" :disabled="submitting" @click="handleSubmit">
            {{ submitting ? '提交中...' : '确定' }}
          </button>
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

.meter-ct-modal {
  display: grid;
  width: min(640px, calc(100vw - 32px));
  max-height: calc(100vh - 40px);
  overflow: hidden;
  background: var(--es-color-bg-elevated);
  border: 1px solid var(--es-color-border);
  border-radius: 5px;
  box-shadow: var(--es-shadow-floating);
  grid-template-rows: auto minmax(0, 1fr) auto;
}

.modal-head,
.modal-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border-bottom: 1px solid var(--es-color-border);
}

.modal-actions {
  justify-content: flex-end;
  gap: 8px;
  border-top: 1px solid var(--es-color-border);
  border-bottom: 0;
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
  color: var(--es-color-text-secondary);
  background: #fff;
  border: 1px solid var(--es-color-border-strong);
  border-radius: 5px;
}

.modal-body {
  padding: 20px;
  overflow: auto;
}

.ct-form-block {
  margin-bottom: 18px;
}

.field {
  display: grid;
  gap: 8px;
}

.field-label {
  font-size: var(--es-font-size-sm);
  font-weight: 600;
  color: var(--es-color-text-primary);
}

.form-control {
  width: 100%;
  height: 36px;
  padding: 0 12px;
  font-size: var(--es-font-size-sm);
  color: var(--es-color-text-primary);
  background: #fff;
  border: 1px solid var(--es-color-border-strong);
  border-radius: 5px;
}

.form-control::placeholder {
  color: var(--es-color-text-placeholder);
}

.form-control:focus {
  border-color: var(--es-color-primary);
  outline: none;
  box-shadow: 0 0 0 3px rgb(29 78 216 / 14%);
}

.field-error {
  font-size: var(--es-font-size-xs);
  color: var(--es-color-error-text);
}

.detail-panel {
  padding: 16px;
  background: #fff;
  border: 1px solid var(--es-color-border);
  border-radius: 5px;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px 56px;
}

.detail-item {
  display: grid;
  grid-template-columns: 96px minmax(0, 1fr);
  align-items: center;
  gap: 2px;
}

.detail-label {
  white-space: nowrap;
}

.detail-value {
  width: 100%;
}

.btn {
  height: 36px;
  min-width: 72px;
  padding: 0 12px;
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
  background: #f8fbff;
  border-color: var(--es-color-border-strong);
}

.meter-modal-fade-enter-active,
.meter-modal-fade-leave-active {
  transition: opacity 0.18s ease-out;
}

.meter-modal-fade-enter-active .modal-panel,
.meter-modal-fade-leave-active .modal-panel {
  transition: transform 0.2s ease-out, opacity 0.2s ease-out;
}

.meter-modal-fade-enter-from,
.meter-modal-fade-leave-to {
  opacity: 0;
}

.meter-modal-fade-enter-from .modal-panel,
.meter-modal-fade-leave-to .modal-panel {
  opacity: 0;
  transform: translateY(8px) scale(0.995);
}
</style>
