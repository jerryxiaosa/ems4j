<script setup lang="ts">
import { computed, reactive, watch } from 'vue'
import { createDefaultWarnPlanForm } from '@/components/plans/warn-plan.mock'
import type { WarnPlanFormValue, WarnPlanItem } from '@/types/plan'

const props = defineProps<{
  modelValue: boolean
  mode: 'create' | 'edit'
  plan: WarnPlanItem | null
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  submit: [payload: WarnPlanFormValue]
}>()

const form = reactive<WarnPlanFormValue>(createDefaultWarnPlanForm())
const errors = reactive<Record<string, string>>({})

const dialogTitle = computed(() => (props.mode === 'edit' ? '编辑预警方案' : '添加预警方案'))

const resetErrors = () => {
  Object.keys(errors).forEach((key) => delete errors[key])
}

const fillForm = (plan: WarnPlanItem | null) => {
  Object.assign(form, createDefaultWarnPlanForm())
  resetErrors()

  if (!plan) {
    return
  }

  form.id = plan.id
  form.name = plan.name
  form.firstLevel = String(plan.firstLevel)
  form.secondLevel = String(plan.secondLevel)
  form.autoClose = plan.autoClose ? 'true' : 'false'
  form.remark = plan.remark || ''
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

const getRequiredLabelClass = (required: boolean) => ({ 'field-label-required': required })

const isValidAmount = (value: string) => /^(0|[1-9]\d*)(\.\d{1,2})?$/.test(value.trim())

const validate = () => {
  resetErrors()

  if (!form.name.trim()) {
    errors.name = '请输入预警方案名称'
  }

  if (!form.firstLevel.trim()) {
    errors.firstLevel = '请输入第一预警余额'
  } else if (!isValidAmount(form.firstLevel)) {
    errors.firstLevel = '第一预警余额格式不正确'
  }

  if (!form.secondLevel.trim()) {
    errors.secondLevel = '请输入第二预警余额'
  } else if (!isValidAmount(form.secondLevel)) {
    errors.secondLevel = '第二预警余额格式不正确'
  }

  if (!errors.firstLevel && !errors.secondLevel) {
    const first = Number(form.firstLevel)
    const second = Number(form.secondLevel)
    if (second >= first) {
      errors.secondLevel = '第二预警余额必须小于第一预警余额'
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
    firstLevel: form.firstLevel.trim(),
    secondLevel: form.secondLevel.trim(),
    remark: form.remark.trim()
  })
}
</script>

<template>
  <Transition name="warn-plan-modal-fade" appear>
    <div v-if="modelValue" class="modal-mask" @click.self.prevent>
      <div class="modal-panel warn-plan-edit-modal">
        <div class="modal-head">
          <h3 class="modal-title">{{ dialogTitle }}</h3>
          <button class="icon-btn" type="button" @click="close">关闭</button>
        </div>

        <div class="modal-body">
          <div class="form-grid">
            <label class="field field-full">
              <span :class="['field-label', getRequiredLabelClass(true)]">预警方案名称</span>
              <input
                v-model="form.name"
                class="form-control"
                type="text"
                placeholder="请输入预警方案名称"
              />
              <span v-if="errors.name" class="field-error">{{ errors.name }}</span>
            </label>

            <label class="field field-full">
              <span :class="['field-label', getRequiredLabelClass(true)]">第一预警余额</span>
              <input
                v-model="form.firstLevel"
                class="form-control"
                type="text"
                placeholder="请输入第一预警余额"
              />
              <span v-if="errors.firstLevel" class="field-error">{{ errors.firstLevel }}</span>
            </label>

            <label class="field field-full">
              <span :class="['field-label', getRequiredLabelClass(true)]">第二预警余额</span>
              <input
                v-model="form.secondLevel"
                class="form-control"
                type="text"
                placeholder="请输入第二预警余额"
              />
              <span v-if="errors.secondLevel" class="field-error">{{ errors.secondLevel }}</span>
            </label>

            <label class="field field-full">
              <span :class="['field-label', getRequiredLabelClass(true)]">是否欠费断闸</span>
              <span class="radio-group">
                <label class="radio-option">
                  <input v-model="form.autoClose" type="radio" value="true" />
                  <span>是</span>
                </label>
                <label class="radio-option">
                  <input v-model="form.autoClose" type="radio" value="false" />
                  <span>否</span>
                </label>
              </span>
            </label>

            <label class="field field-full">
              <span class="field-label">备注</span>
              <textarea
                v-model="form.remark"
                class="form-control form-textarea"
                placeholder="请输入备注"
              />
            </label>
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

.warn-plan-edit-modal {
  display: grid;
  width: min(560px, calc(100vw - 32px));
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
  gap: 14px 20px;
}

.field {
  display: flex;
  flex-direction: column;
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
  color: var(--es-color-error-text);
  content: '*';
}

.form-control {
  width: 100%;
  height: 36px;
  padding: 0 10px;
  font-size: var(--es-font-size-sm);
  color: var(--es-color-text-primary);
  background: #fff;
  border: 1px solid var(--es-color-border-strong);
  border-radius: 5px;
}

.form-control:focus {
  border-color: var(--es-color-primary);
  outline: none;
  box-shadow: 0 0 0 3px rgb(29 78 216 / 14%);
}

.form-textarea {
  min-height: 92px;
  padding: 10px;
  line-height: 1.5;
  resize: vertical;
}

.radio-group {
  display: inline-flex;
  align-items: center;
  gap: 18px;
  min-height: 36px;
}

.radio-option {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: var(--es-font-size-sm);
  color: var(--es-color-text-secondary);
  cursor: pointer;
}

.radio-option input {
  margin: 0;
}

.field-error {
  font-size: 12px;
  color: var(--es-color-error-text);
}

.warn-plan-modal-fade-enter-active,
.warn-plan-modal-fade-leave-active {
  transition: opacity 0.22s ease, transform 0.22s ease;
}

.warn-plan-modal-fade-enter-from,
.warn-plan-modal-fade-leave-to {
  opacity: 0;
}

.warn-plan-modal-fade-enter-from .warn-plan-edit-modal,
.warn-plan-modal-fade-leave-to .warn-plan-edit-modal {
  transform: translateY(10px) scale(0.98);
}
</style>
