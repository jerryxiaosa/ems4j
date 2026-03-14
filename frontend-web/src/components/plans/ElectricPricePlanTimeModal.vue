<script setup lang="ts">
import { reactive, watch } from 'vue'
import type { ElectricPriceTimeSettingItem } from '@/types/electric-price-plan'

export type ElectricPriceTimeRow = ElectricPriceTimeSettingItem

const props = defineProps<{
  modelValue: boolean
  rows: ElectricPriceTimeRow[]
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  submit: [payload: ElectricPriceTimeRow[]]
}>()

const formRows = reactive<ElectricPriceTimeRow[]>(
  Array.from({ length: 10 }, () => ({
    type: '',
    time: ''
  }))
)

const options = [
  { label: '尖', value: 'higher' },
  { label: '峰', value: 'high' },
  { label: '平', value: 'low' },
  { label: '谷', value: 'lower' },
  { label: '深谷', value: 'deepLow' }
] as const

const typeLabelMap: Record<string, string> = {
  higher: '尖',
  high: '峰',
  low: '平',
  lower: '谷',
  deepLow: '深谷'
}

const errors = reactive<Record<number, string>>({})

const fillRows = () => {
  formRows.splice(
    0,
    formRows.length,
    ...Array.from({ length: 10 }, (_, index) => ({
      type: (props.rows[index]?.type || '') as ElectricPriceTimeRow['type'],
      time: props.rows[index]?.time || ''
    }))
  )
  Object.keys(errors).forEach((key) => delete errors[Number(key)])
}

watch(
  () => props.modelValue,
  (visible) => {
    if (visible) {
      fillRows()
    }
  }
)

const close = () => emit('update:modelValue', false)

const isValidTime = (value: string) => /^([01]\d|2[0-3]):([0-5]\d)$/.test(value.trim())

const validate = () => {
  Object.keys(errors).forEach((key) => delete errors[Number(key)])

  let encounteredGap = false

  formRows.forEach((row, index) => {
    const hasType = !!row.type
    const hasTime = !!row.time.trim()

    if (!hasType && !hasTime) {
      if (index > 0 && formRows.slice(0, index).some((item) => item.type || item.time.trim())) {
        encounteredGap = true
      }
      return
    }

    if (encounteredGap) {
      errors[index] = '请按顺序填写，中间不能跳行'
      return
    }

    if (!hasType || !hasTime) {
      errors[index] = '费率和时间需同时填写'
      return
    }

    if (!isValidTime(row.time)) {
      errors[index] = '请输入正确的时间，格式为 HH:mm'
    }
  })

  return Object.keys(errors).length === 0
}

const handleSubmit = () => {
  if (!validate()) {
    return
  }

  emit(
    'submit',
    formRows
      .filter((row) => row.type && row.time.trim())
      .map((row) => ({
        type: row.type,
        time: row.time.trim()
      }))
  )
}
</script>

<template>
  <Transition name="electric-plan-time-fade" appear>
    <div v-if="modelValue" class="modal-mask" @click.self.prevent>
      <div class="modal-panel electric-plan-time-modal">
        <div class="modal-head">
          <h3 class="modal-title">尖峰平谷时间设置</h3>
          <button class="icon-btn" type="button" @click="close">关闭</button>
        </div>

        <div class="modal-body">
          <div class="section-head">时段</div>

          <div class="table-wrap">
            <table class="table">
              <thead>
                <tr>
                  <th class="col-index">序号</th>
                  <th class="col-type">费率</th>
                  <th>时间</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(row, index) in formRows" :key="index">
                  <td>{{ index + 1 }}</td>
                  <td>
                    <select v-model="row.type" class="form-control">
                      <option value="">请选择</option>
                      <option v-for="option in options" :key="option.value" :value="option.value">
                        {{ option.label }}
                      </option>
                    </select>
                  </td>
                  <td>
                    <div class="time-field">
                      <span class="time-icon">◷</span>
                      <input
                        v-model="row.time"
                        class="form-control time-input"
                        type="time"
                        step="60"
                      />
                    </div>
                    <div v-if="errors[index]" class="field-error">{{ errors[index] }}</div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
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

.electric-plan-time-modal {
  display: grid;
  width: min(860px, calc(100vw - 32px));
  max-height: calc(100vh - 40px);
  overflow: hidden;
  background: var(--es-color-bg-elevated);
  border-radius: 5px;
  box-shadow: var(--es-shadow-floating);
  grid-template-rows: auto minmax(0, 1fr) auto;
}

.modal-head,
.modal-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
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
  font-size: 16px;
  font-weight: 600;
  color: var(--es-color-text-primary);
}

.icon-btn,
.btn {
  display: inline-flex;
  height: 36px;
  min-width: 72px;
  padding: 0 12px;
  font-size: var(--es-font-size-sm);
  border: 1px solid transparent;
  border-radius: 5px;
  align-items: center;
  justify-content: center;
}

.icon-btn,
.btn-secondary {
  color: var(--es-color-text-secondary);
  background: #fff;
  border-color: var(--es-color-border-strong);
}

.btn-primary {
  color: #fff;
  background: var(--es-color-primary);
}

.modal-body {
  padding: 20px;
  overflow: auto;
}

.section-head {
  margin-bottom: 12px;
  font-size: var(--es-font-size-sm);
  font-weight: 600;
  color: var(--es-color-text-primary);
}

.table-wrap {
  overflow: hidden;
  border: 1px solid var(--es-color-border);
  border-radius: 5px;
}

.table {
  width: 100%;
  border-collapse: collapse;
  table-layout: fixed;
}

.table th,
.table td {
  padding: 8px 10px;
  font-size: var(--es-font-size-sm);
  color: var(--es-color-text-secondary);
  text-align: left;
  vertical-align: top;
  border-right: 1px solid var(--es-color-border);
  border-bottom: 1px solid var(--es-color-border);
}

.table th:last-child,
.table td:last-child {
  border-right: 0;
}

.table tbody tr:last-child td {
  border-bottom: 0;
}

.table th {
  font-weight: 500;
  color: #1e3a8a;
  background: var(--es-color-table-header-bg);
}

.col-index {
  width: 72px;
}

.col-type {
  width: 220px;
}

.form-control {
  width: 100%;
  height: 32px;
  padding: 0 10px;
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

.time-field {
  position: relative;
}

.time-icon {
  position: absolute;
  top: 50%;
  left: 10px;
  font-size: 14px;
  color: var(--es-color-text-placeholder);
  pointer-events: none;
  transform: translateY(-50%);
}

.time-input {
  padding-left: 30px;
}

.field-error {
  margin-top: 6px;
  font-size: 12px;
  color: var(--es-color-error-text);
}

.electric-plan-time-fade-enter-active,
.electric-plan-time-fade-leave-active {
  transition: opacity 0.22s ease, transform 0.22s ease;
}

.electric-plan-time-fade-enter-from,
.electric-plan-time-fade-leave-to {
  opacity: 0;
}

.electric-plan-time-fade-enter-from .electric-plan-time-modal,
.electric-plan-time-fade-leave-to .electric-plan-time-modal {
  transform: translateY(10px) scale(0.98);
}
</style>
