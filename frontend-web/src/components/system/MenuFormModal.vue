<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import MenuIconSelect from '@/components/common/MenuIconSelect.vue'
import MultiSelectDropdown from '@/components/common/MultiSelectDropdown.vue'
import { fetchEnumOptionsByKey, fetchSystemApiOptions } from '@/api/adapters/system'
import type { EnumOption } from '@/api/adapters/system'
import {
  createDefaultSystemMenuForm,
  systemMenuHiddenOptions,
  systemMenuPlatformOptions
} from '@/components/system/menu.mock'
import type { SystemMenuFormValue, SystemMenuItem } from '@/modules/system/menus/types'

const props = defineProps<{
  modelValue: boolean
  mode: 'create' | 'edit'
  parentMenu: SystemMenuItem | null
  menu: SystemMenuItem | null
  fixedPlatformValue?: string
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  submit: [payload: SystemMenuFormValue]
}>()

const form = reactive<SystemMenuFormValue>(createDefaultSystemMenuForm())
const errors = reactive<Record<string, string>>({})
const menuTypeOptions = ref<EnumOption[]>([])
const backendApiOptions = ref<EnumOption[]>([])

const dialogTitle = computed(() => (props.mode === 'edit' ? '编辑菜单' : '新增菜单'))
const selectedBackendApiOptions = computed(() =>
  backendApiOptions.value.filter((item) => form.backendApis.includes(item.value))
)

const resetErrors = () => {
  Object.keys(errors).forEach((key) => delete errors[key])
}

const fillForm = () => {
  Object.assign(form, createDefaultSystemMenuForm())
  resetErrors()

  if (props.menu) {
    form.id = props.menu.id
    form.parentId = props.menu.parentId == null ? '' : String(props.menu.parentId)
    form.name = props.menu.name
    form.key = props.menu.key
    form.routePath = props.menu.routePath
    form.backendApis = [...props.menu.permissionCodes]
    form.categoryValue = props.menu.categoryValue
    form.platformValue = props.menu.platformValue
    form.hiddenValue = props.menu.hiddenValue
    form.icon = props.menu.icon
    form.sortNum = String(props.menu.sortNum)
    form.remark = props.menu.remark
  }

  if (props.parentMenu) {
    form.parentId = String(props.parentMenu.id)
    form.parentName = props.parentMenu.name
  } else {
    form.parentId = ''
    form.parentName = '顶级菜单'
  }

  if (props.fixedPlatformValue?.trim()) {
    form.platformValue = props.fixedPlatformValue.trim()
  }
}

watch(
  () => props.modelValue,
  (visible) => {
    if (visible) {
      void loadSelectOptions()
      fillForm()
    }
  }
)

watch(
  () => [props.menu, props.parentMenu, props.mode, props.fixedPlatformValue],
  () => {
    if (props.modelValue) {
      fillForm()
    }
  }
)

const validate = () => {
  resetErrors()
  if (!form.name.trim()) {
    errors.name = '请输入名称'
  }
  if (!form.key.trim()) {
    errors.key = '请输入菜单标识'
  }
  if (!form.categoryValue.trim()) {
    errors.categoryValue = '请选择菜单类型'
  }
  if (!form.platformValue.trim()) {
    errors.platformValue = '请选择菜单平台'
  }
  if (!form.hiddenValue.trim()) {
    errors.hiddenValue = '请选择是否隐藏'
  }
  if (!form.sortNum.trim()) {
    errors.sortNum = '请输入排序'
  } else if (!Number.isInteger(Number(form.sortNum.trim()))) {
    errors.sortNum = '排序必须为整数'
  }
  if (form.remark.length > 300) {
    errors.remark = '备注不能超过300个字符'
  }
  return Object.keys(errors).length === 0
}

const close = () => emit('update:modelValue', false)

