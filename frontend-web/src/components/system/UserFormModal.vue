<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import MultiSelectDropdown from '@/components/common/MultiSelectDropdown.vue'
import { fetchEnumOptionsByKey, type EnumOption } from '@/api/adapters/system'
import OrganizationPicker from '@/components/common/OrganizationPicker.vue'
import { createDefaultSystemUserForm } from '@/components/system/user.mock'
import type { SystemUserFormValue, SystemUserItem } from '@/modules/system/users/types'
import { searchOrganizationOptions, type OrganizationOption } from '@/api/adapters/organization'
import type { SystemOption } from '@/types/system'

const props = defineProps<{
  modelValue: boolean
  mode: 'create' | 'edit'
  user: SystemUserItem | null
  organizations: SystemOption[]
  roles: SystemOption[]
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  submit: [payload: SystemUserFormValue]
}>()

const form = reactive<SystemUserFormValue>(createDefaultSystemUserForm())
const errors = reactive<Record<string, string>>({})
const organizationKeyword = ref('')
const certificatesTypeOptions = ref<EnumOption[]>([])

const dialogTitle = computed(() => (props.mode === 'edit' ? '编辑用户' : '添加用户'))
const isEditMode = computed(() => props.mode === 'edit')
const PASSWORD_RULE_TEXT = '请输入8-16位字符，至少包含大、小写字母、数字和特殊符号的三种'
const genderOptions = [
  { value: '1', label: '男' },
  { value: '2', label: '女' }
]

const resetErrors = () => {
  Object.keys(errors).forEach((key) => delete errors[key])
}

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

const validatePasswordField = () => {
  if (isEditMode.value) {
    delete errors.password
    return
  }

  const password = form.password.trim()
  if (!password) {
    errors.password = '请输入登录密码'
    return
  }

  if (!isValidPassword(password)) {
    errors.password = PASSWORD_RULE_TEXT
    return
  }

  delete errors.password
}

const fillForm = (user: SystemUserItem | null) => {
  Object.assign(form, createDefaultSystemUserForm())
  resetErrors()

  if (!user) {
    return
  }

  form.id = user.id
  form.username = user.username
  form.realName = user.realName
  form.phone = user.phone
  form.userGender =
    user.genderName === '男' ? '1' : user.genderName === '女' ? '2' : ''
  form.certificatesType =
    certificatesTypeOptions.value.find((item) => item.label === user.certificatesTypeText)?.value || ''
  form.certificatesNo = user.certificatesNo || ''
  form.organizationId = user.organizationId
  organizationKeyword.value =
    props.organizations.find((item) => item.value === user.organizationId)?.label || user.organizationName || ''
  form.roleIds = [...user.roleIds]
  form.remark = user.remark
}

watch(
  () => props.organizations,
  (organizations) => {
    if (!form.organizationId) {
      return
    }
    const matched = organizations.find((item) => item.value === form.organizationId)
    if (matched) {
      organizationKeyword.value = matched.label
    }
  },
  { deep: true }
)

const handleOrganizationSelect = (option: OrganizationOption) => {
  organizationKeyword.value = option.name
  form.organizationId = String(option.id)
  delete errors.organizationId
}

const handleOrganizationSearch = async (keyword: string) => {
  const list = await searchOrganizationOptions(keyword, 200)
  return list
}

const loadCertificatesTypeOptions = async () => {
  certificatesTypeOptions.value = await fetchEnumOptionsByKey('certificatesType')
}

watch(organizationKeyword, (value) => {
  const normalized = value.trim()
  const matched = props.organizations.find((item) => item.label === normalized)
  form.organizationId = matched ? matched.value : ''
})

watch(
  () => props.modelValue,
  (visible) => {
    if (visible) {
      void loadCertificatesTypeOptions()
      fillForm(props.user)
    }
  }
)

watch(
  () => props.user,
  (user) => {
    if (props.modelValue) {
      fillForm(user)
    }
  }
)

const validate = () => {
  resetErrors()

  if (!form.username.trim()) {
    errors.username = '请输入登录用户名'
  }

  validatePasswordField()

  if (!form.realName.trim()) {
    errors.realName = '请输入姓名'
  }

  if (!form.phone.trim()) {
    errors.phone = '请输入手机号码'
  } else if (!/^1\d{10}$/.test(form.phone.trim())) {
    errors.phone = '请输入正确的手机号码'
  }

  if (!form.userGender) {
    errors.userGender = '请选择性别'
  }

  if (!form.organizationId) {
    errors.organizationId = '请选择所在机构'
  }

  if (form.roleIds.length === 0) {
    errors.roleIds = '请选择角色'
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
    username: form.username.trim(),
    password: form.password.trim(),
    realName: form.realName.trim(),
    phone: form.phone.trim(),
    remark: form.remark.trim()
  })
}
</script>

