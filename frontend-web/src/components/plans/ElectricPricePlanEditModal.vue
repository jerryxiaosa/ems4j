<script setup lang="ts">
import { computed, reactive, watch } from 'vue'
import { createDefaultElectricPricePlanForm } from '@/components/plans/electric-price-plan.mock'
import type {
  ElectricPriceDefaultStepPrice,
  ElectricPricePlanFormValue,
  ElectricPricePlanItem,
  ElectricPriceStandardPrice
} from '@/types/electric-price-plan'

const props = defineProps<{
  modelValue: boolean
  mode: 'create' | 'edit'
  plan: ElectricPricePlanItem | null
  standardPrices: ElectricPriceStandardPrice
  defaultStepPrices: ElectricPriceDefaultStepPrice
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  submit: [payload: ElectricPricePlanFormValue]
}>()

const form = reactive<ElectricPricePlanFormValue>(createDefaultElectricPricePlanForm())
const errors = reactive<Record<string, string>>({})

const dialogTitle = computed(() => (props.mode === 'edit' ? '编辑电价方案' : '添加电价方案'))

const priceKeys = [
  {
    index: 1,
    label: '尖',
    priceKey: 'priceHigher',
    ratioKey: 'ratioHigher',
    standardKey: 'priceHigher'
  },
  { index: 2, label: '峰', priceKey: 'priceHigh', ratioKey: 'ratioHigh', standardKey: 'priceHigh' },
  { index: 3, label: '平', priceKey: 'priceLow', ratioKey: 'ratioLow', standardKey: 'priceLow' },
  {
    index: 4,
    label: '谷',
    priceKey: 'priceLower',
    ratioKey: 'ratioLower',
    standardKey: 'priceLower'
  },
  {
    index: 5,
    label: '深谷',
    priceKey: 'priceDeepLow',
    ratioKey: 'ratioDeepLow',
    standardKey: 'priceDeepLow'
  }
] as const

const stepRows = computed(
  () =>
    [
      {
        label: '第一阶梯',
        start: '0',
        endKey: 'step1End',
        ratioKey: 'step1Ratio'
      },
      {
        label: '第二阶梯',
        start: form.step1End || '--',
        endKey: 'step2End',
        ratioKey: 'step2Ratio'
      },
      {
        label: '第三阶梯',
        start: form.step2End || '--',
        endKey: 'step3End',
        ratioKey: 'step3Ratio'
      }
    ] as const
)

const resetErrors = () => {
  Object.keys(errors).forEach((key) => delete errors[key])
}

const formatDecimal = (value: number) => {
  if (!Number.isFinite(value)) {
    return ''
  }
  return value.toFixed(4).replace(/\.?0+$/, '')
}

const isValidAmount = (value: string) => /^(0|[1-9]\d*)(\.\d{1,4})?$/.test(value.trim())
const isValidInteger = (value: string) => /^[1-9]\d*$/.test(value.trim())

const applyDefaultStepPrices = () => {
  form.step1End = props.defaultStepPrices.step1End
  form.step1Ratio = props.defaultStepPrices.step1Ratio
  form.step2End = props.defaultStepPrices.step2End
  form.step2Ratio = props.defaultStepPrices.step2Ratio
  form.step3End = props.defaultStepPrices.step3End
  form.step3Ratio = props.defaultStepPrices.step3Ratio
}

const hasExistingStepValue = () => {
  return !!(
    form.step1End.trim() ||
    form.step2End.trim() ||
    form.step3End.trim() ||
    (form.step1Ratio.trim() && form.step1Ratio.trim() !== '1') ||
    (form.step2Ratio.trim() && form.step2Ratio.trim() !== '1.2') ||
    (form.step3Ratio.trim() && form.step3Ratio.trim() !== '1.3')
  )
}

const syncComputedPrices = () => {
  if (form.isCustomPrice !== 'false') return

  priceKeys.forEach((row) => {
    const ratioValue = form[row.ratioKey].trim()
    const standardPrice = Number(props.standardPrices[row.standardKey] || 0)

    if (!ratioValue) {
      form[row.priceKey] = formatDecimal(standardPrice)
      return
    }

    if (!isValidAmount(ratioValue)) {
      form[row.priceKey] = ''
      return
    }

    form[row.priceKey] = formatDecimal(standardPrice * Number(ratioValue))
  })
}

