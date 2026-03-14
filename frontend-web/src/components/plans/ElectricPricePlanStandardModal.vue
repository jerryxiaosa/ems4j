<script setup lang="ts">
import { reactive, watch } from 'vue'
import type { ElectricPriceStandardPrice } from '@/types/electric-price-plan'

const props = defineProps<{
  modelValue: boolean
  prices: ElectricPriceStandardPrice
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  submit: [payload: ElectricPriceStandardPrice]
}>()

const form = reactive({
  priceHigher: '',
  priceHigh: '',
  priceLow: '',
  priceLower: '',
  priceDeepLow: ''
})

const errors = reactive<Record<string, string>>({})

const periodRows = [
  { index: 1, label: '尖', key: 'priceHigher' },
  { index: 2, label: '峰', key: 'priceHigh' },
  { index: 3, label: '平', key: 'priceLow' },
  { index: 4, label: '谷', key: 'priceLower' },
  { index: 5, label: '深谷', key: 'priceDeepLow' }
] as const

const fillForm = () => {
  form.priceHigher = String(props.prices.priceHigher ?? '')
  form.priceHigh = String(props.prices.priceHigh ?? '')
  form.priceLow = String(props.prices.priceLow ?? '')
  form.priceLower = String(props.prices.priceLower ?? '')
  form.priceDeepLow = String(props.prices.priceDeepLow ?? '')
  Object.keys(errors).forEach((key) => delete errors[key])
}

watch(
  () => props.modelValue,
  (visible) => {
    if (visible) {
      fillForm()
    }
  }
)

const isValidAmount = (value: string) => /^(0|[1-9]\d*)(\.\d{1,4})?$/.test(value.trim())

const validate = () => {
  Object.keys(errors).forEach((key) => delete errors[key])

  periodRows.forEach((row) => {
    const value = form[row.key]
    if (!value.trim()) {
      errors[row.key] = '请输入标准电价'
      return
    }
    if (!isValidAmount(value)) {
      errors[row.key] = '请输入正确的标准电价'
      return
    }

    if (Number(value) <= 0) {
      errors[row.key] = '标准电价必须大于 0'
    }
  })

  return Object.keys(errors).length === 0
}

const close = () => emit('update:modelValue', false)

const handleSubmit = () => {
  if (!validate()) {
    return
  }

  emit('submit', {
    priceHigher: Number(form.priceHigher),
    priceHigh: Number(form.priceHigh),
    priceLow: Number(form.priceLow),
    priceLower: Number(form.priceLower),
    priceDeepLow: Number(form.priceDeepLow)
  })
}
</script>

<template>
  <Transition name="electric-plan-standard-fade" appear>
    <div v-if="modelValue" class="modal-mask" @click.self.prevent>
      <div class="modal-panel electric-plan-standard-modal">
        <div class="modal-head">
          <h3 class="modal-title">标准电价设置</h3>
          <button class="icon-btn" type="button" @click="close">关闭</button>
        </div>

        <div class="modal-body">
          <div class="table-wrap">
            <table class="table">
              <thead>
                <tr>
                  <th class="col-index">序号</th>
                  <th class="col-period">时段</th>
                  <th>价格（元/度）</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="row in periodRows" :key="row.key">
                  <td>{{ row.index }}</td>
                  <td>{{ row.label }}</td>
                  <td>
                    <div class="cell-field">
                      <input
                        v-model="form[row.key]"
                        class="form-control"
                        type="text"
                        placeholder="请输入"
                      />
                      <span v-if="errors[row.key]" class="field-error">{{ errors[row.key] }}</span>
                    </div>
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

.electric-plan-standard-modal {
  display: grid;
  width: min(760px, calc(100vw - 32px));
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
  padding: 10px 12px;
  font-size: var(--es-font-size-sm);
  color: var(--es-color-text-secondary);
  text-align: left;
  vertical-align: top;
  border-right: 1px solid var(--es-color-border);
  border-bottom: 1px solid var(--es-color-border);
}

.table th:last-child,
.table td:last-child {
  width: 240px;
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
  width: 84px;
}

.col-period {
  width: 120px;
}

.cell-field {
  display: grid;
  gap: 6px;
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

.field-error {
  font-size: 12px;
  color: var(--es-color-error-text);
}

.electric-plan-standard-fade-enter-active,
.electric-plan-standard-fade-leave-active {
  transition: opacity 0.22s ease, transform 0.22s ease;
}

.electric-plan-standard-fade-enter-from,
.electric-plan-standard-fade-leave-to {
  opacity: 0;
}

.electric-plan-standard-fade-enter-from .electric-plan-standard-modal,
.electric-plan-standard-fade-leave-to .electric-plan-standard-modal {
  transform: translateY(10px) scale(0.98);
}
</style>