<template>
  <Transition name="user-modal-fade" appear>
    <div v-if="modelValue" class="modal-mask" @click.self.prevent>
      <div class="modal-panel user-form-modal">
        <div class="modal-head">
          <h3 class="modal-title">{{ dialogTitle }}</h3>
          <button class="icon-btn" type="button" @click="close">关闭</button>
        </div>

        <div class="modal-body">
          <div class="form-grid">
            <label class="field field-full">
              <span class="field-label field-label-required">登录用户名</span>
              <input
                v-model="form.username"
                class="form-control"
                :class="{ 'is-readonly': isEditMode }"
                type="text"
                placeholder="请输入登录用户名"
                :readonly="isEditMode"
              />
              <span v-if="errors.username" class="field-error">{{ errors.username }}</span>
            </label>

            <label v-if="!isEditMode" class="field field-full">
              <span class="field-label field-label-required">登录密码</span>
              <input
                v-model="form.password"
                class="form-control"
                type="password"
                :placeholder="PASSWORD_RULE_TEXT"
                @blur="validatePasswordField"
              />
              <span class="field-helper">{{ PASSWORD_RULE_TEXT }}</span>
              <span v-if="errors.password" class="field-error">{{ errors.password }}</span>
            </label>

            <label class="field field-full">
              <span class="field-label field-label-required">姓名</span>
              <input
                v-model="form.realName"
                class="form-control"
                type="text"
                placeholder="请输入姓名"
              />
              <span v-if="errors.realName" class="field-error">{{ errors.realName }}</span>
            </label>

            <label class="field field-full">
              <span class="field-label field-label-required">手机号码</span>
              <input
                v-model="form.phone"
                class="form-control"
                type="text"
                placeholder="请输入手机号码"
              />
              <span v-if="errors.phone" class="field-error">{{ errors.phone }}</span>
            </label>

            <label class="field field-full">
              <span class="field-label field-label-required">性别</span>
              <select
                v-model="form.userGender"
                class="form-control"
                :class="{ 'form-control-placeholder': !form.userGender }"
              >
                <option value="">请选择</option>
                <option v-for="item in genderOptions" :key="item.value" :value="item.value">
                  {{ item.label }}
                </option>
              </select>
              <span v-if="errors.userGender" class="field-error">{{ errors.userGender }}</span>
            </label>

            <label class="field field-full">
              <span class="field-label">证件类型</span>
              <select
                v-model="form.certificatesType"
                class="form-control"
                :class="{ 'form-control-placeholder': !form.certificatesType }"
              >
                <option value="">请选择</option>
                <option
                  v-for="item in certificatesTypeOptions"
                  :key="item.value"
                  :value="item.value"
                >
                  {{ item.label }}
                </option>
              </select>
            </label>

            <label class="field field-full">
              <span class="field-label">证件号码</span>
              <input
                v-model="form.certificatesNo"
                class="form-control"
                type="text"
                placeholder="请输入证件号码"
              />
            </label>

            <label class="field field-full">
              <span class="field-label field-label-required">所在机构</span>
              <OrganizationPicker
                v-model="organizationKeyword"
                placeholder="请选择机构"
                :search-fn="handleOrganizationSearch"
                :auto-blur-on-select="true"
                :clear-on-open="isEditMode"
                @select="handleOrganizationSelect"
              />
              <span v-if="errors.organizationId" class="field-error">{{ errors.organizationId }}</span>
            </label>

            <div class="field field-full">
              <span class="field-label field-label-required">角色</span>
              <MultiSelectDropdown
                v-model="form.roleIds"
                :options="roles"
                placeholder="请选择角色"
              />
              <span v-if="errors.roleIds" class="field-error">{{ errors.roleIds }}</span>
            </div>

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

.user-form-modal {
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

.form-grid {
  display: grid;
  gap: 14px;
}

.field {
  display: grid;
  gap: 8px;
}

.field-full {
  grid-column: 1 / -1;
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

.form-control.is-readonly {
  color: #8f9baa;
  background: #f5f7fb;
  border-color: #d8e0ec;
}

.form-textarea {
  height: auto;
  min-height: 96px;
  padding: 10px 12px;
  resize: vertical;
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

.user-modal-fade-enter-active,
.user-modal-fade-leave-active {
  transition: opacity 0.22s ease, transform 0.22s ease;
}

.user-modal-fade-enter-from,
.user-modal-fade-leave-to {
  opacity: 0;
}

.user-modal-fade-enter-from .user-form-modal,
.user-modal-fade-leave-to .user-form-modal {
  transform: translateY(10px) scale(0.98);
}
</style>