const fillForm = (plan: ElectricPricePlanItem | null) => {
  Object.assign(form, createDefaultElectricPricePlanForm())
  resetErrors()

  if (!plan) {
    syncComputedPrices()
    return
  }

  form.id = plan.id
  form.name = plan.name
  form.isCustomPrice = plan.isCustomPrice ? 'true' : 'false'
  form.hasStepPrice = plan.hasStepPrice ? 'true' : 'false'

  form.priceHigher = String(plan.priceHigher)
  form.priceHigh = String(plan.priceHigh)
  form.priceLow = String(plan.priceLow)
  form.priceLower = String(plan.priceLower)
  form.priceDeepLow = String(plan.priceDeepLow)
  form.ratioHigher =
    plan.ratioHigher != null
      ? String(plan.ratioHigher)
      : formatDecimal(plan.priceHigher / props.standardPrices.priceHigher)
  form.ratioHigh =
    plan.ratioHigh != null
      ? String(plan.ratioHigh)
      : formatDecimal(plan.priceHigh / props.standardPrices.priceHigh)
  form.ratioLow =
    plan.ratioLow != null
      ? String(plan.ratioLow)
      : formatDecimal(plan.priceLow / props.standardPrices.priceLow)
  form.ratioLower =
    plan.ratioLower != null
      ? String(plan.ratioLower)
      : formatDecimal(plan.priceLower / props.standardPrices.priceLower)
  form.ratioDeepLow =
    plan.ratioDeepLow != null
      ? String(plan.ratioDeepLow)
      : formatDecimal(plan.priceDeepLow / props.standardPrices.priceDeepLow)
  form.step1End = plan.step1End != null ? String(plan.step1End) : ''
  form.step1Ratio = plan.step1Ratio != null ? String(plan.step1Ratio) : '1'
  form.step2End = plan.step2End != null ? String(plan.step2End) : ''
  form.step2Ratio = plan.step2Ratio != null ? String(plan.step2Ratio) : '1.2'
  form.step3End = plan.step3End != null ? String(plan.step3End) : ''
  form.step3Ratio = plan.step3Ratio != null ? String(plan.step3Ratio) : '1.3'

  syncComputedPrices()
}

watch(
  () => props.modelValue,
  (visible) => {
    if (visible) {
      fillForm(props.plan)
    }
  }
)

watch(
  () => props.plan,
  (plan) => {
    if (props.modelValue) {
      fillForm(plan)
    }
  }
)

watch(
  () => form.isCustomPrice,
  () => {
    resetErrors()
    if (form.isCustomPrice === 'false') {
      syncComputedPrices()
    }
  }
)

watch(
  () => form.hasStepPrice,
  (value, previous) => {
    if (value !== 'true' || previous === 'true') {
      return
    }

    if (props.plan?.hasStepPrice) {
      return
    }

    if (!hasExistingStepValue()) {
      applyDefaultStepPrices()
    }
  }
)

watch(
  () => [
    props.standardPrices,
    form.ratioHigher,
    form.ratioHigh,
    form.ratioLow,
    form.ratioLower,
    form.ratioDeepLow
  ],
  () => {
    syncComputedPrices()
  },
  { deep: true }
)

watch(
  () => props.defaultStepPrices,
  () => {
    if (
      props.modelValue &&
      form.hasStepPrice === 'true' &&
      !props.plan?.hasStepPrice &&
      !hasExistingStepValue()
    ) {
      applyDefaultStepPrices()
    }
  },
  { deep: true }
)

const getRequiredLabelClass = (required: boolean) => ({ 'field-label-required': required })

const validate = () => {
  resetErrors()

  if (!form.name.trim()) {
    errors.name = '请输入电价方案名称'
  }

  priceKeys.forEach((row) => {
    if (form.isCustomPrice === 'true') {
      const value = form[row.priceKey].trim()
      if (!value) {
        errors[row.priceKey] = `请输入${row.label}电价`
        return
      }
      if (!isValidAmount(value)) {
        errors[row.priceKey] = '请输入正确的电价值'
        return
      }
      if (Number(value) <= 0) {
        errors[row.priceKey] = '电价必须大于 0'
      }
      return
    }

    const ratioValue = form[row.ratioKey].trim()
    if (!ratioValue) {
      errors[row.ratioKey] = '请输入倍率'
      return
    }
    if (!isValidAmount(ratioValue)) {
      errors[row.ratioKey] = '请输入正确的倍率'
      return
    }
    if (Number(ratioValue) <= 0) {
      errors[row.ratioKey] = '倍率必须大于 0'
    }
  })

  if (form.hasStepPrice === 'true') {
    if (!isValidInteger(form.step1End)) {
      errors.step1End = '请输入正确的结束度数'
    }
    if (!isValidAmount(form.step1Ratio) || Number(form.step1Ratio) <= 0) {
      errors.step1Ratio = '请输入大于 0 的倍率'
    }

    if (!isValidInteger(form.step2End)) {
      errors.step2End = '请输入正确的结束度数'
    }
    if (!isValidAmount(form.step2Ratio) || Number(form.step2Ratio) <= 0) {
      errors.step2Ratio = '请输入大于 0 的倍率'
    }

    if (
      isValidInteger(form.step1End) &&
      isValidInteger(form.step2End) &&
      Number(form.step2End) <= Number(form.step1End)
    ) {
      errors.step2End = '第二阶梯结束度数必须大于第一阶梯'
    }

    if (!isValidAmount(form.step3Ratio) || Number(form.step3Ratio) <= 0) {
      errors.step3Ratio = '请输入大于 0 的倍率'
    }
  }

  return Object.keys(errors).length === 0
}

