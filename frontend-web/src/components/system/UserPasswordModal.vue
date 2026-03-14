<script setup lang="ts">
import { reactive, watch } from 'vue'
import type { SystemUserItem, SystemUserPasswordFormValue } from '@/modules/system/users/types'

const props = defineProps<{
  modelValue: boolean
  user: SystemUserItem | null
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  submit: [payload: SystemUserPasswordFormValue]
}>()

const form = reactive<SystemUserPasswordFormValue>({
  password: '',
  confirmPassword: ''
})
const errors = reactive<Record<string, string>>({})
const PASSWORD_RULE_TEXT = '请输入8-16位字符，至少包含大、小写字母、数字和特殊符号的三种'

const isValidPassword = (value: string) => {
  const text = value.trim()
  if (text.length < 8 || text.length > 16) {
    return false
  }

  let categoryCount = 0
  if (/[A-Z]/.test(text)) categoryCount += 1
  if (/[a-z]/.test(text)) categoryCount += 1
  if (/\d/.test(text)) categoryCount += 1
  if (/[^A-Za-z\d]/.test(text)) categoryCount += 1

  return categoryCount >= 3
}

const resetState = () => {
  form.password = ''
  form.confirmPassword = ''
  Object.keys(errors).forEach((key) => delete errors[key])
}

watch(
  () => props.modelValue,
  (visible) => {
    if (visible) {
      resetState()
    }
  }
)

const validate = () => {
  Object.keys(errors).forEach((key) => delete errors[key])

  if (!form.password.trim()) {
    errors.password = '请输入新密码'
  } else if (!isValidPassword(form.password)) {
    errors.password = PASSWORD_RULE_TEXT
  }

  if (!form.confirmPassword.trim()) {
    errors.confirmPassword = '请输入确认密码'
  } else if (form.confirmPassword !== form.password) {
    errors.confirmPassword = '两次输入的密码不一致'
  }

  return Object.keys(errors).length === 0
}

const close = () => emit('update:modelValue', false)

const handleSubmit = () => {
  if (!validate()) {
    return
  }

  emit('submit', {
    password: form.password.trim(),
    confirmPassword: form.confirmPassword.trim()
  })
}
</script>

<template>
  <Transition name="user-password-fade" appear>
    <div v-if="modelValue" class="modal-mask" @click.self.prevent>
      <div class="modal-panel user-password-modal">
        <div class="modal-head">
          <h3 class="modal-title">重置密码</h3>
          <button class="icon-btn" type="button" @click="close">关闭</button>
        </div>

        <div class="modal-body">
          <div class="user-meta">{{ props.user?.username || '--' }}</div>

          <div class="form-grid">
            <label class="field">
              <span class="field-label field-label-required">新密码</span>
              <input
                v-model="form.password"
                class="form-control"
                type="password"
                placeholder="请输入新密码"
                @blur="validate"
              />
              <span class="field-helper">{{ PASSWORD_RULE_TEXT }}</span>
              <span v-if="errors.password" class="field-error">{{ errors.password }}</span>
            </label>

            <label class="field">
              <span class="field-label field-label-required">确认密码</span>
              <input
                v-model="form.confirmPassword"
                class="form-control"
                type="password"
                placeholder="请再次输入新密码"
                @blur="validate"
              />
              <span v-if="errors.confirmPassword" class="field-error">{{
                errors.confirmPassword
              }}</span>
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

.user-password-modal {
  display: grid;
  width: min(540px, calc(100vw - 32px));
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

.user-meta {
  margin-bottom: 16px;
  font-size: var(--es-font-size-sm);
  color: var(--es-color-text-secondary);
}

.form-grid {
  display: grid;
  gap: 14px;
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

.field-helper {
  font-size: 12px;
  line-height: 1.5;
  color: var(--es-color-text-placeholder);
}

.field-error {
  font-size: 12px;
  color: var(--es-color-error-text);
}

.user-password-fade-enter-active,
.user-password-fade-leave-active {
  transition: opacity 0.22s ease, transform 0.22s ease;
}

.user-password-fade-enter-from,
.user-password-fade-leave-to {
  opacity: 0;
}

.user-password-fade-enter-from .user-password-modal,
.user-password-fade-leave-to .user-password-modal {
  transform: translateY(10px) scale(0.98);
}
</style>
