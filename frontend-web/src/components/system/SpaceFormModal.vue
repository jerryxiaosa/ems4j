<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { fetchEnumOptionsByKey } from '@/api/adapters/system'
import type { EnumOption } from '@/api/adapters/system'
import { createDefaultSystemSpaceForm } from '@/components/system/space.mock'
import type { SystemSpaceFormValue, SystemSpaceItem } from '@/modules/system/spaces/types'

const props = defineProps<{
  modelValue: boolean
  mode: 'create' | 'edit'
  parentSpace: SystemSpaceItem | null
  space: SystemSpaceItem | null
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  submit: [payload: SystemSpaceFormValue]
}>()

const form = reactive<SystemSpaceFormValue>(createDefaultSystemSpaceForm())
const errors = reactive<Record<string, string>>({})
const spaceTypeOptions = ref<EnumOption[]>([])

const dialogTitle = computed(() => (props.mode === 'edit' ? '编辑空间' : '新增空间'))

const resetErrors = () => {
  Object.keys(errors).forEach((key) => delete errors[key])
}

const fillForm = () => {
  Object.assign(form, createDefaultSystemSpaceForm())
  resetErrors()

  const target = props.space
  const parent = props.mode === 'edit' ? props.parentSpace : props.parentSpace

  if (target) {
    form.id = target.id
    form.parentId = target.parentId == null ? '' : String(target.parentId)
    form.name = target.name
    form.typeValue = target.typeValue
    form.typeName = target.typeName
    form.area = target.area ? String(target.area) : ''
    form.sortNum = target.sortNum ? String(target.sortNum) : ''
  }

  if (parent) {
    form.parentId = String(parent.id)
    form.parentName = parent.name
  } else {
    form.parentId = ''
    form.parentName = '顶级空间'
  }
}

const ensureSpaceTypeOptions = async () => {
  if (spaceTypeOptions.value.length > 0) {
    return
  }
  spaceTypeOptions.value = await fetchEnumOptionsByKey('spaceType')
}

watch(
  () => props.modelValue,
  (visible) => {
    if (visible) {
      void ensureSpaceTypeOptions()
      fillForm()
    }
  }
)

watch(
  () => [props.space, props.parentSpace, props.mode],
  () => {
    if (props.modelValue) {
      fillForm()
    }
  }
)

const validate = () => {
  resetErrors()
  if (!form.name.trim()) {
    errors.name = '请输入空间名称'
  }
  if (!form.typeValue.trim()) {
    errors.typeValue = '请选择空间类型'
  }
  if (form.area.trim() && Number.isNaN(Number(form.area.trim()))) {
    errors.area = '请输入正确的空间面积'
  }
  if (!form.sortNum.trim()) {
    errors.sortNum = '请输入排序号'
  } else if (!Number.isInteger(Number(form.sortNum.trim()))) {
    errors.sortNum = '排序号必须为整数'
  }
  return Object.keys(errors).length === 0
}

const close = () => emit('update:modelValue', false)

const handleSubmit = () => {
  if (!validate()) {
    return
  }

  emit('submit', {
    id: form.id,
    parentId: form.parentId,
    parentName: form.parentName,
    name: form.name.trim(),
    typeValue: form.typeValue,
    typeName: spaceTypeOptions.value.find((item) => item.value === form.typeValue)?.label || '',
    area: form.area.trim(),
    sortNum: form.sortNum.trim()
  })
}
</script>

<template>
  <Transition name="space-form-fade" appear>
    <div v-if="modelValue" class="modal-mask" @click.self.prevent>
      <div class="modal-panel space-form-modal">
        <div class="modal-head">
          <h3 class="modal-title">{{ dialogTitle }}</h3>
          <button class="icon-btn" type="button" @click="close">关闭</button>
        </div>

        <div class="modal-body">
          <div class="form-grid">
            <label class="field">
              <span class="field-label">上级空间</span>
              <input v-model="form.parentName" class="form-control is-readonly" type="text" readonly />
            </label>

            <label class="field">
              <span class="field-label field-label-required">空间名称</span>
              <input v-model="form.name" class="form-control" type="text" placeholder="请输入空间名称" />
              <span v-if="errors.name" class="field-error">{{ errors.name }}</span>
            </label>

            <label class="field">
              <span class="field-label field-label-required">空间类型</span>
              <select v-model="form.typeValue" class="form-control">
                <option value="">请选择空间类型</option>
                <option v-for="item in spaceTypeOptions" :key="item.value" :value="item.value">
                  {{ item.label }}
                </option>
              </select>
              <span v-if="errors.typeValue" class="field-error">{{ errors.typeValue }}</span>
            </label>

            <label class="field">
              <span class="field-label">空间面积（㎡）</span>
              <input v-model="form.area" class="form-control" type="text" placeholder="请输入空间面积" />
              <span v-if="errors.area" class="field-error">{{ errors.area }}</span>
            </label>

            <label class="field">
              <span class="field-label field-label-required">排序号</span>
              <input v-model="form.sortNum" class="form-control" type="text" placeholder="请输入排序号" />
              <span v-if="errors.sortNum" class="field-error">{{ errors.sortNum }}</span>
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

.space-form-modal {
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
  height: 120px;
  padding: 12px;
  line-height: 1.6;
  resize: none;
}

.field-error {
  font-size: 12px;
  color: var(--es-color-error-text);
}

.space-form-fade-enter-active,
.space-form-fade-leave-active {
  transition: opacity 0.22s ease, transform 0.22s ease;
}

.space-form-fade-enter-from,
.space-form-fade-leave-to {
  opacity: 0;
}

.space-form-fade-enter-from .space-form-modal,
.space-form-fade-leave-to .space-form-modal {
  transform: translateY(10px) scale(0.98);
}
</style>