const close = () => emit('update:modelValue', false)

const handleSubmit = () => {
  if (!validate()) {
    return
  }

  emit('submit', {
    ...form,
    name: form.name.trim(),
    priceHigher: form.priceHigher.trim(),
    priceHigh: form.priceHigh.trim(),
    priceLow: form.priceLow.trim(),
    priceLower: form.priceLower.trim(),
    priceDeepLow: form.priceDeepLow.trim(),
    ratioHigher: form.ratioHigher.trim(),
    ratioHigh: form.ratioHigh.trim(),
    ratioLow: form.ratioLow.trim(),
    ratioLower: form.ratioLower.trim(),
    ratioDeepLow: form.ratioDeepLow.trim(),
    step1End: form.step1End.trim(),
    step1Ratio: form.step1Ratio.trim(),
    step2End: form.step2End.trim(),
    step2Ratio: form.step2Ratio.trim(),
    step3End: form.step3End.trim(),
    step3Ratio: form.step3Ratio.trim()
  })
}
</script>

<template>
  <Transition name="electric-plan-modal-fade" appear>
    <div v-if="modelValue" class="modal-mask" @click.self.prevent>
      <div class="modal-panel electric-plan-edit-modal">
        <div class="modal-head">
          <h3 class="modal-title">{{ dialogTitle }}</h3>
          <button class="icon-btn" type="button" @click="close">关闭</button>
        </div>

        <div class="modal-body">
          <div class="form-grid">
            <label class="field field-full">
              <span :class="['field-label', getRequiredLabelClass(true)]">电价方案名称</span>
              <input
                v-model="form.name"
                class="form-control"
                type="text"
                placeholder="请输入电价方案名称"
              />
              <span v-if="errors.name" class="field-error">{{ errors.name }}</span>
            </label>

            <div class="section-title field-full">尖峰平谷价格设置</div>

            <label class="switch-field field-full">
              <span :class="['field-label', getRequiredLabelClass(true)]">是否自定义价格</span>
              <button
                type="button"
                :class="['switch-btn', { 'is-checked': form.isCustomPrice === 'true' }]"
                @click="form.isCustomPrice = form.isCustomPrice === 'true' ? 'false' : 'true'"
              >
                <span class="switch-thumb" />
              </button>
            </label>

            <div class="field field-full">
              <div class="table-wrap">
                <table class="table">
                  <thead>
                    <tr>
                      <th class="col-index">序号</th>
                      <th class="col-period">时段</th>
                      <th>价格（元/度）</th>
                      <th>倍率</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="row in priceKeys" :key="row.priceKey">
                      <td>{{ row.index }}</td>
                      <td>{{ row.label }}</td>
                      <td>
                        <div class="cell-field">
                          <input
                            v-model="form[row.priceKey]"
                            :class="[
                              'form-control',
                              { 'is-readonly': form.isCustomPrice === 'false' }
                            ]"
                            :readonly="form.isCustomPrice === 'false'"
                            type="text"
                            placeholder="请输入"
                          />
                          <span v-if="errors[row.priceKey]" class="field-error">{{
                            errors[row.priceKey]
                          }}</span>
                        </div>
                      </td>
                      <td>
                        <div v-if="form.isCustomPrice === 'true'" class="table-placeholder">--</div>
                        <div v-else class="cell-field">
                          <input
                            v-model="form[row.ratioKey]"
                            class="form-control"
                            type="text"
                            placeholder="请输入"
                          />
                          <span v-if="errors[row.ratioKey]" class="field-error">{{
                            errors[row.ratioKey]
                          }}</span>
                        </div>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>

            <label class="switch-field field-full">
              <span :class="['field-label', getRequiredLabelClass(true)]">是否开启阶梯电费</span>
              <button
                type="button"
                :class="['switch-btn', { 'is-checked': form.hasStepPrice === 'true' }]"
                @click="form.hasStepPrice = form.hasStepPrice === 'true' ? 'false' : 'true'"
              >
                <span class="switch-thumb" />
              </button>
            </label>

            <div v-if="form.hasStepPrice === 'true'" class="field field-full">
              <div class="step-grid">
                <div v-for="row in stepRows" :key="row.label" class="step-row">
                  <span class="step-label">{{ row.label }}：</span>
                  <input
                    class="form-control step-input is-readonly"
                    :value="row.start"
                    readonly
                    type="text"
                  />
                  <span class="step-separator">~</span>
                  <template v-if="row.endKey === 'step3End'">
                    <input
                      class="form-control step-input is-readonly"
                      value="--"
                      readonly
                      type="text"
                    />
                  </template>
                  <div v-else class="step-field">
                    <input
                      v-model="form[row.endKey]"
                      class="form-control step-input"
                      type="text"
                      placeholder="请输入"
                    />
                    <span v-if="errors[row.endKey]" class="field-error">{{
                      errors[row.endKey]
                    }}</span>
                  </div>
                  <span class="step-unit">（度）</span>
                  <span class="step-ratio-label">倍率：</span>
                  <div class="step-field">
                    <input
                      v-model="form[row.ratioKey]"
                      class="form-control step-ratio-input"
                      type="text"
                      placeholder="请输入"
                    />
                    <span v-if="errors[row.ratioKey]" class="field-error">{{
                      errors[row.ratioKey]
                    }}</span>
                  </div>
                </div>
              </div>
            </div>
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