const loadSelectOptions = async () => {
  const [menuTypes, apiOptions] = await Promise.all([fetchEnumOptionsByKey('menuType'), fetchSystemApiOptions()])
  menuTypeOptions.value = menuTypes
  backendApiOptions.value = apiOptions
}

const removeSelectedBackendApi = (value: string) => {
  form.backendApis = form.backendApis.filter((item) => item !== value)
}

const handleSubmit = () => {
  if (!validate()) {
    return
  }
  emit('submit', {
    id: form.id,
    parentId: form.parentId,
    parentName: form.parentName,
    name: form.name.trim(),
    key: form.key.trim(),
    routePath: form.routePath.trim(),
    backendApis: [...form.backendApis],
    categoryValue: form.categoryValue,
    platformValue: form.platformValue,
    hiddenValue: form.hiddenValue,
    icon: form.icon.trim(),
    sortNum: form.sortNum.trim(),
    remark: form.remark.trim()
  })
}

</script>

<template>
  <Transition name="menu-form-fade" appear>
    <div v-if="modelValue" class="modal-mask" @click.self.prevent>
      <div class="modal-panel menu-form-modal">
        <div class="modal-head">
          <h3 class="modal-title">{{ dialogTitle }}</h3>
          <button class="icon-btn" type="button" @click="close">关闭</button>
        </div>

        <div class="modal-body">
          <div class="form-grid">
            <label class="field">
              <span class="field-label">上级菜单</span>
              <input v-model="form.parentName" class="form-control is-readonly" type="text" readonly />
            </label>

            <label class="field">
              <span class="field-label field-label-required">名称</span>
              <input v-model="form.name" class="form-control" type="text" placeholder="请输入名称" />
              <span v-if="errors.name" class="field-error">{{ errors.name }}</span>
            </label>

            <label class="field">
              <span class="field-label field-label-required">菜单标识</span>
              <input
                v-model="form.key"
                class="form-control"
                :class="{ 'is-readonly': mode === 'edit' }"
                type="text"
                placeholder="请输入菜单标识"
                :readonly="mode === 'edit'"
              />
              <span v-if="errors.key" class="field-error">{{ errors.key }}</span>
            </label>

            <label class="field">
              <span class="field-label">前端路由</span>
              <input v-model="form.routePath" class="form-control" type="text" placeholder="请输入前端路由" />
              <span v-if="errors.routePath" class="field-error">{{ errors.routePath }}</span>
            </label>

            <label class="field">
              <span class="field-label">后端接口</span>
              <MultiSelectDropdown
                v-model="form.backendApis"
                :options="backendApiOptions"
                placeholder="请选择后端接口"
                wrap-text
                :max-text-lines="2"
              />
              <div v-if="selectedBackendApiOptions.length > 0" class="selected-api-list">
                <button
                  v-for="item in selectedBackendApiOptions"
                  :key="item.value"
                  type="button"
                  class="selected-api-item"
                  :title="item.label"
                  @mousedown.prevent.stop="removeSelectedBackendApi(item.value)"
                  @click.prevent.stop
                >
                  <span class="selected-api-item-text">{{ item.label }}</span>
                  <span class="selected-api-item-remove">×</span>
                </button>
              </div>
            </label>

            <label class="field">
              <span class="field-label field-label-required">菜单类型</span>
              <div class="radio-group">
                <label v-for="item in menuTypeOptions" :key="item.value" class="radio-option">
                  <input v-model="form.categoryValue" type="radio" :value="item.value" />
                  <span>{{ item.label }}</span>
                </label>
              </div>
              <span v-if="errors.categoryValue" class="field-error">{{ errors.categoryValue }}</span>
            </label>

            <label class="field">
              <span class="field-label field-label-required">菜单平台</span>
              <div class="radio-group">
                <label v-for="item in systemMenuPlatformOptions" :key="item.value" class="radio-option">
                  <input
                    v-model="form.platformValue"
                    type="radio"
                    :value="item.value"
                    :disabled="Boolean(props.fixedPlatformValue)"
                  />
                  <span>{{ item.label }}</span>
                </label>
              </div>
              <span v-if="errors.platformValue" class="field-error">{{ errors.platformValue }}</span>
            </label>

            <label class="field">
              <span class="field-label field-label-required">是否隐藏</span>
              <div class="radio-group">
                <label v-for="item in systemMenuHiddenOptions" :key="item.value" class="radio-option">
                  <input v-model="form.hiddenValue" type="radio" :value="item.value" />
                  <span>{{ item.label }}</span>
                </label>
              </div>
              <span v-if="errors.hiddenValue" class="field-error">{{ errors.hiddenValue }}</span>
            </label>

            <div class="field">
              <span class="field-label">图标</span>
              <MenuIconSelect v-model="form.icon" />
            </div>

            <label class="field field-sort">
              <span class="field-label field-label-required">排序</span>
              <input v-model="form.sortNum" class="form-control" type="text" placeholder="请输入排序" />
              <span v-if="errors.sortNum" class="field-error">{{ errors.sortNum }}</span>
            </label>

            <label class="field field-remark">
              <span class="field-label">备注</span>
              <textarea
                v-model="form.remark"
                class="form-control form-textarea"
                maxlength="300"
                placeholder="请输入备注"
              ></textarea>
              <div class="text-counter">{{ form.remark.length }} / 300</div>
              <span v-if="errors.remark" class="field-error">{{ errors.remark }}</span>
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

