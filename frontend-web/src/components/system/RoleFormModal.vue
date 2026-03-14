<script setup lang="ts">
import { computed, reactive, watch } from 'vue'
import { createDefaultSystemRoleForm } from '@/components/system/role.mock'
import type { SystemRoleFormValue, SystemRoleItem } from '@/modules/system/roles/types'

const props = defineProps<{
  modelValue: boolean
  mode: 'create' | 'edit'
  role: SystemRoleItem | null
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  submit: [payload: SystemRoleFormValue]
}>()

const form = reactive<SystemRoleFormValue>(createDefaultSystemRoleForm())
const errors = reactive<Record<string, string>>({})
const DESCRIPTION_LIMIT = 100
const ROLE_KEY_LIMIT = 20

const dialogTitle = computed(() => (props.mode === 'edit' ? '编辑角色' : '添加角色'))
const isEditMode = computed(() => props.mode === 'edit')
const descriptionLength = computed(() => form.description.length)

const resetErrors = () => {
  Object.keys(errors).forEach((key) => delete errors[key])
}

const fillForm = (role: SystemRoleItem | null) => {
  Object.assign(form, createDefaultSystemRoleForm())
  resetErrors()
  if (!role) {
    return
  }
  form.id = role.id
  form.name = role.name
  form.key = role.key
  form.description = role.description
}

watch(
  () => props.modelValue,
  (visible) => {
    if (visible) {
      fillForm(props.role)
    }
  }
)

watch(
  () => props.role,
  (role) => {
    if (props.modelValue) {
      fillForm(role)
    }
  }
)

const validate = () => {
  resetErrors()
  const roleKey = form.key?.trim() || ''

  if (!form.name.trim()) {
    errors.name = '请输入角色名称'
  }

  if (!isEditMode.value && !roleKey) {
    errors.key = '请输入角色标识'
  } else if (!isEditMode.value && roleKey.length > ROLE_KEY_LIMIT) {
    errors.key = `角色标识不能超过${ROLE_KEY_LIMIT}个字符`
  }

  if (form.description.length > DESCRIPTION_LIMIT) {
    errors.description = `角色描述不能超过${DESCRIPTION_LIMIT}个字`
  }

  return Object.keys(errors).length === 0
}

const close = () => emit('update:modelValue', false)

const handleInputDescription = (event: Event) => {
  const nextValue = (event.target as HTMLTextAreaElement).value
  form.description = nextValue.slice(0, DESCRIPTION_LIMIT)
}

const handleSubmit = () => {
  if (!validate()) {
    return
  }
  const roleKey = form.key?.trim() || ''

  const payload: SystemRoleFormValue = {
    id: form.id,
    name: form.name.trim(),
    description: form.description.trim()
  }

  if (!isEditMode.value) {
    payload.key = roleKey
  }

  emit('submit', payload)
}
</script>

<template>
  <Transition name="role-form-fade" appear>
    <div v-if="modelValue" class="modal-mask" @click.self.prevent>
      <div class="modal-panel role-form-modal">
        <div class="modal-head">
          <h3 class="modal-title">{{ dialogTitle }}</h3>
          <button class="icon-btn" type="button" @click="close">关闭</button>
        </div>

        <div class="modal-body">
          <div class="form-grid">
            <label class="field">
              <span class="field-label field-label-required">角色名称</span>
              <input
                v-model="form.name"
                class="form-control"
                type="text"
                placeholder="请输入角色名称"
              />
              <span v-if="errors.name" class="field-error">{{ errors.name }}</span>
            </label>

            <label class="field">
              <span class="field-label field-label-required">角色标识</span>
              <input
                v-model="form.key"
                class="form-control"
                :class="{ 'is-readonly': isEditMode }"
                type="text"
                placeholder="请输入角色标识"
                :maxlength="ROLE_KEY_LIMIT"
                :readonly="isEditMode"
              />
              <span v-if="errors.key" class="field-error">{{ errors.key }}</span>
            </label>

            <label class="field">
              <span class="field-label">角色描述</span>
              <textarea
                class="form-control form-textarea"
                :value="form.description"
                placeholder="请输入角色描述"
                @input="handleInputDescription"
              ></textarea>
              <span class="field-counter">{{ descriptionLength }} / {{ DESCRIPTION_LIMIT }}</span>
              <span v-if="errors.description" class="field-error">{{ errors.description }}</span>
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

.role-form-modal {
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

.form-textarea {
  height: 160px;
  padding: 12px;
  line-height: 1.6;
  resize: none;
}

.field-counter {
  justify-self: end;
  font-size: 12px;
  color: var(--es-color-text-placeholder);
}

.field-error {
  font-size: 12px;
  color: var(--es-color-error-text);
}

.role-form-fade-enter-active,
.role-form-fade-leave-active {
  transition: opacity 0.22s ease, transform 0.22s ease;
}

.role-form-fade-enter-from,
.role-form-fade-leave-to {
  opacity: 0;
}

.role-form-fade-enter-from .role-form-modal,
.role-form-fade-leave-to .role-form-modal {
  transform: translateY(10px) scale(0.98);
}
</style>