.electric-plan-edit-modal {
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

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.field {
  display: grid;
  gap: 8px;
  min-width: 0;
}

.field-full {
  grid-column: 1 / -1;
}

.field-label {
  font-size: var(--es-font-size-sm);
  font-weight: 600;
  color: var(--es-color-text-primary);
}

.field-label-required::before {
  margin-right: 4px;
  color: #ef4444;
  content: '*';
}

.section-title {
  font-size: var(--es-font-size-sm);
  font-weight: 600;
  color: var(--es-color-text-primary);
}

.switch-field {
  display: flex;
  align-items: center;
  gap: 12px;
}

.switch-btn {
  position: relative;
  width: 40px;
  height: 22px;
  cursor: pointer;
  background: #e5e7eb;
  border: 1px solid #cbd5e1;
  border-radius: 999px;
  transition: background 0.2s ease, border-color 0.2s ease, box-shadow 0.2s ease;
}

.switch-btn.is-checked {
  background: rgb(29 78 216 / 90%);
  border-color: rgb(29 78 216 / 72%);
}

.switch-btn:hover {
  border-color: #94a3b8;
}

.switch-btn:focus-visible {
  outline: none;
  box-shadow: 0 0 0 3px rgb(29 78 216 / 14%);
}

.switch-thumb {
  position: absolute;
  top: 2px;
  left: 2px;
  width: 16px;
  height: 16px;
  background: #fff;
  border-radius: 50%;
  box-shadow: 0 1px 2px rgb(15 23 42 / 18%);
  transition: transform 0.2s ease;
}

.switch-btn.is-checked .switch-thumb {
  transform: translateX(18px);
}

.table-wrap {
  overflow: hidden;
  border: 1px solid var(--es-color-border);
  border-radius: 5px;
}

.table-placeholder {
  display: inline-flex;
  align-items: center;
  height: 32px;
  color: var(--es-color-text-placeholder);
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
  width: 84px;
}

.col-period {
  width: 100px;
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

.step-grid {
  display: grid;
  gap: 14px;
}

.step-row {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  flex-wrap: wrap;
}

.step-label,
.step-unit,
.step-ratio-label,
.step-separator {
  display: inline-flex;
  height: 32px;
  font-size: var(--es-font-size-sm);
  color: var(--es-color-text-primary);
  align-items: center;
}

.step-label {
  min-width: 70px;
  font-weight: 600;
}

.step-ratio-label {
  margin-left: 16px;
  font-weight: 600;
}

.step-field {
  display: grid;
  gap: 6px;
}

.step-input,
.step-ratio-input {
  width: 92px;
}

.field-error {
  font-size: 12px;
  color: var(--es-color-error-text);
}

.electric-plan-modal-fade-enter-active,
.electric-plan-modal-fade-leave-active {
  transition: opacity 0.22s ease, transform 0.22s ease;
}

.electric-plan-modal-fade-enter-from,
.electric-plan-modal-fade-leave-to {
  opacity: 0;
}

.electric-plan-modal-fade-enter-from .electric-plan-edit-modal,
.electric-plan-modal-fade-leave-to .electric-plan-edit-modal {
  transform: translateY(10px) scale(0.98);
}
</style>