.menu-form-modal {
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
  font-size: 14px;
  color: var(--es-color-text-secondary);
}

.field-label-required::before {
  margin-right: 4px;
  color: var(--es-color-danger);
  content: '*';
}

.form-control {
  width: 100%;
  height: 36px;
  padding: 0 12px;
  font-size: 14px;
  color: var(--es-color-text-primary);
  background: #fff;
  border: 1px solid var(--es-color-border);
  border-radius: 5px;
  outline: none;
}

.form-control:focus {
  border-color: var(--es-color-primary);
}

.form-control.is-readonly {
  color: var(--es-color-text-tertiary);
  background: var(--es-color-fill-muted);
}

.form-textarea {
  height: 146px;
  padding: 10px 12px;
  resize: none;
}

.radio-group {
  display: flex;
  flex-wrap: wrap;
  gap: 18px;
  min-height: 34px;
  align-items: center;
}

.radio-option {
  display: inline-flex;
  gap: 6px;
  align-items: center;
  font-size: 14px;
  color: var(--es-color-text-primary);
}

.field-remark {
  position: relative;
}

.selected-api-list {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.selected-api-item {
  display: inline-flex;
  max-width: 100%;
  padding: 4px 8px;
  overflow: hidden;
  font-size: 12px;
  color: var(--es-color-primary);
  background: #eef6ff;
  border: 1px solid #bfdbfe;
  border-radius: 5px;
  align-items: center;
  gap: 6px;
  cursor: pointer;
}

.selected-api-item:hover {
  background: #dbeafe;
}

.selected-api-item-text {
  overflow: hidden;
  text-align: left;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.selected-api-item-remove {
  flex: 0 0 auto;
  font-size: 14px;
  line-height: 1;
}

.text-counter {
  margin-top: -2px;
  justify-self: end;
  font-size: 12px;
  color: var(--es-color-text-tertiary);
}

.field-error {
  font-size: 12px;
  color: var(--es-color-danger);
}

.menu-form-fade-enter-active,
.menu-form-fade-leave-active {
  transition: opacity 0.22s ease, transform 0.22s ease;
}

.menu-form-fade-enter-from,
.menu-form-fade-leave-to {
  opacity: 0;
}

.menu-form-fade-enter-from .menu-form-modal,
.menu-form-fade-leave-to .menu-form-modal {
  transform: translateY(8px) scale(0.98);
}
</style>
