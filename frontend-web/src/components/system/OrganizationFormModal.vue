<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { fetchEnumOptionsByKey, type EnumOption } from '@/api/adapters/system'
import { createDefaultSystemOrganizationForm } from '@/components/system/organization.mock'
import type { SystemOrganizationFormValue, SystemOrganizationItem } from '@/modules/system/organizations/types'

const props = defineProps<{
  modelValue: boolean
  mode: 'create' | 'edit'
  organization: SystemOrganizationItem | null
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  submit: [payload: SystemOrganizationFormValue]
}>()

const form = reactive<SystemOrganizationFormValue>(createDefaultSystemOrganizationForm())
const errors = reactive<Record<string, string>>({})
const organizationTypeOptions = ref<EnumOption[]>([])

const dialogTitle = computed(() => (props.mode === 'edit' ? '修改机构' : '新增机构'))

const resetErrors = () => {
  Object.keys(errors).forEach((key) => delete errors[key])
}

const fillForm = (organization: SystemOrganizationItem | null) => {
  Object.assign(form, createDefaultSystemOrganizationForm())
  resetErrors()
  if (!organization) {
    return
  }
  form.id = organization.id
  form.name = organization.name
  form.code = organization.code
  form.typeValue = organization.typeValue || ''
  form.managerName = organization.managerName
  form.managerPhone = organization.managerPhone
  form.address = organization.address || ''
  form.settledAt = organization.settledAt || ''
  form.remark = organization.remark
}

watch(
  () => props.modelValue,
  (visible) => {
    if (visible) {
      void loadOrganizationTypeOptions()
      fillForm(props.organization)
    }
  }
)

watch(
  () => props.organization,
  (organization) => {
    if (props.modelValue) {
      fillForm(organization)
    }
  }
)

const validate = () => {
  resetErrors()

  if (!form.name.trim()) {
    errors.name = '请输入机构名称'
  }
  if (!form.code.trim()) {
    errors.code = '请输入机构编码'
  }
  if (!form.typeValue.trim()) {
    errors.typeValue = '请选择机构类型'
  }
  if (!form.managerName.trim()) {
    errors.managerName = '请输入负责人名称'
  }
  if (!form.managerPhone.trim()) {
    errors.managerPhone = '请输入负责人电话'
  } else if (!/^1\d{10}$/.test(form.managerPhone.trim())) {
    errors.managerPhone = '请输入正确的负责人电话'
  }

  return Object.keys(errors).length === 0
}

const close = () => emit('update:modelValue', false)

const loadOrganizationTypeOptions = async () => {
  organizationTypeOptions.value = await fetchEnumOptionsByKey('organizationType')
}

const handleSubmit = () => {
  if (!validate()) {
    return
  }

  emit('submit', {
    id: form.id,
    name: form.name.trim(),
    code: form.code.trim(),
    typeValue: form.typeValue.trim(),
    managerName: form.managerName.trim(),
    managerPhone: form.managerPhone.trim(),
    address: form.address.trim(),
    settledAt: form.settledAt,
    remark: form.remark.trim()
  })
}
</script>

<template>
  <Transition name="organization-form-fade" appear>
    <div v-if="modelValue" class="modal-mask" @click.self.prevent>
      <div class="modal-panel organization-form-modal">
        <div class="modal-head">
          <h3 class="modal-title">{{ dialogTitle }}</h3>
          <button class="icon-btn" type="button" @click="close">关闭</button>
        </div>

        <div class="modal-body">
          <div class="form-grid">
            <label class="field">
              <span class="field-label field-label-required">机构名称</span>
              <input v-model="form.name" class="form-control" type="text" placeholder="请输入机构名称" />
              <span v-if="errors.name" class="field-error">{{ errors.name }}</span>
            </label>

            <label class="field">
              <span class="field-label field-label-required">机构编码</span>
              <input v-model="form.code" class="form-control" type="text" placeholder="请输入机构编码" />
              <span v-if="errors.code" class="field-error">{{ errors.code }}</span>
            </label>

            <label class="field">
              <span class="field-label field-label-required">机构类型</span>
              <select
                v-model="form.typeValue"
                class="form-control"
                :class="{ 'form-control-placeholder': !form.typeValue }"
              >
                <option value="">请选择机构类型</option>
                <option
                  v-for="item in organizationTypeOptions"
                  :key="item.value"
                  :value="item.value"
                >
                  {{ item.label }}
                </option>
              </select>
              <span v-if="errors.typeValue" class="field-error">{{ errors.typeValue }}</span>
            </label>

            <label class="field">
              <span class="field-label field-label-required">负责人名称</span>
              <input
                v-model="form.managerName"
                class="form-control"
                type="text"
                placeholder="请输入负责人名称"
              />
              <span v-if="errors.managerName" class="field-error">{{ errors.managerName }}</span>
            </label>

            <label class="field">
              <span class="field-label field-label-required">负责人电话</span>
              <input
                v-model="form.managerPhone"
                class="form-control"
                type="text"
                placeholder="请输入负责人电话"
              />
              <span v-if="errors.managerPhone" class="field-error">{{ errors.managerPhone }}</span>
            </label>

            <label class="field">
              <span class="field-label">机构地址</span>
              <input
                v-model="form.address"
                class="form-control"
                type="text"
                placeholder="请输入机构地址"
              />
            </label>

            <label class="field">
              <span class="field-label">入驻日期</span>
              <input v-model="form.settledAt" class="form-control" type="date" />
            </label>

            <label class="field">
              <span class="field-label">备注说明</span>
              <textarea
                v-model="form.remark"
                class="form-control form-textarea"
                placeholder="请输入备注说明"
              ></textarea>
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

.organization-form-modal {
  display: grid;
  width: min(760px, calc(100vw - 32px));
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
  padding: 14px 16px;
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
  gap: 16px;
}

.field {
  display: grid;
  gap: 8px;
}

.field-label {
  font-size: var(--es-font-size-sm);
  color: var(--es-color-text-secondary);
}

.field-label-required::before {
  margin-right: 4px;
  color: var(--es-color-error-text);
  content: '*';
}

.form-control {
  width: 100%;
  min-width: 0;
  height: 34px;
  padding: 0 12px;
  font-size: var(--es-font-size-sm);
  color: var(--es-color-text-primary);
  background: #fff;
  border: 1px solid var(--es-color-border-strong);
  border-radius: 5px;
}

.form-control-placeholder {
  color: var(--es-color-text-placeholder);
}

.form-textarea {
  height: 120px;
  padding: 12px;
  line-height: 1.6;
  resize: none;
}

.field-error {
  font-size: 12px;
  color: var(--es-color-error-text);
}

.organization-form-fade-enter-active,
.organization-form-fade-leave-active {
  transition: opacity 0.22s ease, transform 0.22s ease;
}

.organization-form-fade-enter-from,
.organization-form-fade-leave-to {
  opacity: 0;
}

.organization-form-fade-enter-from .organization-form-modal,
.organization-form-fade-leave-to .organization-form-modal {
  transform: translateY(10px) scale(0.98);
}
</style>
